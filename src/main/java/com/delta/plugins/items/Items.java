package com.delta.plugins.items;

import com.delta.plugins.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.List;
import java.util.Random;

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
    // Agregar nuevos campos para los bump de gemas/metal
    public static ItemStack silver_whacka_bump;
    public static ItemStack amethyst_whacka_bump;
    public static ItemStack ruby_whacka_bump;
    public static ItemStack emerald_whacka_bump;
    public static ItemStack bronze_whacka_bump;
    public static ItemStack aquamarine_whacka_bump;
    public static ItemStack sapphire_whacka_bump;
    public static ItemStack onyx_whacka_bump;
    public static ItemStack rainbow_whacka_bump;
    public static ItemStack Moly_holy;

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
        silver_whacka_bump = silverWhackaBump();
        // Inicializar los nuevos bump items
        amethyst_whacka_bump = amethystWhackaBump();
        ruby_whacka_bump = rubyWhackaBump();
        emerald_whacka_bump = emeraldWhackaBump();
        bronze_whacka_bump = bronzeWhackaBump();
        aquamarine_whacka_bump = aquamarineWhackaBump();
        sapphire_whacka_bump = sapphireWhackaBump();
        onyx_whacka_bump = onyxWhackaBump();
        rainbow_whacka_bump = rainbowWhackaBump();
        Moly_holy = HolyMoly();
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
        if(id.equals("whacka_abyss")){
            return rainbowWhackaBump();
        }
        ItemStack item = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setMaxStackSize(1);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"),
            org.bukkit.persistence.PersistentDataType.STRING, id
        );
        meta.setItemModel(NamespacedKey.minecraft(getAbyssModel(id)));
        meta.setItemName(ChatColor.DARK_PURPLE + "Abyss ID: " + id);
        item.setItemMeta(meta);
        return item;
    }


    public static String getAbyssModel(String id){
        String model = "abyss_";
        switch (id) {
            case "roaring_soul":
                model += "blue";
                break;
            case "poet":
                model += "green";
                break;
            case "chosen_one", "offspring":
                model += "red";
                break;
            case "devourer":
                model += "black";
                break;
            case "chaos_wielder":
                model += "violet";
                break;
            case "exsolig", "queen":
                model += "white";
                break;
            default:
                break;
        }

        if(!model.equals("abyss_")){
            return model;
        }

        List<String> colors = List.of("blue", "green", "red", "white", "yellow", "violet", "light_blue", "gold");
        model += colors.get((new Random()).nextInt(colors.size()));

        return model;
    }
    public static ItemStack hoe(){
        ItemStack item = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Plugin.getPlugin(Plugin.class), "damagegobrrr");
        AttributeModifier mod = new AttributeModifier(key, 15, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, mod);
        meta.addEnchant(Enchantment.SHARPNESS, 10, true);
        meta.addEnchant(Enchantment.WIND_BURST, 5, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("geno")), 1, true);
        meta.setUnbreakable(true);
        meta.setDisplayName( ChatColor.YELLOW + (ChatColor.BOLD +"☭")+ChatColor.DARK_RED + (ChatColor.BOLD + "Our hoe"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack whackaBump() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Chichón de Guaka");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka. Por alguna razón, es muy sabroso.",
                ChatColor.LIGHT_PURPLE + "Regeneration IV (0:03)",
                ChatColor.GOLD + "Absorption V (10:00)"
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump"));
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        food.setNutrition(12);
        food.setSaturation(15);
        meta.setFood(food);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack voidBump() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Chichón de Guaka de Vacío");
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "void_whacka_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_void"));
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        food.setNutrition(12);
        food.setSaturation(15);
        meta.setFood(food);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack rareWhackaBump() {
        ItemStack item = new ItemStack(Material.COOKIE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Chichón Raro de Guaka");
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
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        food.setNutrition(20);
        food.setSaturation(20);
        meta.setFood(food);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack goldWhackaBump() {
        ItemStack item = new ItemStack(Material.RAW_GOLD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Chichón de Guaka Dorado");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de oro puro. Por desgracia(?), no es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_gold_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_gold"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack silverWhackaBump() {
        ItemStack item = new ItemStack(Material.RAW_GOLD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Chichón de Guaka Plateado");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de plata pura. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_silver_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_silver"));
        item.setItemMeta(meta);
        return item;
    }

    // Nuevos métodos para los bumps de gema/metal
    public static ItemStack amethystWhackaBump() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Chichón de Guaka de Amatista");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de amatista pura. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_amethyst_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_amethyst"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack rubyWhackaBump() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Chichón de Guaka de Rubí");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de rubí puro. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_ruby_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_ruby"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack emeraldWhackaBump() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Chichón de Guaka de Esmeralda");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de esmeralda pura. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_emerald_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_emerald"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack bronzeWhackaBump() {
        ItemStack item = new ItemStack(Material.RAW_COPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Chichón de Guaka de Bronce");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de bronce puro. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_bronze_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_bronze"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack aquamarineWhackaBump() {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Chichón de Guaka de Aguamarina");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de aguamarina pura. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_aquamarine_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_aquamarine"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack sapphireWhackaBump() {
        ItemStack item = new ItemStack(Material.LAPIS_LAZULI);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Chichón de Guaka de Zafiro");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de zafiro puro. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_sapphire_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_sapphire"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack onyxWhackaBump() {
        ItemStack item = new ItemStack(Material.INK_SAC);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "Chichón de Guaka de Ónix");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka hecho de ónix puro. No es comestible."
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_onyx_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_onyx"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack rainbowWhackaBump() {
        ItemStack item = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        meta.setDisplayName(ChatColor.DARK_GRAY + "Chichón del Guaka Ancestral");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Un chichón de un Guaka legendario.",
                ChatColor.DARK_RED + (ChatColor.BOLD + "Abyss ID: whacka_abyss")
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_rainbow_bump"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"),
                org.bukkit.persistence.PersistentDataType.STRING, "whacka_abyss"
        );
        meta.setItemModel(NamespacedKey.minecraft("whacka_bump_rainbow"));
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack HolyMoly() {
        ItemStack item = new ItemStack(Material.GLOW_BERRIES);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Holy Moly");
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "holy_moly"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true
        );
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        food.setNutrition(20);
        food.setSaturation(20);
        meta.setFood(food);
        item.setItemMeta(meta);
        return item;
    }
}
