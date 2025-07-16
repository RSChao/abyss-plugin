package com.delta.plugins.techs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

import net.md_5.bungee.api.ChatColor;

public class assasin {
    static final String ID = "assasin";
    public static void registerTechniques() {
        TechRegistry.registerTechnique(ID, antiShoot);
        TechRegistry.registerTechnique(ID, tp);
        TechRegistry.registerTechnique(ID, crit);
    }
    public static Technique antiShoot = new Technique("antiShoot", "Assassin's Anti-Shoot Technique", false, 180000, (player, item, args) -> {
        for(int i = 0; i < 10; i++) {
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
                for(Entity entity : player.getWorld().getEntities()) {
                    if(entity instanceof Projectile && entity.getLocation().distance(player.getLocation()) <= 10) {
                        Projectile projectile = (Projectile) entity;
                        if(!projectile.getShooter().equals(player)) {
                            projectile.remove();
                            hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the Assassin's Anti-Shoot technique to remove a projectile!");
                        }
                    }
                }
            }, 0);
        }
    });
    static Technique tp = new Technique("tp", "Dont get Lost", false, cooldownHelper.secondsToMiliseconds(180), (player, fruit, code) -> {
        Location someLocation = player.getLocation();
        Player closestPlayer = getClosestPlayer(someLocation);

        if (closestPlayer != null) {
            if (someLocation.distance(closestPlayer.getLocation()) < 400) {
                player.teleport(closestPlayer.getLocation());
                hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_PURPLE + "You have used the Dont get Lost technique!");
            } else {
                hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "The closest player is too far away to teleport to!");
            }
        } else {
            hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "No players found nearby to teleport to!");
        }
    });

    static Technique crit = new Technique("crit", "Critical Damage", false, cooldownHelper.secondsToMiliseconds(300), (player, item, args) -> {
        events.hasCritDamage.put(player.getUniqueId(), true);
        hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "You have used the Critical Damage technique! Your next attack will deal critical damage.");
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
            events.hasCritDamage.put(player.getUniqueId(), false);
            hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "Your Critical Damage technique has worn off.");
        }, 10*20);
    });

    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }
}
