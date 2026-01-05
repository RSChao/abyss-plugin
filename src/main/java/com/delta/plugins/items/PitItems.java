package com.delta.plugins.items;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

public class PitItems {
    public static ItemStack floor_key;
    public static ItemStack coin;
    public static ItemStack coin2;
    public static ItemStack key_hole;

    public static void Init() {
        floor_key = FloorKey();
        coin = Coin();
        coin2 = Coin2();
        key_hole = KeyHole();
    }
    static ItemStack FloorKey() {
        ItemStack item = new ItemStack(Material.TRIAL_KEY);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.GOLD + "Floor Key");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(org.bukkit.ChatColor.WHITE + "Una llave usada en las Cien Pruebas.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), "floor_key"),
            org.bukkit.persistence.PersistentDataType.STRING,
            "floor_key"
        );
        meta.setFireResistant(true);
        item.setItemMeta(meta);
        return item;
    }
    static ItemStack Coin() {
        ItemStack item = new ItemStack(org.bukkit.Material.GOLD_NUGGET);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.GOLD + "Coin");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(org.bukkit.ChatColor.WHITE + "Una moneda usada en las Cien Pruebas.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), "coin"),
                org.bukkit.persistence.PersistentDataType.STRING,
                "coin"
        );
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack CoinPaper(int value) {
        ItemStack item = new ItemStack(Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.GOLD + "Coin Token");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.WHITE + "Una token que puede cambiarse por monedas.");
        lore.add(ChatColor.WHITE + "Valor: " + value);
        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), "coin_value"),
                PersistentDataType.INTEGER,
                value
        );
        item.setItemMeta(meta);
        return item;
    }
    static ItemStack Coin2() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.GOLD + "Special Coin");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(org.bukkit.ChatColor.WHITE + "Una moneda usada en las Cien Pruebas.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), "coin"),
                org.bukkit.persistence.PersistentDataType.STRING,
                "coin"
        );
        item.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft("special_coin"), item);
        recipe.shape("GGG", "G0G", "GGG");
        recipe.setIngredient('G', new RecipeChoice.ExactChoice(Coin()));
        Bukkit.addRecipe(recipe);
        ItemStack res = new ItemStack(Coin());
        res.setAmount(8);
        recipe = new ShapedRecipe(NamespacedKey.minecraft("average_coin"), res);
        recipe.shape("G");
        recipe.setIngredient('G', new RecipeChoice.ExactChoice(item));
        Bukkit.addRecipe(recipe);
        return item;
    }
    static ItemStack KeyHole() {
        ItemStack item = new ItemStack(org.bukkit.Material.IRON_TRAPDOOR);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.GOLD + "Key Hole");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(org.bukkit.ChatColor.WHITE + "Un agujero para una llave.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), "key_hole"),
            org.bukkit.persistence.PersistentDataType.STRING,
            "key_hole"
        );
        item.setItemMeta(meta);
        return item;
    }
}
