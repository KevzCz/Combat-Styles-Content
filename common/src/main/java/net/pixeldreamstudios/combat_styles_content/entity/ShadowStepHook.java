package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;

import java.util.List;
import java.util.Set;

public final class ShadowStepHook implements CustomHook {

    private static final double STEP_BACK = 0.6D;
    private static final float SOUND_VOLUME = 0.6F;
    private static final float SOUND_PITCH = 1.3F;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.shadow_step";

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public void onHit(CustomContext context, Entity source, LivingEntity target) {
        if (!ProjectileStrikes.striking()) {
            step(context.owner(), source);
        }
    }

    @Override
    public void onLand(CustomContext context, Entity entity) {
        step(context.owner(), entity);
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP));
    }

    private static void step(Player owner, Entity star) {
        if (owner == null || star == null || star.level() != owner.level() || star.level().isClientSide()) {
            return;
        }
        Vec3 heading = star.getDeltaMovement();
        Vec3 landing = heading.lengthSqr() > 0.0D
                ? star.position().subtract(heading.normalize().scale(STEP_BACK))
                : star.position();
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        owner.teleportTo(landing.x, landing.y, landing.z);
        owner.resetFallDistance();
        owner.level().playSound(null, landing.x, landing.y, landing.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }
}
