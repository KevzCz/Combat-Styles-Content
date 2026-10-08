package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.pixeldreamstudios.combat_styles.affix.ProjectileUpkeep;
import net.pixeldreamstudios.combat_styles.api.CombatScheduler;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.custom.Customs;
import net.pixeldreamstudios.combat_styles.equipment.ProjectileCopies;
import net.pixeldreamstudios.combat_styles_content.enchantment.ContentEnchantments;

public final class Volley {

    public static final float CHANCE_PER_LEVEL = 0.25F;
    public static final int DELAY_TICKS = 4;
    private static final String LABEL = "volley";

    private Volley() {
    }

    public static void roll(Player owner, ShurikenEntity shot) {
        if (!(shot.level() instanceof ServerLevel level)) {
            return;
        }
        int enchant = ContentEnchantments.levelOf(level, ContentEnchantments.VOLLEY, shot.weapon());
        if (enchant <= 0 || level.getRandom().nextFloat() >= enchant * CHANCE_PER_LEVEL) {
            return;
        }
        CompoundTag saved = new CompoundTag();
        shot.saveWithoutId(saved);
        Entity created = shot.getType().create(level);
        if (!(created instanceof ShurikenEntity copy)) {
            return;
        }
        copy.load(saved);
        copy.setUUID(Mth.createInsecureUUID(level.getRandom()));
        ProjectileCopies.mark(copy);
        copy.arm(shot.weapon());
        ProjectileUpkeep.trackLike(shot, copy);
        CombatScheduler.after(owner, DELAY_TICKS, LABEL, () -> {
            level.addFreshEntity(copy);
            Customs.spawned(CustomScopes.PROJECTILE, ProjectileUpkeep.boxFor(copy), owner, owner, copy);
        });
    }
}
