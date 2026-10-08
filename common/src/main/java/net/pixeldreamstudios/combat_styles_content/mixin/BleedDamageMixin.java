package net.pixeldreamstudios.combat_styles_content.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.pixeldreamstudios.combat_styles_content.effect.Bleeding;

@Mixin(LivingEntity.class)
public abstract class BleedDamageMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 1)
    private float combat_styles_content$amplifyBleeding(float amount, DamageSource source) {
        return Bleeding.amplify((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"), require = 1)
    private void combat_styles_content$burstBleeding(DamageSource source, float amount, CallbackInfo callback) {
        Bleeding.afterHit((LivingEntity) (Object) this, source, amount);
    }
}
