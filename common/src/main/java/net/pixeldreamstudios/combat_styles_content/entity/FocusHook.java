package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.style.StyleText;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FocusHook implements CustomHook {

    public static final String KEY_HITS = "hits";
    public static final String KEY_BONUS = "bonus";
    public static final String KEY_WINDOW_TICKS = "window_ticks";
    private static final int DEFAULT_HITS = 3;
    private static final float DEFAULT_BONUS = 0.5F;
    private static final int DEFAULT_WINDOW_TICKS = 10;
    private static final double PERCENT = 100.0D;
    private static final String KEY_TOOLTIP = "tooltip.combat_styles_content.focus";

    private record Streak(long startedAt, int hits) {
    }

    private record Pair(UUID owner, UUID target) {
    }

    private static final Map<Pair, Streak> STREAKS = new ConcurrentHashMap<>();

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_HITS, Codec.INT, DEFAULT_HITS),
            OptionField.of(KEY_BONUS, Codec.FLOAT, DEFAULT_BONUS),
            OptionField.of(KEY_WINDOW_TICKS, Codec.INT, DEFAULT_WINDOW_TICKS));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public float modifyDamage(CustomContext context, float amount) {
        Player owner = context.owner();
        LivingEntity target = context.target();
        if (owner == null || target == null) {
            return amount;
        }
        JsonObject options = context.options();
        long now = owner.level().getGameTime();
        int window = HookOptions.intOf(options, KEY_WINDOW_TICKS, DEFAULT_WINDOW_TICKS);
        STREAKS.values().removeIf(streak -> now - streak.startedAt() > window);
        Pair key = new Pair(owner.getUUID(), target.getUUID());
        Streak streak = STREAKS.get(key);
        Streak next = streak == null ? new Streak(now, 1) : new Streak(streak.startedAt(), streak.hits() + 1);
        int needed = HookOptions.intOf(options, KEY_HITS, DEFAULT_HITS);
        if (next.hits() >= needed) {
            STREAKS.remove(key);
            return amount * (1.0F + HookOptions.floatOf(options, KEY_BONUS, DEFAULT_BONUS));
        }
        STREAKS.put(key, next);
        return amount;
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        return List.of(Component.translatable(KEY_TOOLTIP,
                HookOptions.intOf(options, KEY_HITS, DEFAULT_HITS),
                StyleText.formatDecimal(HookOptions.floatOf(options, KEY_BONUS, DEFAULT_BONUS) * PERCENT)));
    }
}
