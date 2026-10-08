package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.affix.AffixText;
import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.damage.DamageSpec;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SolarBrandHook implements CustomHook {

    public static final String KEY_DURATION_TICKS = "duration_ticks";
    public static final String KEY_PULSE_EVERY_TICKS = "pulse_every_ticks";
    public static final String KEY_BONUS_DAMAGE_SPEC = "bonus_damage_spec";
    public static final String KEY_HOMING_TURN = "homing_turn";
    public static final String KEY_HOMING_RANGE = "homing_range";
    public static final String KEY_PARTICLE = "particle";
    private static final int DEFAULT_DURATION_TICKS = 200;
    private static final int DEFAULT_PULSE_EVERY_TICKS = 10;
    private static final float DEFAULT_HOMING_TURN = 0.35F;
    private static final float DEFAULT_HOMING_RANGE = 24.0F;
    private static final String DEFAULT_PARTICLE = "minecraft:flame";
    private static final float SAMPLE_PULSE_DAMAGE = 1.0F;
    private static final float SAMPLE_BONUS_DAMAGE = 2.0F;
    private static final int PARTICLES_PER_PULSE = 8;
    private static final double PARTICLE_SPREAD = 0.25D;
    private static final double PARTICLE_SPEED = 0.02D;
    private static final double MID_HEIGHT = 0.5D;
    private static final String KEY_TOOLTIP_LATCH = "tooltip.combat_styles_content.solar_brand";
    private static final String KEY_TOOLTIP_PULSE = "tooltip.combat_styles_content.solar_brand.pulse";
    private static final String KEY_TOOLTIP_HOMING = "tooltip.combat_styles_content.solar_brand.homing";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_DURATION_TICKS, Codec.INT, DEFAULT_DURATION_TICKS),
            OptionField.of(KEY_PULSE_EVERY_TICKS, Codec.INT, DEFAULT_PULSE_EVERY_TICKS),
            OptionField.of(HookOptions.KEY_DAMAGE_SPEC, DamageSpec.CODEC,
                    DamageSpec.ofFlat(SAMPLE_PULSE_DAMAGE), DamageSpec.SCHEMA),
            OptionField.of(KEY_BONUS_DAMAGE_SPEC, DamageSpec.CODEC,
                    DamageSpec.ofFlat(SAMPLE_BONUS_DAMAGE), DamageSpec.SCHEMA),
            OptionField.of(KEY_HOMING_TURN, Codec.FLOAT, DEFAULT_HOMING_TURN),
            OptionField.of(KEY_HOMING_RANGE, Codec.FLOAT, DEFAULT_HOMING_RANGE),
            OptionField.of(KEY_PARTICLE, Codec.STRING, DEFAULT_PARTICLE));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onSpawn(CustomContext context, Entity spawned) {
        if (spawned instanceof ShurikenEntity star) {
            star.latchOnImpact();
        }
    }

    @Override
    public void onTick(CustomContext context, Entity entity) {
        if (!(entity instanceof ShurikenEntity star) || !star.anchored()
                || !(star.level() instanceof ServerLevel level) || context.owner() == null) {
            return;
        }
        LivingEntity target = star.latchedTarget();
        if (target == null) {
            return;
        }
        JsonObject options = context.options();
        int duration = HookOptions.intOf(options, KEY_DURATION_TICKS, DEFAULT_DURATION_TICKS);
        int age = star.anchoredTicks();
        if (age >= duration || !target.isAlive()) {
            SolarBrands.clear(context.owner(), target);
            star.discard();
            return;
        }
        if (age == 0 || SolarBrands.of(context.owner()) == null) {
            SolarBrands.brand(context.owner(), new SolarBrands.Brand(target,
                    bonusOf(options), HookOptions.floatOf(options, KEY_HOMING_TURN, DEFAULT_HOMING_TURN),
                    HookOptions.floatOf(options, KEY_HOMING_RANGE, DEFAULT_HOMING_RANGE),
                    level.getGameTime() + duration - age));
        }
        int every = Math.max(1, HookOptions.intOf(options, KEY_PULSE_EVERY_TICKS,
                DEFAULT_PULSE_EVERY_TICKS));
        if (age % every != 0) {
            return;
        }
        ProjectileStrikes.strikeOne(star, context.owner(), HookOptions.specOf(options), target);
        ParticleOptions particle = HookOptions.particleOf(
                HookOptions.stringOf(options, KEY_PARTICLE, DEFAULT_PARTICLE));
        if (particle != null) {
            Vec3 at = target.position().add(0.0D, target.getBbHeight() * MID_HEIGHT, 0.0D);
            level.sendParticles(particle, at.x, at.y, at.z, PARTICLES_PER_PULSE,
                    PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        }
    }

    private static DamageSpec bonusOf(JsonObject options) {
        return HookOptions.specOf(options, KEY_BONUS_DAMAGE_SPEC);
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        int duration = HookOptions.intOf(options, KEY_DURATION_TICKS, DEFAULT_DURATION_TICKS);
        lines.add(Component.translatable(KEY_TOOLTIP_LATCH,
                StyleText.formatDecimal(HookOptions.seconds(duration))));
        DamageSpec pulse = HookOptions.specOf(options);
        if (pulse != null) {
            lines.add(Component.translatable(KEY_TOOLTIP_PULSE, AffixText.specText(pulse),
                    StyleText.formatDecimal(HookOptions.seconds(HookOptions.intOf(options,
                            KEY_PULSE_EVERY_TICKS, DEFAULT_PULSE_EVERY_TICKS)))));
        }
        DamageSpec bonus = bonusOf(options);
        if (bonus != null) {
            lines.add(Component.translatable(KEY_TOOLTIP_HOMING, AffixText.specText(bonus)));
        }
        return lines;
    }
}
