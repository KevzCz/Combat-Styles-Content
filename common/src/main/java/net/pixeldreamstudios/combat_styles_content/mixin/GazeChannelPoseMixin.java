package net.pixeldreamstudios.combat_styles_content.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.pixeldreamstudios.combat_styles_content.client.GazeChannelPose;

@Mixin(ItemInHandRenderer.class)
public abstract class GazeChannelPoseMixin {

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            require = 1)
    private void combat_styles$floatChannelledBlade(LivingEntity holder, ItemStack stack,
                                                    ItemDisplayContext transform,
                                                    boolean left, PoseStack poses,
                                                    MultiBufferSource buffers, int light,
                                                    CallbackInfo callback) {
        if (!(holder instanceof Player player) || !GazeChannelPose.channeling(player, stack)) {
            return;
        }
        GazeChannelPose.applyHand(poses, player, left);
        GazeChannelPose.applyItem(poses, player, left);
    }
}
