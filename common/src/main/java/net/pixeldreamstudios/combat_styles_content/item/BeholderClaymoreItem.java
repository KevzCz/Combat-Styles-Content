package net.pixeldreamstudios.combat_styles_content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import net.pixeldreamstudios.combat_styles.item.TooltipContextAccess;
import net.pixeldreamstudios.combat_styles.affix.AffixRegistry;
import net.pixeldreamstudios.combat_styles.affix.StyleAffix;
import net.pixeldreamstudios.combat_styles_content.effect.GazingEffect;
import net.pixeldreamstudios.combat_styles_content.entity.BeholderEyes;
import net.pixeldreamstudios.combat_styles_content.entity.Convergence;
import net.pixeldreamstudios.combat_styles_content.entity.GazeRitual;
import net.pixeldreamstudios.combat_styles_content.entity.Gazing;
import net.pixeldreamstudios.combat_styles.style.ClientHolder;
import net.pixeldreamstudios.combat_styles.style.InnateAffixes;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;

public class BeholderClaymoreItem extends SwordItem {

    private static final String KEY_SUBTITLE = "item.combat_styles_content.beholder_claymore.subtitle";
    private static final String KEY_HEADER = "item.combat_styles_content.beholder_claymore.header";
    private static final String KEY_ABILITY = "item.combat_styles_content.beholder_claymore.ability_";
    private static final String KEY_VARIANT_ECLIPSE =
            "item.combat_styles_content.beholder_claymore.variant_eclipse";
    private static final String KEY_VARIANT_SUPERNOVA =
            "item.combat_styles_content.beholder_claymore.variant_supernova";
    private static final String KEY_BOUND = "item.combat_styles_content.beholder_claymore.bound";
    private static final String KEY_CONVERGE = "item.combat_styles_content.beholder_claymore.converge";
    private static final String KEY_GAZED_EFFECT = "item.combat_styles_content.beholder_claymore.gazed_effect";
    private static final String KEY_FOCUS = "item.combat_styles_content.beholder_claymore.focus";
    private static final String KEY_FOCUS_DESC = "item.combat_styles_content.beholder_claymore.focus_desc";
    private static final String KEY_GAZING_DESC = "item.combat_styles_content.beholder_claymore.gazing_desc";
    private static final String KEY_EXPAND = "tooltip.combat_styles.expand_hint";

    private static final int SECOND = 20;
    private static final int FOCUS_COOLDOWN = 400;
    private static final int CHANNEL_TICKS = 72000;
    private static final int MIN_CROWN = 2;

    private int settled;
    private static final float FOCUS_VOLUME = 0.7F;
    private static final float FOCUS_PITCH = 1.3F;

