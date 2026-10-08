package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.equipment.EquipmentActivation;
import net.pixeldreamstudios.combat_styles.equipment.UseEvent;

import java.util.List;
import java.util.Set;

public final class GazeRitualHook implements CustomHook {

    private static final int ONE_EYE = 1;
    private static final float FOCUS_VOLUME = 0.7F;
    private static final float FOCUS_PITCH = 1.3F;

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.ON_HIT, CustomScopes.EQUIPMENT_EFFECT, CustomScopes.TRIGGERED_EFFECT);
    }

    @Override
    public List<OptionField> options() {
        return GazeTuning.OPTIONS;
    }

    @Override
    public void onApply(CustomContext context) {
        UseEvent use = EquipmentActivation.currentUse();
        Player player = context.owner();
        if (use == null || player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Item item = use.weapon().getItem();
        GazeTuning tuning = GazeTuning.of(context.options());
        switch (use.phase()) {
            case START -> play(level, player, SoundEvents.WARDEN_SONIC_CHARGE);
            case CHANNEL -> channel(level, player, item, use.heldTicks(), tuning);
            case RELEASE -> release(level, player, item, tuning);
            default -> {
            }
        }
    }

    private static void channel(ServerLevel level, Player player, Item item, int elapsed,
                                GazeTuning tuning) {
        int gathered = GazeRitual.tick(level, player, elapsed, tuning);
        if (!GazeRitual.complete(player, gathered)) {
            GazeRitual.unsettle(player);
            return;
        }
        if (GazeRitual.settle(player, tuning)) {
            grant(level, player, item, tuning);
            player.stopUsingItem();
        }
    }

    private static void release(ServerLevel level, Player player, Item item, GazeTuning tuning) {
        if (player.getCooldowns().isOnCooldown(item)) {
            return;
        }
        int gathered = BeholderEyes.crownSize(player);
        int owned = BeholderEyes.ownedEyesOf(player).size();
        int floor = Math.min(tuning.minCrown(), Math.max(ONE_EYE, owned));
        if (gathered < floor) {
            GazeRitual.release(player);
            play(level, player, SoundEvents.AMETHYST_BLOCK_BREAK);
            return;
        }
        grant(level, player, item, tuning);
    }

    private static void grant(ServerLevel level, Player player, Item item, GazeTuning tuning) {
        int gathered = BeholderEyes.crownSize(player);
        GazeRitual.release(player);
        if (gathered <= 0) {
            return;
        }
        GazeRitual.shockwave(level, player, gathered, tuning);
        Gazing.begin(player, gathered);
        player.getCooldowns().addCooldown(item, tuning.cooldownTicks());
        play(level, player, SoundEvents.WARDEN_SONIC_BOOM);
    }

    private static void play(ServerLevel level, Player player, SoundEvent sound) {
        level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS,
                FOCUS_VOLUME, FOCUS_PITCH);
    }
}
