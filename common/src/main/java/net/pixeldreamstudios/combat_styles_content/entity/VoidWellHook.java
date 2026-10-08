package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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

public final class VoidWellHook implements CustomHook {

    public static final String KEY_GROW_TO = "grow_to";
    public static final String KEY_GROW_TICKS = "grow_ticks";
    public static final String KEY_DURATION_TICKS = "duration_ticks";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_PULL = "pull";
    private static final Vec3 HORIZONTAL = new Vec3(1.0D, 0.0D, 1.0D);
    public static final String KEY_TICK_EVERY_DEGREES = "tick_every_degrees";
    public static final String KEY_HURT_RADIUS = "hurt_radius";
    private static final float DEFAULT_GROW_TO = 3.0F;
    private static final int DEFAULT_GROW_TICKS = 40;
    private static final int DEFAULT_DURATION_TICKS = 80;
    private static final float DEFAULT_RADIUS = 5.0F;
    private static final float DEFAULT_PULL = 0.12F;
    private static final float DEFAULT_TICK_EVERY_DEGREES = 360.0F;
    private static final float SAMPLE_PULSE_DAMAGE = 2.0F;
    private static final float DEFAULT_HURT_RADIUS = 1.5F;
    private static final double MIN_DISTANCE = 0.5D;
    private static final int PARTICLES_PER_TICK = 4;
    private static final double PARTICLE_SPREAD_PER_SIZE = 0.35D;
    private static final double PARTICLE_SPEED = 0.6D;
    private static final String KEY_TOOLTIP_ANCHOR = "tooltip.combat_styles_content.void_well.anchor";
    private static final String KEY_TOOLTIP_PULL = "tooltip.combat_styles_content.void_well.pull";
    private static final String KEY_TOOLTIP_PULSE = "tooltip.combat_styles_content.void_well.pulse";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_GROW_TO, Codec.FLOAT, DEFAULT_GROW_TO),
            OptionField.of(KEY_GROW_TICKS, Codec.INT, DEFAULT_GROW_TICKS),
            OptionField.of(KEY_DURATION_TICKS, Codec.INT, DEFAULT_DURATION_TICKS),
            OptionField.of(KEY_RADIUS, Codec.FLOAT, DEFAULT_RADIUS),
            OptionField.of(KEY_PULL, Codec.FLOAT, DEFAULT_PULL),
            OptionField.of(KEY_TICK_EVERY_DEGREES, Codec.FLOAT, DEFAULT_TICK_EVERY_DEGREES),
            OptionField.of(HookOptions.KEY_DAMAGE_SPEC, DamageSpec.CODEC,
                    DamageSpec.ofFlat(SAMPLE_PULSE_DAMAGE), DamageSpec.SCHEMA),
            OptionField.of(KEY_HURT_RADIUS, Codec.FLOAT, DEFAULT_HURT_RADIUS));

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
        if (spawned instanceof ShurikenEntity wheel) {
            wheel.anchorOnImpact();
        }
    }

    @Override
    public void onTick(CustomContext context, Entity entity) {
        if (!(entity instanceof ShurikenEntity wheel) || !wheel.anchored()
                || !(wheel.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        float growTo = HookOptions.floatOf(options, KEY_GROW_TO, DEFAULT_GROW_TO);
        int growTicks = Math.max(1, HookOptions.intOf(options, KEY_GROW_TICKS, DEFAULT_GROW_TICKS));
        int duration = HookOptions.intOf(options, KEY_DURATION_TICKS, DEFAULT_DURATION_TICKS);
        int age = wheel.anchoredTicks();
        if (age >= duration) {
            wheel.discard();
            return;
        }
        float progress = Mth.clamp((float) age / growTicks, 0.0F, 1.0F);
        float size = Mth.lerp(progress, ShurikenEntity.DEFAULT_SIZE, growTo);
        wheel.setSize(size);
        double growth = size / Math.max(growTo, ShurikenEntity.DEFAULT_SIZE);
        double radius = HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS) * growth;
        double pull = HookOptions.floatOf(options, KEY_PULL, DEFAULT_PULL);
        Vec3 centre = wheel.position();
        for (LivingEntity victim : ProjectileStrikes.foesAround(level, context.owner(), centre, radius, null)) {
            Vec3 toward = centre.subtract(victim.position()).multiply(HORIZONTAL);
            double distance = toward.length();
            if (distance < MIN_DISTANCE) {
                continue;
            }
            victim.setDeltaMovement(victim.getDeltaMovement().add(toward.scale(pull / distance)));
            victim.hurtMarked = true;
        }
        if (pulsesAt(age, wheel.spin(), HookOptions.floatOf(options, KEY_TICK_EVERY_DEGREES,
                DEFAULT_TICK_EVERY_DEGREES))) {
            ProjectileStrikes.strikeArea(level, wheel, context.owner(), HookOptions.specOf(options), centre,
                    HookOptions.floatOf(options, KEY_HURT_RADIUS, DEFAULT_HURT_RADIUS) * growth, null);
        }
        double spread = size * PARTICLE_SPREAD_PER_SIZE;
        level.sendParticles(ParticleTypes.PORTAL, centre.x, centre.y, centre.z, PARTICLES_PER_TICK,
                spread, spread, spread, PARTICLE_SPEED);
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY_TOOLTIP_ANCHOR,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_GROW_TO, DEFAULT_GROW_TO)),
                StyleText.formatDecimal(HookOptions.seconds(
                        HookOptions.intOf(options, KEY_GROW_TICKS, DEFAULT_GROW_TICKS)))));
        lines.add(Component.translatable(KEY_TOOLTIP_PULL,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS)),
                StyleText.formatDecimal(HookOptions.seconds(
                        HookOptions.intOf(options, KEY_DURATION_TICKS, DEFAULT_DURATION_TICKS)))));
        DamageSpec spec = HookOptions.specOf(options);
        if (spec != null) {
            lines.add(Component.translatable(KEY_TOOLTIP_PULSE, AffixText.specText(spec),
                    StyleText.formatDecimal(HookOptions.floatOf(options, KEY_HURT_RADIUS,
                            DEFAULT_HURT_RADIUS))));
        }
        return lines;
    }

    private static boolean pulsesAt(int age, float spin, float degreesPerPulse) {
        if (spin <= 0.0F || degreesPerPulse <= 0.0F || age <= 0) {
            return false;
        }
        return Mth.floor(age * spin / degreesPerPulse) > Mth.floor((age - 1) * spin / degreesPerPulse);
    }
}
