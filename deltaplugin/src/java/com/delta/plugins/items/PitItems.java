package com.delta.plugins.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PitItems {
    public static ItemStack floor_key;
    public static ItemStack coin;
    public static ItemStack key_hole;

    public static void Init() {
        floor_key = FloorKey();
        coin = Coin();
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
