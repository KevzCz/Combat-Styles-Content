package net.pixeldreamstudios.combat_styles_content;

import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pixeldreamstudios.combat_styles_content.config.ContentConfig;
import net.pixeldreamstudios.combat_styles_content.effect.ModEffects;
import net.pixeldreamstudios.combat_styles_content.entity.BeholderEyes;
import net.pixeldreamstudios.combat_styles_content.entity.ContentEntities;
import net.pixeldreamstudios.combat_styles_content.entity.EyeAbilities;
import net.pixeldreamstudios.combat_styles_content.entity.SkullAbilities;
import net.pixeldreamstudios.combat_styles_content.entity.GreatEyeFlock;
import net.pixeldreamstudios.combat_styles_content.entity.SkullFlock;
import net.pixeldreamstudios.combat_styles_content.item.ContentItems;

import net.pixeldreamstudios.combat_styles.entity.SummonHooks;
import net.pixeldreamstudios.combat_styles.equipment.AccessoryCompat;

public final class CombatStylesContent {

    public static final String MOD_ID = "combat_styles_content";
    public static final String PARENT_ID = "combat_styles";
    public static final Logger LOGGER = LoggerFactory.getLogger("Combat Styles Content");

    private CombatStylesContent() {
    }

    public static void init() {
        ContentConfig.register();
        EyeAbilities.registerDefaults();
        SkullAbilities.registerDefaults();
        ContentItems.register();
        ContentEntities.register();
        ModEffects.register();
        registerSummonHooks();
        registerAccessories();
        if (!Platform.isModLoaded(PARENT_ID)) {
            LOGGER.error("Combat Styles is not installed; content will not load");
        }
    }

    private static void registerSummonHooks() {
        SummonHooks.onForget(BeholderEyes::forget);
        SummonHooks.onForget(SkullFlock::forget);
        SummonHooks.onKeepAlive(SkullFlock::keepAlive);
        SummonHooks.onKeepAlive(GreatEyeFlock::keepAlive);
        SummonHooks.onDive(SkullFlock::diveAll);
    }

    private static void registerAccessories() {
        if (!AccessoryCompat.isLoaded()) {
            return;
        }
        ContentItems.equipment().forEach(item -> item.listen(AccessoryCompat::registerItem));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
