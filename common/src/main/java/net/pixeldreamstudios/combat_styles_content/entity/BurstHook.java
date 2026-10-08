package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

public final class BurstHook implements CustomHook {

    public static final String KEY_RADIUS = "radius";
    public static final String KEY_ON_HIT = "on_hit";
    public static final String KEY_ON_LAND = "on_land";
    public static final String KEY_HITS_TARGET = "hits_target";
    public static final String KEY_IGNITE_SECONDS = "ignite_seconds";
    public static final String KEY_PARTICLE = "particle";
    public static final String KEY_PARTICLE_COUNT = "particle_count";
    public static final String KEY_SOUND = "sound";
    private static final float DEFAULT_RADIUS = 2.5F;
    private static final int DEFAULT_IGNITE_SECONDS = 0;
    private static final String DEFAULT_PARTICLE = "minecraft:explosion";
    private static final int DEFAULT_PARTICLE_COUNT = 12;
    private static final String DEFAULT_SOUND = "minecraft:entity.generic.explode";
    private static final float SAMPLE_DAMAGE = 3.0F;
    private static final float SOUND_VOLUME = 0.6F;
    private static final float SOUND_PITCH = 1.2F;
    private static final double PARTICLE_SPREAD_PER_RADIUS = 0.4D;
    private static final double PARTICLE_SPEED = 0.05D;
    private static final String KEY_TOOLTIP_BURST = "tooltip.combat_styles_content.burst";
    private static final String KEY_TOOLTIP_BURST_HIT = "tooltip.combat_styles_content.burst.hit";
    private static final String KEY_TOOLTIP_BURST_LAND = "tooltip.combat_styles_content.burst.land";
    private static final String KEY_TOOLTIP_IGNITE = "tooltip.combat_styles_content.burst.ignite";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_ON_HIT, Codec.BOOL, true),
            OptionField.of(KEY_ON_LAND, Codec.BOOL, true),
            OptionField.of(KEY_HITS_TARGET, Codec.BOOL, true),
            OptionField.of(KEY_RADIUS, Codec.FLOAT, DEFAULT_RADIUS),
            OptionField.of(HookOptions.KEY_DAMAGE_SPEC, DamageSpec.CODEC, DamageSpec.ofFlat(SAMPLE_DAMAGE),
                    DamageSpec.SCHEMA),
            OptionField.of(KEY_IGNITE_SECONDS, Codec.INT, DEFAULT_IGNITE_SECONDS),
            OptionField.of(KEY_PARTICLE, Codec.STRING, DEFAULT_PARTICLE),
            OptionField.of(KEY_PARTICLE_COUNT, Codec.INT, DEFAULT_PARTICLE_COUNT),
            OptionField.of(KEY_SOUND, Codec.STRING, DEFAULT_SOUND));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onHit(CustomContext context, Entity source, LivingEntity target) {
        if (ProjectileStrikes.striking() || !firesOn(context.options(), KEY_ON_HIT)) {
            return;
        }
        int ignite = HookOptions.intOf(context.options(), KEY_IGNITE_SECONDS, DEFAULT_IGNITE_SECONDS);
        if (ignite > 0 && target != null) {
            target.igniteForSeconds(ignite);
        }
        Entity excluded = HookOptions.boolOf(context.options(), KEY_HITS_TARGET, true) ? null : target;
        burst(context, source, target == null ? source.position() : target.position(), excluded);
    }

    @Override
    public void onLand(CustomContext context, Entity entity) {
        if (firesOn(context.options(), KEY_ON_LAND)) {
            burst(context, entity, entity.position(), null);
        }
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        boolean onHit = firesOn(options, KEY_ON_HIT);
        boolean onLand = firesOn(options, KEY_ON_LAND);
        String when = onHit && !onLand ? KEY_TOOLTIP_BURST_HIT
                : onLand && !onHit ? KEY_TOOLTIP_BURST_LAND
                : KEY_TOOLTIP_BURST;
        DamageSpec spec = HookOptions.specOf(options);
        lines.add(Component.translatable(when,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS)),
                spec == null ? Component.empty() : AffixText.specText(spec)));
        int ignite = HookOptions.intOf(options, KEY_IGNITE_SECONDS, DEFAULT_IGNITE_SECONDS);
        if (ignite > 0) {
            lines.add(Component.translatable(KEY_TOOLTIP_IGNITE, ignite));
        }
        return lines;
    }

    private static boolean firesOn(JsonObject options, String moment) {
        return HookOptions.boolOf(options, moment, true);
    }

    private static void burst(CustomContext context, Entity source, Vec3 centre, Entity excluded) {
        if (!(source.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        Player owner = context.owner();
        float radius = HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS);
        int ignite = HookOptions.intOf(options, KEY_IGNITE_SECONDS, DEFAULT_IGNITE_SECONDS);
        if (ignite > 0) {
            for (LivingEntity foe : ProjectileStrikes.foesAround(level, owner, centre, radius, excluded)) {
                foe.igniteForSeconds(ignite);
            }
        }
        ProjectileStrikes.strikeArea(level, source, owner, HookOptions.specOf(options), centre, radius,
                excluded);
        ParticleOptions particle = HookOptions.particleOf(
                HookOptions.stringOf(options, KEY_PARTICLE, DEFAULT_PARTICLE));
        if (particle != null) {
            double spread = radius * PARTICLE_SPREAD_PER_RADIUS;
            level.sendParticles(particle, centre.x, centre.y, centre.z,
                    HookOptions.intOf(options, KEY_PARTICLE_COUNT, DEFAULT_PARTICLE_COUNT),
                    spread, spread, spread, PARTICLE_SPEED);
        }
        SoundEvent sound = HookOptions.soundOf(HookOptions.stringOf(options, KEY_SOUND, DEFAULT_SOUND));
        if (sound != null) {
            level.playSound(null, centre.x, centre.y, centre.z, sound, SoundSource.PLAYERS,
                    SOUND_VOLUME, SOUND_PITCH);
        }
    }
}
