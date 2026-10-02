package net.pixeldreamstudios.combat_styles_content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles_content.entity.WraithEntity;

public class WraithRenderer extends EntityRenderer<WraithEntity> {

    private static final ResourceLocation TEXTURE = CombatStylesContent.id("textures/entity/wraith.png");

    private static final int FULL_BRIGHT = 15728880;

    private static final float HEAD_HALF = 0.17F;
    private static final float HEAD_DEPTH = 0.15F;
    private static final float SHOULDER_HALF = 0.24F;
    private static final float SHOULDER_DEPTH = 0.19F;
    private static final float SHOULDER_Y = -0.05F;
    private static final float TAIL_Y = -0.62F;
    private static final float TAIL_HALF = 0.05F;
    private static final int TAIL_SEGMENTS = 5;
    private static final float TAIL_WAVE = 0.10F;
    private static final float TAIL_WAVE_SPEED = 0.22F;

    private static final float WING_LENGTH = 0.46F;
    private static final float WING_HEIGHT = 0.26F;
    private static final float WING_ROOT_Z = 0.02F;
    private static final float WING_TIP_Z = -0.18F;
    private static final float WING_ANCHOR = 0.12F;

    private static final float BEAT_SPEED = 0.9F;
    private static final float BEAT_SWEEP = 52.0F;
    private static final float BEAT_REST = 14.0F;
    private static final float BOB_SPEED = 0.18F;
    private static final float BOB_AMOUNT = 0.045F;
    private static final float LEAN_MAX = 28.0F;

    private static final float TINT_R = 0.42F;
    private static final float TINT_G = 0.86F;
    private static final float TINT_B = 0.92F;
    private static final float WING_ALPHA = 0.55F;
    private static final float TAIL_ALPHA = 0.42F;
    private static final float CORE_LIFT = 1.12F;

    private static final float U_BODY_MIN = 0.0F;
    private static final float U_BODY_MAX = 0.5F;
    private static final float U_WING_MIN = 0.5F;
    private static final float U_WING_MAX = 1.0F;
    private static final float V_MIN = 0.0F;
    private static final float V_MID = 0.5F;

    public WraithRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(WraithEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(WraithEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float alpha = fadeAlpha(entity, partialTick);
        if (alpha <= 0.0F) {
            return;
        }

        poses.pushPose();
        poses.translate(0.0D, Mth.sin(age * BOB_SPEED) * BOB_AMOUNT, 0.0D);
        poses.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poses.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        float speed = (float) entity.getDeltaMovement().length();
        float lean = Mth.clamp(speed * 40.0F, 0.0F, LEAN_MAX);
        poses.mulPose(Axis.XP.rotationDegrees(lean));

        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));

        body(poses, buffer, alpha);
        tail(poses, buffer, alpha, age);

        float beatRate = BEAT_SPEED + speed * 2.4F;
        float flap = Mth.sin(age * beatRate) * BEAT_SWEEP + BEAT_REST;
        wing(poses, buffer, alpha, flap, 1.0F);
        wing(poses, buffer, alpha, flap, -1.0F);