    private static final String TOP = "╭─────────────────────────╮";
    private static final String BOTTOM = "╰─────────────────────────╯";
    private static final String EDGE = "│ ";
    private static final String PIP = "◈";
    private static final String STEP = "├─ ";
    private static final String TRAIL = "   ";
    private static final char BRACKET_OPEN = 0x5B;
    private static final char BRACKET_CLOSE = 0x5D;
    private static final String SEPARATOR = " · ";
    private static final ChatFormatting ACCENT = ChatFormatting.AQUA;
    private static final ChatFormatting NAME = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting NUMERAL = ChatFormatting.GOLD;
    private static final ChatFormatting FRAME = ChatFormatting.DARK_GRAY;
    private static final ChatFormatting BODY = ChatFormatting.GRAY;
    public BeholderClaymoreItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot,
                              boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (selected || player.getOffhandItem() == stack) {
            BeholderEyes.keepAlive(player);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(held);
        }
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE,
                    SoundSource.PLAYERS, FOCUS_VOLUME, FOCUS_PITCH);
        }
        return InteractionResultHolder.consume(held);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity holder) {
        return CHANNEL_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity holder, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(holder instanceof Player player)) {
            return;
        }
        int elapsed = CHANNEL_TICKS - remaining;
        int gathered = GazeRitual.tick(server, player, elapsed);

        if (!GazeRitual.complete(player, gathered)) {
            settled = 0;
            return;
        }
        if (++settled >= GazeRitual.SETTLE_TICKS) {
            settled = 0;
            grant(level, player);
            player.stopUsingItem();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity holder) {
        release(stack, level, holder, 0);
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity holder, int remaining) {
        release(stack, level, holder, remaining);
    }

    private void release(ItemStack stack, Level level, LivingEntity holder, int remaining) {
        settled = 0;
        if (level.isClientSide() || !(holder instanceof Player player)) {
            return;
        }
        int gathered = BeholderEyes.crownSize(player);
        int owned = BeholderEyes.ownedEyesOf(player).size();
        int floor = Math.min(MIN_CROWN, Math.max(1, owned));

        if (gathered < floor) {
            GazeRitual.release(player);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundSource.PLAYERS, FOCUS_VOLUME, FOCUS_PITCH);
            return;
        }
        grant(level, player);
    }

    private void grant(Level level, Player player) {
        int gathered = BeholderEyes.crownSize(player);
        GazeRitual.release(player);
        if (gathered <= 0) {
            return;
        }
        Gazing.begin(player, gathered);
        player.getCooldowns().addCooldown(this, FOCUS_COOLDOWN);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, FOCUS_VOLUME, FOCUS_PITCH);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        int slots = InnateAffixes.slotCount(stack);
        if (slots <= 0) {
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.literal(PIP + " ").withStyle(ACCENT)
                .append(Component.translatable(KEY_SUBTITLE)
                        .withStyle(ACCENT, ChatFormatting.BOLD)));

        if (!TooltipContextAccess.expanded()) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal(EDGE).withStyle(FRAME)
                    .append(namesOf(stack, slots).withStyle(FRAME)));
            tooltip.add(Component.translatable(KEY_EXPAND).withStyle(FRAME));
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.literal(TOP).withStyle(FRAME));
        tooltip.add(Component.literal(EDGE).withStyle(FRAME)
                .append(Component.translatable(KEY_HEADER).withStyle(NUMERAL)));

        for (int slot = 0; slot < slots; slot++) {
            tooltip.add(Component.literal(EDGE + STEP).withStyle(FRAME)
                    .append(abilityLine(KEY_ABILITY + (slot + 1))));
        }
        tooltip.add(Component.literal(EDGE + TRAIL).withStyle(FRAME)
                .append(abilityLine(KEY_VARIANT_ECLIPSE)));
        tooltip.add(Component.literal(EDGE + TRAIL).withStyle(FRAME)
                .append(abilityLine(KEY_VARIANT_SUPERNOVA)));

        tooltip.add(Component.literal(EDGE).withStyle(FRAME));
        tooltip.add(Component.literal(EDGE).withStyle(FRAME)
                .append(Component.translatable(KEY_FOCUS).withStyle(ACCENT)));
        tooltip.add(Component.literal(EDGE + "  ").withStyle(FRAME)
                .append(Component.translatable(KEY_FOCUS_DESC).withStyle(BODY)));
        tooltip.add(Component.literal(EDGE + "  ").withStyle(FRAME)
                .append(Component.translatable(KEY_GAZING_DESC,
                        (int) (GazingEffect.soulBonus() * 100),
                        StyleText.formatDecimal(Gazing.durationTicks() / (double) SECOND))
                        .withStyle(BODY)));
        tooltip.add(Component.literal(EDGE + "  ").withStyle(FRAME)
                .append(bracketed(Component.translatable(KEY_GAZED_EFFECT,
                        (int) (Convergence.perStack() * 100),
                        Convergence.maxStacks(),
                        Convergence.durationTicks() / SECOND))));

        appendAttunement(tooltip);
        tooltip.add(Component.literal(BOTTOM).withStyle(FRAME));
    }

    private static void appendAttunement(List<Component> tooltip) {
        Player player = ClientHolder.localPlayer();
        if (player == null || !BeholderEyes.attuned(player)) {
            return;
        }
        tooltip.add(Component.literal(EDGE).withStyle(FRAME));
        tooltip.add(Component.literal(EDGE).withStyle(FRAME)
                .append(Component.translatable(KEY_BOUND,
                        StyleText.formatDecimal(BeholderEyes.attunedBonus(player)))
                        .withStyle(NAME)));
        tooltip.add(Component.literal(EDGE).withStyle(FRAME)
                .append(Component.translatable(KEY_CONVERGE).withStyle(NAME)));
    }

    private static Component abilityLine(String key) {
        return bracketed(Component.translatable(key));
    }

    private static Component bracketed(Component source) {
        String text = source.getString();
        MutableComponent line = Component.empty();
        int cursor = 0;
        while (cursor < text.length()) {
            int open = text.indexOf(BRACKET_OPEN, cursor);
            int close = open < 0 ? -1 : text.indexOf(BRACKET_CLOSE, open);
            if (open < 0 || close < 0) {
                line.append(Component.literal(text.substring(cursor)).withStyle(BODY));
                break;
            }
            line.append(Component.literal(text.substring(cursor, open)).withStyle(BODY));
            line.append(Component.literal(text.substring(open, close + 1)).withStyle(NAME));
            cursor = close + 1;
        }
        return line;
    }

    private static StyleAffix affixAt(ItemStack stack, int slot) {
        String affixId = InnateAffixes.affixFor(stack, slot);
        return affixId == null ? null : AffixRegistry.get(affixId);
    }

    private static MutableComponent nameOf(StyleAffix affix) {
        return Component.translatableWithFallback(affix.nameKey(), affix.fallbackName());
    }

    private static MutableComponent namesOf(ItemStack stack, int slots) {
        MutableComponent line = Component.empty();
        for (int slot = 0; slot < slots; slot++) {
            StyleAffix affix = affixAt(stack, slot);
            if (affix == null) {
                continue;
            }
            if (!line.getSiblings().isEmpty()) {
                line.append(Component.literal(SEPARATOR));
            }
            line.append(nameOf(affix));
        }
        return line;
    }
}
