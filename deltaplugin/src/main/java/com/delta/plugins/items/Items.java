package com.delta.plugins.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.delta.plugins.Plugin;

public class Items {
    public static ItemStack abyss_test;
    static public void Init() {
        // Initialize items here
        abyss_test = abyss("Abyss Channeler");
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
}
