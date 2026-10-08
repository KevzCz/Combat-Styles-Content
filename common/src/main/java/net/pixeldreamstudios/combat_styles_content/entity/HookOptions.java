package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.pixeldreamstudios.combat_styles.damage.DamageSpec;
import net.pixeldreamstudios.combat_styles.damage.ScaledNumbers;

public final class HookOptions {

    public static final String KEY_DAMAGE_SPEC = "damage_spec";
    public static final double TICKS_PER_SECOND = 20.0D;

    private HookOptions() {
    }

    public static float floatOf(JsonObject options, String key, float fallback) {
        return options != null && options.has(key)
                ? ScaledNumbers.resolve(options.get(key), fallback)
                : fallback;
    }

    public static int intOf(JsonObject options, String key, int fallback) {
        return options != null && options.has(key)
                ? Math.round(ScaledNumbers.resolve(options.get(key), fallback))
                : fallback;
    }

    public static boolean boolOf(JsonObject options, String key, boolean fallback) {
        return options != null && options.has(key) ? options.get(key).getAsBoolean() : fallback;
    }

    public static String stringOf(JsonObject options, String key, String fallback) {
        return options != null && options.has(key) ? options.get(key).getAsString() : fallback;
    }

    public static DamageSpec specOf(JsonObject options) {
        return specOf(options, KEY_DAMAGE_SPEC);
    }

    public static DamageSpec specOf(JsonObject options, String key) {
        if (options == null || !options.has(key)) {
            return null;
        }
        return DamageSpec.CODEC.parse(JsonOps.INSTANCE, options.get(key)).result().orElse(null);
    }

    public static ParticleOptions particleOf(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.PARTICLE_TYPE.getOptional(id)
                .filter(SimpleParticleType.class::isInstance)
                .map(SimpleParticleType.class::cast)
                .orElse(null);
    }

    public static SoundEvent soundOf(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id == null ? null : BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(null);
    }

    public static double seconds(int ticks) {
        return ticks / TICKS_PER_SECOND;
    }
}
