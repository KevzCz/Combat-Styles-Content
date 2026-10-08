package net.pixeldreamstudios.combat_styles_content.entity;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.bettercombat.api.AttackHand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.pixeldreamstudios.combat_styles.api.CombatStylesAPI;
import net.pixeldreamstudios.combat_styles.config.OptionField;
import net.pixeldreamstudios.combat_styles.custom.CustomContext;
import net.pixeldreamstudios.combat_styles.custom.CustomHook;
import net.pixeldreamstudios.combat_styles.custom.CustomScopes;
import net.pixeldreamstudios.combat_styles.equipment.ProjectileCopies;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShurikenHook implements CustomHook {

    public static final String KEY_ITEM = "item";
    public static final String KEY_PIERCE = "pierce";
    public static final String KEY_SPIN_SPEED = "spin_speed";
    public static final String KEY_SIZE = "size";
    public static final String KEY_BOUNCES = "bounces";
    public static final String KEY_BOUNCE_RESTITUTION = "bounce_restitution";
    public static final String KEY_REHIT = "rehit";
    public static final String HELD_ITEM = "@held";
    private static final int SAMPLE_PIERCE = 2;
    private static final int SINGLE = 1;
    private static final String KEY_TOOLTIP_PIERCE = "tooltip.combat_styles_content.shuriken.pierce";
    private static final String KEY_TOOLTIP_PIERCE_ONE = "tooltip.combat_styles_content.shuriken.pierce_one";
    private static final int SINGLE_PIERCE = 1;
    private static final String KEY_TOOLTIP_BOUNCES = "tooltip.combat_styles_content.shuriken.bounces";
    private static final String KEY_TOOLTIP_BOUNCES_REHIT = "tooltip.combat_styles_content.shuriken.bounces_rehit";
    private static final int NO_BOUNCES = 0;

    private static final List<OptionField> OPTIONS = List.of(
            OptionField.of(KEY_ITEM, Codec.STRING, HELD_ITEM),
            OptionField.of(KEY_PIERCE, Codec.INT, SAMPLE_PIERCE),
            OptionField.of(KEY_SPIN_SPEED, Codec.FLOAT, ShurikenEntity.DEFAULT_SPIN),
            OptionField.of(KEY_SIZE, Codec.FLOAT, ShurikenEntity.DEFAULT_SIZE),
            OptionField.of(KEY_BOUNCES, Codec.INT, NO_BOUNCES),
            OptionField.of(KEY_BOUNCE_RESTITUTION, Codec.FLOAT, ShurikenEntity.DEFAULT_RESTITUTION),
            OptionField.of(KEY_REHIT, Codec.BOOL, false));

    @Override
    public Set<String> scopes() {
        return Set.of(CustomScopes.PROJECTILE);
    }

    @Override
    public List<OptionField> options() {
        return OPTIONS;
    }

    @Override
    public void onSpawn(CustomContext context, Entity spawned) {
        if (!(spawned instanceof ShurikenEntity shuriken)) {
            return;
        }
        JsonObject options = context.options();
        String declared = HookOptions.stringOf(options, KEY_ITEM, HELD_ITEM);
        ItemStack shown = itemFor(declared, context.owner());
        if (!shown.isEmpty()) {
            shuriken.setItem(shown);
        }
        shuriken.setPierce(HookOptions.intOf(options, KEY_PIERCE, ShurikenEntity.DEFAULT_PIERCE));
        shuriken.setSpin(HookOptions.floatOf(options, KEY_SPIN_SPEED, ShurikenEntity.DEFAULT_SPIN));
        shuriken.setSize(HookOptions.floatOf(options, KEY_SIZE, ShurikenEntity.DEFAULT_SIZE));
        shuriken.setBounces(HookOptions.intOf(options, KEY_BOUNCES, NO_BOUNCES),
                HookOptions.floatOf(options, KEY_BOUNCE_RESTITUTION, ShurikenEntity.DEFAULT_RESTITUTION),
                HookOptions.boolOf(options, KEY_REHIT, false));
        if (ProjectileCopies.isCopy(shuriken) || context.owner() == null) {
            return;
        }
        ItemStack held = swinging(context.owner());
        if (!HELD_ITEM.equals(declared) && !held.is(itemFor(declared, context.owner()).getItem())) {
            return;
        }
        shuriken.arm(held);
        Volley.roll(context.owner(), shuriken);
    }

    @Override
    public float modifyDamage(CustomContext context, float amount) {
        return context.actor() instanceof ShurikenEntity shuriken ? amount * shuriken.damageScale() : amount;
    }

    @Override
    public List<Component> tooltip(JsonObject options) {
        List<Component> lines = new ArrayList<>();
        int pierce = HookOptions.intOf(options, KEY_PIERCE, ShurikenEntity.DEFAULT_PIERCE);
        if (pierce > ShurikenEntity.DEFAULT_PIERCE) {
            lines.add(Component.translatable(pierce == SINGLE_PIERCE ? KEY_TOOLTIP_PIERCE_ONE : KEY_TOOLTIP_PIERCE, pierce));
        }
        int bounces = HookOptions.intOf(options, KEY_BOUNCES, NO_BOUNCES);
        if (bounces > NO_BOUNCES) {
            lines.add(Component.translatable(HookOptions.boolOf(options, KEY_REHIT, false)
                    ? KEY_TOOLTIP_BOUNCES_REHIT : KEY_TOOLTIP_BOUNCES, bounces));
        }
        return lines;
    }

    private static ItemStack swinging(Player owner) {
        AttackHand hand = CombatStylesAPI.swungHand(owner);
        return hand != null && hand.isOffHand() ? owner.getOffhandItem() : owner.getMainHandItem();
    }

    private static ItemStack itemFor(String declared, Player owner) {
        if (HELD_ITEM.equals(declared)) {
            return owner == null ? ItemStack.EMPTY : swinging(owner).copyWithCount(SINGLE);
        }
        ResourceLocation id = ResourceLocation.tryParse(declared);
        return id == null
                ? ItemStack.EMPTY
                : BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
