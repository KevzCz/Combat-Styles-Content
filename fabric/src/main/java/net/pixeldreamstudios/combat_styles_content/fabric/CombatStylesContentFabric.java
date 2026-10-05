package net.pixeldreamstudios.combat_styles_content.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public class CombatStylesContentFabric implements ModInitializer {

    private static final String SPELL_PACK = "spell_combat_styles";

    @Override
    public void onInitialize() {
        CombatStylesContent.init();

        FabricLoader.getInstance().getModContainer(CombatStylesContent.MOD_ID)
                .ifPresent(container -> {
                    if (SpellPowerAccess.isLoaded()) {
                        ResourceManagerHelper.registerBuiltinResourcePack(
                                ResourceLocation.fromNamespaceAndPath(
                                        CombatStylesContent.MOD_ID, SPELL_PACK),
                                container,
                                ResourcePackActivationType.ALWAYS_ENABLED);
                    }
                });
    }
}
