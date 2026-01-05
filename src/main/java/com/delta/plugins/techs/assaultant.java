package com.delta.plugins.techs;

import net.kryspin.Plugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class assaultant {
    static final String GROUP_ID = "assaultant";
    public static void registerTechniques() {
        TechRegistry.registerTechnique(GROUP_ID, HighJump);
        TechRegistry.registerTechnique(GROUP_ID, SpeedBoost);
        TechRegistry.registerTechnique(GROUP_ID, freePearl);
        com.delta.plugins.Plugin.registerAbyssID(GROUP_ID);
    }

    static Technique HighJump = new Technique(
        "highJump",
        "High Jump Technique",
        new TechniqueMeta(false, 10000, List.of("Launch forward or enable pro flight.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
            if (!player.isGliding()) {
                Vector direction = player.getLocation().getDirection();
                player.setVelocity(direction.multiply(4));
            } else {
                Plugin plugin = Plugin.getPlugin(Plugin.class);
                plugin.enableProFlight(player);
            }
            hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the High Jump technique!");
        }
    );

    static Technique SpeedBoost = new Technique(
        "speedBoost",
        "Speed Boost Technique",
        new TechniqueMeta(false, 40000, List.of("Gain a strong speed buff.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50 * 20, 2));
            hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the Speed Boost technique!");
        }
    );

    static Technique freePearl = new Technique(
        "freePearl",
        "Free Pearl Technique",
        new TechniqueMeta(false, 120000, List.of("Give a stack of ender pearls if inventory space.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
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
        }
    );

}
