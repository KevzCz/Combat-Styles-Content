package net.pixeldreamstudios.combat_styles_content.fabric.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;

import net.pixeldreamstudios.combat_styles_content.client.BeholderEyeRenderer;
import net.pixeldreamstudios.combat_styles_content.client.BoneShardRenderer;
import net.pixeldreamstudios.combat_styles_content.client.KamikazeSkullRenderer;
import net.pixeldreamstudios.combat_styles_content.client.ShurikenRenderer;
import net.pixeldreamstudios.combat_styles_content.client.WraithRenderer;
import net.pixeldreamstudios.combat_styles_content.entity.ContentEntities;

public class CombatStylesContentFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ContentEntities.WRAITH, WraithRenderer::new);
        EntityRendererRegistry.register(ContentEntities.BEHOLDER_EYE, BeholderEyeRenderer::new);
        EntityRendererRegistry.register(ContentEntities.GREAT_EYE, BeholderEyeRenderer::new);
        EntityRendererRegistry.register(ContentEntities.SHURIKEN, ShurikenRenderer::new);
        EntityRendererRegistry.register(ContentEntities.SHURIKEN_SHARD, ShurikenRenderer::new);
        EntityRendererRegistry.register(ContentEntities.BONE_SHARD, BoneShardRenderer::new);
        EntityRendererRegistry.register(ContentEntities.KAMIKAZE_SKULL,
                KamikazeSkullRenderer::new);
    }
}
