package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BoneShatterHook implements CustomHook {

    public static final String KEY_SHATTER_CHANCE = "shatter_chance";
    public static final String KEY_SHATTER_COUNT = "shatter_count";
    public static final String KEY_SHATTER_SPEED = "shatter_speed";
    public static final String KEY_HIT_CHANCE = "hit_chance";
    public static final String KEY_HIT_COUNT = "hit_count";
    public static final String KEY_HIT_RISE = "hit_rise";
    public static final String KEY_BONE = "bone";
    private static final float DEFAULT_SHATTER_CHANCE = 0.5F;
    private static final int DEFAULT_SHATTER_COUNT = 4;
    private static final float DEFAULT_SHATTER_SPEED = 0.55F;
    private static final float DEFAULT_HIT_CHANCE = 0.1F;
    private static final int DEFAULT_HIT_COUNT = 2;
    private static final float DEFAULT_HIT_RISE = 0.7F;
    private static final double SCATTER = 0.35D;
    private static final double HALF = 0.5D;
    private static final double LIFT_MIN = 0.25D;
    private static final double LIFT_RANGE = 0.35D;
    private static final double TARGET_HEIGHT_RATIO = 0.75D;
    private static final double PERCENT = 100.0D;
    private static final float SOUND_VOLUME = 0.7F;
    private static final float SOUND_PITCH = 0.8F;
    private static final String KEY_ENTITY = "entity";
    private static final String SAMPLE_BONE_ENTITY = CombatStylesContent.MOD_ID + ":bone_shard";
    private static final String KEY_TOOLTIP_SHATTER = "tooltip.combat_styles_content.bone_shatter.wall";
    private static final String KEY_TOOLTIP_HIT = "tooltip.combat_styles_content.bone_shatter.hit";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_SHATTER_CHANCE, Codec.FLOAT, DEFAULT_SHATTER_CHANCE),
            OptionField.of(KEY_SHATTER_COUNT, Codec.INT, DEFAULT_SHATTER_COUNT),
            OptionField.of(KEY_SHATTER_SPEED, Codec.FLOAT, DEFAULT_SHATTER_SPEED),
            OptionField.of(KEY_HIT_CHANCE, Codec.FLOAT, DEFAULT_HIT_CHANCE),
            OptionField.of(KEY_HIT_COUNT, Codec.INT, DEFAULT_HIT_COUNT),
            OptionField.of(KEY_HIT_RISE, Codec.FLOAT, DEFAULT_HIT_RISE),
            OptionField.of(KEY_BONE, AffixEffects.Projectile.CODEC, sampleBone()));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onLand(CustomContext context, Entity entity) {
        Player owner = context.owner();
        if (owner == null || !(entity.level() instanceof ServerLevel level)
                || !(entity instanceof ShurikenEntity wheel) || ReturningHook.returning(entity)) {
            return;
        }
        JsonObject options = context.options();
        if (level.getRandom().nextFloat() >= HookOptions.floatOf(options, KEY_SHATTER_CHANCE,
                DEFAULT_SHATTER_CHANCE)) {
            return;
        }
        double speed = HookOptions.floatOf(options, KEY_SHATTER_SPEED, DEFAULT_SHATTER_SPEED);
        launch(level, owner, entity, entity.position(), options,
                HookOptions.intOf(options, KEY_SHATTER_COUNT, DEFAULT_SHATTER_COUNT), random -> {
                    Vec3 away = entity.getDeltaMovement().reverse().normalize();
                    return new Vec3(away.x + (random.nextDouble() - HALF) * 2.0D,
                            LIFT_MIN + random.nextDouble() * LIFT_RANGE,
                            away.z + (random.nextDouble() - HALF) * 2.0D).normalize().scale(speed);
                });
        level.playSound(null, entity.blockPosition(), SoundEvents.SKELETON_DEATH, SoundSource.PLAYERS,
                SOUND_VOLUME, SOUND_PITCH);
        wheel.shatterOnLanding();
    }

    @Override
    public void onHit(CustomContext context, Entity source, LivingEntity target) {
        Player owner = context.owner();
        if (ProjectileStrikes.striking() || owner == null || target == null
                || !(target.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        if (level.getRandom().nextFloat() >= HookOptions.floatOf(options, KEY_HIT_CHANCE, DEFAULT_HIT_CHANCE)) {
            return;
        }
        double rise = HookOptions.floatOf(options, KEY_HIT_RISE, DEFAULT_HIT_RISE);
        Vec3 from = target.position().add(0.0D, target.getBbHeight() * TARGET_HEIGHT_RATIO, 0.0D);
        launch(level, owner, source, from, options,
                HookOptions.intOf(options, KEY_HIT_COUNT, DEFAULT_HIT_COUNT),
                random -> new Vec3((random.nextDouble() - HALF) * SCATTER * 2.0D, rise,
                        (random.nextDouble() - HALF) * SCATTER * 2.0D));
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY_TOOLTIP_SHATTER,
                Math.round(HookOptions.floatOf(options, KEY_SHATTER_CHANCE, DEFAULT_SHATTER_CHANCE) * PERCENT),
                HookOptions.intOf(options, KEY_SHATTER_COUNT, DEFAULT_SHATTER_COUNT)));
        lines.add(Component.translatable(KEY_TOOLTIP_HIT,
                Math.round(HookOptions.floatOf(options, KEY_HIT_CHANCE, DEFAULT_HIT_CHANCE) * PERCENT),
                HookOptions.intOf(options, KEY_HIT_COUNT, DEFAULT_HIT_COUNT)));
        return lines;
    }

    private interface Velocity {
        Vec3 next(RandomSource random);
    }

    private static void launch(ServerLevel level, Player owner, Entity origin, Vec3 from, JsonObject options,
                               int count, Velocity velocity) {
        AffixEffects.Projectile bone = boneOf(options);
        if (bone == null) {
            return;
        }
        float triggering = ProjectileUpkeep.triggeringFor(origin);
        for (int index = 0; index < count; index++) {
            for (Entity spawned : ProjectileLaunches.spawnFrom(level, owner, origin, null, bone,
                    triggering, false)) {
                spawned.setPos(from.x, from.y, from.z);
                spawned.setDeltaMovement(velocity.next(level.getRandom()));
                spawned.hasImpulse = true;
            }
        }
    }

    private static AffixEffects.Projectile boneOf(JsonObject options) {
        if (options == null || !options.has(KEY_BONE)) {
            return sampleBone();
        }
        return AffixEffects.Projectile.CODEC.parse(JsonOps.INSTANCE, options.get(KEY_BONE))
                .result().orElse(null);
    }

    private static AffixEffects.Projectile sampleBone() {
        JsonObject sample = new JsonObject();
        sample.addProperty(KEY_ENTITY, SAMPLE_BONE_ENTITY);
        return AffixEffects.Projectile.CODEC.parse(JsonOps.INSTANCE, sample).result().orElse(null);
    }
}
