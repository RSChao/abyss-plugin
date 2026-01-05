package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

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

    static Technique sonic_boom = new Technique("sonicboom", "Sound Ray", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {
        Location origin = player.getLocation();
        Player target = roaring_soul.getClosestPlayer(origin);
        if (target != null && !target.equals(player) && !PlayerTechniqueManager.isInmune(target.getUniqueId())) {
            Location targetLoc = target.getEyeLocation();
            Vector direction = targetLoc.toVector().subtract(player.getEyeLocation().toVector()).normalize();
            // Generar partículas de sonic boom del warden
            double distance = player.getEyeLocation().distance(targetLoc) ;
            int steps = (int) (distance * 4); // más pasos = más partículas
            for (int i = 0; i <= steps; i++) {
                Location particleLoc = player.getEyeLocation().clone().add(direction.clone().multiply(i * (distance / steps)));
                player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
            }



            target.damage(14.0, player); // ignora armadura si el plugin/servidor lo permite
            // Para ignorar armadura completamente, puedes usar:
            target.setNoDamageTicks(5); // permite daño inmediato
            target.setHealth(Math.max(1, target.getHealth() - 14.0));
        }
        hotbarMessage.sendHotbarMessage(player, "Sonic Boom activated!");
    });

    static Technique light = new Technique("light", "Shine your Light", false, cooldownHelper.secondsToMiliseconds(90), (player, item, args) -> {
        Location origin = player.getEyeLocation();
        // Aplicar efecto de brillo a los jugadores cercanos
        double effectRadius = 300.0;
        for (Player target : player.getWorld().getPlayers()) {
            if (!target.equals(player) && target.getLocation().distance(origin) <= effectRadius) {
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                target.setGlowing(true);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> target.setGlowing(false), 600L); // 30 segundos
            }
        }
        hotbarMessage.sendHotbarMessage(player, "Light activated!");
    });

    static Technique downexplode = new Technique("downexplode", "Down Explosin", false, cooldownHelper.secondsToMiliseconds(45), (player, item, args) -> {
        Location origin = player.getLocation();
        // Crear una explosión hacia abajo
        player.getWorld().createExplosion(origin.getX(), origin.getY() - 3, origin.getZ(), 7.0f, false, true, player);
    });
    static Technique tp = new Technique("tp", "Dont get Lost", false, cooldownHelper.secondsToMiliseconds(180), (player, fruit, code) -> {
        Location someLocation = player.getLocation();
        Player closestPlayer = chao.getClosestPlayer(someLocation);
        for(Player p: Bukkit.getOnlinePlayers()){
            if(p.hasPermission("gaster.boss")){
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                player.teleport(p.getLocation());
                hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_PURPLE + "You have used the Dont get Lost technique!");
                return;
            }
        }
        if (closestPlayer != null) {
            // Excluir si el closest es inmune
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
    });
}
