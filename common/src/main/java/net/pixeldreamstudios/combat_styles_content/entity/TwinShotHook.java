package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.custom.Customs;
import net.pixeldreamstudios.combat_styles.equipment.EquipmentActivation;
import net.pixeldreamstudios.combat_styles.equipment.ProjectileCopies;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;
import java.util.Set;

public final class TwinShotHook implements CustomHook {

    public static final String KEY_COUNT = "count";
    public static final String KEY_SPREAD_DEGREES = "spread_degrees";
    private static final int DEFAULT_COUNT = 2;
    private static final float DEFAULT_SPREAD_DEGREES = 10.0F;
    private static final double HALF = 2.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.twin_shot";

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_COUNT, Codec.INT, DEFAULT_COUNT),
            OptionField.of(KEY_SPREAD_DEGREES, Codec.FLOAT, DEFAULT_SPREAD_DEGREES));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.EQUIPMENT_EFFECT, CustomScopes.TRIGGERED_EFFECT);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onApply(CustomContext context) {
        Entity shot = EquipmentActivation.currentShot();
        if (shot == null || !(shot.level() instanceof ServerLevel level)) {
            return;
        }
        JsonObject options = context.options();
        int count = Math.max(0, HookOptions.intOf(options, KEY_COUNT, DEFAULT_COUNT));
        float spread = HookOptions.floatOf(options, KEY_SPREAD_DEGREES, DEFAULT_SPREAD_DEGREES);
        CompoundTag saved = new CompoundTag();
        shot.saveWithoutId(saved);
        for (int index = 0; index < count; index++) {
            Entity copy = shot.getType().create(level);
            if (copy == null) {
                return;
            }
            copy.load(saved);
            copy.setUUID(Mth.createInsecureUUID(level.getRandom()));
            double offset = (index - (count - 1) / HALF) * spread;
            double turn = (offset == 0.0D ? spread : offset) * Mth.DEG_TO_RAD;
            Vec3 motion = shot.getDeltaMovement().yRot((float) turn);
            copy.setDeltaMovement(motion);
            if (copy instanceof AbstractArrow arrow) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            }
            ProjectileUpkeep.trackLike(shot, copy);
            ProjectileCopies.mark(copy);
            level.addFreshEntity(copy);
            Customs.spawned(CustomScopes.PROJECTILE, ProjectileUpkeep.boxFor(copy),
                    context.owner(), context.owner(), copy);
        }
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                HookOptions.intOf(options, KEY_COUNT, DEFAULT_COUNT),
                StyleText.formatCompact(HookOptions.floatOf(options, KEY_SPREAD_DEGREES,
                        DEFAULT_SPREAD_DEGREES))));
    }
}
