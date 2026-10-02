package net.pixeldreamstudios.combat_styles_content.effect;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(CombatStylesContent.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> GAZED =
            EFFECTS.register("gazed", GazedEffect::new);

    public static final RegistrySupplier<MobEffect> GAZING =
            EFFECTS.register("gazing", GazingEffect::new);

    private ModEffects() {
    }

    public static void register() {
        EFFECTS.register();
    }
}
