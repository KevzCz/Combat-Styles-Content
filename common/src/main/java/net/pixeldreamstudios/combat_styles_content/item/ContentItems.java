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
import net.pixeldreamstudios.combat_styles.style.StyleRarity;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.util.Collection;
import java.util.EnumMap;
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

    private static final Map<String, RegistrySupplier<Item>> EQUIPMENT = new LinkedHashMap<>();
    private static final Map<StyleRarity, RegistrySupplier<Item>> BOOKS =
            new EnumMap<>(StyleRarity.class);

    static {
        for (String id : EquipmentIndex.all()) {
            EQUIPMENT.put(id, ITEMS.register(id, () -> new EquipmentItem(
                    new Item.Properties().stacksTo(1),
                    CombatStylesContent.MOD_ID + ":" + id)));
        }
        for (StyleRarity rarity : StyleRarity.values()) {
            BOOKS.put(rarity, ITEMS.register("style_book_" + rarity.key(),
                    () -> new StyleBookItem(new Item.Properties()
                            .stacksTo(16)
                            .rarity(rarity.itemRarity()), rarity)));
        }
    }

    public static RegistrySupplier<Item> equipment(String id) {
        return EQUIPMENT.get(id);
    }

    public static Collection<RegistrySupplier<Item>> equipment() {
        return EQUIPMENT.values();
    }

    public static List<String> equipmentIds() {
        return List.copyOf(EQUIPMENT.keySet());
    }

    public static Item bookFor(StyleRarity rarity) {
        RegistrySupplier<Item> book = BOOKS.get(rarity == null ? StyleRarity.COMMON : rarity);
        return book == null ? BOOKS.get(StyleRarity.COMMON).get() : book.get();
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
        for (StyleRarity rarity : StyleRarity.values()) {
            output.accept(new ItemStack(bookFor(rarity)));
        }
    }
}
