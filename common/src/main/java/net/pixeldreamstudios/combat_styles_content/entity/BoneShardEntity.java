package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles_content.item.ContentItems;

public class BoneShardEntity extends ThrowableItemProjectile {

    public static final int MAX_BOUNCES = 3;
    private static final double GRAVITY = 0.05D;
    private static final double RESTITUTION = 0.55D;
    private static final double FRICTION = 0.8D;
    private static final double REST_SPEED = 0.08D;
    private static final int MAX_LIFE_TICKS = 100;
    private static final float CARRIED_ELSEWHERE = 0.0F;
    private static final float SPIN_PER_SPEED = 55.0F;
    private static final float BOUNCE_VOLUME = 0.35F;
    private static final float BOUNCE_PITCH = 1.4F;
    private static final float BREAK_PITCH = 1.1F;
    private static final int BREAK_PARTICLES = 6;
    private static final double BREAK_SPREAD = 0.1D;
    private static final double BREAK_SPEED = 0.05D;

    private int bounces;
    private float spin;
    private float spinPrevious;

    public BoneShardEntity(EntityType<? extends BoneShardEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ContentItems.BONE_SHARD.get();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    public float spin(float partialTick) {
        return spinPrevious + (spin - spinPrevious) * partialTick;
    }

    @Override
    public void tick() {
        super.tick();
        spinPrevious = spin;
        spin += (float) getDeltaMovement().length() * SPIN_PER_SPEED;
        if (!level().isClientSide() && tickCount > MAX_LIFE_TICKS) {
            shatter();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        target.hurt(damageSources().thrown(this, getOwner()), CARRIED_ELSEWHERE);
        if (!level().isClientSide()) {
            shatter();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide()) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        if (bounces >= MAX_BOUNCES || motion.length() < REST_SPEED) {
            shatter();
            return;
        }
        bounces++;
        Direction face = result.getDirection();
        Vec3 bounced = switch (face.getAxis()) {
            case X -> new Vec3(-motion.x * RESTITUTION, motion.y * FRICTION, motion.z * FRICTION);
            case Y -> new Vec3(motion.x * FRICTION, -motion.y * RESTITUTION, motion.z * FRICTION);
            case Z -> new Vec3(motion.x * FRICTION, motion.y * FRICTION, -motion.z * RESTITUTION);
        };
        Vec3 hit = result.getLocation();
        setPos(hit.x + face.getStepX() * getBbWidth(), hit.y + face.getStepY() * getBbHeight(),
                hit.z + face.getStepZ() * getBbWidth());
        setDeltaMovement(bounced);
        hasImpulse = true;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_STEP, SoundSource.PLAYERS,
                BOUNCE_VOLUME, BOUNCE_PITCH);
    }

    private void shatter() {
        if (level() instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()), getX(), getY(), getZ(),
                    BREAK_PARTICLES, BREAK_SPREAD, BREAK_SPREAD, BREAK_SPREAD, BREAK_SPEED);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HURT, SoundSource.PLAYERS,
                    BOUNCE_VOLUME, BREAK_PITCH);
        }
        discard();
    }
}
