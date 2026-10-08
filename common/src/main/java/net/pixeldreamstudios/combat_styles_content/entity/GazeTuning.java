package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.damage.DamageSpec;

import java.util.List;

public record GazeTuning(int cooldownTicks, int minCrown, int gatherIntervalTicks, int settleTicks,
                         float radius, float radiusPerEye, float strength, float strengthPerEye,
                         float lift, DamageSpec damage, float damagePerEye, int gazedPerEye) {

    public static final String KEY_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String KEY_MIN_CROWN = "min_crown";
    public static final String KEY_GATHER_INTERVAL_TICKS = "gather_interval_ticks";
    public static final String KEY_SETTLE_TICKS = "settle_ticks";
    public static final String KEY_RADIUS = "shockwave_radius";
    public static final String KEY_RADIUS_PER_EYE = "shockwave_radius_per_eye";
    public static final String KEY_STRENGTH = "shockwave_strength";
    public static final String KEY_STRENGTH_PER_EYE = "shockwave_strength_per_eye";
    public static final String KEY_LIFT = "shockwave_lift";
    public static final String KEY_DAMAGE_PER_EYE = "shockwave_damage_per_eye";
    public static final String KEY_GAZED_PER_EYE = "gazed_per_eye";

    private static final int DEFAULT_COOLDOWN_TICKS = 400;
    private static final int DEFAULT_MIN_CROWN = 2;
    private static final int DEFAULT_GATHER_INTERVAL_TICKS = 10;
    private static final int DEFAULT_SETTLE_TICKS = 20;
    private static final float DEFAULT_RADIUS = 3.0F;
    private static final float DEFAULT_RADIUS_PER_EYE = 0.75F;
    private static final float DEFAULT_STRENGTH = 0.4F;
    private static final float DEFAULT_STRENGTH_PER_EYE = 0.12F;
    private static final float DEFAULT_LIFT = 0.25F;
    private static final float DEFAULT_DAMAGE = 3.0F;
    private static final float DEFAULT_DAMAGE_PER_EYE = 0.5F;
    private static final int DEFAULT_GAZED_PER_EYE = 1;
    private static final int MIN_INTERVAL = 1;

    public static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_COOLDOWN_TICKS, Codec.INT, DEFAULT_COOLDOWN_TICKS),
            OptionField.of(KEY_MIN_CROWN, Codec.INT, DEFAULT_MIN_CROWN),
            OptionField.of(KEY_GATHER_INTERVAL_TICKS, Codec.INT, DEFAULT_GATHER_INTERVAL_TICKS),
            OptionField.of(KEY_SETTLE_TICKS, Codec.INT, DEFAULT_SETTLE_TICKS),
            OptionField.of(KEY_RADIUS, Codec.FLOAT, DEFAULT_RADIUS),
            OptionField.of(KEY_RADIUS_PER_EYE, Codec.FLOAT, DEFAULT_RADIUS_PER_EYE),
            OptionField.of(KEY_STRENGTH, Codec.FLOAT, DEFAULT_STRENGTH),
            OptionField.of(KEY_STRENGTH_PER_EYE, Codec.FLOAT, DEFAULT_STRENGTH_PER_EYE),
            OptionField.of(KEY_LIFT, Codec.FLOAT, DEFAULT_LIFT),
            OptionField.of(HookOptions.KEY_DAMAGE_SPEC, DamageSpec.CODEC, DamageSpec.ofFlat(DEFAULT_DAMAGE),
                    DamageSpec.SCHEMA),
            OptionField.of(KEY_DAMAGE_PER_EYE, Codec.FLOAT, DEFAULT_DAMAGE_PER_EYE),
            OptionField.of(KEY_GAZED_PER_EYE, Codec.INT, DEFAULT_GAZED_PER_EYE));

    public static GazeTuning of(JsonObject options) {
        DamageSpec damage = HookOptions.specOf(options);
        return new GazeTuning(
                HookOptions.intOf(options, KEY_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS),
                HookOptions.intOf(options, KEY_MIN_CROWN, DEFAULT_MIN_CROWN),
                Math.max(MIN_INTERVAL, HookOptions.intOf(options, KEY_GATHER_INTERVAL_TICKS,
                        DEFAULT_GATHER_INTERVAL_TICKS)),
                Math.max(MIN_INTERVAL, HookOptions.intOf(options, KEY_SETTLE_TICKS, DEFAULT_SETTLE_TICKS)),
                HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS),
                HookOptions.floatOf(options, KEY_RADIUS_PER_EYE, DEFAULT_RADIUS_PER_EYE),
                HookOptions.floatOf(options, KEY_STRENGTH, DEFAULT_STRENGTH),
                HookOptions.floatOf(options, KEY_STRENGTH_PER_EYE, DEFAULT_STRENGTH_PER_EYE),
                HookOptions.floatOf(options, KEY_LIFT, DEFAULT_LIFT),
                damage == null ? DamageSpec.ofFlat(DEFAULT_DAMAGE) : damage,
                HookOptions.floatOf(options, KEY_DAMAGE_PER_EYE, DEFAULT_DAMAGE_PER_EYE),
                HookOptions.intOf(options, KEY_GAZED_PER_EYE, DEFAULT_GAZED_PER_EYE));
    }

    public double radiusFor(int crown) {
        return radius + radiusPerEye * Math.max(0, crown - 1);
    }

    public double strengthFor(int crown) {
        return strength + strengthPerEye * Math.max(0, crown - 1);
    }

    public float damageScaleFor(int crown) {
        return 1.0F + damagePerEye * Math.max(0, crown - 1);
    }
}
