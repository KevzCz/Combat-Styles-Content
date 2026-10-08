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

public final class OrbitHook implements CustomHook {

    public static final String KEY_ORBIT_TICKS = "orbit_ticks";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_DEGREES_PER_TICK = "degrees_per_tick";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_LAUNCH_SPEED = "launch_speed";
    public static final String KEY_SCORCH_EVERY_TICKS = "scorch_every_ticks";
    public static final String KEY_SCORCH_RADIUS = "scorch_radius";
    public static final String KEY_IGNITE_SECONDS = "ignite_seconds";
    public static final String KEY_PARTICLE = "particle";
    private static final int DEFAULT_ORBIT_TICKS = 60;
    private static final float DEFAULT_RADIUS = 2.4F;
    private static final float DEFAULT_DEGREES_PER_TICK = 18.0F;
    private static final float DEFAULT_HEIGHT = 1.0F;
    private static final float DEFAULT_LAUNCH_SPEED = 1.8F;
    private static final int DEFAULT_SCORCH_EVERY_TICKS = 10;
    private static final float DEFAULT_SCORCH_RADIUS = 1.2F;
    private static final int DEFAULT_IGNITE_SECONDS = 3;
    private static final String DEFAULT_PARTICLE = "minecraft:flame";
    private static final float SAMPLE_SCORCH_DAMAGE = 2.0F;
    private static final int PARTICLES_PER_TICK = 2;
    private static final double PARTICLE_SPREAD = 0.15D;
    private static final double PARTICLE_SPEED = 0.01D;
    private static final String KEY_TOOLTIP_ORBIT = "tooltip.combat_styles_content.orbit";
    private static final String KEY_TOOLTIP_SCORCH = "tooltip.combat_styles_content.orbit.scorch";
    private static final String KEY_TOOLTIP_LAUNCH = "tooltip.combat_styles_content.orbit.launch";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_ORBIT_TICKS, Codec.INT, DEFAULT_ORBIT_TICKS),
            OptionField.of(KEY_RADIUS, Codec.FLOAT, DEFAULT_RADIUS),
            OptionField.of(KEY_DEGREES_PER_TICK, Codec.FLOAT, DEFAULT_DEGREES_PER_TICK),
            OptionField.of(KEY_HEIGHT, Codec.FLOAT, DEFAULT_HEIGHT),
            OptionField.of(KEY_LAUNCH_SPEED, Codec.FLOAT, DEFAULT_LAUNCH_SPEED),
            OptionField.of(KEY_SCORCH_EVERY_TICKS, Codec.INT, DEFAULT_SCORCH_EVERY_TICKS),
            OptionField.of(KEY_SCORCH_RADIUS, Codec.FLOAT, DEFAULT_SCORCH_RADIUS),
            OptionField.of(HookOptions.KEY_DAMAGE_SPEC, DamageSpec.CODEC,
                    DamageSpec.ofFlat(SAMPLE_SCORCH_DAMAGE), DamageSpec.SCHEMA),
            OptionField.of(KEY_IGNITE_SECONDS, Codec.INT, DEFAULT_IGNITE_SECONDS),
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
        if (!(spawned instanceof ShurikenEntity star)) {
            return;
        }
        JsonObject options = context.options();
        star.orbit(HookOptions.intOf(options, KEY_ORBIT_TICKS, DEFAULT_ORBIT_TICKS),
                HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS),
                HookOptions.floatOf(options, KEY_DEGREES_PER_TICK, DEFAULT_DEGREES_PER_TICK),
                HookOptions.floatOf(options, KEY_HEIGHT, DEFAULT_HEIGHT),
                HookOptions.floatOf(options, KEY_LAUNCH_SPEED, DEFAULT_LAUNCH_SPEED));
    }

    @Override
    public void onTick(CustomContext context, Entity entity) {
        if (!(entity instanceof ShurikenEntity star) || !star.orbiting()
                || !(star.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        Vec3 centre = star.position();
        ParticleOptions particle = HookOptions.particleOf(
                HookOptions.stringOf(options, KEY_PARTICLE, DEFAULT_PARTICLE));
        if (particle != null) {
            level.sendParticles(particle, centre.x, centre.y, centre.z, PARTICLES_PER_TICK,
                    PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        }
        int every = Math.max(1, HookOptions.intOf(options, KEY_SCORCH_EVERY_TICKS,
                DEFAULT_SCORCH_EVERY_TICKS));
        if (star.tickCount % every != 0) {
            return;
        }
        float radius = HookOptions.floatOf(options, KEY_SCORCH_RADIUS, DEFAULT_SCORCH_RADIUS);
        int ignite = HookOptions.intOf(options, KEY_IGNITE_SECONDS, DEFAULT_IGNITE_SECONDS);
        if (ignite > 0) {
            for (LivingEntity foe : ProjectileStrikes.foesAround(level, context.owner(), centre, radius, null)) {
                foe.igniteForSeconds(ignite);
            }
        }
        ProjectileStrikes.strikeArea(level, star, context.owner(), HookOptions.specOf(options), centre,
                radius, null);
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY_TOOLTIP_ORBIT, StyleText.formatDecimal(HookOptions.seconds(
                HookOptions.intOf(options, KEY_ORBIT_TICKS, DEFAULT_ORBIT_TICKS)))));
        DamageSpec spec = HookOptions.specOf(options);
        if (spec != null) {
            lines.add(Component.translatable(KEY_TOOLTIP_SCORCH, AffixText.specText(spec),
                    HookOptions.intOf(options, KEY_IGNITE_SECONDS, DEFAULT_IGNITE_SECONDS)));
        }
        lines.add(Component.translatable(KEY_TOOLTIP_LAUNCH));
        return lines;
    }
}
