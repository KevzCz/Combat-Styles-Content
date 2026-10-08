package net.pixeldreamstudios.combat_styles_content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BleedingEffect extends MobEffect {

    private static final int COLOUR = 0xA3121A;

    protected BleedingEffect() {
        super(MobEffectCategory.HARMFUL, COLOUR);
    }
}
