package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.affix.AffixTargets;
import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.damage.DamageDealer;
import net.pixeldreamstudios.combat_styles.damage.DamageSpec;

import java.util.ArrayList;
import java.util.List;

public final class ProjectileStrikes {

    private static final ThreadLocal<Boolean> STRIKING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ProjectileStrikes() {
    }

    public static boolean striking() {
        return STRIKING.get();
    }

    public static List<LivingEntity> foesAround(ServerLevel level, Player owner, Vec3 centre,
                                                double radius, Entity excluded) {
        List<LivingEntity> found = new ArrayList<>();
        AABB area = new AABB(centre, centre).inflate(radius);
        double reach = radius * radius;
        for (Entity candidate : level.getEntities((Entity) null, area,
                entity -> reachSqr(entity.getBoundingBox(), centre) <= reach)) {
            LivingEntity victim = candidate instanceof EnderDragonPart part ? part.parentMob
                    : candidate instanceof LivingEntity living ? living : null;
            if (victim != null && victim.isAlive() && victim != excluded && !found.contains(victim)
                    && AffixTargets.canHit(owner, victim)) {
                found.add(victim);
            }
        }
        return found;
    }

    private static double reachSqr(AABB box, Vec3 point) {
        double dx = Math.max(box.minX - point.x, Math.max(0.0D, point.x - box.maxX));
        double dy = Math.max(box.minY - point.y, Math.max(0.0D, point.y - box.maxY));
        double dz = Math.max(box.minZ - point.z, Math.max(0.0D, point.z - box.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }

    public static int strikeArea(ServerLevel level, Entity source, Player owner, DamageSpec spec,
                                 Vec3 centre, double radius, Entity excluded) {
        if (spec == null || owner == null || STRIKING.get()) {
            return 0;
        }
        return strike(source, owner, spec, foesAround(level, owner, centre, radius, excluded));
    }

    public static boolean strikeOne(Entity source, Player owner, DamageSpec spec, LivingEntity victim) {
        if (spec == null || owner == null || victim == null || !victim.isAlive() || STRIKING.get()) {
            return false;
        }
        return strike(source, owner, spec, List.of(victim)) > 0;
    }

    private static int strike(Entity source, Player owner, DamageSpec spec, List<LivingEntity> victims) {
        float triggering = ProjectileUpkeep.triggeringFor(source);
        STRIKING.set(Boolean.TRUE);
        DamageDealer.beginDealing();
        try {
            for (LivingEntity victim : victims) {
                DamageDealer.dealSpec(owner, victim, spec, spec.amountFor(owner, triggering, victim),
                        source.damageSources().thrown(source, owner));
            }
        } finally {
            DamageDealer.endDealing();
            STRIKING.set(Boolean.FALSE);
        }
        return victims.size();
    }
}
