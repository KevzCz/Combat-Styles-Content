package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles_content.item.ContentItems;
import net.pixeldreamstudios.combat_styles.entity.SummonCompanion;
import net.pixeldreamstudios.combat_styles.entity.SummonRoster;
import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.effect.EffectLookup;
import net.pixeldreamstudios.combat_styles.equipment.ProfileCondition;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfile;
import net.pixeldreamstudios.combat_styles.equipment.SummonProfiles;
import net.pixeldreamstudios.combat_styles.equipment.WornItems;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeholderEyes {
    public static final int EYE_COUNT = 1;

    public static int anchoredCount(Player player) {
        SummonProfile.Resolved resolved = resolvedFor(player);
        if (resolved == null) {
            return EYE_COUNT;
        }
        return resolved.companion().map(SummonCompanion::count).orElse(EYE_COUNT);
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

    private static SummonProfile eyeProfile(Player player) {
        ResourceLocation eye = BuiltInRegistries.ENTITY_TYPE.getKey(ContentEntities.BEHOLDER_EYE.get());
        SummonProfile matched = eye == null ? null : SummonProfiles.profileFor(player, eye.toString());
        return matched != null ? matched : SummonProfiles.replacingProfileOf(player);
    }

    public static SummonProfile.Resolved resolvedFor(Player player) {
        if (player == null) {
            return null;
        }
        long now = player.level().getGameTime();
        Cached cached = RESOLVED.get(player.getUUID());
        if (cached != null && cached.tick() == now) {
            return cached.resolved();
        }
        SummonProfile profile = eyeProfile(player);
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
        return SummonProfiles.replacesSummons(player);
    }

    public static int maxTemporary(Player player) {
        return Math.max(0, maxEyes(player) - anchoredCount(player));
    }

    public static int maxEyes(Player player) {
        SummonProfile.Resolved resolved = resolvedFor(player);
        if (resolved == null) {
            return MAX_TEMPORARY + EYE_COUNT;
        }
        return resolved.maxActiveOr(MAX_TEMPORARY_ATTUNED);
    }

    public static boolean attuned(Player player) {
        if (player == null) {
            return false;
        }
        SummonProfile.Resolved resolved = resolvedFor(player);
        if (resolved != null && !resolved.profile().variants().isEmpty()) {
            return resolved.varied();
        }
        return replacesSummons(player) && holdingClaymore(player);
    }

    public static float damageBonus(Player player) {
        SummonProfile.Resolved resolved = resolvedFor(player);
        return resolved == null ? 1.0F : resolved.damageScale();
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

    private BeholderEyes() {
    }

    public static void forgetAll() {
        RESOLVED.clear();
    }

    public static void forget(UUID playerId) {
        RESOLVED.remove(playerId);
    }

    public static boolean holdingClaymore(Player player) {
        return isClaymore(player.getMainHandItem()) || isClaymore(player.getOffhandItem());
    }

    private static boolean isClaymore(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ContentItems.BEHOLDER_CLAYMORE.get());
    }

    public static String swapKeyFor(Player player, String wanted) {
        SummonProfile.Resolved resolved = wanted == null ? null : resolvedFor(player);
        return resolved == null ? wanted : resolved.swap(AbilityConfig.key(wanted));
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
}
