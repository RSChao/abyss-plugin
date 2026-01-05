package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class assasin {
    static final String ID = "assasin";
    public static void registerTechniques() {
        TechRegistry.registerTechnique(ID, antiShoot);
        TechRegistry.registerTechnique(ID, tp);
        TechRegistry.registerTechnique(ID, crit);
        Plugin.registerAbyssID(ID);
    }

    // Migrated: antiShoot
    public static Technique antiShoot = new Technique(
        "antiShoot",
        "Assassin's Anti-Shoot Technique",
        new TechniqueMeta(false, 180000, List.of("Remove nearby projectiles.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for (int i = 0; i < 10; i++) {
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                    for (Entity entity : player.getWorld().getEntities()) {
                        if (entity instanceof Projectile && entity.getLocation().distance(player.getLocation()) <= 10) {
                            Projectile projectile = (Projectile) entity;
                            // Si el shooter es un Player y es inmune, ignorar
                            if (projectile.getShooter() instanceof Player) {
                                Player shooter = (Player) projectile.getShooter();
                                if (PlayerTechniqueManager.isInmune(shooter.getUniqueId())) continue;
                            }
                            if (!projectile.getShooter().equals(player)) {
                                projectile.remove();
                                hotbarMessage.sendHotbarMessage(player, ChatColor.GREEN + "You have used the Assassin's Anti-Shoot technique to remove a projectile!");
                            }
                        }
                    }
                }, 0);
            }
        }
    );

    static Technique tp = new Technique(
        "tp",
        "Dont get Lost",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(180), List.of("Teleport to closest player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
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
        }
    );

    static Technique crit = new Technique(
        "crit",
        "Critical Damage",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(300), List.of("Next attack deals critical damage.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            events.hasCritDamage.put(player.getUniqueId(), true);
            hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "You have used the Critical Damage technique! Your next attack will deal critical damage.");
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                events.hasCritDamage.put(player.getUniqueId(), false);
                hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "Your Critical Damage technique has worn off.");
            }, 10 * 20);
        }
    );

    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != location.getWorld()) continue; // Skip players in different worlds
            // Excluir jugadores inmunes
            if (PlayerTechniqueManager.isInmune(player.getUniqueId())) continue;
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }
        return closestPlayer;
    }
}
