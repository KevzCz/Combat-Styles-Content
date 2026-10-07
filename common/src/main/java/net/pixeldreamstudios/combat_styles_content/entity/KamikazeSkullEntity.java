package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.entity.ConfigurableSummon;
import net.pixeldreamstudios.combat_styles.entity.SummonCompanions;
import net.pixeldreamstudios.combat_styles.equipment.AbilityTuning;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfiles;
import net.pixeldreamstudios.combat_styles.affix.AffixDamageDealer;
import net.pixeldreamstudios.combat_styles.damage.DamageHurt;
import net.pixeldreamstudios.combat_styles.affix.AffixTargets;

import java.util.Optional;
import java.util.UUID;

public class KamikazeSkullEntity extends Entity implements ConfigurableSummon {

    private static final double SPAWN_LIFT = 1.6D;

    @Override
    public double spawnLift() {
        return SPAWN_LIFT;
    }

    @Override
    public void onSpawned(Player owner, int index) {
        setOrbitOffset(SkullFlock.freeOrbitOffset(owner, this));
    }

    @Override
    public void companionSpawned(Player owner, int slot, int count) {
        companion = true;
        life = COMPANION_LIFE;
        setOrbitOffset(SkullFlock.slotOffset(slot, count));
    }

    public static final int FADE_TICKS = 5;

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(KamikazeSkullEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> FADE =
            SynchedEntityData.defineId(KamikazeSkullEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DIVING =
            SynchedEntityData.defineId(KamikazeSkullEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ORBIT =
            SynchedEntityData.defineId(KamikazeSkullEntity.class, EntityDataSerializers.FLOAT);

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_TARGET = "Target";
    private static final String TAG_DAMAGE = "Damage";
    private static final String TAG_LIFE = "Life";
    private static final String TAG_ORBIT = "Orbit";

    private static final double ORBIT_RADIUS = 1.6D;
    private static final double ORBIT_SPEED = 0.06D;
    private static final double ORBIT_HEIGHT = 1.3D;
    private static final double ORBIT_EASE = 0.3D;
    private static final double DIVE_SPEED = 0.75D;
    private static final double DIVE_TURN = 0.4D;
    private static final double HIT_REACH = 0.9D;
    private static final double BURST_RADIUS = 2.5D;
    private static final float BURST_SHARE = 0.45F;
    private static final int DEFAULT_LIFE = 240;
    private static final int COMPANION_LIFE = Integer.MAX_VALUE;
    private static final int UNTETHERED_GRACE = 10;
    private static final float DEGREES = 180.0F;

    private UUID ownerId;
    private UUID targetId;
    private LivingEntity cachedTarget;
    private float damage;
    private int life = DEFAULT_LIFE;
    private int untethered;
    private boolean companion;
    private AbilityTuning orderTuning;

    public KamikazeSkullEntity(EntityType<? extends KamikazeSkullEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FADE, 0);
        builder.define(DIVING, Boolean.FALSE);
        builder.define(ORBIT, 0.0F);
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

    public void setOrbitOffset(float offset) {
        entityData.set(ORBIT, offset);
    }

    public float orbitOffset() {
        return entityData.get(ORBIT);
    }

    public boolean diving() {
        return entityData.get(DIVING);
    }

    public int fadeTicks() {
        return entityData.get(FADE);
    }

    public boolean fading() {
        return fadeTicks() > 0;
    }

    @Override
    public boolean acting() {
        return diving();
    }

    public void dive() {
        beginDive(target());
    }

    @Override
    public void orderedWith(AbilityTuning tuning) {
        this.orderTuning = tuning;
    }

    @Override
    public AbilityTuning orderTuning() {
        return orderTuning;
    }

    @Override
    public void acceptAbility(String ability) {
        if (SkullAbilities.DIVE.equals(ability)) {
            dive();
        }
    }

    private AbilityConfig.Entry tuned(Player owner) {
        SummonProfile.Resolved resolved = owner == null
                ? null
                : SummonProfiles.resolvedFor(owner);
        AbilityConfig.Entry entry = resolved == null
                ? AbilityConfig.get(SkullAbilities.DIVE)
                : resolved.tuningFor(SkullAbilities.DIVE, caster());
        return orderTuning == null ? entry : orderTuning.over(entry);
    }

    public void beginDive(LivingEntity target) {
        if (diving() || fading()) {
            return;
        }
        if (target != null) {
            this.targetId = target.getUUID();
            this.cachedTarget = target;
        }
        entityData.set(DIVING, Boolean.TRUE);
    }

    public void beginFade() {
        if (!fading()) {
            entityData.set(FADE, FADE_TICKS);
        }
    }

    private Player owner() {
        return level().isClientSide() ? null : ownerPlayer(level());
    }

    private LivingEntity target() {
        if (cachedTarget != null && cachedTarget.isAlive() && !cachedTarget.isRemoved()) {
            return cachedTarget;
        }
        if (targetId == null || !(level() instanceof ServerLevel server)) {
            return null;
        }
        Entity found = server.getEntity(targetId);
        cachedTarget = found instanceof LivingEntity living ? living : null;
        return cachedTarget;
    }

    @Override
    public void tick() {
        super.tick();

        if (fading()) {
            int left = fadeTicks() - 1;
            entityData.set(FADE, left);
            if (left <= 0 && !level().isClientSide()) {
                discard();
            }
            return;
        }
        if (level().isClientSide()) {
            return;
        }
        if (--life <= 0 || (companion && !SummonCompanions.sustains(this))) {
            beginFade();
            return;
        }

        Player owner = owner();
        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            if (++untethered > UNTETHERED_GRACE) {
                beginFade();
            }
            return;
        }
        untethered = 0;

        if (diving()) {
            dive(owner);
        } else {
            circle(owner);
        }
    }

    private void circle(Player owner) {
        double angle = tickCount * ORBIT_SPEED + entityData.get(ORBIT);
        Vec3 want = new Vec3(
                owner.getX() + Math.cos(angle) * ORBIT_RADIUS,
                owner.getY() + ORBIT_HEIGHT,
                owner.getZ() + Math.sin(angle) * ORBIT_RADIUS);
        Vec3 eased = position().add(want.subtract(position()).scale(ORBIT_EASE));
        setPos(eased.x, eased.y, eased.z);
        faceAlong(want.subtract(position()));
    }

    private void dive(Player owner) {
        LivingEntity target = target();
        if (target == null || !target.isAlive()) {
            burst(owner, null);
            return;
        }
        Vec3 wanted = target.getBoundingBox().getCenter().subtract(position())
                .normalize().scale(DIVE_SPEED);
        Vec3 steered = getDeltaMovement().scale(1.0D - DIVE_TURN).add(wanted.scale(DIVE_TURN));
        setDeltaMovement(steered);
        move(MoverType.SELF, steered);
        faceAlong(steered);

        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY(), getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
        }
        if (getBoundingBox().inflate(HIT_REACH).intersects(target.getBoundingBox())) {
            burst(owner, target);
        }
    }

    private void burst(Player owner, LivingEntity struck) {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(),
                    18, 0.4D, 0.4D, 0.4D, 0.06D);
            server.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.WITHER_SKELETON_DEATH, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
        AbilityConfig.Entry tuned = tuned(owner);
        float hit = damage + tuned.asDamageSpec().base();
        if (hit > 0.0F && owner != null) {
            DamageSource source = owner.damageSources().indirectMagic(this, owner);
            if (struck != null) {
                DamageHurt.bypassingIFrames(struck, source, hit);
                AffixDamageDealer.onSummonHit(owner, struck, hit);
            }
            int capped = tuned.shape.maxTargets > 1 ? tuned.shape.maxTargets : 0;
            int reached = 0;
            AABB area = getBoundingBox().inflate(tuned.area(BURST_RADIUS));
            for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby == struck || !AffixTargets.canHit(owner, nearby)) {
                    continue;
                }
                if (capped > 0 && reached >= capped) {
                    break;
                }
                reached++;
                float splash = hit * BURST_SHARE;
                DamageHurt.bypassingIFrames(nearby, source, splash);
                AffixDamageDealer.onSummonHit(owner, nearby, splash);
            }
        }
        beginFade();
    }

    private void faceAlong(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6D) {
            return;
        }
        double flat = direction.horizontalDistance();
        setYRot((float) (Math.atan2(direction.z, direction.x) * (DEGREES / Math.PI)) - 90.0F);
        setXRot((float) (-Math.atan2(direction.y, flat) * (DEGREES / Math.PI)));
        yRotO = getYRot();
        xRotO = getXRot();
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
        entityData.set(ORBIT, tag.getFloat(TAG_ORBIT));
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
        tag.putFloat(TAG_ORBIT, entityData.get(ORBIT));
    }

    @Override
    public ClientboundAddEntityPacket getAddEntityPacket(
            ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}
