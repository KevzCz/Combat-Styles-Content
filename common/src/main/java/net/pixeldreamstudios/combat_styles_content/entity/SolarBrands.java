package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.world.entity.LivingEntity;
import net.pixeldreamstudios.combat_styles.damage.DamageSpec;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SolarBrands {

    public record Brand(LivingEntity target, DamageSpec bonus, float turn, double range,
                        long expiresAt) {
    }

    private static final Map<UUID, Brand> BRANDS = new ConcurrentHashMap<>();

    private SolarBrands() {
    }

    public static void brand(LivingEntity owner, Brand brand) {
        if (owner != null && !owner.level().isClientSide() && brand != null) {
            BRANDS.put(owner.getUUID(), brand);
        }
    }

    public static Brand of(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) {
            return null;
        }
        Brand brand = BRANDS.get(owner.getUUID());
        if (brand == null) {
            return null;
        }
        LivingEntity target = brand.target();
        if (!target.isAlive() || target.isRemoved() || target.level() != owner.level()
                || owner.level().getGameTime() > brand.expiresAt()) {
            BRANDS.remove(owner.getUUID(), brand);
            return null;
        }
        return brand;
    }

    public static void clear(LivingEntity owner, LivingEntity target) {
        if (owner == null) {
            return;
        }
        Brand brand = BRANDS.get(owner.getUUID());
        if (brand != null && brand.target() == target) {
            BRANDS.remove(owner.getUUID(), brand);
        }
    }
}
