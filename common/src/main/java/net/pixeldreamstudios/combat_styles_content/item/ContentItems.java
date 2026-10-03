package net.pixeldreamstudios.combat_styles_content.item;

import dev.architectury.registry.CreativeTabOutput;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

import net.pixeldreamstudios.combat_styles.item.EquipmentItem;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ContentItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(CombatStylesContent.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(CombatStylesContent.MOD_ID, Registries.CREATIVE_MODE_TAB);

    private static final String MASCOT = "sunderers_oath";
    private static final int BEHOLDER_ATTACK_DAMAGE = 6;
    private static final float BEHOLDER_ATTACK_SPEED = -2.9F;

    public static final RegistrySupplier<Item> BEHOLDER_CLAYMORE =
            ITEMS.register("beholder_claymore",
                    () -> new BeholderClaymoreItem(Tiers.DIAMOND, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .attributes(SwordItem.createAttributes(Tiers.DIAMOND,
                                    BEHOLDER_ATTACK_DAMAGE, BEHOLDER_ATTACK_SPEED))));

    private static final List<String> ACCESSORIES = List.of(
            "ashen_band", "bloodbound_band", "cracked_signet", "dull_circle", "echo_of_embers",
            "echoing_band", "executioners_seal", "mythwrought_seal", "nature_ring", "occular_ring",
            "piercing_loop", "rhythm_band", "rootbound_ring", "serpents_coil", "stillwater_ring",
            "striped_ring", "sunderers_oath", "twocolored_ring", "unbroken_circle",
            "verdant_stone_ring", "wardens_loop", "eye_necklace", "skull_necklace",
            "windstone_necklace");
    private static final List<String> SPELL_POWER_ACCESSORIES = List.of(
            "azure_stone_ring", "band_of_embers", "frostbound_loop", "infernal_seal",
            "tarnished_loop");

    private static final Map<String, RegistrySupplier<Item>> EQUIPMENT = new LinkedHashMap<>();

    static {
        ACCESSORIES.forEach(ContentItems::registerAccessory);
        if (SpellPowerAccess.isLoaded()) {
            SPELL_POWER_ACCESSORIES.forEach(ContentItems::registerAccessory);
        }
    }

    private static void registerAccessory(String id) {
        EQUIPMENT.put(id, ITEMS.register(id, () -> new EquipmentItem(
                new Item.Properties().stacksTo(1),
                CombatStylesContent.MOD_ID + ":" + id)));
    }

    public static RegistrySupplier<Item> equipment(String id) {
        return EQUIPMENT.get(id);
    }

    public static Collection<RegistrySupplier<Item>> equipment() {
        return EQUIPMENT.values();
    }

    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("content", () ->
            CreativeTabRegistry.create(
                    Component.translatable("itemGroup.combat_styles_content.content"),
                    () -> new ItemStack(EQUIPMENT.containsKey(MASCOT)
                            ? EQUIPMENT.get(MASCOT).get()
                            : BEHOLDER_CLAYMORE.get())));

    private ContentItems() {
    }

    public static void register() {
        ITEMS.register();
        TABS.register();
        CreativeTabRegistry.modify(TAB, ContentItems::fillTab);
    }

    private static void fillTab(FeatureFlagSet flags, CreativeTabOutput output,
                                boolean canUseGameMasterBlocks) {
        output.accept(new ItemStack(BEHOLDER_CLAYMORE.get()));
        for (RegistrySupplier<Item> item : EQUIPMENT.values()) {
            output.accept(new ItemStack(item.get()));
        }
    }
}
