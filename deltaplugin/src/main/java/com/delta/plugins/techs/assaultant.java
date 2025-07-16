package com.delta.plugins.techs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

import net.md_5.bungee.api.ChatColor;

public class assaultant {
    static final String GROUP_ID = "assaultant";
    public static void registerTechniques() {
        TechRegistry.registerTechnique(GROUP_ID, HighJump);
        TechRegistry.registerTechnique(GROUP_ID, SpeedBoost);
        TechRegistry.registerTechnique(GROUP_ID, freePearl);
    }

    static Technique HighJump = new Technique("highJump", "High Jump Technique", false, 10000, (player, item, args) -> {
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(4));
        hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the High Jump technique!");
    });
    static Technique SpeedBoost = new Technique("speedBoost", "Speed Boost Technique", false, 40000, (player, item, args) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50*20, 2));
        hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the Speed Boost technique!");
    });
    static Technique freePearl = new Technique("freePearl", "Free Pearl Technique", false, 120000, (player, item, args) -> {
        ItemStack[] inventoryContents = player.getInventory().getContents();
        int freeSlot = -1;
        for (int i = 0; i < inventoryContents.length; i++) {
            if (inventoryContents[i] == null || inventoryContents[i].getType() == Material.AIR) {
                freeSlot = i;
                break;
            }
        }
        if (freeSlot == -1) {
            hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "You have no free inventory slots for a Free Pearl!");
            return;
        }
        ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 64);
        player.getInventory().setItem(freeSlot, pearl);
        hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the Free Pearl technique!");
    });

}
