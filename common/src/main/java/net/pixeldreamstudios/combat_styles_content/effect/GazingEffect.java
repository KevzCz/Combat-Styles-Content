package net.pixeldreamstudios.combat_styles_content.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;

public class GazingEffect extends MobEffect {

    private static final int COLOUR = 0xA986D8;
    private static final String SOUL = SpellPowerAccess.MOD_ID + ":soul";
    private static final String MODIFIER = "gazing_soul";
    private static final double SOUL_BONUS = 0.2D;

    protected GazingEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOUR);
        Holder<Attribute> soul = soulPower();
        if (soul != null) {
            addAttributeModifier(soul, CombatStylesContent.id(MODIFIER), SOUL_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    public static double soulBonus() {
        return SOUL_BONUS;
    }

    private static Holder<Attribute> soulPower() {
        ResourceLocation id = ResourceLocation.tryParse(SOUL);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
    }
}
