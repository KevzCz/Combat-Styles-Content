package net.pixeldreamstudios.combat_styles_content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import net.pixeldreamstudios.combat_styles_content.entity.BeholderEyeEntity;
import net.pixeldreamstudios.combat_styles_content.entity.GreatEyeEntity;

public class BeholderEyeRenderer extends GeoEntityRenderer<BeholderEyeEntity> {

    private static final float SCALE = BeholderEyeEntity.RENDER_SCALE;

    private static float scaleOf(BeholderEyeEntity entity) {
        return entity instanceof GreatEyeEntity ? SCALE * GreatEyeEntity.SCALE : SCALE;
    }
    private static final float HALF_TURN = 180.0F;
    private static final int FULL_BRIGHT = 15728880;
    private static final int COLOUR_MASK = 0x00FFFFFF;
    private static final int ALPHA_SHIFT = 24;
    private static final float MAX_ALPHA = 255.0F;

    public BeholderEyeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeholderEyeModel());
        this.shadowRadius = 0.0F;
    }

    private static final float BEAM_WIDTH = 0.09F;
    private static final float BEAM_R = 0.42F;
    private static final float BEAM_G = 0.58F;
    private static final float BEAM_B = 0.95F;
    private static final float BEAM_ALPHA = 0.85F;
    private static final String ROOT_BONE = "bone";

    private Vector3d bonePosition;

    @Override
    public void render(BeholderEyeEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        super.render(entity, yaw, partialTick, poses, buffers, packedLight);

        Vec3 end = entity.beamEnd();
        if (end == null) {
            return;
        }
        Vec3 muzzle = muzzleFrom(entity);
        poses.pushPose();
        poses.translate(-entity.getX(), -entity.getY(), -entity.getZ());
        EyeBeamRenderer.draw(poses, buffers, muzzle, end,
                BEAM_WIDTH * (entity.renderScale() / SCALE),
                BEAM_R, BEAM_G, BEAM_B, BEAM_ALPHA, entity.tickCount + partialTick);
        poses.popPose();
    }

    private Vec3 muzzleFrom(BeholderEyeEntity entity) {
        if (bonePosition == null) {
            return entity.muzzle();
        }
        return new Vec3(bonePosition.x, bonePosition.y, bonePosition.z)
                .add(0.0D, entity.beamLift(), 0.0D)
                .add(entity.getLookAngle().scale(entity.muzzleReach()));
    }

    @Override
    public void preRender(PoseStack poses, BeholderEyeEntity animatable, BakedGeoModel model,
                          MultiBufferSource buffers, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        poses.scale(scaleOf(animatable), scaleOf(animatable), scaleOf(animatable));
        super.preRender(poses, animatable, model, buffers, buffer, isReRender, partialTick,
                FULL_BRIGHT, packedOverlay, fadeColour(animatable, partialTick, colour));
    }

    @Override
    public void postRender(PoseStack poses, BeholderEyeEntity animatable, BakedGeoModel model,
                           MultiBufferSource buffers, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay, int colour) {
        super.postRender(poses, animatable, model, buffers, buffer, isReRender, partialTick,
                packedLight, packedOverlay, colour);
        model.getBone(ROOT_BONE).ifPresent(bone -> bonePosition = bone.getWorldPosition());
    }

    @Override
    protected void applyRotations(BeholderEyeEntity animatable, PoseStack poses,
                                  float ageInTicks, float rotationYaw, float partialTick,
                                  float nativeScale) {
        poses.mulPose(Axis.YP.rotationDegrees(HALF_TURN - animatable.getYRot()));
        poses.mulPose(Axis.XP.rotationDegrees(-animatable.getXRot()));
    }

    private int fadeColour(BeholderEyeEntity entity, float partialTick, int colour) {
        int fade = entity.fadeTicks();
        if (fade <= 0) {
            return colour;
        }
        float alpha = Mth.clamp((fade - partialTick) / BeholderEyeEntity.FADE_TICKS, 0.0F, 1.0F);
        int packed = (int) (alpha * MAX_ALPHA) << ALPHA_SHIFT;
        return (colour & COLOUR_MASK) | packed;
    }
}
