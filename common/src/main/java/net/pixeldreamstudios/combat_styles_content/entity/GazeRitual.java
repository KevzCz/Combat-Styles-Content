package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.affix.AffixTargets;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GazeRitual {

    public static final int GATHER_INTERVAL = 10;
    public static final int SETTLE_TICKS = 20;

    private static final double KNOCKBACK_RADIUS = 4.5D;
    private static final double KNOCKBACK_STRENGTH = 0.45D;
    private static final double KNOCKBACK_LIFT = 0.22D;
    private static final int PULSE_PARTICLES = 10;
    private static final double PULSE_SPREAD = 0.6D;
    private static final int TRAIL_PARTICLES = 1;
    private static final int TRAIL_INTERVAL = 4;
    private static final double TRAIL_SPREAD = 0.15D;
    private static final float CALL_VOLUME = 0.45F;
    private static final float PITCH_STEP = 0.08F;
    private static final float BASE_PITCH = 0.9F;

    private static final Map<UUID, Integer> SETTLED = new ConcurrentHashMap<>();

    private GazeRitual() {
    }

    public static boolean settle(Player player) {
        int held = SETTLED.merge(player.getUUID(), 1, Integer::sum);
        if (held < SETTLE_TICKS) {
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

    public static int tick(ServerLevel level, Player player, int elapsed) {
        List<BeholderEyeEntity> eyes = BeholderEyes.ownedEyesOf(player);
        if (eyes.isEmpty()) {
            return 0;
        }

        int gathered = countCrowned(eyes);
        if (elapsed >= GATHER_INTERVAL && elapsed % GATHER_INTERVAL == 0
                && gathered < eyes.size()) {
            BeholderEyeEntity next = nearestFree(player, eyes);
            if (next != null) {
                next.joinCrown(gathered);
                gathered++;
                announce(level, player, next, gathered);
                knockback(level, player);
            }
        }

        if (elapsed % TRAIL_INTERVAL == 0) {
            for (BeholderEyeEntity eye : eyes) {
                if (eye.inCrown()) {
                    level.sendParticles(ParticleTypes.END_ROD, eye.getX(), eye.getY(), eye.getZ(),
                            TRAIL_PARTICLES, TRAIL_SPREAD, TRAIL_SPREAD, TRAIL_SPREAD, 0.01D);
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
                PULSE_PARTICLES, PULSE_SPREAD, PULSE_SPREAD, PULSE_SPREAD, 0.02D);
        level.playSound(null, BlockPos.containing(eye.position()),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                CALL_VOLUME, BASE_PITCH + gathered * PITCH_STEP);
    }

    private static void knockback(ServerLevel level, Player player) {
        level.sendParticles(ParticleTypes.SONIC_BOOM,
                player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(KNOCKBACK_RADIUS))) {
            if (victim == player || !AffixTargets.canHit(player, victim)) {
                continue;
            }
            Vec3 away = victim.position().subtract(player.position());
            if (away.lengthSqr() < 1.0E-4D) {
                continue;
            }
            Vec3 push = away.normalize().scale(KNOCKBACK_STRENGTH);
            victim.push(push.x, KNOCKBACK_LIFT, push.z);
            victim.hurtMarked = true;
        }
    }
}
