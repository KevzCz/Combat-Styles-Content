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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

import net.pixeldreamstudios.combat_styles_content.entity.BoneShardEntity;

public class BoneShardRenderer extends EntityRenderer<BoneShardEntity> {

    private static final float SCALE = 0.6F;
    private static final float CENTRE = 0.1F;
    private static final float AXIS_SPREAD = 2.0F;
    private static final float AXIS_OFFSET = 1.0F;

    private final ItemRenderer items;

    public BoneShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override
    public void render(BoneShardEntity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.translate(0.0F, CENTRE, 0.0F);
        poses.mulPose(Axis.of(tumbleAxis(entity.getId())).rotationDegrees(entity.spin(partialTick)));
        poses.scale(SCALE, SCALE, SCALE);
        items.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                poses, buffers, entity.level(), entity.getId());
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    private static Vector3f tumbleAxis(int seed) {
        RandomSource random = RandomSource.create(seed);
        Vector3f axis = new Vector3f(random.nextFloat() * AXIS_SPREAD - AXIS_OFFSET,
                random.nextFloat() * AXIS_SPREAD - AXIS_OFFSET,
                random.nextFloat() * AXIS_SPREAD - AXIS_OFFSET);
        return axis.lengthSquared() < Float.MIN_NORMAL ? new Vector3f(0.0F, 0.0F, 1.0F) : axis.normalize();
    }

    @Override
    public ResourceLocation getTextureLocation(BoneShardEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
