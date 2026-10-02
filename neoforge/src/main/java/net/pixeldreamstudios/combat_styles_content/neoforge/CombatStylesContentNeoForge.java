package net.pixeldreamstudios.combat_styles_content.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

@Mod(CombatStylesContent.MOD_ID)
public class CombatStylesContentNeoForge {

    public CombatStylesContentNeoForge(IEventBus eventBus, ModContainer container) {
        CombatStylesContent.init();
    }
}
