package net.pixeldreamstudios.combat_styles_content.entity;

import net.minecraft.server.level.ServerLevel;

import net.pixeldreamstudios.combat_styles.config.AbilityConfig;
import net.pixeldreamstudios.combat_styles.entity.AbilityHost;
import net.pixeldreamstudios.combat_styles.entity.AbilityRegistry;
import net.pixeldreamstudios.combat_styles.entity.ActiveAbility;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.List;

public final class SkullAbilities {

    public static final String DIVE = CombatStylesContent.MOD_ID + ":dive";
    private static final List<String> USED_BY = List.of(CombatStylesContent.MOD_ID + ":kamikaze_skull");
    private static final List<String> TUNABLE = List.of("damage", "shape");

    private SkullAbilities() {
    }

    public static void registerDefaults() {
        AbilityRegistry.register(DIVE, new Dive());
    }

    private static final class Dive implements ActiveAbility {

        @Override
        public void run(ServerLevel level, AbilityHost host, int elapsed, float damage,
                        AbilityConfig.Entry tuned) {
            if (host.body() instanceof KamikazeSkullEntity skull) {
                skull.dive();
            }
        }

        @Override
        public List<String> usedBy() {
            return USED_BY;
        }

        @Override
        public List<String> tunable() {
            return TUNABLE;
        }
    }
}
