package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

public class Exsolig {
    static final String TECH_ID = "exsolig";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, sonic_boom);
        TechRegistry.registerTechnique(TECH_ID, light);
        TechRegistry.registerTechnique(TECH_ID, downexplode);
        TechRegistry.registerTechnique(TECH_ID, tp);
    }

    static Technique sonic_boom = new Technique(
        "sonicboom",
        "Sound Ray",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Shoot a sonic particle ray to target.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location origin = player.getLocation();
            Player target = roaring_soul.getClosestPlayer(origin);
            if (target != null && !target.equals(player) && !PlayerTechniqueManager.isInmune(target.getUniqueId())) {
                Location targetLoc = target.getEyeLocation();
                Vector direction = targetLoc.toVector().subtract(player.getEyeLocation().toVector()).normalize();
                double distance = player.getEyeLocation().distance(targetLoc);
                int steps = (int) (distance * 4);
                for (int i = 0; i <= steps; i++) {
                    Location particleLoc = player.getEyeLocation().clone().add(direction.clone().multiply(i * (distance / steps)));
                    player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
                }
                target.damage(14.0, player);
                target.setNoDamageTicks(5);
                target.setHealth(Math.max(1, target.getHealth() - 14.0));
            }
            hotbarMessage.sendHotbarMessage(player, "Sonic Boom activated!");
        }
    );

    static Technique light = new Technique(
        "light",
        "Shine your Light",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(90), List.of("Glow nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location origin = player.getEyeLocation();
            double effectRadius = 300.0;
            for (Player target : player.getWorld().getPlayers()) {
                if (!target.equals(player) && target.getLocation().distance(origin) <= effectRadius) {
                    if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                    target.setGlowing(true);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> target.setGlowing(false), 600L);
                }
            }
            hotbarMessage.sendHotbarMessage(player, "Light activated!");
        }
    );

    static Technique downexplode = new Technique(
        "downexplode",
        "Down Explosin",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(45), List.of("Create a downward explosion.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location origin = player.getLocation();
            player.getWorld().createExplosion(origin.getX(), origin.getY() - 3, origin.getZ(), 7.0f, false, true, player);
        }
    );

    static Technique tp = new Technique(
        "tp",
        "Dont get Lost",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(180), List.of("Teleport to a privileged player or closest player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location someLocation = player.getLocation();
            Player closestPlayer = chao.getClosestPlayer(someLocation);
            for(Player p: Bukkit.getOnlinePlayers()){
                if(p.hasPermission("gaster.boss")){
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    player.teleport(p.getLocation());
                    hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_PURPLE + "You have used the Dont get Lost technique!");
                    return;
                }
            }
            if (closestPlayer != null) {
                if (PlayerTechniqueManager.isInmune(closestPlayer.getUniqueId())) {
                    hotbarMessage.sendHotbarMessage(player, ChatColor.RED + "No players found nearby to teleport to!");
                    return;
                }
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
}
