package net.pixeldreamstudios.combat_styles_content.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.pixeldreamstudios.combat_styles.CombatStyles;
import net.pixeldreamstudios.combat_styles.attribute.ModAttributes;
import net.pixeldreamstudios.combat_styles.item.AddonItems;
import net.pixeldreamstudios.combat_styles.item.EquipmentItem;
import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;


public final class ContentItems {

    private static ItemAttributeModifiers throwingAttributes(Tier tier, int attackDamage,
                                                             float attackSpeed) {
        ItemAttributeModifiers base = SwordItem.createAttributes(tier, attackDamage, attackSpeed);
        return BuiltInRegistries.ATTRIBUTE
                .getHolder(ResourceKey.create(Registries.ATTRIBUTE,
                        CombatStyles.id(ModAttributes.MELEE_KNOCKBACK_ID)))
                .map(holder -> base.withModifierAdded(holder,
                        new AttributeModifier(SHURIKEN_KNOCKBACK_MODIFIER, SHURIKEN_KNOCKBACK,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.MAINHAND))
                .orElse(base);
    }

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

    private static final int SHURIKEN_ATTACK_DAMAGE = 2;
    private static final float SHURIKEN_ATTACK_SPEED = -1.8F;
    private static final double SHURIKEN_KNOCKBACK = -0.5D;
    private static final ResourceLocation SHURIKEN_KNOCKBACK_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(CombatStylesContent.MOD_ID, "shuriken_knockback");

    private static final float SWIFT_ATTACK_SPEED = -1.5F;
    private static final float HEAVY_ATTACK_SPEED = -2.02F;
    private static final int UNIQUE_ATTACK_DAMAGE = 3;
    private static final int SCALER_ATTACK_DAMAGE = 0;

    public static final RegistrySupplier<SwordItem> SHURIKEN =
            thrower("shuriken", Tiers.IRON, SHURIKEN_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.UNCOMMON);
    public static final RegistrySupplier<SwordItem> STONE_SHURIKEN =
            thrower("stone_shuriken", Tiers.STONE, SHURIKEN_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.COMMON);
    public static final RegistrySupplier<SwordItem> GOLDEN_SHURIKEN =
            thrower("golden_shuriken", Tiers.GOLD, SHURIKEN_ATTACK_DAMAGE, SWIFT_ATTACK_SPEED, Rarity.UNCOMMON);
    public static final RegistrySupplier<SwordItem> DIAMOND_SHURIKEN =
            thrower("diamond_shuriken", Tiers.DIAMOND, SHURIKEN_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> NETHERITE_SHURIKEN =
            AddonItems.item(CombatStylesContent.MOD_ID, "netherite_shuriken",
                    () -> new SwordItem(Tiers.NETHERITE, new Item.Properties()
                            .rarity(Rarity.EPIC)
                            .fireResistant()
                            .attributes(throwingAttributes(Tiers.NETHERITE, SHURIKEN_ATTACK_DAMAGE,
                                    SHURIKEN_ATTACK_SPEED))));

    public static final RegistrySupplier<SwordItem> VOIDWHEEL =
            thrower("voidwheel", Tiers.IRON, SCALER_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.EPIC);
    public static final RegistrySupplier<SwordItem> AZURE_CROSSBLADE =
            thrower("azure_crossblade", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, HEAVY_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> BLOODWIND_STAR =
            thrower("bloodwind_star", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> BONE_WHEEL =
            thrower("bone_wheel", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.EPIC);
    public static final RegistrySupplier<SwordItem> EMBER_STAR =
            thrower("ember_star", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> OCULAR_STAR =
            thrower("ocular_star", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.EPIC);
    public static final RegistrySupplier<SwordItem> SOLAR_FLARE =
            thrower("solar_flare", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.EPIC);
    public static final RegistrySupplier<SwordItem> SUNSTAR =
            thrower("sunstar", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> UMBRAL_STAR =
            thrower("umbral_star", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.EPIC);
    public static final RegistrySupplier<SwordItem> SLIME_SHURIKEN =
            thrower("slime_shuriken", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SHURIKEN_ATTACK_SPEED, Rarity.RARE);
    public static final RegistrySupplier<SwordItem> SILVER_STAR =
            thrower("silver_star", Tiers.IRON, UNIQUE_ATTACK_DAMAGE, SWIFT_ATTACK_SPEED, Rarity.RARE);

    public static final RegistrySupplier<Item> BONE_SHARD =
            AddonItems.item(CombatStylesContent.MOD_ID, "bone_shard", () -> new Item(new Item.Properties()));

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
    public static final RegistrySupplier<EquipmentItem> NIGHT_OWL_PENDANT = accessory("night_owl_pendant");
    public static final RegistrySupplier<EquipmentItem> FANG_CHARM = accessory("fang_charm");
    public static final RegistrySupplier<EquipmentItem> GAMBLERS_COIN = accessory("gamblers_coin");
    public static final RegistrySupplier<EquipmentItem> TIDECALLER_SHELL = accessory("tidecaller_shell");
    public static final RegistrySupplier<EquipmentItem> LESSER_GAZE = accessory("lesser_gaze");
    public static final RegistrySupplier<EquipmentItem> MOSSFANG_RING = accessory("mossfang_ring");
    public static final RegistrySupplier<EquipmentItem> GILDED_LOOP = accessory("gilded_loop");
    public static final RegistrySupplier<EquipmentItem> NIGHTFALL_BAND = accessory("nightfall_band");
    public static final RegistrySupplier<EquipmentItem> HEMORRHAGE_BAND = accessory("hemorrhage_band");
    public static final RegistrySupplier<EquipmentItem> BLACK_BELT = accessory("black_belt");
    public static final RegistrySupplier<EquipmentItem> BROWN_BELT = accessory("brown_belt");
    public static final RegistrySupplier<EquipmentItem> GOLD_SKULL_BELT = accessory("gold_skull_belt");
    public static final RegistrySupplier<EquipmentItem> WHITE_SKULL_BELT = accessory("white_skull_belt");
    public static final RegistrySupplier<EquipmentItem> TWINSHOT_BELT = accessory("twinshot_belt");

    public static final RegistrySupplier<EquipmentItem> AZURE_STONE_RING = spellAccessory("azure_stone_ring");
    public static final RegistrySupplier<EquipmentItem> BAND_OF_EMBERS = spellAccessory("band_of_embers");
    public static final RegistrySupplier<EquipmentItem> FROSTBOUND_LOOP = spellAccessory("frostbound_loop");
    public static final RegistrySupplier<EquipmentItem> INFERNAL_SEAL = spellAccessory("infernal_seal");
    public static final RegistrySupplier<EquipmentItem> TARNISHED_LOOP = spellAccessory("tarnished_loop");
    public static final RegistrySupplier<EquipmentItem> SPELLWEAVE_SASH = spellAccessory("spellweave_sash");

    private static RegistrySupplier<SwordItem> thrower(String id, Tier tier, int attackDamage,
                                                       float attackSpeed, Rarity rarity) {
        return AddonItems.item(CombatStylesContent.MOD_ID, id,
                () -> new SwordItem(tier, new Item.Properties()
                        .rarity(rarity)
                        .attributes(throwingAttributes(tier, attackDamage, attackSpeed))));
    }

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
