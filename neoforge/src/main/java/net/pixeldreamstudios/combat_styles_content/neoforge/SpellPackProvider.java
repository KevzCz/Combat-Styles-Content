package net.pixeldreamstudios.combat_styles_content.neoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = CombatStylesContent.MOD_ID)
public final class SpellPackProvider {

    private static final String PACK_NAME = "spell_combat_styles";
    private static final String PACK_TITLE = "Combat Styles: Spell Power";

    private SpellPackProvider() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        if (!SpellPowerAccess.isLoaded()) {
            return;
        }
        ModList.get().getModContainerById(CombatStylesContent.MOD_ID).ifPresent(container -> {
            Path root = container.getModInfo().getOwningFile().getFile()
                    .findResource("resourcepacks", PACK_NAME);
            PackLocationInfo location = new PackLocationInfo(
                    CombatStylesContent.MOD_ID + ":" + PACK_NAME,
                    Component.literal(PACK_TITLE),
                    PackSource.BUILT_IN,
                    Optional.empty());
            Pack pack = Pack.readMetaAndCreate(location,
                    new PathPackResources.PathResourcesSupplier(root),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(true, Pack.Position.TOP, false));
            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        });
    }
}
