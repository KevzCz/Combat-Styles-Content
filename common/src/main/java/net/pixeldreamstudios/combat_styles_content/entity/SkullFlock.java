package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class SkullFlock {

    private static final double SEARCH_RADIUS = 24.0D;
    private static final double FULL_TURN = Math.PI * 2.0D;

    private static final float ORBIT_SLOT_EPSILON = 1.0F;

    private SkullFlock() {
    }

    public static List<KamikazeSkullEntity> skullsOf(Player player) {
        List<KamikazeSkullEntity> found = new ArrayList<>();
        for (KamikazeSkullEntity skull : player.level().getEntitiesOfClass(
                KamikazeSkullEntity.class, player.getBoundingBox().inflate(SEARCH_RADIUS))) {
            if (!skull.fading() && player.getUUID().equals(skull.summonOwner())) {
                found.add(skull);
            }
        }
        return found;
    }

    public static float freeOrbitOffset(Player player, KamikazeSkullEntity placing) {
        List<KamikazeSkullEntity> flock = new ArrayList<>(skullsOf(player));
        flock.remove(placing);
        return freeOrbitOffset(flock, flock.size() + 1);
    }

    private static float freeOrbitOffset(List<KamikazeSkullEntity> flock, int wanted) {
        int slots = Math.max(1, wanted);
        float step = (float) (FULL_TURN / slots);
        for (int slot = 0; slot < slots; slot++) {
            float offset = slot * step;
            boolean taken = false;
            for (KamikazeSkullEntity skull : flock) {
                if (Math.abs(Mth.degreesDifference(
                        skull.orbitOffset() * Mth.RAD_TO_DEG,
                        offset * Mth.RAD_TO_DEG)) < ORBIT_SLOT_EPSILON) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return offset;
            }
        }
        return 0.0F;
    }

    public static float slotOffset(int slot, int count) {
        return (float) (FULL_TURN * slot / Math.max(1, count));
    }

    public static void diveAll(Player player, LivingEntity target) {
        diveAll(player, target, 0.0F);
    }

    public static int diveAll(Player player, LivingEntity target, float damage) {
        return diveAll(player, target, damage, List.of(), 0);
    }

    public static int diveAll(Player player, LivingEntity target, float damage,
                              List<String> entities, int limit) {
        if (player == null || !(player.level() instanceof ServerLevel)) {
            return 0;
        }
        if (!entities.isEmpty() && !entities.contains(skullId())) {
            return 0;
        }
        int budget = limit <= 0 ? Integer.MAX_VALUE : limit;
        int sent = 0;
        for (KamikazeSkullEntity skull : skullsOf(player)) {
            if (sent >= budget) {
                break;
            }
            if (skull.diving()) {
                continue;
            }
            skull.configure(player, target, damage, 0);
            skull.beginDive(target);
            sent++;
        }
        return sent;
    }

    private static String skullId() {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(
                ContentEntities.KAMIKAZE_SKULL.get());
        return id == null ? "" : id.toString();
    }
}
