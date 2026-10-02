package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.pixeldreamstudios.combat_styles.entity.SummonStance;
import net.pixeldreamstudios.combat_styles.affix.AffixTargets;
import net.pixeldreamstudios.combat_styles_content.config.ContentConfig;

import java.util.List;

public class GreatEyeEntity extends BeholderEyeEntity {
    public static final float SCALE = 1.9F;

    private static final int DECISION_INTERVAL = 40;
    private static final double TARGET_RANGE = 20.0D;
    private static final int FURROW_HITS_PER_STACK = 3;

    private static final List<String> ROSTER = List.of(
            "combat_styles_content:twin_lance",
            "combat_styles_content:blink_watch",
            "combat_styles_content:furrow",
            "combat_styles_content:tether",
            "combat_styles_content:wide_cut");

    private int furrowHits;

    public GreatEyeEntity(EntityType<? extends BeholderEyeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public float renderScale() {
        return BeholderEyeEntity.RENDER_SCALE * SCALE;
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
    protected boolean sustainedBy(Player owner) {
        return GreatEyeFlock.wearing(owner);
    }

    @Override
    protected boolean usesFinale() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        Player owner = owner();
        if (owner == null || !owner.isAlive()) {
            return;
        }
        if (acting() || level().getGameTime() % DECISION_INTERVAL != 0L) {
            return;
        }
        LivingEntity mark = markFor(owner);
        if (mark == null) {
            return;
        }

        configure(owner, mark, (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE), 0);
        acceptAbility(ROSTER.get(random.nextInt(ROSTER.size())));
    }

    private LivingEntity markFor(Player owner) {
        LivingEntity struck = owner.getLastHurtMob();
        if (usable(owner, struck)) {
            return struck;
        }
        LivingEntity aggressor = owner.getLastHurtByMob();
        return usable(owner, aggressor) ? aggressor : null;
    }

    private boolean usable(Player owner, LivingEntity candidate) {
        return candidate != null
                && candidate.isAlive()
                && candidate != owner
                && candidate.distanceToSqr(this) <= TARGET_RANGE * TARGET_RANGE
                && AffixTargets.canHit(owner, candidate);
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

    public static int wanted(Player player) {
        return player == null || !ContentConfig.skulls().skullsFollow ? 0 : 1;
    }
}
