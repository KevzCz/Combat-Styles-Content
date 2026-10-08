package net.pixeldreamstudios.combat_styles_content.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public final class ContentEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(CombatStylesContent.MOD_ID, Registries.ENTITY_TYPE);

    private static final int UPDATE_INTERVAL = 1;

    private static final float WRAITH_WIDTH = 0.4F;
    private static final float WRAITH_HEIGHT = 0.8F;
    private static final int WRAITH_TRACKING = 8;

    public static final RegistrySupplier<EntityType<WraithEntity>> WRAITH =
            ENTITIES.register("wraith", () -> EntityType.Builder
                    .<WraithEntity>of(WraithEntity::new, MobCategory.MISC)
                    .sized(WRAITH_WIDTH, WRAITH_HEIGHT)
                    .clientTrackingRange(WRAITH_TRACKING)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":wraith"));

    private static final float EYE_SIZE = 0.6F;
    private static final int EYE_TRACKING = 12;

    public static final RegistrySupplier<EntityType<GreatEyeEntity>> GREAT_EYE =
            ENTITIES.register("great_eye", () -> EntityType.Builder
                    .<GreatEyeEntity>of(GreatEyeEntity::new, MobCategory.MISC)
                    .sized(EYE_SIZE * GreatEyeEntity.SCALE, EYE_SIZE * GreatEyeEntity.SCALE)
                    .clientTrackingRange(EYE_TRACKING)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":great_eye"));

    public static final RegistrySupplier<EntityType<BeholderEyeEntity>> BEHOLDER_EYE =
            ENTITIES.register("beholder_eye", () -> EntityType.Builder
                    .<BeholderEyeEntity>of(BeholderEyeEntity::new, MobCategory.MISC)
                    .sized(EYE_SIZE, EYE_SIZE)
                    .clientTrackingRange(EYE_TRACKING)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":beholder_eye"));

    private static final float SKULL_SIZE = 0.5F;
    private static final int SKULL_TRACKING = 12;

    public static final RegistrySupplier<EntityType<KamikazeSkullEntity>> KAMIKAZE_SKULL =
            ENTITIES.register("kamikaze_skull", () -> EntityType.Builder
                    .<KamikazeSkullEntity>of(KamikazeSkullEntity::new, MobCategory.MISC)
                    .sized(SKULL_SIZE, SKULL_SIZE)
                    .clientTrackingRange(SKULL_TRACKING)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":kamikaze_skull"));

    private static final float SHURIKEN_SIZE = 0.5F;
    private static final int SHURIKEN_TRACKING = 4;
    private static final int SHURIKEN_UPDATE_INTERVAL = 10;

    public static final RegistrySupplier<EntityType<ShurikenEntity>> SHURIKEN =
            ENTITIES.register("shuriken_projectile", () -> EntityType.Builder
                    .<ShurikenEntity>of(ShurikenEntity::new, MobCategory.MISC)
                    .sized(SHURIKEN_SIZE, SHURIKEN_SIZE)
                    .clientTrackingRange(SHURIKEN_TRACKING)
                    .updateInterval(SHURIKEN_UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":shuriken_projectile"));

    private static final float SHARD_SIZE = 0.3F;

    public static final RegistrySupplier<EntityType<ShurikenEntity>> SHURIKEN_SHARD =
            ENTITIES.register("shuriken_shard", () -> EntityType.Builder
                    .<ShurikenEntity>of(ShurikenEntity::new, MobCategory.MISC)
                    .sized(SHARD_SIZE, SHARD_SIZE)
                    .clientTrackingRange(SHURIKEN_TRACKING)
                    .updateInterval(SHURIKEN_UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":shuriken_shard"));

    private static final float BONE_SIZE = 0.25F;

    public static final RegistrySupplier<EntityType<BoneShardEntity>> BONE_SHARD =
            ENTITIES.register("bone_shard", () -> EntityType.Builder
                    .<BoneShardEntity>of(BoneShardEntity::new, MobCategory.MISC)
                    .sized(BONE_SIZE, BONE_SIZE)
                    .clientTrackingRange(SHURIKEN_TRACKING)
                    .updateInterval(UPDATE_INTERVAL)
                    .build(CombatStylesContent.MOD_ID + ":bone_shard"));

    private ContentEntities() {
    }

    public static void register() {
        ENTITIES.register();
    }
}
