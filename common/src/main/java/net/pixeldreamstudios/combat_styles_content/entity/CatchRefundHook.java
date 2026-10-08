package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.custom.CustomUse;
import net.pixeldreamstudios.combat_styles.custom.Customs;
import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.equipment.EffectKeys;
import net.pixeldreamstudios.combat_styles.equipment.EquipmentDefinition;
import net.pixeldreamstudios.combat_styles.equipment.GateScope;
import net.pixeldreamstudios.combat_styles.equipment.Gates;
import net.pixeldreamstudios.combat_styles.equipment.TriggeredEffect;
import net.pixeldreamstudios.combat_styles.equipment.WornEquipment;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CatchRefundHook implements CustomHook {

    public static final String ID = CombatStylesContent.MOD_ID + ":catch_refund";
    public static final String KEY_EFFECT = "effect";
    public static final String KEY_RATIO = "ratio";
    private static final String ALL_EFFECTS = "";
    private static final float DEFAULT_RATIO = 0.35F;
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.catch_refund";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_EFFECT, Codec.STRING, ALL_EFFECTS),
            OptionField.of(KEY_RATIO, Codec.FLOAT, DEFAULT_RATIO));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                Math.round(HookOptions.floatOf(options, KEY_RATIO, DEFAULT_RATIO) * PERCENT)));
    }

    public static void caught(Player owner, Entity projectile) {
        if (owner == null || projectile == null || owner.level().isClientSide()) {
            return;
        }
        for (CustomUse use : Customs.of(ProjectileUpkeep.boxFor(projectile))) {
            if (ID.equals(use.hook())) {
                refund(owner, projectile, use.optionsOrEmpty());
            }
        }
    }

    private static void refund(Player owner, Entity projectile, JsonObject options) {
        ItemStack weapon = projectile instanceof ShurikenEntity shuriken && !shuriken.weapon().isEmpty()
                ? shuriken.weapon()
                : owner.getMainHandItem();
        String itemId = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        String only = HookOptions.stringOf(options, KEY_EFFECT, ALL_EFFECTS);
        float ratio = HookOptions.floatOf(options, KEY_RATIO, DEFAULT_RATIO);
        for (WornEquipment worn : WornEquipment.of(owner)) {
            EquipmentDefinition definition = worn.definition();
            if (!definition.id().equals(itemId)) {
                continue;
            }
            String ownerKey = EffectKeys.ownerOf(definition.id(), worn.copy());
            for (Map.Entry<String, TriggeredEffect> named : definition.effects().entrySet()) {
                if (!only.isBlank() && !only.equals(named.getKey())) {
                    continue;
                }
                int ticks = Math.round(named.getValue().cooldown().ticks() * ratio);
                Gates.refund(owner, GateScope.EFFECT, ownerKey, named.getKey(), ticks);
            }
            return;
        }
    }
}
