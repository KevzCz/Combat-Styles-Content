package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.entity.SummonStance;


public class GreatEyeEntity extends BeholderEyeEntity {
    public static final float SCALE = 1.9F;
    private static final double SLAB_HEIGHT = 0.5D;

    private static final int FURROW_HITS_PER_STACK = 3;

    private int furrowHits;

    public GreatEyeEntity(EntityType<? extends BeholderEyeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public float renderScale() {
        return BeholderEyeEntity.RENDER_SCALE * SCALE;
    }

    @Override
    protected double extraBeamLift() {
        return SLAB_HEIGHT;
    }

    @Override
    public SummonStance stance() {
        return SummonStance.INDEPENDENT;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    protected boolean usesFinale() {
        return false;
    }

    @Override
    protected float recastDamage(Player owner) {
        return (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public void onAbilityHit(String ability, LivingEntity victim) {
        Player owner = owner();
        if (owner == null || victim == null || level().isClientSide()) {
            return;
        }
        if (ability != null && ability.endsWith("furrow")) {
            furrowHits++;
            if (furrowHits % FURROW_HITS_PER_STACK != 0) {
                return;
            }
        }
        Convergence.apply(owner, victim, null, 1);
    }

    @Override
    protected Vec3 chooseHeading(Player owner) {
        Vec3 toTarget = headingToTarget();
        if (toTarget != null) {
            return toTarget;
        }
        double angle = random.nextDouble() * Math.PI * 2.0D;
        return new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
    }

    @Override
    public void stopActing() {
        super.stopActing();
        furrowHits = 0;
    }
}
