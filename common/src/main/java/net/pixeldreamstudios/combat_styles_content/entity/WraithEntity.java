package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.entity.ConfigurableSummon;
import net.pixeldreamstudios.combat_styles.affix.AffixDamageDealer;
import net.pixeldreamstudios.combat_styles.damage.DamageHurt;

import java.util.Optional;
import java.util.UUID;

public class WraithEntity extends Entity implements ConfigurableSummon {

    private static final double SPAWN_LIFT = 1.2D;

    @Override
    public double spawnLift() {
        return SPAWN_LIFT;
    }

    @Override
    public boolean fading() {
        return isFading();
    }

    @Override
    public boolean beginDespawn() {
        beginFade();
        return true;
    }

    public static final int FADE_TICKS = 8;

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(WraithEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> FADE =
            SynchedEntityData.defineId(WraithEntity.class, EntityDataSerializers.INT);

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_TARGET = "Target";
    private static final String TAG_DAMAGE = "Damage";
    private static final String TAG_LIFE = "Life";

    private static final double CHARGE_SPEED = 0.62D;
    private static final double TURN_RATE = 0.35D;
    private static final double HIT_REACH = 0.9D;
    private static final int DEFAULT_LIFE = 160;
    private static final float DEGREES = 180.0F;

    private UUID ownerId;
    private UUID targetId;
    private LivingEntity cachedTarget;
    private float damage;
    private int life = DEFAULT_LIFE;

    public WraithEntity(EntityType<? extends WraithEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FADE, 0);
        builder.define(OWNER, Optional.empty());
    }

    @Override
    public void configure(Player owner, LivingEntity target, float damage, int lifespanTicks) {
        setOwnerId(owner == null ? null : owner.getUUID());
        this.targetId = target == null ? null : target.getUUID();
        this.cachedTarget = target;
        this.damage = damage;
        this.life = lifespanTicks > 0 ? lifespanTicks : DEFAULT_LIFE;
    }

    public Player owner() {
        return ownerId == null ? null : level().getPlayerByUUID(ownerId);
    }

private void setOwnerId(UUID id) {
        this.ownerId = id;
        entityData.set(OWNER, Optional.ofNullable(id));
    }

    @Override
    public UUID trackedOwner() {
        return ownerId;
    }

    @Override
    public Optional<UUID> syncedOwner() {
        return entityData.get(OWNER);
    }

    public int fadeTicks() {
        return entityData.get(FADE);
    }

    public boolean isFading() {
        return fadeTicks() > 0;
    }

    private LivingEntity target() {
        if (cachedTarget != null && cachedTarget.isAlive()) {
            return cachedTarget;
        }
        if (targetId == null || !(level() instanceof ServerLevel server)) {
            return null;
        }
        Entity found = server.getEntity(targetId);
        cachedTarget = found instanceof LivingEntity living && living.isAlive() ? living : null;
        return cachedTarget;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public void tick() {
        super.tick();

        int fade = entityData.get(FADE);
        if (fade > 0) {
            entityData.set(FADE, fade - 1);
            move(MoverType.SELF, getDeltaMovement().scale(0.6D));
            if (fade <= 1 && !level().isClientSide()) {
                discard();
            }
            return;
        }

        if (level().isClientSide()) {
            move(MoverType.SELF, getDeltaMovement());
            return;
        }

        Player owner = owner();
        if (owner == null || tickCount > life) {
            beginFade();
            return;
        }

        LivingEntity target = target();
        if (target == null) {
            beginFade();
            return;
        }

        steerToward(target);
        move(MoverType.SELF, getDeltaMovement());

        if (getBoundingBox().inflate(HIT_REACH).intersects(target.getBoundingBox())) {
            strike(owner, target);
        }
    }

    private void steerToward(LivingEntity target) {
        Vec3 wanted = target.getBoundingBox().getCenter().subtract(position()).normalize()
                .scale(CHARGE_SPEED);
        Vec3 steered = getDeltaMovement().scale(1.0D - TURN_RATE).add(wanted.scale(TURN_RATE));
        setDeltaMovement(steered);

        double flat = steered.horizontalDistance();
        setYRot((float) (Math.atan2(steered.z, steered.x) * (DEGREES / Math.PI)) - 90.0F);
        setXRot((float) (-(Math.atan2(steered.y, flat) * (DEGREES / Math.PI))));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void strike(Player owner, LivingEntity target) {
        if (damage > 0.0F) {
            DamageSource source = owner.damageSources().indirectMagic(this, owner);
            DamageHurt.bypassingIFrames(target, source, damage);
            AffixDamageDealer.onSummonHit(owner, target, damage);
        }
        beginFade();
    }

    public void beginFade() {
        if (entityData.get(FADE) <= 0) {
            entityData.set(FADE, FADE_TICKS);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_OWNER)) {
            setOwnerId(tag.getUUID(TAG_OWNER));
        }
        if (tag.hasUUID(TAG_TARGET)) {
            targetId = tag.getUUID(TAG_TARGET);
        }
        damage = tag.getFloat(TAG_DAMAGE);
        life = tag.contains(TAG_LIFE) ? tag.getInt(TAG_LIFE) : DEFAULT_LIFE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID(TAG_OWNER, ownerId);
        }
        if (targetId != null) {
            tag.putUUID(TAG_TARGET, targetId);
        }
        tag.putFloat(TAG_DAMAGE, damage);
        tag.putInt(TAG_LIFE, life);
    }
}
