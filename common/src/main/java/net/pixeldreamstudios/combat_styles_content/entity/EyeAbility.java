package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.resources.ResourceLocation;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.Locale;

public enum EyeAbility {

    IDLE,
    TWIN_LANCE,
    FURROW,
    WIDE_CUT,
    BLINK_WATCH,
    TETHER,
    COLLAPSE,
    SUPERNOVA,
    ECLIPSE;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ResourceLocation id() {
        return CombatStylesContent.id(key());
    }

    public boolean acting() {
        return this != IDLE;
    }

    public static EyeAbility byName(String name) {
        if (name == null || name.isBlank()) {
            return IDLE;
        }
        String bare = name.trim();
        int colon = bare.indexOf(':');
        if (colon >= 0) {
            if (!CombatStylesContent.MOD_ID.equals(bare.substring(0, colon))) {
                return IDLE;
            }
            bare = bare.substring(colon + 1);
        }
        try {
            return valueOf(bare.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return IDLE;
        }
    }
}
