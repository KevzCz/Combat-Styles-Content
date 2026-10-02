package net.pixeldreamstudios.combat_styles_content.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles_content.item.ContentItems;


public final class GazeChannelPose {

    private static final float RISE_TICKS = 16.0F;
    private static final float SETTLE_TICKS = 28.0F;

    private static final float OUT_FORWARD = 0.55F;
    private static final float OUT_UP = 0.30F;
    private static final float OUT_INWARD = 0.34F;

    private static final float UPRIGHT_DEGREES = 92.0F;
    private static final float LEAN_DEGREES = 12.0F;

    private static final float BOB_SPEED = 0.055F;
    private static final float BOB_AMOUNT = 0.028F;
    private static final float DRIFT_SPEED = 0.031F;
    private static final float DRIFT_DEGREES = 3.5F;

    private static final float HAND_DROP = 0.18F;
    private static final float HAND_OUT = 0.10F;
    private static final float HAND_TURN = 26.0F;

    private static final float FULL = 1.0F;
    private static final float HALF = 0.5F;

    private GazeChannelPose() {
    }

    public static boolean channeling(Player player, ItemStack stack) {
        return player != null
                && stack != null
                && !stack.isEmpty()
                && stack.is(ContentItems.BEHOLDER_CLAYMORE.get())
                && player.isUsingItem()
                && player.getUseItem() == stack;
    }

    private static float partialTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? FULL : minecraft.getTimer().getGameTimeDeltaPartialTick(false);
    }

    private static float age(Player player) {
        return player.tickCount + partialTick();
    }

    private static float held(Player player) {
        int used = player.getTicksUsingItem();
        return used <= 0 ? 0.0F : used + partialTick();
    }

    public static float lift(Player player) {
        return smooth(Mth.clamp(held(player) / RISE_TICKS, 0.0F, FULL));
    }

    private static float settle(Player player) {
        return smooth(Mth.clamp(held(player) / SETTLE_TICKS, 0.0F, FULL));
    }

    private static float smooth(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, FULL);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    public static void applyItem(PoseStack poses, Player player, boolean left) {
        float rise = lift(player);
        if (rise <= 0.0F) {
            return;
        }

        float side = left ? -FULL : FULL;
        float clock = age(player);
        float hover = Mth.sin(clock * BOB_SPEED) * BOB_AMOUNT * rise;

        poses.translate(-side * OUT_INWARD * rise, OUT_UP * rise + hover, -OUT_FORWARD * rise);

        poses.mulPose(Axis.XP.rotationDegrees(-UPRIGHT_DEGREES * settle(player)));
        poses.mulPose(Axis.ZP.rotationDegrees(side * LEAN_DEGREES * rise
                + Mth.sin(clock * DRIFT_SPEED) * DRIFT_DEGREES * rise));
    }

    public static void applyHand(PoseStack poses, Player player, boolean left) {
        float rise = lift(player);
        if (rise <= 0.0F) {
            return;
        }
        float side = left ? -FULL : FULL;
        poses.translate(side * HAND_OUT * rise, -HAND_DROP * rise, HAND_OUT * HALF * rise);
        poses.mulPose(Axis.XP.rotationDegrees(-HAND_TURN * rise));
    }
}
