package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.pixeldreamstudios.combat_styles.entity.AbilityCadence;
import net.pixeldreamstudios.combat_styles.entity.AbilityCaster;
import net.pixeldreamstudios.combat_styles.entity.AbilityHost;
import net.pixeldreamstudios.combat_styles.entity.SummonCompanions;
import net.pixeldreamstudios.combat_styles.entity.SummonStance;
import net.pixeldreamstudios.combat_styles.entity.SummonOrders;
import net.pixeldreamstudios.combat_styles.entity.ConfigurableSummon;
import net.pixeldreamstudios.combat_styles.entity.AbilityParams;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles.api.event.SummonEvents;
import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.config.SummonTuning;
import net.pixeldreamstudios.combat_styles.equipment.AbilityTuning;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BeholderEyeEntity extends Entity implements GeoEntity, ConfigurableSummon {
    public static final int FADE_TICKS = 6;

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> FADE =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ORBIT =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> ABILITY =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> TEMPORARY =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> BEAM_END =
            SynchedEntityData.defineId(BeholderEyeEntity.class, EntityDataSerializers.VECTOR3);

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_ORBIT = "Orbit";
    private static final String TAG_TEMPORARY = "Temporary";
    private static final String TAG_LIFE = "Life";
    private static final String TAG_FINALE = "FinaleSpent";
    private static final String TAG_DAMAGE = "AbilityDamage";
    private static final String ANIMATION_IDLE = "idle";
    private static final String CONTROLLER = "controller";

    private static final double SHOULDER_RIGHT = 0.55D;
    private static final double SHOULDER_BACK = 0.25D;
    private static final double SHOULDER_HEIGHT = 1.55D;
    private static final double ORBIT_RADIUS = 0.22D;
    private static final double ORBIT_SPEED = 0.05D;
    private static final double BOB_SPEED = 0.08D;
    private static final double BOB_AMOUNT = 0.06D;
    private static final double FOLLOW_SMOOTHING = 0.35D;
    private static final double MODEL_UNITS_PER_BLOCK = 16.0D;

    public static final float RENDER_SCALE = 0.6F;

    private static final double PIVOT_ABOVE_CUBE_CENTRE_PIXELS = 3.0D;
    private static final double BONE_PIVOT_PIXELS = 12.0D;

    public float renderScale() {
        return RENDER_SCALE;
    }

    private static final double BEAM_LIFT_PIXELS = -5.0D;
    private static final double NO_EXTRA_LIFT = 0.0D;

    private static double beamOriginY(float scale) {
        return -PIVOT_ABOVE_CUBE_CENTRE_PIXELS / MODEL_UNITS_PER_BLOCK * scale
                + BEAM_LIFT_PIXELS / MODEL_UNITS_PER_BLOCK * scale;
    }

    public double beamLift() {
        float scale = renderScale();
        return beamOriginY(scale)
                - PIVOT_ABOVE_CUBE_CENTRE_PIXELS / MODEL_UNITS_PER_BLOCK * scale
                + extraBeamLift();
    }

    protected double extraBeamLift() {
        return NO_EXTRA_LIFT;
    }

    public double muzzleReach() {
        return MUZZLE_OFFSET * (renderScale() / RENDER_SCALE);
    }

    public static double beamLiftFromBone() {
        return beamOriginY(RENDER_SCALE)
                - PIVOT_ABOVE_CUBE_CENTRE_PIXELS / MODEL_UNITS_PER_BLOCK * RENDER_SCALE;
    }

    private static final double MUZZLE_OFFSET = 0.159D;

    public static double muzzleOffset() {
        return MUZZLE_OFFSET;
    }

    private static final int DEFAULT_TEMPORARY_LIFE = 110;

    private static final float TURN_RATE_DEGREES = 13.0F;
    private static final double AIM_DISTANCE = 12.0D;

    private static final double FOLLOW_BEHIND = 1.1D;
    private static final double FOLLOW_LIFT = 0.35D;
    private static final double FOLLOW_DEAD_ZONE = 0.45D;
    private static final double FOLLOW_SPEED = 0.25D;
    private static final double FOLLOW_TURN = 0.15D;
    private static final double FOLLOW_RING = 1.45D;
    private static final double FOLLOW_DEPTH = 0.45D;
    private static final double FOLLOW_ARC = Math.PI * 1.1D;
    private static final double FOLLOW_STAGGER = 0.3D;
    private static final double FOLLOW_DRIFT = 0.18D;
    private static final double FOLLOW_DRIFT_SPEED = 0.03D;
    private static final double FORMATION_STEP = 2.4D;
    private static final double CROWN_RADIUS = 1.35D;
    private static final double CROWN_HEIGHT = 2.15D;
    private static final double CROWN_SPIN = 0.06D;
    private static final double CROWN_EASE = 0.35D;
    private static final int UNTETHERED_GRACE = 10;
    private static final float DEGREES = 180.0F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID ownerId;
    private int untethered;
    private final AbilityCaster caster = new AbilityCaster();
    private float abilityDamage;
    private AbilityTuning orderTuning;
    private int life = -1;
    private boolean finale;
    private boolean wasGazing;
    private EyeAbility pendingFinale = EyeAbility.IDLE;
    private UUID targetId;
    private LivingEntity cachedTarget;
    private Vec3 anchored;
    private Vec3 abilityStart;
    private int crownSlot = -1;

    public BeholderEyeEntity(EntityType<? extends BeholderEyeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FADE, 0);
        builder.define(ORBIT, 0.0F);
        builder.define(ABILITY, EyeAbility.IDLE.key());
        builder.define(TEMPORARY, Boolean.FALSE);
        builder.define(BEAM_END, new Vector3f());
        builder.define(OWNER, Optional.empty());
    }

    public Vec3 beamEnd() {
        Vector3f end = entityData.get(BEAM_END);
        return end.lengthSquared() < 1.0E-6F
                ? null
                : new Vec3(end.x(), end.y(), end.z());
    }

    public void setBeamEnd(Vec3 point) {
        entityData.set(BEAM_END, point == null
                ? new Vector3f()
                : new Vector3f((float) point.x, (float) point.y, (float) point.z));
    }

    @Override
    public boolean acting() {
        return EyeAbilities.acts(abilityKey());
    }

    public EyeAbility ability() {
        String key = entityData.get(ABILITY);
        if (!key.equals(parsedKey)) {
            parsedKey = key;
            parsedAbility = EyeAbility.byName(key);
        }
        return parsedAbility;
    }

    public Map<String, Double> params(AbilityConfig.Entry tuned) {
        int stamp = tuned == null ? 0 : tuned.fingerprint();
        if (cachedParams == null || cachedParamsStamp != stamp) {
            cachedParams = AbilityParams.of(tuned);
            cachedParamsStamp = stamp;
        }
        return cachedParams;
    }

    private Map<String, Double> cachedParams;
    private int cachedParamsStamp = Integer.MIN_VALUE;

    private String parsedKey = "";
    private EyeAbility parsedAbility = EyeAbility.IDLE;

    public String abilityKey() {
        return entityData.get(ABILITY);
    }

    private void setAbility(EyeAbility value) {
        setAbility(value.key());
    }

    @Override
    public AbilityCaster caster() {
        return caster;
    }

    private void setAbility(String key) {
        String wanted = key == null || key.isBlank()
                ? EyeAbility.IDLE.key()
                : AbilityConfig.key(key);
        if (wanted.length() > MAX_ABILITY_KEY) {
            CombatStylesContent.LOGGER.warn("Ability key {} is too long to sync", wanted);
            wanted = EyeAbility.IDLE.key();
        }
        entityData.set(ABILITY, wanted);
        int span = EyeAbilities.durationOf(this, owner(), wanted);
        if (EyeAbilities.acts(wanted) && span <= 0) {
            CombatStylesContent.LOGGER.warn("Ability {} has no duration; using {} ticks",
                    wanted, MINIMUM_DURATION);
            span = MINIMUM_DURATION;
        }
        if (EyeAbilities.acts(wanted)) {
            caster.begin(wanted, span, EyeAbilities.cooldownOf(this, owner(), wanted),
                    level().getGameTime(), abilityStart);
        } else {
            caster.stop();
        }
        cachedParams = null;
        sweepHeading = null;
    }

    private static final int MAX_ABILITY_KEY = 64;
    private static final int MINIMUM_DURATION = 1;

    public boolean temporary() {
        return entityData.get(TEMPORARY);
    }

    public double formationAngle() {
        return entityData.get(ORBIT);
    }

    public void setLife(int ticks) {
        life = Math.max(1, ticks);
        finale = false;
        wasGazing = false;
        pendingFinale = EyeAbility.IDLE;
    }

    public void markTemporary(int index) {
        entityData.set(TEMPORARY, Boolean.TRUE);
        entityData.set(ORBIT, (float) (index * FORMATION_STEP));
        if (life <= 0) {
            life = DEFAULT_TEMPORARY_LIFE;
        }
    }

    private static final double EYE_SPAWN_LIFT = 1.7D;
    private static final double COMPANION_TURN = Math.PI * 2.0D;

    @Override
    public double spawnLift() {
        return EYE_SPAWN_LIFT;
    }

    @Override
    public boolean canSpawn(Player owner, int index) {
        return BeholderEyes.temporaryCount(owner) < BeholderEyes.maxTemporary(owner);
    }

    @Override
    public float damageScale(Player owner) {
        return BeholderEyes.damageBonus(owner);
    }

    @Override
    public double formationAngle(int index, int spread) {
        return formationAngle();
    }

    @Override
    public void onSpawned(Player owner, int index) {
        markTemporary(BeholderEyes.temporaryCount(owner));
    }

    @Override
    public String abilityFor(Player owner, SummonProfile profile, String declared, int index) {
        return SummonOrders.abilityFor(owner, profile, declared,
                BeholderEyes.temporaryCount(owner));
    }

    @Override
    public boolean despawnsWithLifespan() {
        return false;
    }

    @Override
    public boolean beginDespawn() {
        beginFade();
        return true;
    }

    @Override
    public void configure(Player owner, LivingEntity target, float damage, int lifespanTicks) {
        setOwnerId(owner == null ? null : owner.getUUID());
        this.targetId = target == null ? null : target.getUUID();
        this.cachedTarget = target;
        this.abilityDamage = damage;
        if (lifespanTicks > 0 && temporary()) {
            this.life = lifespanTicks;
        }
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
        String wanted = substitutedKey(ability);
        if (acting() && EyeAbilities.acts(wanted)) {
            caster.queue(wanted);
            return;
        }
        beginAbility(wanted);
    }

    private void startQueuedAbility() {
        String wanted = caster.takeQueued();
        if (wanted != null) {
            beginAbility(wanted);
        }
    }

    private String substitutedKey(String wanted) {
        Player owner = owner();
        return owner == null ? wanted : BeholderEyes.swapKeyFor(owner, wanted);
    }

    private void beginAbility(String wanted) {
        if (EyeAbilities.acts(wanted)) {
            anchored = position();
            abilityStart = position();
            setAbility(wanted);
        }
    }

    private void beginAbility(EyeAbility wanted) {
        if (wanted.acting()) {
            anchored = position();
            abilityStart = position();
            setAbility(wanted);
        }
    }

    @Override
    public boolean reusable() {
        return !temporary() && !fading();
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

    public void assign(Player owner, float orbitOffset) {
        setOwnerId(owner.getUUID());
        entityData.set(ORBIT, orbitOffset);
        setPos(owner.getX(), owner.getY() + SHOULDER_HEIGHT, owner.getZ());
    }

    public UUID ownerId() {
        return ownerId;
    }

    public Vec3 muzzle() {
        return position()
                .add(0.0D, muzzleHeight(), 0.0D)
                .add(getLookAngle().scale(muzzleReach()));
    }

    private double muzzleHeight() {
        return BONE_PIVOT_PIXELS / MODEL_UNITS_PER_BLOCK * renderScale() + beamLift();
    }

    public int fadeTicks() {
        return entityData.get(FADE);
    }

    @Override
    public boolean fading() {
        return fadeTicks() > 0;
    }

    @Override
    public void stopActing() {
        orderedWith(null);
        setAbility(EyeAbility.IDLE);
        anchored = null;
        setBeamEnd(null);
        startQueuedAbility();
    }

    @Override
    public void dismiss() {
        SummonEvents.fireDismissed(owner(), this);
        caster.clearQueue();
        stopActing();
        beginFade();
    }

    protected boolean sustainedBy(Player owner) {
        return SummonCompanions.sustains(this);
    }

    @Override
    public void companionSpawned(Player owner, int slot, int count) {
        assign(owner, (float) (COMPANION_TURN * slot / Math.max(1, count)));
    }

    protected boolean usesFinale() {
        return true;
    }

    public void beginFade() {
        if (!fading()) {
            entityData.set(FADE, FADE_TICKS);
            setBeamEnd(null);
        }
    }

    protected Player owner() {
        return level().isClientSide() ? null : ownerPlayer(level());
    }

    @Override
    public void tick() {
        super.tick();

        if (fading()) {
            if (level().isClientSide()) {
                return;
            }
            int left = fadeTicks() - 1;
            entityData.set(FADE, left);
            if (left <= 0) {
                discard();
            }
            return;
        }

        if (level().isClientSide()) {
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
        recallIfStray(owner);

        boolean gazing = Gazing.active(owner) || inCrown();

        if (gazing) {
            wasGazing = true;
            pendingFinale = BeholderEyes.finaleFor(owner);
        } else if (wasGazing) {
            wasGazing = false;
            EyeAbility ending = pendingFinale;
            pendingFinale = EyeAbility.IDLE;
            if (usesFinale() && ending.acting() && !acting()) {
                beginAbility(ending);
                return;
            }
        }

        if (temporary()) {
            if (life > 0 && !inCrown()) {
                life--;
            }
            if (life <= 0 && !inCrown()) {
                if (acting()) {
                    tickAbility(owner);
                    return;
                }
                if (!finale) {
                    finale = true;
                    beginAbility(BeholderEyes.finaleFor(owner));
                    return;
                }
                beginFade();
                return;
            }
        } else if (!sustainedBy(owner)) {
            beginFade();
            return;
        }

        if (acting()) {
            tickAbility(owner);
            return;
        }

        if (!gazing && !inCrown() && recast(owner)) {
            return;
        }

        switch (SummonStance.of(this, owner, gazing)) {
            case CROWNED -> orbitCrown(owner, Math.max(1, BeholderEyes.crownSize(owner)));
            case LOOSE -> followLoosely(owner);
            default -> rideShoulder(owner);
        }

        aimAt(owner.getEyePosition().add(owner.getLookAngle().scale(AIM_DISTANCE)));
    }

    private boolean recast(Player owner) {
        AbilityCadence.Recast next = AbilityCadence.next(AbilityHost.of(this, owner));
        if (next == null) {
            return false;
        }
        retarget(next.target());
        abilityDamage = recastDamage(owner);
        acceptAbility(next.ability());
        return acting();
    }

    protected float recastDamage(Player owner) {
        return abilityDamage;
    }

    private void tickAbility(Player owner) {
        String current = abilityKey();
        int elapsed = caster.elapsed();

        if (anchored != null) {
            setPos(anchored.x, anchored.y, anchored.z);
        }
        try {
            aimAt(EyeAbilities.focusFor(this, owner, current, elapsed));
            EyeAbilities.run(this, owner, current, elapsed, abilityDamage);
        } catch (Throwable throwable) {
            CombatStylesContent.LOGGER.error("Beholder eye ability {} failed", current, throwable);
            stopActing();
            return;
        }

        if (caster.advance()) {
            stopActing();
        }
    }

    private void recallIfStray(Player owner) {
        if (level().isClientSide() || !SummonTuning.get().recallStrays
                || acting() || inCrown()) {
            return;
        }
        double reach = SummonTuning.get().rosterRadius;
        if (position().distanceToSqr(owner.position()) <= reach * reach) {
            return;
        }
        Vec3 home = owner.position().add(0.0D, owner.getBbHeight(), 0.0D);
        setPos(home.x, home.y, home.z);
        setDeltaMovement(Vec3.ZERO);
    }

    private void orbitCrown(Player owner, int total) {
        double share = Math.PI * 2.0D / Math.max(1, total);
        double angle = crownSlot * share + tickCount * CROWN_SPIN;
        Vec3 wanted = owner.position()
                .add(Math.cos(angle) * CROWN_RADIUS, CROWN_HEIGHT, Math.sin(angle) * CROWN_RADIUS);
        Vec3 eased = position().add(wanted.subtract(position()).scale(CROWN_EASE));
        setPos(eased.x, eased.y, eased.z);
        aimAt(owner.getEyePosition().add(owner.getLookAngle().scale(AIM_DISTANCE)));
    }

    private void rideShoulder(Player owner) {
        float bodyYaw = owner.yBodyRot * ((float) Math.PI / DEGREES);
        Vec3 right = new Vec3(-Math.cos(bodyYaw), 0.0D, -Math.sin(bodyYaw));
        Vec3 back = new Vec3(Math.sin(bodyYaw), 0.0D, -Math.cos(bodyYaw));

        double offset = entityData.get(ORBIT);
        double angle = (tickCount * ORBIT_SPEED) + offset;
        double bob = Math.sin(tickCount * BOB_SPEED + offset) * BOB_AMOUNT;
        double side = Math.cos(offset) >= 0.0D ? 1.0D : -1.0D;

        Vec3 shoulder = owner.position()
                .add(right.scale(side * SHOULDER_RIGHT + Math.cos(angle) * ORBIT_RADIUS))
                .add(back.scale(SHOULDER_BACK + Math.sin(angle) * ORBIT_RADIUS));
        Vec3 target = new Vec3(shoulder.x, owner.getY() + SHOULDER_HEIGHT + bob, shoulder.z);

        Vec3 eased = position().add(target.subtract(position()).scale(FOLLOW_SMOOTHING));
        setPos(eased.x, eased.y, eased.z);
    }

    private void followLoosely(Player owner) {
        float bodyYaw = owner.yBodyRot * ((float) Math.PI / DEGREES);
        Vec3 back = new Vec3(Math.sin(bodyYaw), 0.0D, -Math.cos(bodyYaw));
        Vec3 right = new Vec3(-Math.cos(bodyYaw), 0.0D, -Math.sin(bodyYaw));
        double bob = Math.sin(tickCount * BOB_SPEED) * BOB_AMOUNT;

        int total = Math.max(1, BeholderEyes.temporaryCount(owner));
        int slot = Math.floorMod(Math.round((float) (entityData.get(ORBIT) / FORMATION_STEP)), total);
        double spread = total == 1 ? 0.0D
                : -FOLLOW_ARC / 2.0D + FOLLOW_ARC * (slot / (double) (total - 1));
        double drift = tickCount * FOLLOW_DRIFT_SPEED + slot;

        Vec3 want = owner.position()
                .add(back.scale(FOLLOW_BEHIND + Math.cos(spread) * FOLLOW_DEPTH
                        + Math.sin(drift) * FOLLOW_DRIFT))
                .add(right.scale(Math.sin(spread) * FOLLOW_RING
                        + Math.cos(drift) * FOLLOW_DRIFT))
                .add(0.0D, SHOULDER_HEIGHT + FOLLOW_LIFT
                        + (slot % 2 == 0 ? FOLLOW_STAGGER : -FOLLOW_STAGGER) + bob, 0.0D);

        Vec3 delta = want.subtract(position());
        if (delta.lengthSqr() < FOLLOW_DEAD_ZONE * FOLLOW_DEAD_ZONE) {
            return;
        }
        Vec3 wanted = delta.normalize().scale(FOLLOW_SPEED);
        Vec3 steered = getDeltaMovement().scale(1.0D - FOLLOW_TURN).add(wanted.scale(FOLLOW_TURN));
        setDeltaMovement(steered);
        setPos(getX() + steered.x, getY() + steered.y, getZ() + steered.z);
    }

    public void aimAt(Vec3 point) {
        Vec3 delta = point.subtract(position().add(0.0D, muzzleHeight(), 0.0D));
        double flat = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float wantYaw = (float) (Math.atan2(delta.z, delta.x) * (DEGREES / Math.PI)) - 90.0F;
        float wantPitch = (float) (-Math.atan2(delta.y, flat) * (DEGREES / Math.PI));

        setYRot(approach(getYRot(), wantYaw));
        setXRot(approach(getXRot(), wantPitch));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private static float approach(float from, float to) {
        float diff = Mth.wrapDegrees(to - from);
        float step = Mth.clamp(diff, -TURN_RATE_DEGREES, TURN_RATE_DEGREES);
        return Mth.wrapDegrees(from + step);
    }

    @Override
    public SummonStance stance() {
        Player owner = owner();
        return SummonStance.of(this, owner, owner != null && Gazing.active(owner));
    }

    @Override
    public Vec3 abilityOrigin() {
        return muzzle();
    }

    @Override
    public void showBeam(Vec3 end) {
        setBeamEnd(end);
    }

    @Override
    public boolean drawsOwnBeam() {
        return true;
    }

    @Override
    public void holdAt(Vec3 point) {
        anchored = point;
        setPos(point.x, point.y, point.z);
    }

    public void joinCrown(int slot) {
        crownSlot = slot;
    }

    public void leaveCrown() {
        crownSlot = -1;
    }

    @Override
    public boolean inCrown() {
        return crownSlot >= 0;
    }

    public int crownSlot() {
        return crownSlot;
    }

    private Vec3 sweepHeading;

    public Vec3 sweepHeading(Player owner) {
        if (sweepHeading == null) {
            sweepHeading = chooseHeading(owner);
        }
        return sweepHeading;
    }

    protected Vec3 chooseHeading(Player owner) {
        Vec3 toTarget = headingToTarget();
        return toTarget == null ? owner.getLookAngle() : toTarget;
    }

    protected Vec3 headingToTarget() {
        LivingEntity target = abilityTarget();
        if (target == null || !target.isAlive()) {
            return null;
        }
        Vec3 delta = target.getBoundingBox().getCenter().subtract(position());
        Vec3 flat = new Vec3(delta.x, 0.0D, delta.z);
        return flat.lengthSqr() < 1.0E-4D ? null : flat.normalize();
    }

    @Override
    public Vec3 abilityStart() {
        return abilityStart == null ? position() : abilityStart;
    }

    @Override
    public float abilityDamage() {
        return abilityDamage;
    }

    @Override
    public void retarget(LivingEntity target) {
        this.targetId = target == null ? null : target.getUUID();
        this.cachedTarget = target;
    }

    @Override
    public LivingEntity abilityTarget() {
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, state ->
                state.setAndContinue(RawAnimation.begin().thenLoop(ANIMATION_IDLE))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
        entityData.set(ORBIT, tag.getFloat(TAG_ORBIT));
        entityData.set(TEMPORARY, tag.getBoolean(TAG_TEMPORARY));
        life = tag.contains(TAG_LIFE) ? tag.getInt(TAG_LIFE) : -1;
        finale = tag.getBoolean(TAG_FINALE);
        abilityDamage = tag.getFloat(TAG_DAMAGE);
        caster.load(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID(TAG_OWNER, ownerId);
        }
        tag.putFloat(TAG_ORBIT, entityData.get(ORBIT));
        tag.putBoolean(TAG_TEMPORARY, temporary());
        if (life > 0) {
            tag.putInt(TAG_LIFE, life);
        }
        if (finale) {
            tag.putBoolean(TAG_FINALE, true);
        }
        if (abilityDamage > 0.0F) {
            tag.putFloat(TAG_DAMAGE, abilityDamage);
        }
        caster.save(tag);
    }

    @Override
    public ClientboundAddEntityPacket getAddEntityPacket(
            ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}
