package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ReturningHook implements CustomHook {

    public static final String KEY_RANGE = "range";
    public static final String KEY_RETURN_SPEED = "return_speed";
    public static final String KEY_CATCH_RADIUS = "catch_radius";
    public static final String KEY_RETURN_TO = "return_to";
    public static final String KEY_DROP_TICKS = "drop_ticks";
    public static final String KEY_PICKUP_RADIUS = "pickup_radius";
    public static final String RETURN_TO_OWNER = "owner";
    public static final String RETURN_TO_ORIGIN = "origin";
    private static final String RETURNING_TAG = "combat_styles_content.returning";
    private static final float DEFAULT_RANGE = 10.0F;
    private static final float DEFAULT_RETURN_SPEED = 1.2F;
    private static final float DEFAULT_CATCH_RADIUS = 1.5F;
    private static final int DEFAULT_DROP_TICKS = 40;
    private static final float DEFAULT_PICKUP_RADIUS = 1.5F;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.returning";
    private static final String KEY_TOOLTIP_ORIGIN = "tooltip.combat_styles_content.returning.origin";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_RANGE, Codec.FLOAT, DEFAULT_RANGE),
            OptionField.of(KEY_RETURN_SPEED, Codec.FLOAT, DEFAULT_RETURN_SPEED),
            OptionField.of(KEY_CATCH_RADIUS, Codec.FLOAT, DEFAULT_CATCH_RADIUS),
            OptionField.of(KEY_RETURN_TO, Codec.STRING, RETURN_TO_OWNER),
            OptionField.of(KEY_DROP_TICKS, Codec.INT, DEFAULT_DROP_TICKS),
            OptionField.of(KEY_PICKUP_RADIUS, Codec.FLOAT, DEFAULT_PICKUP_RADIUS));

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
        if (spawned instanceof ShurikenEntity wheel && toOrigin(context.options())) {
            wheel.setThrowPoint(spawned.position());
        }
    }

    @Override
    public void onTick(CustomContext context, Entity entity) {
        Player owner = context.owner();
        if (owner == null || entity.level().isClientSide()) {
            return;
        }
        ShurikenEntity wheel = entity instanceof ShurikenEntity shuriken ? shuriken : null;
        if (wheel != null && wheel.dropped()) {
            return;
        }
        JsonObject options = context.options();
        boolean toOrigin = toOrigin(options) && wheel != null && wheel.throwPoint() != null;
        Vec3 home = toOrigin ? wheel.throwPoint() : owner.getEyePosition();
        double distance = entity.position().distanceTo(home);
        boolean returning = entity.getTags().contains(RETURNING_TAG);
        if (!returning && distance >= HookOptions.floatOf(options, KEY_RANGE, DEFAULT_RANGE)) {
            entity.addTag(RETURNING_TAG);
            if (wheel != null) {
                wheel.forgetHits();
            }
            returning = true;
        }
        if (!returning) {
            return;
        }
        if (distance <= HookOptions.floatOf(options, KEY_CATCH_RADIUS, DEFAULT_CATCH_RADIUS)) {
            if (toOrigin) {
                dropAt(wheel, options);
            } else {
                CatchRefundHook.caught(owner, entity);
                entity.discard();
            }
            return;
        }
        Vec3 toward = home.subtract(entity.position()).normalize()
                .scale(HookOptions.floatOf(options, KEY_RETURN_SPEED, DEFAULT_RETURN_SPEED));
        entity.setDeltaMovement(toward);
        entity.hasImpulse = true;
    }

    public static boolean returning(Entity entity) {
        return entity != null && entity.getTags().contains(RETURNING_TAG);
    }

    @Override
    public void onLand(CustomContext context, Entity entity) {
        if (!(entity instanceof ShurikenEntity wheel)) {
            return;
        }
        JsonObject options = context.options();
        if (entity.getTags().contains(RETURNING_TAG)) {
            if (wheel.justTurned()) {
                wheel.surviveLanding();
                headHome(context.owner(), wheel, options);
            } else if (toOrigin(options)) {
                wheel.surviveLanding();
                dropAt(wheel, options);
            }
            return;
        }
        entity.addTag(RETURNING_TAG);
        wheel.forgetHits();
        wheel.surviveLanding();
        headHome(context.owner(), wheel, options);
    }

    private static void headHome(Player owner, ShurikenEntity wheel, JsonObject options) {
        Vec3 home = toOrigin(options) && wheel.throwPoint() != null
                ? wheel.throwPoint()
                : owner == null ? null : owner.getEyePosition();
        if (home == null) {
            return;
        }
        Vec3 toward = home.subtract(wheel.position());
        if (toward.lengthSqr() > 0.0D) {
            wheel.setDeltaMovement(toward.normalize()
                    .scale(HookOptions.floatOf(options, KEY_RETURN_SPEED, DEFAULT_RETURN_SPEED)));
            wheel.hasImpulse = true;
        }
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY_TOOLTIP,
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_RANGE, DEFAULT_RANGE))));
        if (toOrigin(options)) {
            lines.add(Component.translatable(KEY_TOOLTIP_ORIGIN, StyleText.formatDecimal(
                    HookOptions.seconds(HookOptions.intOf(options, KEY_DROP_TICKS, DEFAULT_DROP_TICKS)))));
        }
        return lines;
    }

    private static boolean toOrigin(JsonObject options) {
        return RETURN_TO_ORIGIN.equals(HookOptions.stringOf(options, KEY_RETURN_TO, RETURN_TO_OWNER));
    }

    private static void dropAt(ShurikenEntity wheel, JsonObject options) {
        wheel.drop(HookOptions.intOf(options, KEY_DROP_TICKS, DEFAULT_DROP_TICKS),
                HookOptions.floatOf(options, KEY_PICKUP_RADIUS, DEFAULT_PICKUP_RADIUS));
    }
}
