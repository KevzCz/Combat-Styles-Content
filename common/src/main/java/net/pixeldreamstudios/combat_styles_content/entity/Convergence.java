package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles.config.StatusEffectConfig;
import net.pixeldreamstudios.combat_styles.effect.EffectLookup;

public final class Convergence {

    public static final String ID = "gazed";

    public static int durationTicks() {
        return StatusEffectConfig.get(ID).durationTicks;
    }

    public static int maxStacks() {
        return StatusEffectConfig.get(ID).maxStacks;
    }

    public static float perStack() {
        return StatusEffectConfig.get(ID).perStack;
    }

    private static boolean enabled() {
        return StatusEffectConfig.get(ID).enabled;
    }

    private Convergence() {
    }

    public static double maxMultiplier() {
        return 1.0D + maxStacks() * perStack();
    }

    public static float amplify(Player owner, LivingEntity victim) {
        if (owner == null || victim == null) {
            return 1.0F;
        }
        return 1.0F + stacksOn(victim) * perStack();
    }

    public static void apply(Player owner, LivingEntity victim) {
        apply(owner, victim, null, 1);
    }

    public static void apply(Player owner, LivingEntity victim, String effectId, int stacks) {
        Holder<MobEffect> holder = effectId == null || effectId.isBlank()
                ? effect()
                : EffectLookup.of(effectId);
        if (owner == null || victim == null || victim.level().isClientSide()
                || holder == null || !enabled()) {
            return;
        }
        MobEffectInstance present = victim.getEffect(holder);
        int added = Math.max(1, stacks);
        int amplifier = present == null
                ? added - 1
                : Math.min(maxStacks() - 1, present.getAmplifier() + added);
        int duration = present == null
                ? durationTicks()
                : present.getDuration() + EXTENSION_TICKS;
        boolean ambient = present != null && present.isAmbient();
        boolean visible = present == null || present.isVisible();
        boolean icon = present == null || present.showIcon();
        victim.removeEffect(holder);
        victim.addEffect(new MobEffectInstance(holder, duration, amplifier,
                ambient, visible, icon));
    }

    public static int extensionTicks() {
        return EXTENSION_TICKS;
    }

    public static int highestNear(Player owner) {
        if (owner == null) {
            return 0;
        }
        int best = 0;
        for (LivingEntity nearby : owner.level().getEntitiesOfClass(LivingEntity.class,
                owner.getBoundingBox().inflate(SIGHT_RADIUS))) {
            if (nearby == owner) {
                continue;
            }
            best = Math.max(best, stacksOn(nearby));
        }
        return best;
    }

    private static final double SIGHT_RADIUS = 16.0D;

    public static float detonate(Player owner, LivingEntity victim) {
        int stacks = stacksOn(victim);
        if (stacks <= 0) {
            return 0.0F;
        }
        Holder<MobEffect> holder = effect();
        if (holder != null) {
            victim.removeEffect(holder);
        }
        return stacks * perStack();
    }

    public static int stacksOn(LivingEntity victim) {
        Holder<MobEffect> holder = effect();
        if (holder == null) {
            return 0;
        }
        MobEffectInstance instance = victim.getEffect(holder);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    public static Holder<MobEffect> effect() {
        return BuiltInRegistries.MOB_EFFECT
                .getHolder(ResourceKey.create(Registries.MOB_EFFECT,
                        CombatStylesContent.id(ID)))
                .orElse(null);
    }

    private static final int EXTENSION_TICKS = 1;
}
