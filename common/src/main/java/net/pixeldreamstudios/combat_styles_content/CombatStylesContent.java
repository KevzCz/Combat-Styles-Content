package net.pixeldreamstudios.combat_styles_content;

import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pixeldreamstudios.combat_styles.custom.CustomRegistry;
import net.pixeldreamstudios.combat_styles_content.config.ContentConfig;
import net.pixeldreamstudios.combat_styles_content.effect.ModEffects;
import net.pixeldreamstudios.combat_styles_content.entity.BeholderEyes;
import net.pixeldreamstudios.combat_styles_content.entity.ContentEntities;
import net.pixeldreamstudios.combat_styles_content.entity.EyeAbilities;
import net.pixeldreamstudios.combat_styles_content.entity.GazeRitual;
import net.pixeldreamstudios.combat_styles_content.entity.SkullAbilities;
import net.pixeldreamstudios.combat_styles_content.entity.SkullFlock;
import net.pixeldreamstudios.combat_styles_content.item.ContentItems;

import net.pixeldreamstudios.combat_styles.entity.SummonHooks;
import net.pixeldreamstudios.combat_styles.equipment.AccessoryCompat;
import net.pixeldreamstudios.combat_styles_content.entity.ShurikenHook;
import net.pixeldreamstudios.combat_styles_content.entity.VoidWellHook;
import net.pixeldreamstudios.combat_styles_content.entity.OrbitHook;
import net.pixeldreamstudios.combat_styles_content.entity.SolarBrandHook;
import net.pixeldreamstudios.combat_styles_content.entity.BurstHook;
import net.pixeldreamstudios.combat_styles_content.entity.BoneShatterHook;
import net.pixeldreamstudios.combat_styles_content.entity.CatchRefundHook;
import net.pixeldreamstudios.combat_styles_content.entity.ReturningHook;
import net.pixeldreamstudios.combat_styles_content.entity.SummonQuickenHook;
import net.pixeldreamstudios.combat_styles_content.entity.ShadowStepHook;
import net.pixeldreamstudios.combat_styles_content.entity.BackstabHook;
import net.pixeldreamstudios.combat_styles_content.entity.FocusHook;
import net.pixeldreamstudios.combat_styles_content.entity.SplinterHook;
import net.pixeldreamstudios.combat_styles_content.entity.TwinShotHook;
import net.pixeldreamstudios.combat_styles_content.entity.AfflictedHook;
import net.pixeldreamstudios.combat_styles_content.entity.GazeRitualHook;

public final class CombatStylesContent {

    public static final String MOD_ID = "combat_styles_content";
    public static final String SPELL_PACK = "spell_combat_styles";
    public static final String PARENT_ID = "combat_styles";
    public static final Logger LOGGER = LoggerFactory.getLogger("Combat Styles Content");

    private static final String SHURIKEN_HOOK = ":shuriken";
    private static final String VOID_WELL_HOOK = ":void_well";
    private static final String ORBIT_HOOK = ":orbit";
    private static final String SOLAR_BRAND_HOOK = ":solar_brand";
    private static final String BURST_HOOK = ":burst";
    private static final String RETURNING_HOOK = ":returning";
    private static final String BONE_SHATTER_HOOK = ":bone_shatter";
    private static final String SUMMON_QUICKEN_HOOK = ":summon_quicken";
    private static final String SHADOW_STEP_HOOK = ":shadow_step";
    private static final String BACKSTAB_HOOK = ":backstab";
    private static final String FOCUS_HOOK = ":focus";
    private static final String SPLINTER_HOOK = ":splinter";
    private static final String TWIN_SHOT_HOOK = ":twin_shot";
    private static final String AFFLICTED_HOOK = ":afflicted";
    private static final String GAZE_RITUAL_HOOK = ":gaze_ritual";

    private CombatStylesContent() {
    }

    public static void init() {
        ContentConfig.register();
        EyeAbilities.registerDefaults();
        SkullAbilities.registerDefaults();
        ContentItems.register();
        ContentEntities.register();
        CustomRegistry.register(MOD_ID + SHURIKEN_HOOK, new ShurikenHook());
        CustomRegistry.register(MOD_ID + VOID_WELL_HOOK, new VoidWellHook());
        CustomRegistry.register(MOD_ID + ORBIT_HOOK, new OrbitHook());
        CustomRegistry.register(MOD_ID + SOLAR_BRAND_HOOK, new SolarBrandHook());
        CustomRegistry.register(MOD_ID + BURST_HOOK, new BurstHook());
        CustomRegistry.register(MOD_ID + RETURNING_HOOK, new ReturningHook());
        CustomRegistry.register(MOD_ID + SUMMON_QUICKEN_HOOK, new SummonQuickenHook());
        CustomRegistry.register(MOD_ID + SHADOW_STEP_HOOK, new ShadowStepHook());
        CustomRegistry.register(MOD_ID + BACKSTAB_HOOK, new BackstabHook());
        CustomRegistry.register(MOD_ID + FOCUS_HOOK, new FocusHook());
        CustomRegistry.register(MOD_ID + SPLINTER_HOOK, new SplinterHook());
        CustomRegistry.register(MOD_ID + TWIN_SHOT_HOOK, new TwinShotHook());
        CustomRegistry.register(MOD_ID + AFFLICTED_HOOK, new AfflictedHook());
        CustomRegistry.register(MOD_ID + GAZE_RITUAL_HOOK, new GazeRitualHook());
        CustomRegistry.register(CatchRefundHook.ID, new CatchRefundHook());
        CustomRegistry.register(MOD_ID + BONE_SHATTER_HOOK, new BoneShatterHook());
        ModEffects.register();
        registerSummonHooks();
        registerAccessories();
        if (!Platform.isModLoaded(PARENT_ID)) {
            LOGGER.error("Combat Styles is not installed; content will not load");
        }
    }

    private static void registerSummonHooks() {
        SummonHooks.onForget(BeholderEyes::forget);
        SummonHooks.onForget(GazeRitual::forget);
        SummonHooks.onDive(SkullFlock::diveAll);
    }

    private static void registerAccessories() {
        if (!AccessoryCompat.isLoaded()) {
            return;
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
