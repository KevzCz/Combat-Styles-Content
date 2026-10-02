package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.entity.ai.attributes.Attributes;

import net.pixeldreamstudios.combat_styles.entity.SummonRoster;
import net.pixeldreamstudios.combat_styles.equipment.EquipmentDefinition;
import net.pixeldreamstudios.combat_styles.equipment.EquipmentProviders;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfiles;
import net.pixeldreamstudios.combat_styles.equipment.WornItems;

import java.util.List;

public final class GreatEyeFlock {
    public static final String RING = "combat_styles_content:occular_ring";

    private static final int UPKEEP_INTERVAL = 20;
    private static final double SPAWN_LIFT = 2.0D;
    private static final int DEFAULT_MAX_ACTIVE = 1;
    private static final float DEFAULT_DAMAGE_MULTIPLIER = 1.0F;

    private GreatEyeFlock() {
    }

    public static void keepAlive(Player player) {
        if (player == null || player.level().isClientSide()
                || !(player.level() instanceof ServerLevel server)) {
            return;
        }
        if (server.getGameTime() % UPKEEP_INTERVAL != 0L) {
            return;
        }
        List<GreatEyeEntity> existing = SummonRoster.ofType(player, GreatEyeEntity.class);
        SummonProfile profile = profileOf(player);
        if (profile == null) {
            for (GreatEyeEntity eye : existing) {
                eye.dismiss();
            }
            return;
        }
        int maxActive = Math.max(1, profile.maxActive().orElse(DEFAULT_MAX_ACTIVE));
        if (existing.size() >= maxActive) {
            return;
        }
        GreatEyeEntity eye = ContentEntities.GREAT_EYE.get().create(server);
        if (eye == null) {
            return;
        }
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * profile.damageMultiplier().orElse(DEFAULT_DAMAGE_MULTIPLIER);
        eye.setPos(player.getX(), player.getY() + SPAWN_LIFT, player.getZ());
        eye.configure(player, null, damage,
                profile.lifespan().orElse(GreatEyeEntity.KEEP_LIFESPAN));
        eye.assign(player, 0.0F);
        server.addFreshEntity(eye);
    }

    private static SummonProfile profileOf(Player player) {
        for (ItemStack stack : WornItems.of(player)) {
            EquipmentDefinition definition = EquipmentProviders.definitionFor(stack);
            if (definition != null && RING.equals(definition.id())) {
                SummonProfile profile = SummonProfiles.profileOf(player, stack);
                return profile == null ? SummonProfile.EMPTY : profile;
            }
        }
        return null;
    }

    public static boolean wearing(Player player) {
        for (ItemStack stack : WornItems.of(player)) {
            EquipmentDefinition definition = EquipmentProviders.definitionFor(stack);
            if (definition != null && RING.equals(definition.id())) {
                return true;
            }
        }
        return false;
    }
}
