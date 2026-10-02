package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import net.pixeldreamstudios.combat_styles.config.StatusEffectConfig;
import net.pixeldreamstudios.combat_styles.effect.EffectLookup;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public final class Gazing {

    public static final String ID = "gazing";

    private Gazing() {
    }

    public static int durationTicks() {
        return StatusEffectConfig.get(ID).durationTicks;
    }

    public static boolean enabled() {
        return StatusEffectConfig.get(ID).enabled;
    }

    public static void begin(Player player, int gathered) {
        if (player == null || player.level().isClientSide() || !enabled()) {
            return;
        }
        Holder<MobEffect> holder = effect();
        if (holder == null) {
            return;
        }
        int amplifier = Math.max(0, Math.min(MAX_AMPLIFIER, gathered - 1));
        int duration = durationTicks();
        player.addEffect(new MobEffectInstance(holder, duration, amplifier,
                false, true, true));
        for (BeholderEyeEntity eye : BeholderEyes.ownedEyesOf(player)) {
            if (eye.temporary()) {
                eye.setLife(duration);
            }
        }
    }

    private static final int MAX_AMPLIFIER = 9;

    public static boolean active(Player player) {
        if (player == null) {
            return false;
        }
        Holder<MobEffect> holder = effect();
        return holder != null && player.hasEffect(holder);
    }

    public static int remaining(Player player) {
        if (player == null) {
            return 0;
        }
        Holder<MobEffect> holder = effect();
        if (holder == null) {
            return 0;
        }
        MobEffectInstance instance = player.getEffect(holder);
        return instance == null ? 0 : instance.getDuration();
    }

    private static Holder<MobEffect> effect() {
        return EffectLookup.of(CombatStylesContent.id(ID).toString());
    }
}
