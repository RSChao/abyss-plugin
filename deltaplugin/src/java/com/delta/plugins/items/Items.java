package com.delta.plugins.items;

import com.rschao.enchants.GenoEnchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.delta.plugins.Plugin;

public class Items {
    public static ItemStack abyss_test;
    public static ItemStack pure_heart_red;
    public static ItemStack pure_heart_brown;
    public static ItemStack pure_heart_blue;
    public static ItemStack pure_heart_cyan;
    public static ItemStack pure_heart_purple;
    public static ItemStack pure_heart_pink;
    public static ItemStack pure_heart_yellow;
    public static ItemStack pure_heart_grey;
    public static ItemStack pureheart_red;
    public static ItemStack pureheart_orange;
    public static ItemStack pureheart_yellow;
    public static ItemStack pureheart_green;
    public static ItemStack pureheart_blue;
    public static ItemStack pureheart_indigo;
    public static ItemStack pureheart_purple;
    public static ItemStack pureheart_white;
    public static ItemStack nightmare;
    public static ItemStack whacka_bump;
    public static ItemStack rare_whacka_bump;
    public static ItemStack gold_whacka_bump;
    static public void Init() {
        // Initialize items here
        abyss_test = abyss("Abyss Channeler");
        pure_heart_red = pureHeart("red");
        pure_heart_brown = pureHeart("brown");
        pure_heart_blue = pureHeart("blue");
        pure_heart_cyan = pureHeart("cyan");
        pure_heart_purple = pureHeart("purple");
        pure_heart_pink = pureHeart("pink");
        pure_heart_yellow = pureHeart("yellow");
        pure_heart_grey = pureHeart("grey");
        pureheart_red = originlPureHeart("red");
        pureheart_orange = originlPureHeart("orange");
        pureheart_yellow = originlPureHeart("yellow");
        pureheart_green = originlPureHeart("green");
        pureheart_blue = originlPureHeart("blue");
        pureheart_indigo = originlPureHeart("indigo");
        pureheart_purple = originlPureHeart("purple");
        pureheart_white = originlPureHeart("white");
        nightmare = NHeart();
        whacka_bump = whackaBump();
        rare_whacka_bump = rareWhackaBump();
        gold_whacka_bump = goldWhackaBump();
    }
    static ItemStack abyss(String name) {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Plugin.getPlugin(Plugin.class), "channeler"),
            org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemName(name);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack pureHeart(String color){
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setItemModel(NamespacedKey.minecraft("pure_heart_" +color));
        meta.setItemName(ChatColor.LIGHT_PURPLE + "Corazón Puro");
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);

        return item;
    }

    static ItemStack originlPureHeart(String color){
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setItemModel(NamespacedKey.minecraft("pureheart_" +color));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "pureheart"),
                org.bukkit.persistence.PersistentDataType.STRING, color
        );
        meta.setItemName(ChatColor.LIGHT_PURPLE + "Corazón Puro");
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);

        return item;
    }


    static ItemStack NHeart(){
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setItemModel(NamespacedKey.minecraft("nightmare_heart"));
        meta.setItemName(ChatColor.BLACK + "Corazón de la Pesadilla");
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);

        return item;
    }
    public static ItemStack abyssContainer(String id){
        ItemStack item = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setMaxStackSize(1);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"),
            org.bukkit.persistence.PersistentDataType.STRING, id
        );
        meta.setItemName(ChatColor.DARK_PURPLE + "Abyss ID: " + id);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack hoe(){
        ItemStack item = new ItemStack(Material.NETHERITE_HOE);
        item = (new GenoEnchant()).addEnchant(item, 3);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Plugin.getPlugin(Plugin.class), "damagegobrrr");
        AttributeModifier mod = new AttributeModifier(key, 15, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, mod);
        meta.addEnchant(Enchantment.SHARPNESS, 10, true);
        meta.addEnchant(Enchantment.WIND_BURST, 5, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.setUnbreakable(true);
        meta.setDisplayName( ChatColor.YELLOW + (ChatColor.BOLD +"☭")+ChatColor.DARK_RED + (ChatColor.BOLD + "Our hoe"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack whackaBump() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Chichón de Guacka");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guacka. Por alguna razón, es muy sabroso.",
                ChatColor.LIGHT_PURPLE + "Regeneration IV (0:03)",
                ChatColor.GOLD + "Absorption V (10:00)"
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack rareWhackaBump() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Chichón Raro de Guacka");
        meta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Un chichón muy raro de un Guaka. Cómo duermes por la noche?",
            ChatColor.LIGHT_PURPLE + "Regeneration IV (0:03)",
            ChatColor.GOLD + "Absorption VII (10:00)"
        ));
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Plugin.getPlugin(Plugin.class), "rare_whacka_bump"),
            org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_rare"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack goldWhackaBump() {
        ItemStack item = new ItemStack(Material.RAW_GOLD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Chichón de Guacka Dorado");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guacka hecho de oro puro. Por desgracia(?), no es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_gold_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_gold"));
        item.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_gold_bump"),
                item.clone()
        );
        recipe.shape("CGC", "GGG", "CGC");
        recipe.setIngredient('C', Material.BARRIER);
        recipe.setIngredient('G', new RecipeChoice.ExactChoice(whackaBump()));
        Bukkit.addRecipe(recipe);
        return item;
    }
}
