package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.pixeldreamstudios.combat_styles_content.item.ContentItems;
import net.pixeldreamstudios.combat_styles.entity.SummonRoster;
import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.effect.EffectLookup;
import net.pixeldreamstudios.combat_styles.equipment.ProfileCondition;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfiles;
import net.pixeldreamstudios.combat_styles.equipment.SummonReplacement;
import net.pixeldreamstudios.combat_styles.equipment.WornItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeholderEyes {
    public static final int EYE_COUNT = 1;

    public static int anchoredCount(Player player) {
        SummonProfile profile = SummonProfiles.profileFor(player);
        return profile == null
                ? EYE_COUNT
                : Math.max(1, profile.resolve(player, CONTEXT).anchoredOr(EYE_COUNT));
    }

    public static final int MAX_TEMPORARY = 2;

    public static final int MAX_TEMPORARY_ATTUNED = 6;

    private static final ProfileCondition.ConditionContext CONTEXT =
            new ProfileCondition.ConditionContext() {
                @Override
                public boolean hasEffect(Player player, String effectId) {
                    Holder<MobEffect> holder = EffectLookup.of(effectId);
                    return holder != null && player.hasEffect(holder);
                }

                @Override
                public boolean holding(Player player, String itemId) {
                    return BeholderEyes.holding(player, itemId);
                }

                @Override
                public boolean wearing(Player player, String itemId) {
                    return BeholderEyes.wearing(player, itemId);
                }
            };

    public static SummonProfile.Resolved resolvedFor(Player player) {
        if (player == null) {
            return null;
        }
        long now = player.level().getGameTime();
        Cached cached = RESOLVED.get(player.getUUID());
        if (cached != null && cached.tick() == now) {
            return cached.resolved();
        }
        SummonProfile profile = SummonProfiles.profileFor(player);
        SummonProfile.Resolved fresh = profile == null
                ? null
                : profile.resolve(player, CONTEXT);
        RESOLVED.put(player.getUUID(), new Cached(now, fresh));
        return fresh;
    }

    private record Cached(long tick, SummonProfile.Resolved resolved) {
    }

    private static final Map<UUID, Cached> RESOLVED = new ConcurrentHashMap<>();

    private static boolean holding(Player player, String itemId) {
        return itemId != null
                && (matches(player.getMainHandItem(), itemId)
                || matches(player.getOffhandItem(), itemId));
    }

    private static boolean wearing(Player player, String itemId) {
        if (itemId == null) {
            return false;
        }
        for (ItemStack worn : WornItems.of(player)) {
            if (matches(worn, itemId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.toString().equals(itemId);
    }

    public static boolean replacesSummons(Player player) {
        return SummonProfiles.replacementFor(player) != null;
    }

    public static int maxTemporary(Player player) {
        return Math.max(0, maxEyes(player) - anchoredCount(player));
    }

    public static int maxEyes(Player player) {
        SummonProfile profile = SummonProfiles.profileFor(player);
        if (profile != null) {
            return profile.resolve(player, CONTEXT).maxActiveOr(MAX_TEMPORARY_ATTUNED);
        }
        SummonReplacement replacement = SummonProfiles.replacementFor(player);
        if (replacement == null) {
            return MAX_TEMPORARY + EYE_COUNT;
        }
        return replacement.maxActive().orElse(MAX_TEMPORARY_ATTUNED);
    }

    public static String substituteAbility(Player player, int slot) {
        SummonProfile profile = SummonProfiles.profileFor(player);
        if (profile != null) {
            String declared = profile.resolve(player, CONTEXT).abilityFor(slot);
            if (declared != null) {
                return declared;
            }
        }
        SummonReplacement replacement = SummonProfiles.replacementFor(player);
        if (replacement != null) {
            String declared = replacement.abilityFor(slot);
            if (declared != null) {
                return declared;
            }
        }
        List<String> pool = attuned(player) ? ATTUNED_ABILITIES : SUBSTITUTE_ABILITIES;
        return pool.get(Math.floorMod(slot, pool.size()));
    }

    private static final List<String> SUBSTITUTE_ABILITIES = List.of(
            EyeAbility.TWIN_LANCE.name(),
            EyeAbility.WIDE_CUT.name(),
            EyeAbility.BLINK_WATCH.name());

    private static final List<String> ATTUNED_ABILITIES = List.of(
            EyeAbility.TWIN_LANCE.name(),
            EyeAbility.FURROW.name(),
            EyeAbility.WIDE_CUT.name(),
            EyeAbility.TETHER.name(),
            EyeAbility.BLINK_WATCH.name(),
            EyeAbility.COLLAPSE.name());

    public static boolean attuned(Player player) {
        if (player == null) {
            return false;
        }
        SummonProfile profile = SummonProfiles.profileFor(player);
        if (profile != null && !profile.variants().isEmpty()) {
            return profile.resolve(player, CONTEXT).varied();
        }
        return replacesSummons(player) && holdingClaymore(player);
    }

    public static float damageBonus(Player player) {
        SummonProfile profile = SummonProfiles.profileFor(player);
        if (profile != null) {
            return profile.resolve(player, CONTEXT).damageScale();
        }
        SummonReplacement replacement = SummonProfiles.replacementFor(player);
        if (replacement == null) {
            return 1.0F;
        }
        return attuned(player) ? ATTUNED_DAMAGE : replacement.damageScale();
    }

    private static final float ATTUNED_DAMAGE = 1.6F;

    public static float attunedBonus(Player player) {
        SummonProfile.Resolved resolved = resolvedFor(player);
        return resolved != null ? resolved.damageScale() : ATTUNED_DAMAGE;
    }

    public static int temporaryCount(Player player) {
        int found = 0;
        for (BeholderEyeEntity eye : ownedEyesOf(player)) {
            if (eye.temporary()) {
                found++;
            }
        }
        return found;
    }

    private static final int HOLD_GRACE_TICKS = 4;
    private static final int RESTOCK_INTERVAL = 40;
    private static final float FULL_TURN = (float) (Math.PI * 2.0D);

    private record Held(long lastSeen) {
    }

    private static final Map<UUID, Held> HOLDING = new ConcurrentHashMap<>();

    private BeholderEyes() {
    }

    public static void forgetAll() {
        RESOLVED.clear();
    }

    public static void forget(UUID playerId) {
        HOLDING.remove(playerId);
        RESOLVED.remove(playerId);
    }

    public static boolean holdingClaymore(Player player) {
        return isClaymore(player.getMainHandItem()) || isClaymore(player.getOffhandItem());
    }

    private static boolean isClaymore(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ContentItems.BEHOLDER_CLAYMORE.get());
    }

    public static void keepAlive(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        long now = player.level().getGameTime();
        Held previous = HOLDING.put(player.getUUID(), new Held(now));
        boolean resumed = previous == null || now - previous.lastSeen() > HOLD_GRACE_TICKS;
        if (resumed || now % RESTOCK_INTERVAL == 0L) {
            spawnMissing(player);
        }
    }

    public static boolean stillHolding(Player player) {
        Held held = HOLDING.get(player.getUUID());
        if (held == null) {
            return false;
        }
        if (player.level().getGameTime() - held.lastSeen() > HOLD_GRACE_TICKS) {
            HOLDING.remove(player.getUUID());
            return false;
        }
        return true;
    }

    private static void spawnMissing(Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        List<BeholderEyeEntity> existing = livingEyesOf(player);
        int wanted = anchoredCount(player);
        int missing = wanted - existing.size();
        if (missing <= 0) {
            return;
        }
        for (int i = 0; i < missing; i++) {
            BeholderEyeEntity eye = ContentEntities.BEHOLDER_EYE.get().create(server);
            if (eye == null) {
                return;
            }
            float offset = FULL_TURN * ((existing.size() + i) / (float) wanted);
            eye.assign(player, offset);
            server.addFreshEntity(eye);
        }
    }

    public static String swapKeyFor(Player player, String wanted) {
        SummonProfile.Resolved resolved = wanted == null ? null : resolvedFor(player);
        return resolved == null ? wanted : resolved.swap(AbilityConfig.key(wanted));
    }

    public static EyeAbility swapFor(Player player, EyeAbility wanted) {
        SummonProfile.Resolved resolved = wanted == null ? null : resolvedFor(player);
        if (resolved == null) {
            return wanted;
        }
        EyeAbility swapped = EyeAbility.byName(
                resolved.swap(wanted.key()));
        return swapped.acting() ? swapped : wanted;
    }

    public static EyeAbility finaleFor(Player player) {
        SummonProfile.Resolved resolved = resolvedFor(player);
        EyeAbility wanted = EyeAbility.byName(resolved == null ? null : resolved.finale());
        return wanted.acting() ? wanted : EyeAbility.COLLAPSE;
    }

    public static int crownSize(Player player) {
        int found = 0;
        for (BeholderEyeEntity eye : ownedEyesOf(player)) {
            if (eye.inCrown()) {
                found++;
            }
        }
        return found;
    }

    public static List<BeholderEyeEntity> ownedEyesOf(Player player) {
        return SummonRoster.ofType(player, BeholderEyeEntity.class);
    }

    private static List<BeholderEyeEntity> livingEyesOf(Player player) {
        List<BeholderEyeEntity> found = new ArrayList<>();
        for (BeholderEyeEntity eye : ownedEyesOf(player)) {
            if (!eye.temporary() && !(eye instanceof GreatEyeEntity)) {
                found.add(eye);
            }
        }
        return found;
    }
}
