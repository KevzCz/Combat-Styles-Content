package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles_content.item.ContentItems;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles_content.config.ContentConfig;

import net.pixeldreamstudios.combat_styles.equipment.EquipmentRuntime;
import net.pixeldreamstudios.combat_styles.equipment.WornItems;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkullFlock {
    public static final int RESPAWN_TICKS = 40;

    private static final int RESTOCK_INTERVAL = 40;
    private static final double SPAWN_LIFT = 1.6D;
    private static final String NECKLACE = "skull_necklace";

    private static final double SEARCH_RADIUS = 24.0D;
    private static final double FULL_TURN = Math.PI * 2.0D;

    private static final float ORBIT_SLOT_EPSILON = 1.0F;

    private static final Map<UUID, Long> LAST_SPAWN = new ConcurrentHashMap<>();

    private SkullFlock() {
    }

    public static void forget(UUID playerId) {
        LAST_SPAWN.remove(playerId);
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

    public static boolean canSpawn(Player player) {
        if (skullsOf(player).size() >= Math.max(0, ContentConfig.skulls().maxSkulls)) {
            return false;
        }
        Long last = LAST_SPAWN.get(player.getUUID());
        long now = player.level().getGameTime();
        if (last == null) {
            return true;
        }
        if (now < last) {
            LAST_SPAWN.remove(player.getUUID());
            return true;
        }
        return now - last >= RESPAWN_TICKS;
    }

    public static void markSpawned(Player player, KamikazeSkullEntity skull) {
        LAST_SPAWN.put(player.getUUID(), player.level().getGameTime());
        skull.setOrbitOffset(freeOrbitOffset(player, ContentConfig.skulls().maxSkulls, skull));
    }

    public static void keepAlive(Player player) {
        if (player.level().isClientSide() || !(player.level() instanceof ServerLevel server)) {
            return;
        }
        if (!ContentConfig.skulls().skullsFollow || !wearing(player)) {
            return;
        }
        long now = server.getGameTime();
        if (now % RESTOCK_INTERVAL != 0L) {
            return;
        }
        List<KamikazeSkullEntity> existing = skullsOf(player);
        int wanted = Math.max(0, ContentConfig.skulls().maxSkulls);

        List<KamikazeSkullEntity> placed = new ArrayList<>(existing);
        for (int i = existing.size(); i < wanted; i++) {
            if (!EquipmentRuntime.claimCooldown(player, CombatStylesContent.MOD_ID + ":" + NECKLACE)) {
                return;
            }
            KamikazeSkullEntity skull = ContentEntities.KAMIKAZE_SKULL.get().create(server);
            if (skull == null) {
                return;
            }
            skull.setPos(player.getX(), player.getY() + SPAWN_LIFT, player.getZ());
            skull.configure(player, null, 0.0F, 0);
            skull.setOrbitOffset(freeOrbitOffset(placed, wanted));
            server.addFreshEntity(skull);
            placed.add(skull);
        }
    }

    private static float freeOrbitOffset(Player player, int wanted,
                                         KamikazeSkullEntity placing) {
        List<KamikazeSkullEntity> flock = new ArrayList<>(skullsOf(player));
        flock.remove(placing);
        return freeOrbitOffset(flock, wanted);
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

    private static boolean wearing(Player player) {
        for (ItemStack worn : WornItems.of(player)) {
            if (!worn.isEmpty() && worn.is(ContentItems.equipment(NECKLACE).get())) {
                return true;
            }
        }
        return false;
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
