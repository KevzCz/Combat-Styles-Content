package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.pixeldreamstudios.combat_styles.affix.EffectChance;
import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.entity.AbilityHosts;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;
import java.util.Set;

public final class SummonQuickenHook implements CustomHook {

    public static final String KEY_RATIO = "ratio";
    public static final String KEY_CHANCE = "chance";
    private static final float DEFAULT_RATIO = 0.1F;
    private static final float DEFAULT_CHANCE = 0.5F;
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.summon_quicken";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_RATIO, Codec.FLOAT, DEFAULT_RATIO),
            OptionField.of(KEY_CHANCE, Codec.FLOAT, DEFAULT_CHANCE));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onHit(CustomContext context, Entity source, LivingEntity target) {
        if (ProjectileStrikes.striking() || context.owner() == null) {
            return;
        }
        JsonObject options = context.options();
        if (EffectChance.rolls(context.owner(), HookOptions.floatOf(options, KEY_CHANCE, DEFAULT_CHANCE))) {
            AbilityHosts.quicken(context.owner(), HookOptions.floatOf(options, KEY_RATIO, DEFAULT_RATIO));
        }
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_CHANCE, DEFAULT_CHANCE) * PERCENT),
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_RATIO, DEFAULT_RATIO) * PERCENT)));
    }
}
