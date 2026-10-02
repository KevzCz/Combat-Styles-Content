package net.pixeldreamstudios.combat_styles_content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class GazedEffect extends MobEffect {

    private static final int COLOUR = 0x6E97E8;

    protected GazedEffect() {
        super(MobEffectCategory.HARMFUL, COLOUR);
    }
}
