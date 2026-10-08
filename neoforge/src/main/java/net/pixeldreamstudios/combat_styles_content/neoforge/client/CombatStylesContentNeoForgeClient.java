package net.pixeldreamstudios.combat_styles_content.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles_content.client.BeholderEyeRenderer;
import net.pixeldreamstudios.combat_styles_content.client.BoneShardRenderer;
import net.pixeldreamstudios.combat_styles_content.client.KamikazeSkullRenderer;
import net.pixeldreamstudios.combat_styles_content.client.ShurikenRenderer;
import net.pixeldreamstudios.combat_styles_content.client.WraithRenderer;
import net.pixeldreamstudios.combat_styles_content.entity.ContentEntities;

@EventBusSubscriber(modid = CombatStylesContent.MOD_ID, value = Dist.CLIENT)
public final class CombatStylesContentNeoForgeClient {

    private CombatStylesContentNeoForgeClient() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ContentEntities.WRAITH.get(), WraithRenderer::new);
        event.registerEntityRenderer(ContentEntities.BEHOLDER_EYE.get(),
                BeholderEyeRenderer::new);
        event.registerEntityRenderer(ContentEntities.GREAT_EYE.get(),
                BeholderEyeRenderer::new);
        event.registerEntityRenderer(ContentEntities.SHURIKEN.get(), ShurikenRenderer::new);
        event.registerEntityRenderer(ContentEntities.SHURIKEN_SHARD.get(), ShurikenRenderer::new);
        event.registerEntityRenderer(ContentEntities.BONE_SHARD.get(), BoneShardRenderer::new);
        event.registerEntityRenderer(ContentEntities.KAMIKAZE_SKULL.get(),
                KamikazeSkullRenderer::new);
    }
}
