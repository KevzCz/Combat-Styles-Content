package net.pixeldreamstudios.combat_styles_content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WoundedEffect extends MobEffect {

    private static final int COLOUR = 0x5E0B10;

    protected WoundedEffect() {
        super(MobEffectCategory.HARMFUL, COLOUR);
    }
}
