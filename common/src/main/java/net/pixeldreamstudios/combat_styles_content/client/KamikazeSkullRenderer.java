package net.pixeldreamstudios.combat_styles_content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import net.pixeldreamstudios.combat_styles_content.entity.KamikazeSkullEntity;

public class KamikazeSkullRenderer extends EntityRenderer<KamikazeSkullEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/wither_skeleton.png");

    private static final float SCALE = 0.7F;
    private static final float SPIN_SPEED = 22.0F;
    private static final float BOB_SPEED = 0.2F;
    private static final float BOB_AMOUNT = 0.06F;
    private static final float MODEL_LIFT = 0.45F;

    private final SkullModel model;

    public KamikazeSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL));
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(KamikazeSkullEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(KamikazeSkullEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float alpha = fadeAlpha(entity, partialTick);
        if (alpha <= 0.0F) {
            return;
        }

        poses.pushPose();
        poses.translate(0.0D, MODEL_LIFT + Mth.sin(age * BOB_SPEED) * BOB_AMOUNT, 0.0D);
        poses.scale(-SCALE, -SCALE, SCALE);

        if (entity.diving()) {
            poses.mulPose(Axis.YP.rotationDegrees(age * SPIN_SPEED));
            poses.mulPose(Axis.XP.rotationDegrees(age * SPIN_SPEED * 0.5F));
        } else {
            poses.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        }

        model.setupAnim(0.0F, 0.0F, 0.0F);
        model.renderToBuffer(poses, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY,
                argb(alpha));

        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, packedLight);
    }

    private static int argb(float alpha) {
        int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        return (a << 24) | WHITE;
    }

    private static final int WHITE = 0xFFFFFF;

    private float fadeAlpha(KamikazeSkullEntity entity, float partialTick) {
        int fade = entity.fadeTicks();
        if (fade <= 0) {
            return 1.0F;
        }
        return Mth.clamp((fade - partialTick) / KamikazeSkullEntity.FADE_TICKS, 0.0F, 1.0F);
    }
}
