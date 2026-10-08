package net.pixeldreamstudios.combat_styles_content.entity;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.custom.Customs;
import net.pixeldreamstudios.combat_styles.damage.DamageHurt;
import net.pixeldreamstudios.combat_styles_content.enchantment.ContentEnchantments;
import net.pixeldreamstudios.combat_styles_content.item.ContentItems;

public class ShurikenEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Float> SPIN =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> ANCHORED =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DROPPED =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> TILT =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LATCHED =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ORBIT_TICKS =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ORBIT_RADIUS =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORBIT_SPEED =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORBIT_HEIGHT =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORBIT_START =
            SynchedEntityData.defineId(ShurikenEntity.class, EntityDataSerializers.FLOAT);

    public static final float DEFAULT_SPIN = 40.0F;
    public static final float DEFAULT_SIZE = 1.0F;
    public static final float MIN_SIZE = 0.25F;
    public static final int DEFAULT_PIERCE = 0;
    private float launchSpeed;
    private boolean latchesOnImpact;
    private static final float CARRIED_ELSEWHERE = 0.0F;
    private static final double NO_GRAVITY = 0.0D;
    private static final int MAX_LIFE_TICKS = 200;
    private static final int MAX_ANCHORED_TICKS = 400;
    private static final int NO_ORBIT = 0;
    private static final double LATCH_HEIGHT = 0.6D;
    private static final double LAUNCH_REACH = 32.0D;
    private static final float HIT_VOLUME = 0.5F;
    private static final float HIT_PITCH = 1.6F;
    private static final float ANCHOR_PITCH = 0.6F;
    private static final String TAG_SPIN = "Spin";
    private static final String TAG_SIZE = "Size";
    private static final String TAG_PIERCE = "Pierce";
    private static final String TAG_ANCHORS = "AnchorsOnImpact";
    private static final int NO_ENTITY = -1;
    private static final double TARGET_HEIGHT_RATIO = 0.5D;
    private static final int SHARPNESS_LEVELS_PER_HIT = 2;
    private static final int MIN_HITS = 1;
    private static final double RICOCHET_RADIUS = 8.0D;
    private static final float RICOCHET_FALLOFF = 0.75F;
    private static final float FULL_DAMAGE = 1.0F;
    private static final double DROP_GRAVITY = 0.06D;
    private static final double DROP_KEEP = 0.3D;
    private static final double GROUND_FRICTION = 0.6D;
    private static final double AIR_DRAG = 0.98D;
    private static final float NO_SPIN = 0.0F;
    private static final float PICKUP_VOLUME = 0.6F;
    private static final float PICKUP_PITCH = 1.4F;
    private static final int FADE_PARTICLES = 8;
    private static final double FADE_SPREAD = 0.2D;
    private static final double FADE_SPEED = 0.02D;
    public static final float DEFAULT_RESTITUTION = 0.8F;
    private static final int NEVER_TURNED = Integer.MIN_VALUE / 2;
    private static final int TURN_GRACE_TICKS = 6;
    private static final float SPIN_KEEP = 0.85F;
    private static final float MIN_TILT_STEP = 15.0F;
    private static final float TILT_STEP_RANGE = 25.0F;
    private static final float MAX_TILT = 70.0F;
    private static final float BOUNCE_VOLUME = 0.4F;
    private static final float BOUNCE_PITCH = 1.8F;

    private final IntOpenHashSet pierced = new IntOpenHashSet();
    private int pierceLeft = DEFAULT_PIERCE;
    private boolean anchorsOnImpact;
    private int anchoredTicks;
    private LivingEntity homeTarget;
    private int homeAt;
    private float homeTurn;
    private double homeSpeed;
    private int ignoredId = NO_ENTITY;
    private int ignoredUntil;
    private ItemStack weapon = ItemStack.EMPTY;
    private int ricochetsLeft;
    private float damageScale = FULL_DAMAGE;
    private boolean survivesLanding;
    private boolean shattersOnLanding;
    private Vec3 throwPoint;
    private int dropTicksLeft;
    private double pickupRadius;
    private int turnedAt = NEVER_TURNED;
    private int bouncesLeft;
    private float restitution = DEFAULT_RESTITUTION;
    private boolean rehits;

    public ShurikenEntity(EntityType<? extends ShurikenEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPIN, DEFAULT_SPIN);
        builder.define(SIZE, DEFAULT_SIZE);
        builder.define(ANCHORED, false);
        builder.define(DROPPED, false);
        builder.define(TILT, 0.0F);
        builder.define(LATCHED, NO_ENTITY);
        builder.define(ORBIT_TICKS, NO_ORBIT);
        builder.define(ORBIT_RADIUS, 0.0F);
        builder.define(ORBIT_SPEED, 0.0F);
        builder.define(ORBIT_HEIGHT, 0.0F);
        builder.define(ORBIT_START, 0.0F);
    }

    @Override
    protected Item getDefaultItem() {
        return ContentItems.SHURIKEN.get();
    }

    @Override
    protected double getDefaultGravity() {
        return NO_GRAVITY;
    }

    public void setPierce(int targets) {
        pierceLeft = Math.max(DEFAULT_PIERCE, targets);
    }

    public void setSpin(float degreesPerTick) {
        entityData.set(SPIN, degreesPerTick);
    }

    public float spin() {
        return entityData.get(SPIN);
    }

    public void setSize(float size) {
        entityData.set(SIZE, Math.max(MIN_SIZE, size));
    }

    public float size() {
        return entityData.get(SIZE);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(size());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (SIZE.equals(accessor)) {
            refreshDimensions();
        }
    }

    public void arm(ItemStack thrownFrom) {
        weapon = thrownFrom == null ? ItemStack.EMPTY : thrownFrom.copy();
        ricochetsLeft = ContentEnchantments.levelOf(level(), ContentEnchantments.RICOCHET, weapon);
    }

    public ItemStack weapon() {
        return weapon;
    }

    public float damageScale() {
        return damageScale;
    }

    private int sharpnessHits() {
        int sharpness = ContentEnchantments.levelOf(level(), ContentEnchantments.SHARPNESS, weapon);
        return Math.max(MIN_HITS, sharpness / SHARPNESS_LEVELS_PER_HIT);
    }

    private boolean ricochet(Entity struck) {
        if (ricochetsLeft <= 0 || !(level() instanceof ServerLevel server)
                || !(getOwner() instanceof Player owner)) {
            return false;
        }
        List<LivingEntity> near = ProjectileStrikes.foesAround(server, owner, position(), RICOCHET_RADIUS, struck);
        LivingEntity next = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : near) {
            double distance = candidate.distanceToSqr(this);
            if (!pierced.contains(candidate.getId()) && distance < best) {
                best = distance;
                next = candidate;
            }
        }
        if (next == null) {
            return false;
        }
        Vec3 aim = next.position().add(0.0D, next.getBbHeight() * TARGET_HEIGHT_RATIO, 0.0D);
        setDeltaMovement(aim.subtract(position()).normalize().scale(getDeltaMovement().length()));
        hasImpulse = true;
        ricochetsLeft--;
        damageScale *= RICOCHET_FALLOFF;
        return true;
    }

    public void forgetHits() {
        pierced.clear();
    }

    public void ignoreFor(Entity entity, int ticks) {
        if (entity != null) {
            ignoredId = entity.getId();
            ignoredUntil = tickCount + ticks;
        }
    }

    public void homeOn(LivingEntity target, int delayTicks, float turn, double speed) {
        homeTarget = target;
        homeAt = tickCount + Math.max(0, delayTicks);
        homeTurn = turn;
        homeSpeed = speed;
    }

    private void steer() {
        if (homeTarget == null || anchored() || tickCount < homeAt) {
            return;
        }
        if (!homeTarget.isAlive() || homeTarget.isRemoved() || homeTarget.level() != level()) {
            homeTarget = null;
            return;
        }
        Vec3 aim = homeTarget.position().add(0.0D, homeTarget.getBbHeight() * TARGET_HEIGHT_RATIO, 0.0D);
        Vec3 desired = aim.subtract(position()).normalize().scale(homeSpeed);
        setDeltaMovement(getDeltaMovement().lerp(desired, homeTurn));
        hasImpulse = true;
    }

    public void ignore(Entity entity) {
        if (entity != null) {
            pierced.add(entity.getId());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(TAG_SPIN, spin());
        tag.putFloat(TAG_SIZE, size());
        tag.putInt(TAG_PIERCE, pierceLeft);
        tag.putBoolean(TAG_ANCHORS, anchorsOnImpact);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_SPIN)) {
            setSpin(tag.getFloat(TAG_SPIN));
        }
        if (tag.contains(TAG_SIZE)) {
            setSize(tag.getFloat(TAG_SIZE));
        }
        pierceLeft = tag.getInt(TAG_PIERCE);
        anchorsOnImpact = tag.getBoolean(TAG_ANCHORS);
    }

    public void orbit(int ticks, float radius, float degreesPerTick, float height, float speed) {
        Entity owner = getOwner();
        entityData.set(ORBIT_START, owner == null ? 0.0F : owner.getYRot());
        entityData.set(ORBIT_RADIUS, radius);
        entityData.set(ORBIT_SPEED, degreesPerTick);
        entityData.set(ORBIT_HEIGHT, height);
        entityData.set(ORBIT_TICKS, Math.max(NO_ORBIT, ticks));
        launchSpeed = speed;
    }

    public boolean orbiting() {
        return entityData.get(ORBIT_TICKS) > tickCount && getOwner() != null;
    }

    private void tickOrbit(Entity owner) {
        baseTick();
        float angle = (entityData.get(ORBIT_START) + tickCount * entityData.get(ORBIT_SPEED))
                * Mth.DEG_TO_RAD;
        float radius = entityData.get(ORBIT_RADIUS);
        Vec3 at = owner.position().add(Mth.cos(angle) * radius, entityData.get(ORBIT_HEIGHT),
                Mth.sin(angle) * radius);
        setDeltaMovement(at.subtract(position()));
        setPos(at);
        if (!level().isClientSide() && tickCount + 1 >= entityData.get(ORBIT_TICKS)) {
            Vec3 aim = owner.getEyePosition().add(owner.getLookAngle().scale(LAUNCH_REACH));
            setDeltaMovement(aim.subtract(position()).normalize().scale(launchSpeed));
            hasImpulse = true;
        }
    }

    public void latchOnImpact() {
        latchesOnImpact = true;
    }

    public LivingEntity latchedTarget() {
        int id = entityData.get(LATCHED);
        return id == NO_ENTITY || !(level().getEntity(id) instanceof LivingEntity living) ? null : living;
    }

    private void latch(LivingEntity target) {
        anchor();
        entityData.set(LATCHED, target.getId());
        followLatched();
    }

    private void followLatched() {
        if (entityData.get(LATCHED) == NO_ENTITY) {
            return;
        }
        LivingEntity target = latchedTarget();
        if (target == null || !target.isAlive()) {
            if (!level().isClientSide()) {
                discard();
            }
            return;
        }
        setPos(target.getX(), target.getY() + target.getBbHeight() + LATCH_HEIGHT, target.getZ());
    }

    private void seekBrand() {
        if (homeTarget != null || !pierced.isEmpty() || !(getOwner() instanceof Player owner)) {
            return;
        }
        SolarBrands.Brand brand = SolarBrands.of(owner);
        if (brand == null || brand.target().distanceToSqr(this) > brand.range() * brand.range()) {
            return;
        }
        homeOn(brand.target(), 0, brand.turn(), getDeltaMovement().length());
    }

    public void anchorOnImpact() {
        anchorsOnImpact = true;
    }

    public boolean anchored() {
        return entityData.get(ANCHORED);
    }

    public int anchoredTicks() {
        return anchoredTicks;
    }

    public void setBounces(int bounces, float bounceRestitution, boolean rehit) {
        bouncesLeft = Math.max(0, bounces);
        restitution = bounceRestitution;
        rehits = rehit;
    }

    public float tilt() {
        return entityData.get(TILT);
    }

    private boolean bounce(BlockHitResult result) {
        if (bouncesLeft <= 0) {
            return false;
        }
        bouncesLeft--;
        Vec3 motion = getDeltaMovement();
        Direction face = result.getDirection();
        Vec3 reflected = switch (face.getAxis()) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
        };
        Vec3 bounced = reflected.scale(restitution);
        Vec3 hit = result.getLocation();
        setPos(hit.x + face.getStepX() * getBbWidth(), hit.y + face.getStepY() * getBbHeight(),
                hit.z + face.getStepZ() * getBbWidth());
        setDeltaMovement(bounced);
        hasImpulse = true;
        setSpin(-spin() * SPIN_KEEP);
        float step = MIN_TILT_STEP + random.nextFloat() * TILT_STEP_RANGE;
        float tilt = tilt() + (random.nextBoolean() ? step : -step);
        entityData.set(TILT, Mth.clamp(tilt, -MAX_TILT, MAX_TILT));
        if (rehits) {
            forgetHits();
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS,
                BOUNCE_VOLUME, BOUNCE_PITCH);
        return true;
    }

    public boolean justTurned() {
        return tickCount - turnedAt < TURN_GRACE_TICKS;
    }

    public void setThrowPoint(Vec3 point) {
        throwPoint = point;
    }

    public Vec3 throwPoint() {
        return throwPoint;
    }

    public boolean dropped() {
        return entityData.get(DROPPED);
    }

    public void drop(int ticks, double radius) {
        if (dropped()) {
            return;
        }
        entityData.set(DROPPED, true);
        setSpin(NO_SPIN);
        setXRot(0.0F);
        setDeltaMovement(getDeltaMovement().scale(DROP_KEEP));
        dropTicksLeft = ticks;
        pickupRadius = radius;
        hasImpulse = true;
    }

    private void tickDropped() {
        baseTick();
        setDeltaMovement(getDeltaMovement().add(0.0D, -DROP_GRAVITY, 0.0D));
        move(MoverType.SELF, getDeltaMovement());
        double keep = onGround() ? GROUND_FRICTION : AIR_DRAG;
        setDeltaMovement(getDeltaMovement().multiply(keep, AIR_DRAG, keep));
        if (level().isClientSide()) {
            return;
        }
        if (getOwner() instanceof Player owner && owner.isAlive()
                && owner.distanceToSqr(this) <= pickupRadius * pickupRadius) {
            CatchRefundHook.caught(owner, this);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                    PICKUP_VOLUME, PICKUP_PITCH);
            discard();
            return;
        }
        if (--dropTicksLeft <= 0) {
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), FADE_PARTICLES,
                        FADE_SPREAD, FADE_SPREAD, FADE_SPREAD, FADE_SPEED);
            }
            discard();
        }
    }

    @Override
    public void tick() {
        if (dropped()) {
            tickDropped();
            return;
        }
        if (orbiting()) {
            tickOrbit(getOwner());
            return;
        }
        if (anchored()) {
            setDeltaMovement(Vec3.ZERO);
            followLatched();
        } else if (!level().isClientSide()) {
            seekBrand();
            steer();
        }
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (anchored()) {
            anchoredTicks++;
            if (anchoredTicks > MAX_ANCHORED_TICKS) {
                discard();
            }
        } else if (tickCount > MAX_LIFE_TICKS) {
            discard();
        }
    }

    @Override
    protected void updateRotation() {
        if (!anchored()) {
            super.updateRotation();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        boolean ignoredNow = target.getId() == ignoredId && tickCount < ignoredUntil;
        return !anchored() && !orbiting() && !ignoredNow && super.canHitEntity(target)
                && !pierced.contains(target.getId());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        DamageSource source = damageSources().thrown(this, getOwner());
        target.hurt(source, CARRIED_ELSEWHERE);
        if (!level().isClientSide() && target instanceof LivingEntity living) {
            for (int hit = MIN_HITS; hit < sharpnessHits() && living.isAlive(); hit++) {
                DamageHurt.clearIFrames(living);
                living.hurt(source, CARRIED_ELSEWHERE);
            }
        }
        pierced.add(target.getId());
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS,
                HIT_VOLUME, HIT_PITCH);
        if (level().isClientSide()) {
            return;
        }
        if (!latchesOnImpact && target instanceof LivingEntity living
                && getOwner() instanceof Player owner) {
            SolarBrands.Brand brand = SolarBrands.of(owner);
            if (brand != null && brand.target() == living) {
                ProjectileStrikes.strikeOne(this, owner, brand.bonus(), living);
            }
        }
        if (latchesOnImpact && target instanceof LivingEntity living) {
            latch(living);
        } else if (anchorsOnImpact) {
            anchor();
        } else if (!ricochet(target) && pierceLeft-- <= 0) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT_GROUND,
                SoundSource.PLAYERS, HIT_VOLUME, HIT_PITCH);
        if (level().isClientSide()) {
            return;
        }
        if (anchorsOnImpact) {
            anchor();
        } else if (!bounce(result)) {
            survivesLanding = false;
            shattersOnLanding = false;
            Customs.landed(CustomScopes.PROJECTILE, ProjectileUpkeep.boxFor(this),
                    ProjectileUpkeep.ownerFor(this), this);
            if (shattersOnLanding || !survivesLanding) {
                discard();
            } else {
                Vec3 hit = result.getLocation();
                Direction face = result.getDirection();
                setPos(hit.x + face.getStepX() * getBbWidth(), hit.y + face.getStepY() * getBbHeight(),
                        hit.z + face.getStepZ() * getBbWidth());
                turnedAt = tickCount;
            }
        }
    }

    public void surviveLanding() {
        survivesLanding = true;
    }

    public void shatterOnLanding() {
        shattersOnLanding = true;
    }

    private void anchor() {
        if (anchored()) {
            return;
        }
        entityData.set(ANCHORED, true);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS,
                HIT_VOLUME, ANCHOR_PITCH);
    }
}
