package net.pixeldreamstudios.combat_styles_content.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles.damage.DamageHurt;
import net.pixeldreamstudios.combat_styles.equipment.WornItems;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public final class Bleeding {

    public static final int MAX_AMPLIFIER = 4;
    public static final int WOUNDED_TICKS = 120;
    public static final float PER_STACK = 0.05F;
    public static final float ENHANCED_PER_STACK = 0.07F;
    public static final float WOUNDED_BONUS = 0.25F;
    public static final float BURST = 1.2F;
    public static final float ENHANCED_BURST = 1.5F;
    public static final TagKey<Item> AMPLIFIERS = TagKey.create(Registries.ITEM,
            CombatStylesContent.id("bleed_amplifiers"));
    public static final ResourceKey<DamageType> BLEED_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            CombatStylesContent.id("bleed"));

    private static final ThreadLocal<Boolean> BURSTING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private Bleeding() {
    }

    public static float amplify(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide() || BURSTING.get() || amount <= 0.0F) {
            return amount;
        }
        float bonus = 0.0F;
        MobEffectInstance bleeding = target.getEffect(ModEffects.holder(ModEffects.BLEEDING));
        if (bleeding != null) {
            bonus += (bleeding.getAmplifier() + 1) * perStack(source.getEntity());
        }
        if (target.hasEffect(ModEffects.holder(ModEffects.WOUNDED))) {
            bonus += WOUNDED_BONUS;
        }
        return amount * (1.0F + bonus);
    }

    public static void afterHit(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide() || BURSTING.get() || amount <= 0.0F || !target.isAlive()) {
            return;
        }
        Holder<MobEffect> bleedingEffect = ModEffects.holder(ModEffects.BLEEDING);
        MobEffectInstance bleeding = target.getEffect(bleedingEffect);
        if (bleeding == null || bleeding.getAmplifier() < MAX_AMPLIFIER) {
            return;
        }
        Entity attacker = source.getEntity();
        target.removeEffect(bleedingEffect);
        target.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.WOUNDED), WOUNDED_TICKS));
        Holder<DamageType> type = target.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLEED_DAMAGE);
        BURSTING.set(Boolean.TRUE);
        try {
            DamageHurt.bypassingIFrames(target, new DamageSource(type, attacker),
                    amount * burstFor(attacker));
        } finally {
            BURSTING.set(Boolean.FALSE);
        }
    }

    private static float perStack(Entity attacker) {
        return amplified(attacker) ? ENHANCED_PER_STACK : PER_STACK;
    }

    private static float burstFor(Entity attacker) {
        return amplified(attacker) ? ENHANCED_BURST : BURST;
    }

    private static boolean amplified(Entity attacker) {
        if (!(attacker instanceof Player player)) {
            return false;
        }
        for (ItemStack worn : WornItems.of(player)) {
            if (worn.is(AMPLIFIERS)) {
                return true;
            }
        }
        return false;
    }
}
