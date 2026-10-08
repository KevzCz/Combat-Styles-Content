package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.affix.AffixEffects;
import net.pixeldreamstudios.combat_styles.affix.ProjectileLaunches;
import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.style.StyleText;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SplinterHook implements CustomHook {

    public static final String KEY_COUNT = "count";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_SAME_TARGET = "same_target";
    public static final String KEY_SHARD = "shard";
    public static final String KEY_RISE = "rise";
    public static final String KEY_RISE_TICKS = "rise_ticks";
    public static final String KEY_TURN = "turn";
    private static final float DEFAULT_RISE = 0.9F;
    private static final int DEFAULT_RISE_TICKS = 6;
    private static final float DEFAULT_TURN = 0.35F;
    private static final double SCATTER = 0.35D;
    private static final double HALF = 0.5D;
    private static final String KEY_ENTITY = "entity";
    private static final String SAMPLE_SHARD_ENTITY = CombatStylesContent.MOD_ID + ":shuriken_shard";
    private static final int DEFAULT_COUNT = 3;
    private static final float DEFAULT_RADIUS = 6.0F;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.splinter";
    public static final String KEY_RADIAL = "radial";
    public static final String KEY_OUTWARD = "outward";
    public static final String KEY_HOME_BOOST = "home_boost";
    public static final String KEY_BURST_PARTICLE = "burst_particle";
    public static final String KEY_BURST_PARTICLE_COUNT = "burst_particle_count";
    public static final String KEY_BURST_SOUND = "burst_sound";
    private static final float DEFAULT_OUTWARD = 0.5F;
    private static final float DEFAULT_HOME_BOOST = 1.0F;
    private static final String DEFAULT_BURST_PARTICLE = "minecraft:end_rod";
    private static final int DEFAULT_BURST_PARTICLE_COUNT = 0;
    private static final String DEFAULT_BURST_SOUND = "";
    private static final double FULL_TURN = Math.PI * 2.0D;
    private static final double BURST_HEIGHT = 0.6D;
    private static final double BURST_SPREAD = 0.3D;
    private static final double BURST_SPEED = 0.15D;
    private static final float BURST_VOLUME = 0.6F;
    private static final float BURST_PITCH = 1.3F;

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_COUNT, Codec.INT, DEFAULT_COUNT),
            OptionField.of(KEY_RADIUS, Codec.FLOAT, DEFAULT_RADIUS),
            OptionField.of(KEY_SAME_TARGET, Codec.BOOL, true),
            OptionField.of(KEY_RISE, Codec.FLOAT, DEFAULT_RISE),
            OptionField.of(KEY_RISE_TICKS, Codec.INT, DEFAULT_RISE_TICKS),
            OptionField.of(KEY_TURN, Codec.FLOAT, DEFAULT_TURN),
            OptionField.of(KEY_RADIAL, Codec.BOOL, false),
            OptionField.of(KEY_OUTWARD, Codec.FLOAT, DEFAULT_OUTWARD),
            OptionField.of(KEY_HOME_BOOST, Codec.FLOAT, DEFAULT_HOME_BOOST),
            OptionField.of(KEY_BURST_PARTICLE, Codec.STRING, DEFAULT_BURST_PARTICLE),
            OptionField.of(KEY_BURST_PARTICLE_COUNT, Codec.INT, DEFAULT_BURST_PARTICLE_COUNT),
            OptionField.of(KEY_BURST_SOUND, Codec.STRING, DEFAULT_BURST_SOUND),
            OptionField.of(KEY_SHARD, AffixEffects.Projectile.CODEC, sampleShard()));

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
        Player owner = context.owner();
        if (ProjectileStrikes.striking() || owner == null || target == null
                || !(target.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        AffixEffects.Projectile shard = shardOf(options);
        if (shard == null) {
            return;
        }
        List<LivingEntity> others = new ArrayList<>(ProjectileStrikes.foesAround(level, owner,
                target.position(), HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS), target));
        if (HookOptions.boolOf(options, KEY_SAME_TARGET, true)) {
            others.add(target);
        }
        float triggering = ProjectileUpkeep.triggeringFor(source);
        int count = HookOptions.intOf(options, KEY_COUNT, DEFAULT_COUNT);
        double rise = HookOptions.floatOf(options, KEY_RISE, DEFAULT_RISE);
        int riseTicks = HookOptions.intOf(options, KEY_RISE_TICKS, DEFAULT_RISE_TICKS);
        float turn = HookOptions.floatOf(options, KEY_TURN, DEFAULT_TURN);
        boolean radial = HookOptions.boolOf(options, KEY_RADIAL, false);
        double outward = HookOptions.floatOf(options, KEY_OUTWARD, DEFAULT_OUTWARD);
        double homeSpeed = shard.speed() * HookOptions.floatOf(options, KEY_HOME_BOOST, DEFAULT_HOME_BOOST);
        RandomSource random = level.getRandom();
        double startAngle = random.nextDouble() * FULL_TURN;
        burstEffects(level, target, options);
        for (int index = 0; index < count; index++) {
            if (others.isEmpty()) {
                return;
            }
            LivingEntity aim = others.get(index % others.size());
            for (Entity spawned : ProjectileLaunches.spawnFrom(level, owner, target, aim, shard,
                    triggering, false)) {
                Vec3 velocity;
                if (radial) {
                    double angle = startAngle + FULL_TURN * index / Math.max(1, count);
                    velocity = new Vec3(Math.cos(angle) * outward, rise, Math.sin(angle) * outward);
                } else {
                    velocity = new Vec3((random.nextDouble() - HALF) * SCATTER * 2.0D, rise,
                            (random.nextDouble() - HALF) * SCATTER * 2.0D);
                }
                spawned.setDeltaMovement(velocity);
                spawned.hasImpulse = true;
                if (spawned instanceof ShurikenEntity splinter) {
                    splinter.ignoreFor(target, riseTicks);
                    splinter.homeOn(aim, riseTicks, turn, homeSpeed);
                }
            }
        }
    }

    private static void burstEffects(ServerLevel level, LivingEntity target, JsonObject options) {
        ParticleOptions particle = HookOptions.particleOf(
                HookOptions.stringOf(options, KEY_BURST_PARTICLE, DEFAULT_BURST_PARTICLE));
        int particles = HookOptions.intOf(options, KEY_BURST_PARTICLE_COUNT, DEFAULT_BURST_PARTICLE_COUNT);
        Vec3 at = target.position().add(0.0D, target.getBbHeight() * BURST_HEIGHT, 0.0D);
        if (particle != null && particles > 0) {
            level.sendParticles(particle, at.x, at.y, at.z, particles, BURST_SPREAD, BURST_SPREAD,
                    BURST_SPREAD, BURST_SPEED);
        }
        level.sendParticles(ParticleTypes.FLASH, at.x, at.y, at.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        SoundEvent sound = HookOptions.soundOf(HookOptions.stringOf(options, KEY_BURST_SOUND, DEFAULT_BURST_SOUND));
        if (sound != null) {
            level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, BURST_VOLUME, BURST_PITCH);
        }
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                HookOptions.intOf(options, KEY_COUNT, DEFAULT_COUNT),
                StyleText.formatCompact(HookOptions.floatOf(options, KEY_RADIUS, DEFAULT_RADIUS))));
    }

    private static AffixEffects.Projectile shardOf(JsonObject options) {
        if (options == null || !options.has(KEY_SHARD)) {
            return null;
        }
        return AffixEffects.Projectile.CODEC.parse(JsonOps.INSTANCE, options.get(KEY_SHARD))
                .result().orElse(null);
    }

    private static AffixEffects.Projectile sampleShard() {
        JsonObject sample = new JsonObject();
        sample.addProperty(KEY_ENTITY, SAMPLE_SHARD_ENTITY);
        return AffixEffects.Projectile.CODEC.parse(JsonOps.INSTANCE, sample).result().orElse(null);
    }
}
