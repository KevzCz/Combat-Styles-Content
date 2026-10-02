package net.pixeldreamstudios.combat_styles_content.client;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles_content.entity.BeholderEyeEntity;

public class BeholderEyeModel extends GeoModel<BeholderEyeEntity> {

    private static final ResourceLocation MODEL =
            CombatStylesContent.id("geo/beholder_eye.geo.json");
    private static final ResourceLocation TEXTURE =
            CombatStylesContent.id("textures/entity/beholder_eye.png");
    private static final ResourceLocation ANIMATION =
            CombatStylesContent.id("animations/beholder_eye.animation.json");

    @Override
    public ResourceLocation getModelResource(BeholderEyeEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BeholderEyeEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BeholderEyeEntity animatable) {
        return ANIMATION;
    }
}
