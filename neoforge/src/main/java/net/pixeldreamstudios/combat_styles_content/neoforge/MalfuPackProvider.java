package net.pixeldreamstudios.combat_styles_content.neoforge;

import net.minecraft.ChatFormatting;
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

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = CombatStylesContent.MOD_ID)
public final class MalfuPackProvider {

    private static final String PACK_NAME = "malfu_combat_styles";
    private static final String PACK_TITLE = "Combat Styles: Malfu Animations";
    private static final String KEY_NAME_AND_SOURCE = "pack.nameAndSource";
    private static final String KEY_BUILT_IN = "pack.source.builtin";
    private static final PackSource OPT_IN = PackSource.create(
            name -> Component.translatable(KEY_NAME_AND_SOURCE, name,
                    Component.translatable(KEY_BUILT_IN)).withStyle(ChatFormatting.GRAY),
            false);

    private MalfuPackProvider() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        ModList.get().getModContainerById(CombatStylesContent.MOD_ID).ifPresent(container -> {
            Path root = container.getModInfo().getOwningFile().getFile()
                    .findResource("resourcepacks", PACK_NAME);
            PackLocationInfo location = new PackLocationInfo(
                    CombatStylesContent.MOD_ID + ":" + PACK_NAME,
                    Component.literal(PACK_TITLE),
                    OPT_IN,
                    Optional.empty());
            Pack pack = Pack.readMetaAndCreate(location,
                    new PathPackResources.PathResourcesSupplier(root),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(false, Pack.Position.TOP, false));
            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        });
    }
}
