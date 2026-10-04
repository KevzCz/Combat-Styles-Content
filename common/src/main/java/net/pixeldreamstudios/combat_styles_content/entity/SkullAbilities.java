package net.pixeldreamstudios.combat_styles_content.entity;

import net.pixeldreamstudios.combat_styles.entity.AbilityRegistry;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.List;

public final class SkullAbilities {

    public static final String DIVE = CombatStylesContent.MOD_ID + ":dive";
    private static final List<String> USED_BY = List.of(CombatStylesContent.MOD_ID + ":kamikaze_skull");

    private SkullAbilities() {
    }

    public static void registerDefaults() {
        AbilityRegistry.register(DIVE, USED_BY,
                (level, summon, owner, elapsed, damage, tuned, params) -> {
                    if (summon instanceof KamikazeSkullEntity skull) {
                        skull.dive();
                    }
                });
        AbilityRegistry.declareTunable(DIVE, List.of("damage", "shape"));
    }
}
