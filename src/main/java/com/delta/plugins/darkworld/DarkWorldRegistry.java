package com.delta.plugins.darkworld;

import com.delta.plugins.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.io.File;

public class DarkWorldRegistry {
    public static ItemStack locationComp;
    public static ItemStack knife;
    public static ItemStack entrancePortal;
    public static ItemStack exitPortal;
    public static void InitItems() {
        locationComp = darkWorldLocationComp();
        knife = darkWorldKnife();
        entrancePortal = darkWorldEntrancePortal();
        exitPortal = darkWorldExitPortal();
    }
    //a method to get a config "darkworlds.yml"
    static ItemStack darkWorldLocationComp() {
        ItemStack item = new ItemStack(org.bukkit.Material.IRON_NUGGET);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8§lDark World Location Component");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Usa este ítem para marcar");
        lore.add("§7donde aparecerá la Fuente Oscura");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_location_comp"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        item.setItemMeta(meta);

        ShapedRecipe sr = new ShapedRecipe(new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_location_comp"), item);
        if(Bukkit.getRecipe(sr.getKey()) != null) return item;
        sr.shape(" I ", "INI", " I ");
        sr.setIngredient('I', org.bukkit.Material.IRON_INGOT);
        sr.setIngredient('N', org.bukkit.Material.NETHER_STAR);
        sr.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        org.bukkit.Bukkit.addRecipe(sr);

        return item;
    }
    static ItemStack darkWorldKnife() {
        ItemStack item = new ItemStack(org.bukkit.Material.IRON_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8§lDark World Knife");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Usa este cuchillo para abrir");
        lore.add("§7el portal a la Fuente Oscura");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_knife"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        item.setItemMeta(meta);

        ShapedRecipe sr = new ShapedRecipe(new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_knife"), item);
        if(Bukkit.getRecipe(sr.getKey()) != null) return item;
        sr.shape("  I", " I ", "S  ");
        sr.setIngredient('I', org.bukkit.Material.IRON_INGOT);
        sr.setIngredient('S', org.bukkit.Material.STICK);
        org.bukkit.Bukkit.addRecipe(sr);
        return item;
    }
    static ItemStack darkWorldEntrancePortal() {
        ItemStack item = new ItemStack(org.bukkit.Material.OBSIDIAN);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8§lDark World Entrance Portal");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Usa esto para marcar dónde");
        lore.add("aparecerás al entrar a la Fuente Oscura");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_entrance_portal"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        item.setItemMeta(meta);

        ShapedRecipe sr = new ShapedRecipe(new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_entrance_portal"), item);
        if(Bukkit.getRecipe(sr.getKey()) != null) return item;
        sr.shape("OOO", "O O", "OOO");
        sr.setIngredient('O', org.bukkit.Material.OBSIDIAN);
        sr.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        org.bukkit.Bukkit.addRecipe(sr);

        return item;
    }
    static ItemStack darkWorldExitPortal() {
        ItemStack item = new ItemStack(org.bukkit.Material.OBSIDIAN);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8§lDark World Exit Portal");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Usa esto para marcar dónde");
        lore.add("aparecerás al salir la Fuente Oscura sin sellarla");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_exit_portal"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        item.setItemMeta(meta);

        ShapedRecipe sr = new ShapedRecipe(new org.bukkit.NamespacedKey(Plugin.getPlugin(Plugin.class), "darkworld_exit_portal"), item);
        if(Bukkit.getRecipe(sr.getKey()) != null) return item;
        sr.shape("OOO", "O O", "OOO");
        sr.setIngredient('O', org.bukkit.Material.CRYING_OBSIDIAN);
        sr.setCategory(org.bukkit.inventory.recipe.CraftingBookCategory.MISC);
        org.bukkit.Bukkit.addRecipe(sr);

        return item;
    }

    static int getMinYLevel(World w){
        String worldName = w.getName();
        if(worldName.endsWith("_nether") || worldName.endsWith("_the_end")) return 3;
        return -57;
    }
    static int getMaxYLevel(World w){
        String worldName = w.getName();
        if(worldName.endsWith("_nether")) return 124;
        return 316;
    }

}
