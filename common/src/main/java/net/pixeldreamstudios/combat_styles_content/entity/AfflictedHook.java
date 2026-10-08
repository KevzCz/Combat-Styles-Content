package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AfflictedHook implements CustomHook {

    public static final String KEY_EFFECTS = "effects";
    public static final String KEY_BONUS = "bonus";
    private static final float DEFAULT_BONUS = 0.1F;
    private static final String SAMPLE_EFFECT = "minecraft:poison";
    private static final String NAME_SEPARATOR = ", ";
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.afflicted";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_EFFECTS, Codec.STRING.listOf(), List.of(SAMPLE_EFFECT)),
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
        if (target == null) {
            return amount;
        }
        for (ResourceLocation id : effectsOf(context.options())) {
            boolean afflicted = BuiltInRegistries.MOB_EFFECT.getHolder(id)
                    .map(target::hasEffect)
                    .orElse(false);
            if (afflicted) {
                return amount * (1.0F + HookOptions.floatOf(context.options(), KEY_BONUS, DEFAULT_BONUS));
            }
        }
        return amount;
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        MutableComponent names = Component.empty();
        List<ResourceLocation> effects = effectsOf(options);
        for (int index = 0; index < effects.size(); index++) {
            if (index > 0) {
                names.append(Component.literal(NAME_SEPARATOR));
            }
            names.append(StyleText.registryName(effects.get(index).toString()));
        }
        return List.of(Component.translatable(KEY_TOOLTIP,
                StyleText.formatCompact(HookOptions.floatOf(options, KEY_BONUS, DEFAULT_BONUS) * PERCENT),
                names));
    }

    private static List<ResourceLocation> effectsOf(JsonObject options) {
        List<ResourceLocation> found = new ArrayList<>();
        if (options == null || !options.has(KEY_EFFECTS) || !options.get(KEY_EFFECTS).isJsonArray()) {
            return found;
        }
        for (JsonElement element : options.getAsJsonArray(KEY_EFFECTS)) {
            ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
            if (id != null) {
                found.add(id);
            }
        }
        return found;
    }
}
