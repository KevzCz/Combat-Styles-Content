package net.pixeldreamstudios.combat_styles_content.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

public final class ContentEnchantments {

    public static final ResourceKey<Enchantment> VOLLEY = key("volley");
    public static final ResourceKey<Enchantment> RICOCHET = key("ricochet");
    public static final ResourceKey<Enchantment> SHARPNESS = Enchantments.SHARPNESS;

    private static final int NONE = 0;

    private ContentEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, CombatStylesContent.id(path));
    }

    public static int levelOf(Level level, ResourceKey<Enchantment> enchantment, ItemStack stack) {
        if (level == null || stack == null || stack.isEmpty()) {
            return NONE;
        }
        return level.registryAccess().lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(enchantment))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) holder, stack))
                .orElse(NONE);
    }
}
