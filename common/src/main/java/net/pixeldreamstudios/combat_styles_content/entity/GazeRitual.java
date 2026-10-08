package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.damage.DamageDealer;
import net.pixeldreamstudios.combat_styles.damage.DamageSpec;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GazeRitual {

    private static final int PULSE_PARTICLES = 14;
    private static final double PULSE_SPREAD = 0.6D;
    private static final double PULSE_SPEED = 0.02D;
    private static final int TRAIL_PARTICLES = 2;
    private static final int TRAIL_INTERVAL = 4;
    private static final double TRAIL_SPREAD = 0.15D;
    private static final double TRAIL_SPEED = 0.01D;
    private static final double LINK_STEP = 0.35D;
    private static final double CHEST_HEIGHT = 1.1D;
    private static final float CALL_VOLUME = 0.45F;
    private static final float PITCH_STEP = 0.08F;
    private static final float BASE_PITCH = 0.9F;

    private static final int RING_POINTS = 28;
    private static final int RING_POINTS_PER_EYE = 8;
    private static final double RING_HEIGHT = 0.15D;
    private static final double RING_SPEED_PER_BLOCK = 0.09D;
    private static final double INNER_RING_SPEED = 0.55D;
    private static final int COLUMN_PARTICLES = 24;
    private static final double COLUMN_SPREAD = 0.25D;
    private static final double COLUMN_HEIGHT = 1.2D;
    private static final double COLUMN_SPEED = 0.12D;
    private static final int HIT_PARTICLES = 8;
    private static final double HIT_SPREAD = 0.35D;
    private static final double HIT_SPEED = 0.1D;
    private static final double SOURCE_HEIGHT = 1.0D;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double MIN_DISTANCE_SQR = 1.0E-4D;
    private static final float WAVE_VOLUME = 0.6F;
    private static final float WAVE_PITCH = 0.8F;
    private static final double BODY_HEIGHT = 0.5D;
    private static final Vec3 HORIZONTAL = new Vec3(1.0D, 0.0D, 1.0D);

    private static final Map<UUID, Integer> SETTLED = new ConcurrentHashMap<>();

    private GazeRitual() {
    }

    public static boolean settle(Player player, GazeTuning tuning) {
        int held = SETTLED.merge(player.getUUID(), 1, Integer::sum);
        if (held < tuning.settleTicks()) {
            return false;
        }
        SETTLED.remove(player.getUUID());
        return true;
    }

    public static void unsettle(Player player) {
        SETTLED.remove(player.getUUID());
    }

    public static void forget(UUID playerId) {
        SETTLED.remove(playerId);
    }

    public static int tick(ServerLevel level, Player player, int elapsed, GazeTuning tuning) {
        List<BeholderEyeEntity> eyes = BeholderEyes.ownedEyesOf(player);
        if (eyes.isEmpty()) {
            return 0;
        }

        int interval = tuning.gatherIntervalTicks();
        int gathered = countCrowned(eyes);
        if (elapsed >= interval && elapsed % interval == 0 && gathered < eyes.size()) {
            BeholderEyeEntity next = nearestFree(player, eyes);
            if (next != null) {
                next.joinCrown(gathered);
                gathered++;
                announce(level, player, next, gathered);
            }
        }

        if (elapsed % TRAIL_INTERVAL == 0) {
            for (BeholderEyeEntity eye : eyes) {
                if (eye.inCrown()) {
                    level.sendParticles(ParticleTypes.END_ROD, eye.getX(), eye.getY(), eye.getZ(),
                            TRAIL_PARTICLES, TRAIL_SPREAD, TRAIL_SPREAD, TRAIL_SPREAD, TRAIL_SPEED);
                }
            }
        }
        return gathered;
    }

    public static boolean complete(Player player, int gathered) {
        List<BeholderEyeEntity> eyes = BeholderEyes.ownedEyesOf(player);
        return !eyes.isEmpty() && gathered >= eyes.size();
    }

    public static void release(Player player) {
        unsettle(player);
        for (BeholderEyeEntity eye : BeholderEyes.ownedEyesOf(player)) {
            eye.leaveCrown();
        }
    }

    public static int shockwave(ServerLevel level, Player player, int crown, GazeTuning tuning) {
        double radius = tuning.radiusFor(crown);
        double strength = tuning.strengthFor(crown);
        Vec3 centre = player.position();
        waveParticles(level, centre, radius, crown);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, WAVE_VOLUME, WAVE_PITCH);

        List<LivingEntity> victims = ProjectileStrikes.foesAround(level, player, centre, radius, player);
        if (victims.isEmpty()) {
            return 0;
        }
        DamageSpec spec = tuning.damage();
        DamageSource source = DamageDealer.specSourceFor(player, spec)
                .orElseGet(() -> player.damageSources().indirectMagic(player, player));
        float scale = tuning.damageScaleFor(crown);
        int stacks = Math.max(0, tuning.gazedPerEye() * crown);
        DamageDealer.beginDealing();
        try {
            for (LivingEntity victim : victims) {
                float amount = spec.amountFor(player, 0.0F, victim) * scale;
                if (amount > 0.0F) {
                    DamageDealer.dealSpec(player, victim, spec, amount, source);
                }
                push(player, victim, strength, tuning.lift());
                if (stacks > 0) {
                    Convergence.apply(player, victim, null, stacks);
                }
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, victim.getX(), victim.getY(BODY_HEIGHT),
                        victim.getZ(), HIT_PARTICLES, HIT_SPREAD, HIT_SPREAD, HIT_SPREAD, HIT_SPEED);
                level.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY(BODY_HEIGHT),
                        victim.getZ(), HIT_PARTICLES, HIT_SPREAD, HIT_SPREAD, HIT_SPREAD, HIT_SPEED);
            }
        } finally {
            DamageDealer.endDealing();
        }
        return victims.size();
    }

    private static void push(Player player, LivingEntity victim, double strength, double lift) {
        Vec3 away = victim.position().subtract(player.position()).multiply(HORIZONTAL);
        if (away.lengthSqr() < MIN_DISTANCE_SQR) {
            return;
        }
        Vec3 push = away.normalize().scale(strength);
        victim.push(push.x, lift, push.z);
        victim.hurtMarked = true;
    }

    private static void waveParticles(ServerLevel level, Vec3 centre, double radius, int crown) {
        level.sendParticles(ParticleTypes.SONIC_BOOM, centre.x, centre.y + SOURCE_HEIGHT, centre.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, centre.x, centre.y + COLUMN_HEIGHT, centre.z,
                COLUMN_PARTICLES, COLUMN_SPREAD, COLUMN_HEIGHT, COLUMN_SPREAD, COLUMN_SPEED);
        int points = RING_POINTS + RING_POINTS_PER_EYE * Math.max(0, crown - 1);
        double speed = radius * RING_SPEED_PER_BLOCK;
        ring(level, centre, points, ParticleTypes.SOUL_FIRE_FLAME, speed);
        ring(level, centre, points, ParticleTypes.END_ROD, speed * INNER_RING_SPEED);
        ring(level, centre, points / 2, ParticleTypes.SCULK_SOUL, speed);
    }

    private static void ring(ServerLevel level, Vec3 centre, int points, ParticleOptions particle,
                             double speed) {
        for (int index = 0; index < points; index++) {
            double angle = TWO_PI * index / points;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.sendParticles(particle, centre.x, centre.y + RING_HEIGHT, centre.z,
                    0, dx, 0.0D, dz, speed);
        }
    }

    private static int countCrowned(List<BeholderEyeEntity> eyes) {
        int found = 0;
        for (BeholderEyeEntity eye : eyes) {
            if (eye.inCrown()) {
                found++;
            }
        }
        return found;
    }

    private static BeholderEyeEntity nearestFree(Player player, List<BeholderEyeEntity> eyes) {
        BeholderEyeEntity best = null;
        double nearest = Double.MAX_VALUE;
        for (BeholderEyeEntity eye : eyes) {
            if (eye.inCrown()) {
                continue;
            }
            if (!eye.temporary()) {
                return eye;
            }
            double distance = eye.distanceToSqr(player);
            if (distance < nearest) {
                nearest = distance;
                best = eye;
            }
        }
        return best;
    }

    private static void announce(ServerLevel level, Player player,
                                 BeholderEyeEntity eye, int gathered) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, eye.getX(), eye.getY(), eye.getZ(),
                PULSE_PARTICLES, PULSE_SPREAD, PULSE_SPREAD, PULSE_SPREAD, PULSE_SPEED);
        link(level, player.position().add(0.0D, CHEST_HEIGHT, 0.0D), eye.position());
        level.playSound(null, BlockPos.containing(eye.position()),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                CALL_VOLUME, BASE_PITCH + gathered * PITCH_STEP);
    }

    private static void link(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 span = to.subtract(from);
        int steps = (int) Math.ceil(span.length() / LINK_STEP);
        for (int step = 0; step <= steps; step++) {
            Vec3 at = from.add(span.scale(step / (double) Math.max(1, steps)));
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
