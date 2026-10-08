package net.pixeldreamstudios.combat_styles_content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

import net.pixeldreamstudios.combat_styles_content.entity.ShurikenEntity;

public class ShurikenRenderer extends EntityRenderer<ShurikenEntity> {

    private static final float SCALE = 1.0F;
    private static final float LIFT = 0.1F;
    private static final float LIE_FLAT = 90.0F;
    private static final int FIRST_TICKS_HIDDEN = 2;
    private static final double CLOSE_DISTANCE_SQR = 12.25D;

    private final ItemRenderer items;

    public ShurikenRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override
    public void render(ShurikenEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        if (entity.tickCount < FIRST_TICKS_HIDDEN
                && entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < CLOSE_DISTANCE_SQR) {
            return;
        }
        float size = SCALE * entity.size();
        poses.pushPose();
        poses.translate(0.0F, LIFT, 0.0F);
        poses.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot())));
        poses.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poses.mulPose(Axis.ZP.rotationDegrees(entity.tilt()));
        poses.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * entity.spin()));
        poses.mulPose(Axis.XP.rotationDegrees(LIE_FLAT));
        poses.scale(size, size, size);
        items.renderStatic(entity.getItem(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                poses, buffers, entity.level(), entity.getId());
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ShurikenEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
