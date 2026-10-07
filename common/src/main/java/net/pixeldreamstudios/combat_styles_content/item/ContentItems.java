package net.pixeldreamstudios.combat_styles_content.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

import net.pixeldreamstudios.combat_styles.item.AddonItems;
import net.pixeldreamstudios.combat_styles.item.EquipmentItem;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;


public final class ContentItems {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(CombatStylesContent.MOD_ID, Registries.CREATIVE_MODE_TAB);

    private static final int BEHOLDER_ATTACK_DAMAGE = 6;
    private static final float BEHOLDER_ATTACK_SPEED = -2.9F;

    public static final RegistrySupplier<BeholderClaymoreItem> BEHOLDER_CLAYMORE =
            AddonItems.item(CombatStylesContent.MOD_ID, "beholder_claymore",
                    () -> new BeholderClaymoreItem(Tiers.DIAMOND, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .attributes(SwordItem.createAttributes(Tiers.DIAMOND,
                                    BEHOLDER_ATTACK_DAMAGE, BEHOLDER_ATTACK_SPEED))));

    public static final RegistrySupplier<EquipmentItem> ASHEN_BAND = accessory("ashen_band");
    public static final RegistrySupplier<EquipmentItem> BLOODBOUND_BAND = accessory("bloodbound_band");
    public static final RegistrySupplier<EquipmentItem> CRACKED_SIGNET = accessory("cracked_signet");
    public static final RegistrySupplier<EquipmentItem> DULL_CIRCLE = accessory("dull_circle");
    public static final RegistrySupplier<EquipmentItem> ECHO_OF_EMBERS = accessory("echo_of_embers");
    public static final RegistrySupplier<EquipmentItem> ECHOING_BAND = accessory("echoing_band");
    public static final RegistrySupplier<EquipmentItem> EXECUTIONERS_SEAL = accessory("executioners_seal");
    public static final RegistrySupplier<EquipmentItem> MYTHWROUGHT_SEAL = accessory("mythwrought_seal");
    public static final RegistrySupplier<EquipmentItem> NATURE_RING = accessory("nature_ring");
    public static final RegistrySupplier<EquipmentItem> OCCULAR_RING = accessory("occular_ring");
    public static final RegistrySupplier<EquipmentItem> PIERCING_LOOP = accessory("piercing_loop");
    public static final RegistrySupplier<EquipmentItem> RHYTHM_BAND = accessory("rhythm_band");
    public static final RegistrySupplier<EquipmentItem> ROOTBOUND_RING = accessory("rootbound_ring");
    public static final RegistrySupplier<EquipmentItem> SERPENTS_COIL = accessory("serpents_coil");
    public static final RegistrySupplier<EquipmentItem> STILLWATER_RING = accessory("stillwater_ring");
    public static final RegistrySupplier<EquipmentItem> STRIPED_RING = accessory("striped_ring");
    public static final RegistrySupplier<EquipmentItem> SUNDERERS_OATH = accessory("sunderers_oath");
    public static final RegistrySupplier<EquipmentItem> TWOCOLORED_RING = accessory("twocolored_ring");
    public static final RegistrySupplier<EquipmentItem> UNBROKEN_CIRCLE = accessory("unbroken_circle");
    public static final RegistrySupplier<EquipmentItem> VERDANT_STONE_RING = accessory("verdant_stone_ring");
    public static final RegistrySupplier<EquipmentItem> WARDENS_LOOP = accessory("wardens_loop");
    public static final RegistrySupplier<EquipmentItem> EYE_NECKLACE = accessory("eye_necklace");
    public static final RegistrySupplier<EquipmentItem> SKULL_NECKLACE = accessory("skull_necklace");
    public static final RegistrySupplier<EquipmentItem> WINDSTONE_NECKLACE = accessory("windstone_necklace");

    public static final RegistrySupplier<EquipmentItem> AZURE_STONE_RING = spellAccessory("azure_stone_ring");
    public static final RegistrySupplier<EquipmentItem> BAND_OF_EMBERS = spellAccessory("band_of_embers");
    public static final RegistrySupplier<EquipmentItem> FROSTBOUND_LOOP = spellAccessory("frostbound_loop");
    public static final RegistrySupplier<EquipmentItem> INFERNAL_SEAL = spellAccessory("infernal_seal");
    public static final RegistrySupplier<EquipmentItem> TARNISHED_LOOP = spellAccessory("tarnished_loop");

    private static RegistrySupplier<EquipmentItem> accessory(String id) {
        return AddonItems.equipment(CombatStylesContent.MOD_ID, id);
    }

    private static RegistrySupplier<EquipmentItem> spellAccessory(String id) {
        return SpellPowerAccess.isLoaded() ? accessory(id) : null;
    }

    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("content", () ->
            CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.combat_styles_content.content"))
                    .icon(() -> new ItemStack(SUNDERERS_OATH.get()))
                    .displayItems(ContentItems::fillTab)));

    private ContentItems() {
    }

    public static void register() {
        TABS.register();
    }

    private static void fillTab(CreativeModeTab.ItemDisplayParameters parameters,
                                CreativeModeTab.Output output) {
        for (RegistrySupplier<? extends Item> item : AddonItems.of(CombatStylesContent.MOD_ID)) {
            output.accept(new ItemStack(item.get()));
        }
    }
}
