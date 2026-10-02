package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.entity.AbilityBehaviour;
import net.pixeldreamstudios.combat_styles.entity.AbilityRegistry;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfiles;
import net.pixeldreamstudios.combat_styles.affix.AffixDamageDealer;
import net.pixeldreamstudios.combat_styles.damage.DamageHurt;
import net.pixeldreamstudios.combat_styles.affix.AffixTargets;
import net.pixeldreamstudios.combat_styles.affix.EffectChance;
import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.config.SummonTuning;
import net.pixeldreamstudios.combat_styles.equipment.AbilityTuning;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EyeAbilities {
    private static final List<EyeAbility> ROAMING = List.of(
            EyeAbility.TWIN_LANCE,
            EyeAbility.FURROW,
            EyeAbility.WIDE_CUT,
            EyeAbility.BLINK_WATCH,
            EyeAbility.TETHER);

    public static EyeAbility randomAbility(RandomSource random) {
        return ROAMING.get(random.nextInt(ROAMING.size()));
    }

    private EyeAbilities() {
    }

    private static final double FOCUS_DISTANCE = 8.0D;
    private static final double COLLAPSE_FOCUS_DROP = 4.0D;

    public static Vec3 focusFor(BeholderEyeEntity eye, Player owner, String ability,
                                int elapsed) {
        return focusFor(eye, owner, EyeAbility.byName(ability), elapsed);
    }

    public static Vec3 focusFor(BeholderEyeEntity eye, Player owner, EyeAbility ability,
                                int elapsed) {
        LivingEntity target = eye.abilityTarget();
        Vec3 ahead = owner.getEyePosition().add(owner.getLookAngle().scale(FOCUS_DISTANCE));
        AbilityConfig.Entry tuned = cfg(eye, owner, ability.key());
        Vec3 focus = switch (ability) {
            case FURROW -> furrowContact(eye, owner, elapsed, tuned);
            case WIDE_CUT -> sweepHead(eye, owner, elapsed, tuned);
            case BLINK_WATCH -> owner.getEyePosition();
            case COLLAPSE -> eye.position().subtract(0.0D, COLLAPSE_FOCUS_DROP, 0.0D);
            case SUPERNOVA -> owner.getEyePosition();
            case ECLIPSE -> eye.abilityStart().add(0.0D, tuned.shape.rise, 0.0D);
            default -> target != null ? target.getBoundingBox().getCenter() : ahead;
        };
        return focus == null ? ahead : focus;
    }

    public static boolean acts(String ability) {
        return ability != null && !ability.isBlank()
                && !AbilityConfig.key(ability).equals(EyeAbility.IDLE.key())
                && AbilityRegistry.has(ability);
    }

    private static final float GREAT_EYE_RECOVERY = 2.0F;

    public static int durationOf(BeholderEyeEntity eye, Player owner, String ability) {
        if (!acts(ability)) {
            return 0;
        }
        int span = cfg(eye, owner, ability).duration();
        return eye instanceof GreatEyeEntity
                ? Math.round(span * GREAT_EYE_RECOVERY)
                : span;
    }

    public static void run(BeholderEyeEntity eye, Player owner, String ability,
                           int elapsed, float weaponDamage) {
        if (!(eye.level() instanceof ServerLevel level) || !acts(ability)) {
            return;
        }
        AbilityConfig.Entry tuned = cfg(eye, owner, ability);
        float damage = tuned.damageFrom(owner, weaponDamage);
        if (damage < SummonTuning.get().minimumDamage) {
            return;
        }
        AbilityBehaviour behaviour = AbilityRegistry.get(ability);
        if (behaviour != null) {
            behaviour.tick(level, eye, owner, elapsed, damage, tuned, eye.params(tuned));
        }
    }

    public static void run(BeholderEyeEntity eye, Player owner, EyeAbility ability,
                           int elapsed, float weaponDamage) {
        if (!(eye.level() instanceof ServerLevel level) || !ability.acting()) {
            return;
        }
        AbilityConfig.Entry tuned = cfg(eye, owner, ability.key());
        float damage = tuned.damageFrom(owner, weaponDamage);
        if (damage < SummonTuning.get().minimumDamage) {
            return;
        }
        AbilityBehaviour behaviour = AbilityRegistry.get(ability.key());
        if (behaviour != null) {
            behaviour.tick(level, eye, owner, elapsed, damage, tuned, eye.params(tuned));
        }
    }

    public static void registerDefaults() {
        AbilityRegistry.register(TWIN_LANCE,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        twinLance(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(FURROW,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        furrow(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(WIDE_CUT,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        wideCut(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(BLINK_WATCH,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        blinkWatch(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(TETHER,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        tether(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(COLLAPSE,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        collapse(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(SUPERNOVA,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        supernova(level, eye, owner, elapsed, damage, tuned);
                    }
                });
        AbilityRegistry.register(ECLIPSE,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof BeholderEyeEntity eye) {
                        eclipse(level, eye, owner, elapsed, damage, tuned);
                    }
                });
    }

    private static void cue(ServerLevel level, BeholderEyeEntity eye,
                            SoundEvent sound, float pitch) {
        level.playSound(null, BlockPos.containing(eye.position()), sound,
                SoundSource.PLAYERS, SummonTuning.get().cueVolume, pitch);
    }

    private static int lanceBeat(BeholderEyeEntity eye, Player owner, int beat) {
        AbilityConfig.Entry tuned = cfg(eye, owner, TWIN_LANCE);
        int gap = Math.max(1, tuned.duration() / Math.max(1, tuned.damage.hits));
        return tuned.windup() + gap * beat;
    }

    private static void twinLance(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                  int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed != tuned.windup() && elapsed != lanceBeat(eye, owner, 1)) {
            return;
        }
        LivingEntity target = eye.abilityTarget();
        Vec3 aim = target != null
                ? target.getBoundingBox().getCenter()
                : eye.muzzle().add(eye.getLookAngle().scale(tuned.shape.range));
        AbilityConfig.Entry lance = tuned;
        beam(level, eye, owner, eye.muzzle(), aim, damage, lance.targets(), lance);
        cue(level, eye, SoundEvents.TRIDENT_RETURN, tuned.fx.pitch);
        level.sendParticles(ParticleTypes.END_ROD, eye.getX(), eye.getY(), eye.getZ(),
                4, 0.1D, 0.1D, 0.1D, 0.02D);
    }

    private static void furrow(ServerLevel level, BeholderEyeEntity eye, Player owner,
                               int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed < tuned.windup()) {
            return;
        }
        Vec3 contact = furrowContact(eye, owner, elapsed, tuned);
        if (contact == null) {
            return;
        }
        eye.setBeamEnd(contact);
        if (elapsed == tuned.windup()) {
            cue(level, eye, SoundEvents.SCULK_BLOCK_CHARGE, tuned.fx.pitch);
        }
        if (elapsed % SummonTuning.get().trailInterval == 0) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    contact.x, contact.y + 0.1D, contact.z, 2, 0.2D, 0.05D, 0.2D, 0.01D);
        }

        float perTick = damage / Math.max(1, tuned.duration() - tuned.windup());
        AABB box = new AABB(contact.x - tuned.shape.radius, contact.y - 1.0D, contact.z - tuned.shape.radius,
                contact.x + tuned.shape.radius, contact.y + 2.0D, contact.z + tuned.shape.radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (AffixTargets.canHit(owner, victim)) {
                strike(eye, owner, victim, perTick, tuned);
            }
        }
    }

    private static Vec3 furrowContact(BeholderEyeEntity eye, Player owner, int elapsed, AbilityConfig.Entry tuned) {
        int span = Math.max(1, tuned.duration() - tuned.windup());
        double progress = Math.clamp((elapsed - tuned.windup()) / (double) span, 0.0D, 1.0D);
        Vec3 look = eye.sweepHeading(owner);
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        if (flat.lengthSqr() < 1.0E-4D) {
            flat = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 ahead = eye.position()
                .add(flat.normalize().scale(smooth(progress) * tuned.shape.range));
        return surfaceUnder(eye, ahead);
    }

    private static double smooth(double t) {
        double clamped = Math.clamp(t, 0.0D, 1.0D);
        return clamped * clamped * (3.0D - 2.0D * clamped);
    }

    private static Vec3 surfaceUnder(BeholderEyeEntity eye, Vec3 point) {
        Vec3 from = new Vec3(point.x, point.y + 1.5D, point.z);
        Vec3 to = new Vec3(point.x, point.y - SummonTuning.get().furrowProbeDepth, point.z);
        BlockHitResult hit = eye.level().clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, eye));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation();
    }

    private static void wideCut(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed < tuned.windup()) {
            return;
        }
        Vec3 head = sweepHead(eye, owner, elapsed, tuned);
        eye.setBeamEnd(head);
        if (elapsed == tuned.windup()) {
            cue(level, eye, SoundEvents.PLAYER_ATTACK_SWEEP, tuned.fx.pitch);
        }

        float perTick = damage / Math.max(1, tuned.duration() - tuned.windup());

        Vec3 muzzle = eye.muzzle();
        double reach = Math.max(0.5D, tuned.shape.radius);
        AABB box = new AABB(muzzle, head).inflate(reach);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (AffixTargets.canHit(owner, victim)
                    && nearSegment(muzzle, head, victim, reach)) {
                strike(eye, owner, victim, perTick, tuned);
            }
        }
    }

    private static boolean nearSegment(Vec3 from, Vec3 to, LivingEntity victim, double reach) {
        Vec3 span = to.subtract(from);
        double lengthSqr = span.lengthSqr();
        Vec3 centre = victim.getBoundingBox().getCenter();
        Vec3 nearest = lengthSqr < 1.0E-4D
                ? from
                : from.add(span.scale(Math.clamp(
                        centre.subtract(from).dot(span) / lengthSqr, 0.0D, 1.0D)));
        double allowed = reach + victim.getBbWidth() * 0.5D;
        return centre.distanceToSqr(nearest) <= allowed * allowed;
    }

    private static Vec3 sweepHead(BeholderEyeEntity eye, Player owner, int elapsed, AbilityConfig.Entry tuned) {
        int span = Math.max(1, tuned.duration() - tuned.windup());
        double raw = Math.clamp((elapsed - tuned.windup()) / (double) span, 0.0D, 1.0D);
        double progress = smooth(raw);
        double offset = -SummonTuning.get().sweepArcDegrees / 2.0D + SummonTuning.get().sweepArcDegrees * progress;
        float yaw = owner.getYRot() + (float) offset;
        Vec3 dir = Vec3.directionFromRotation(0.0F, yaw).normalize();
        return eye.muzzle().add(dir.scale(tuned.shape.range));
    }

    private static LivingEntity targetFor(ServerLevel level, BeholderEyeEntity eye, Player owner) {
        LivingEntity current = eye.abilityTarget();
        if (current != null && current.isAlive()) {
            return current;
        }
        AABB search = eye.getBoundingBox().inflate(SummonTuning.get().reacquireRadius);
        LivingEntity best = null;
        double nearest = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, search)) {
            if (!candidate.isAlive() || !AffixTargets.canHit(owner, candidate)) {
                continue;
            }
            double distance = candidate.distanceToSqr(eye);
            if (distance < nearest) {
                nearest = distance;
                best = candidate;
            }
        }
        eye.retarget(best);
        return best;
    }

    private static void blinkWatch(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                   int elapsed, float damage, AbilityConfig.Entry tuned) {
        LivingEntity target = targetFor(level, eye, owner);
        if (target == null) {
            return;
        }
        if (elapsed == 1) {
            Vec3 away = target.position().subtract(owner.position()).normalize();
            Vec3 spot = target.position().add(away.scale(tuned.shape.range)).add(0.0D, 1.2D, 0.0D);
            eye.setPos(spot.x, spot.y, spot.z);
            eye.holdAt(spot);
            level.sendParticles(ParticleTypes.PORTAL, spot.x, spot.y, spot.z,
                    12, 0.3D, 0.3D, 0.3D, 0.05D);
            cue(level, eye, SoundEvents.ENDERMAN_TELEPORT, tuned.fx.pitch);
        }
        if (elapsed == tuned.windup()) {
            Vec3 through = target.getBoundingBox().getCenter();
            Vec3 far = through.add(through.subtract(eye.muzzle()).normalize().scale(SummonTuning.get().blinkOvershoot));
            AbilityConfig.Entry cut = cfg(eye, owner, WIDE_CUT);
            beam(level, eye, owner, eye.muzzle(), far, damage, cut.targets(), cut);
        }
    }

    private static void tether(ServerLevel level, BeholderEyeEntity eye, Player owner,
                               int elapsed, float damage, AbilityConfig.Entry tuned) {
        LivingEntity target = targetFor(level, eye, owner);
        if (target == null) {
            return;
        }
        Vec3 centre = target.getBoundingBox().getCenter();
        eye.setBeamEnd(centre);

        if (elapsed % tuned.windup() != 0) {
            return;
        }
        float slice = damage / Math.max(1, tuned.duration() / tuned.windup());
        strike(eye, owner, target, slice, tuned);
        AbilityConfig.Entry bond = tuned;
        if (bond.heals()) {
            owner.heal(slice * bond.damage.lifesteal);
        }
        cue(level, eye, SoundEvents.SOUL_ESCAPE.value(), tuned.fx.pitch);
        level.sendParticles(ParticleTypes.SOUL, centre.x, centre.y, centre.z,
                3, 0.2D, 0.2D, 0.2D, 0.01D);
    }

    private static void collapse(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                 int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed < tuned.windup()) {
            double sunk = Math.abs(tuned.shape.rise) * (elapsed / (double) tuned.windup());
            eye.holdAt(eye.abilityStart().subtract(0.0D, sunk, 0.0D));
            if (elapsed % SummonTuning.get().trailInterval == 0) {
                level.sendParticles(ParticleTypes.SCULK_SOUL,
                        eye.getX(), eye.getY(), eye.getZ(), 1, 0.15D, 0.15D, 0.15D, 0.01D);
            }
            return;
        }
        if (elapsed != tuned.windup()) {
            return;
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, eye.getX(), eye.getY(), eye.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, BlockPos.containing(eye.position()),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6F, 1.4F);

        AABB area = eye.getBoundingBox().inflate(tuned.shape.radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (AffixTargets.canHit(owner, victim)) {
                strike(eye, owner, victim, damage, tuned);
                Convergence.apply(owner, victim);
            }
        }
    }

    private static void eclipse(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed < tuned.windup()) {
            double climb = tuned.shape.rise * (elapsed / (double) tuned.windup());
            eye.holdAt(eye.abilityStart().add(0.0D, climb, 0.0D));
            if (elapsed % SummonTuning.get().trailInterval == 0) {
                level.sendParticles(ParticleTypes.SCULK_SOUL,
                        eye.getX(), eye.getY(), eye.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.01D);
            }
            return;
        }
        if (elapsed != tuned.windup()) {
            return;
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, eye.getX(), eye.getY(), eye.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, eye.getX(), eye.getY(), eye.getZ(),
                tuned.fx.particles, tuned.fx.spread, tuned.fx.spread, tuned.fx.spread, 0.15D);
        level.playSound(null, BlockPos.containing(eye.position()),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                tuned.fx.volume, tuned.fx.pitch);

        AABB area = eye.getBoundingBox().inflate(tuned.shape.radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!AffixTargets.canHit(owner, victim)) {
                continue;
            }
            strike(eye, owner, victim, damage, tuned);
            Convergence.apply(owner, victim);
            pull(eye, victim, tuned);
        }
    }

    private static void pull(BeholderEyeEntity eye, LivingEntity victim, AbilityConfig.Entry tuned) {
        Vec3 toward = eye.position().subtract(victim.position());
        if (toward.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 drag = toward.normalize().scale(tuned.shape.pull);
        victim.push(drag.x, tuned.shape.lift, drag.z);
        victim.hurtMarked = true;
    }

    private static void supernova(ServerLevel level, BeholderEyeEntity eye, Player owner,
                                  int elapsed, float damage, AbilityConfig.Entry tuned) {
        if (elapsed < tuned.windup()) {
            eye.holdAt(eye.abilityStart().add(0.0D,
                    tuned.shape.rise * (elapsed / (double) tuned.windup()), 0.0D));
            if (elapsed % SummonTuning.get().trailInterval == 0) {
                level.sendParticles(ParticleTypes.END_ROD, eye.getX(), eye.getY(), eye.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.02D);
            }
            return;
        }
        if (elapsed != tuned.windup()) {
            return;
        }
        level.sendParticles(ParticleTypes.FLASH, eye.getX(), eye.getY(), eye.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, eye.getX(), eye.getY(), eye.getZ(),
                tuned.fx.particles, tuned.fx.spread, tuned.fx.spread, tuned.fx.spread, 0.35D);
        level.playSound(null, BlockPos.containing(eye.position()),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                tuned.fx.volume, tuned.fx.pitch);

        AABB area = eye.getBoundingBox().inflate(tuned.shape.radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!AffixTargets.canHit(owner, victim)) {
                continue;
            }
            float spent = Convergence.detonate(owner, victim);
            strike(eye, owner, victim, damage * (1.0F + spent), tuned);
            if (spent > 0.0F) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        victim.getX(), victim.getY() + 1.0D, victim.getZ(),
                        tuned.fx.particles / 2, tuned.fx.spread, tuned.fx.spread, tuned.fx.spread, 0.2D);
            }
        }
    }

    private static void beam(ServerLevel level, BeholderEyeEntity eye, Player owner,
                             Vec3 from, Vec3 to, float damage, int maxTargets,
                             AbilityConfig.Entry tuned) {
        eye.setBeamEnd(to);

        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-3D) {
            return;
        }
        Vec3 step = delta.normalize().scale(SummonTuning.get().beamStep);
        Set<LivingEntity> struck = new HashSet<>();
        Vec3 at = from;
        for (double travelled = 0.0D; travelled < length; travelled += SummonTuning.get().beamStep) {
            AABB box = new AABB(at, at).inflate(SummonTuning.get().beamHitPadding);
            List<LivingEntity> found = level.getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity victim : found) {
                if (struck.contains(victim) || !AffixTargets.canHit(owner, victim)) {
                    continue;
                }
                strike(eye, owner, victim, damage, tuned);
                struck.add(victim);
                if (struck.size() >= maxTargets) {
                    return;
                }
            }
            at = at.add(step);
        }
    }

    private static final String TWIN_LANCE = "twin_lance";
    private static final String FURROW = "furrow";
    private static final String WIDE_CUT = "wide_cut";
    private static final String BLINK_WATCH = "blink_watch";
    private static final String TETHER = "tether";
    private static final String ECLIPSE = "eclipse";
    private static final String SUPERNOVA = "supernova";
    private static final String COLLAPSE = "collapse";

    private static AbilityConfig.Entry cfg(Player owner, String ability) {
        SummonProfile.Resolved resolved = owner == null ? null : SummonProfiles.resolvedFor(owner);
        return resolved == null ? AbilityConfig.get(ability) : resolved.tuningFor(ability);
    }

    private static AbilityConfig.Entry cfg(BeholderEyeEntity eye, Player owner, String ability) {
        AbilityConfig.Entry entry = cfg(owner, ability);
        AbilityTuning ordered = eye == null ? null : eye.orderTuning();
        return ordered == null ? entry : ordered.over(entry);
    }

    private static void strike(BeholderEyeEntity eye, Player owner, LivingEntity victim,
                               float damage, AbilityConfig.Entry tuned) {
        if (damage < SummonTuning.get().minimumDamage || victim == null || !victim.isAlive()) {
            return;
        }
        float amplified = damage * Convergence.amplify(owner, victim);
        DamageSource source = schoolSource(eye, owner, tuned);
        DamageHurt.bypassingIFrames(victim, source, amplified);
        if (eye instanceof GreatEyeEntity great) {
            great.onAbilityHit(eye.abilityKey(), victim);
        }
        AffixDamageDealer.onSummonHit(owner, victim, amplified);
        applyMark(owner, victim, tuned);
    }

    private static void applyMark(Player owner, LivingEntity victim, AbilityConfig.Entry tuned) {
        if (tuned == null || !tuned.marks()) {
            return;
        }
        if (!EffectChance.rolls(owner, tuned.damage.appliesChance)) {
            return;
        }
        Convergence.apply(owner, victim, tuned.damage.applies, tuned.damage.appliesStacks);
    }

    private static DamageSource schoolSource(BeholderEyeEntity eye, Player owner,
                                             AbilityConfig.Entry tuned) {
        ResourceLocation typeId = SpellPowerAccess.damageTypeOf(
                tuned == null ? null : tuned.damage.school);
        if (typeId != null) {
            Holder<DamageType> holder = eye.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolder(ResourceKey.create(
                            Registries.DAMAGE_TYPE, typeId))
                    .orElse(null);
            if (holder != null) {
                return new DamageSource(holder, eye, owner);
            }
        }
        return owner.damageSources().indirectMagic(eye, owner);
    }
}
