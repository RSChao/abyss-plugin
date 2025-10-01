package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.smp.enchants.sword.AttractionEnchant;
import com.rschao.smp.enchants.sword.LifeDrainEnchant;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class puppet {
    static final String TECH_ID = "puppet";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, chains);
        TechRegistry.registerTechnique(TECH_ID, dash);
        TechRegistry.registerTechnique(TECH_ID, maus);
    }

    static Technique chains = new Technique("patron_chain", "Patron's Chains", false,cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {
        // Create a chain of diamond blocks around the player
        Player closestPlayer = chao.getClosestPlayer(player.getLocation());
        if (closestPlayer != null) {
            closestPlayer.damage(20);
            for (int i = 0; i < 100; i++) {
                int t = i % 5;
                Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                    Vector direction = player.getEyeLocation().getDirection().normalize().multiply(20);
                    Location targetLocation = player.getEyeLocation().add(direction);
                    closestPlayer.teleport(targetLocation);

                    if (t == 0) {
                        closestPlayer.damage(20);
                        closestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1, false, false));
                    }
                }, i);
            }
            hotbarMessage.sendHotbarMessage(player, "You have used the Patron's Chains technique!");
        } else {
            player.sendMessage("No players nearby to launch.");
        }
    });
    static Technique dash = new Technique("moridodash", "Undead Dash", false, cooldownHelper.secondsToMiliseconds(120), (player, fruit, code) -> {
        Location location = player.getLocation();
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, location, 30);
        player.getWorld().playSound(location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);

        for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
            if (entity.getLocation().distance(location) <= 20 && entity != player) {
                if ((entity instanceof Player)) {
                    Player target = (Player) entity;
                    target.damage(30);
                }
                Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();
                entity.setVelocity(direction.multiply(3));
                LivingEntity le = (LivingEntity) entity;
                le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1, false, false));
            }
        }
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(4));
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Undead Dash technique");
    });
    static Technique maus = new Technique("queputasesesto", "Blade of Maws", false, cooldownHelper.minutesToMiliseconds(5), (player, fruit, code) -> {
        Location location = player.getLocation();

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 90, 1, false, false));
        for(ItemStack item : player.getInventory().getContents()) {
            if(item.getType().toString().contains("SWORD")) {
                AttractionEnchant attraction = new AttractionEnchant();
                attraction.addEnchant(item, 2);
                LifeDrainEnchant lifeDrain = new LifeDrainEnchant();
                lifeDrain.addEnchant(item, 2);
                Bukkit.getScheduler().runTaskLater(plugin, ()->{
                    attraction.removeEnchant(item);
                    lifeDrain.removeEnchant(item);
                }, 20 * 90);
            }
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Blade of Maws technique");
    });
}