        poses.popPose();
    }

    private float fadeAlpha(WraithEntity entity, float partialTick) {
        int fade = entity.fadeTicks();
        if (fade <= 0) {
            return 1.0F;
        }
        return Mth.clamp((fade - partialTick) / WraithEntity.FADE_TICKS, 0.0F, 1.0F);
    }

    private void body(PoseStack poses, VertexConsumer buffer, float alpha) {
        Matrix4f pose = poses.last().pose();
        float hx = HEAD_HALF;
        float hz = HEAD_DEPTH;
        float sx = SHOULDER_HALF;
        float sz = SHOULDER_DEPTH;
        float top = HEAD_HALF + 0.10F;
        float bottom = SHOULDER_Y;

        float[][] head = {
                {-hx, top, -hz}, {hx, top, -hz}, {hx, top, hz}, {-hx, top, hz}
        };
        float[][] shoulder = {
                {-sx, bottom, -sz}, {sx, bottom, -sz}, {sx, bottom, sz}, {-sx, bottom, sz}
        };

        for (int i = 0; i < 4; i++) {
            int n = (i + 1) % 4;
            quad(buffer, pose,
                    head[i], head[n], shoulder[n], shoulder[i],
                    U_BODY_MIN, U_BODY_MAX, V_MIN, V_MID, alpha, CORE_LIFT);
        }

        quad(buffer, pose, head[3], head[2], head[1], head[0],
                U_BODY_MIN, U_BODY_MAX, V_MIN, V_MID, alpha, CORE_LIFT);
    }

    private void tail(PoseStack poses, VertexConsumer buffer, float alpha, float age) {
        Matrix4f pose = poses.last().pose();
        float topY = SHOULDER_Y;
        float step = (TAIL_Y - SHOULDER_Y) / TAIL_SEGMENTS;

        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            float t0 = i / (float) TAIL_SEGMENTS;
            float t1 = (i + 1) / (float) TAIL_SEGMENTS;
            float y0 = topY + step * i;
            float y1 = topY + step * (i + 1);
            float w0 = Mth.lerp(t0, SHOULDER_HALF * 0.7F, TAIL_HALF);
            float w1 = Mth.lerp(t1, SHOULDER_HALF * 0.7F, TAIL_HALF);
            float x0 = Mth.sin(age * TAIL_WAVE_SPEED - t0 * 3.0F) * TAIL_WAVE * t0;
            float x1 = Mth.sin(age * TAIL_WAVE_SPEED - t1 * 3.0F) * TAIL_WAVE * t1;
            float a0 = alpha * Mth.lerp(t0, TAIL_ALPHA, 0.0F);
            float a1 = alpha * Mth.lerp(t1, TAIL_ALPHA, 0.0F);

            band(buffer, pose, x0, y0, w0, a0, x1, y1, w1, a1, false);
            band(buffer, pose, x0, y0, w0, a0, x1, y1, w1, a1, true);
        }
    }

    private void band(VertexConsumer buffer, Matrix4f pose,
                      float x0, float y0, float w0, float a0,
                      float x1, float y1, float w1, float a1, boolean crossed) {
        float z0 = crossed ? w0 : 0.0F;
        float z1 = crossed ? w1 : 0.0F;
        float ox0 = crossed ? 0.0F : w0;
        float ox1 = crossed ? 0.0F : w1;

        vertex(buffer, pose, x0 - ox0, y0, -z0, U_BODY_MIN, V_MID, a0);
        vertex(buffer, pose, x1 - ox1, y1, -z1, U_BODY_MIN, V_MIN, a1);
        vertex(buffer, pose, x1 + ox1, y1, z1, U_BODY_MAX, V_MIN, a1);
        vertex(buffer, pose, x0 + ox0, y0, z0, U_BODY_MAX, V_MID, a0);
    }

    private void wing(PoseStack poses, VertexConsumer buffer,
                      float alpha, float flap, float side) {
        poses.pushPose();
        poses.translate(0.0D, WING_ANCHOR, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(flap * side));
        Matrix4f pose = poses.last().pose();

        float near = side > 0 ? 0.0F : -WING_LENGTH;
        float far = side > 0 ? WING_LENGTH : 0.0F;
        float nearZ = side > 0 ? WING_ROOT_Z : WING_TIP_Z;
        float farZ = side > 0 ? WING_TIP_Z : WING_ROOT_Z;
        float wingAlpha = alpha * WING_ALPHA;

        float[] a = {near, -WING_HEIGHT, nearZ};
        float[] b = {near, WING_HEIGHT, nearZ};
        float[] c = {far, WING_HEIGHT, farZ};
        float[] d = {far, -WING_HEIGHT, farZ};

        quad(buffer, pose, a, b, c, d, U_WING_MIN, U_WING_MAX, V_MIN, V_MID, wingAlpha, 1.0F);
        quad(buffer, pose, d, c, b, a, U_WING_MIN, U_WING_MAX, V_MIN, V_MID, wingAlpha, 1.0F);

        poses.popPose();
    }

    private void quad(VertexConsumer buffer, Matrix4f pose,
                      float[] a, float[] b, float[] c, float[] d,
                      float uMin, float uMax, float vMin, float vMax,
                      float alpha, float lift) {
        vertex(buffer, pose, a[0], a[1], a[2], uMin, vMax, alpha, lift);
        vertex(buffer, pose, b[0], b[1], b[2], uMin, vMin, alpha, lift);
        vertex(buffer, pose, c[0], c[1], c[2], uMax, vMin, alpha, lift);
        vertex(buffer, pose, d[0], d[1], d[2], uMax, vMax, alpha, lift);
    }

    private void vertex(VertexConsumer buffer, Matrix4f pose, float x, float y, float z,
                        float u, float v, float alpha) {
        vertex(buffer, pose, x, y, z, u, v, alpha, 1.0F);
    }

    private void vertex(VertexConsumer buffer, Matrix4f pose, float x, float y, float z,
                        float u, float v, float alpha, float lift) {
        buffer.addVertex(pose, x, y, z)
                .setColor(Math.min(1.0F, TINT_R * lift), Math.min(1.0F, TINT_G * lift),
                        Math.min(1.0F, TINT_B * lift), alpha)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}
