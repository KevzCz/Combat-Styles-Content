package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;
import java.util.Set;

public final class VelocityDamageHook implements CustomHook {

    public static final String KEY_PER_SPEED = "per_speed";
    private static final float DEFAULT_PER_SPEED = 0.1F;
    private static final float NO_BONUS = 0.0F;
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.velocity_damage";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_PER_SPEED, Codec.FLOAT, DEFAULT_PER_SPEED));

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
        Entity actor = context.actor();
        if (actor == null) {
            return amount;
        }
        float perSpeed = HookOptions.floatOf(context.options(), KEY_PER_SPEED, DEFAULT_PER_SPEED);
        float speed = (float) actor.getDeltaMovement().length();
        return amount * (1.0F + Math.max(NO_BONUS, perSpeed * speed));
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP, StyleText.formatDecimal(
                HookOptions.floatOf(options, KEY_PER_SPEED, DEFAULT_PER_SPEED) * PERCENT)));
    }
}
