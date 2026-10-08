package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.equipment.StateGate;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;
import java.util.Set;

public final class BackstabHook implements CustomHook {

    public static final String KEY_BONUS = "bonus";
    private static final float DEFAULT_BONUS = 0.25F;
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.backstab";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_BONUS, Codec.FLOAT, DEFAULT_BONUS));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public float modifyDamage(CustomContext context, float amount) {
        LivingEntity target = context.target();
        if (target == null || context.owner() == null) {
            return amount;
        }
        boolean exposed = !target.onGround() || StateGate.behind(context.owner(), target);
        return exposed
                ? amount * (1.0F + HookOptions.floatOf(context.options(), KEY_BONUS, DEFAULT_BONUS))
                : amount;
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_BONUS, DEFAULT_BONUS) * PERCENT)));
    }
}
