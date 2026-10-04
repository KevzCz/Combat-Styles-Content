package net.pixeldreamstudios.combat_styles_content.config;

import net.pixeldreamstudios.combat_styles.config.StatusEffectConfig;
import net.pixeldreamstudios.combat_styles_content.entity.Convergence;
import net.pixeldreamstudios.combat_styles_content.entity.Gazing;

public final class ContentConfig {

    private static final int GAZED_DURATION = 200;
    private static final int GAZED_MAX_STACKS = 5;
    private static final float GAZED_PER_STACK = 0.25F;
    private static final int GAZING_DURATION = 200;
    private static final int GAZING_MAX_STACKS = 1;
    private static final float GAZING_PER_STACK = 0.0F;

    private ContentConfig() {
    }

    public static void register() {
        StatusEffectConfig.registerDefault(Convergence.ID, GAZED_DURATION, GAZED_MAX_STACKS,
                GAZED_PER_STACK);
        StatusEffectConfig.registerDefault(Gazing.ID, GAZING_DURATION, GAZING_MAX_STACKS,
                GAZING_PER_STACK);
    }
}
