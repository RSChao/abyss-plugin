package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.events.HandEvents;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class lovers {

    static final String TECH_ID = "lovers";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, tatraflama);
        TechRegistry.registerTechnique(TECH_ID, bonk);
        TechRegistry.registerTechnique(TECH_ID, conquerorsHaki);
        TechRegistry.registerTechnique(TECH_ID, towerofflames);
    }

    static Technique tatraflama = new Technique("tetraflare", "Dragon's Fire explosion", false, cooldownHelper.minutesToMiliseconds(6), (player, item, args) -> {
        Location origin = player.getLocation().add(0, 1, 0); // Fireballs spawn slightly above ground
        double radius = 1.5;
        double angleOffset = Math.atan2(player.getLocation().getDirection().getZ(), player.getLocation().getDirection().getX());
        //player.setGameMode(GameMode.SPECTATOR);
        for (int i = 0; i < 4; i++) {
            double angle = angleOffset + (2 * Math.PI * i / 4);
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            Vector direction = new Vector(dx, 0, dz).normalize();
            Location launch = origin.clone().add(direction.multiply(1.5)); // Launch position 1.5 blocks in front of player
            Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                org.bukkit.entity.Fireball fireball = player.getWorld().spawn(launch, org.bukkit.entity.Fireball.class);
                fireball.setDirection(direction);
                fireball.setYield(2F);
                fireball.setIsIncendiary(true);
                fireball.setShooter(player);
                fireball.setCustomName("blasterBall");
            }, 1);
        }

    });

    static Technique bonk = new Technique("Bonk", "Nuclear Bonk", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) ->{
        player.setVelocity(new Vector(0, 50, 0));
        new BukkitRunnable(){
            @Override
            public void run() {
                HandEvents handEvents = new HandEvents();
                if(!handEvents.isPlayerInMidAir(player)){
                    this.cancel();
                    player.getWorld().createExplosion(player.getLocation(), 30, false, false, player);
                }
            }
        }.runTaskTimer(plugin, 10, 0);
    });
    static Technique conquerorsHaki = new Technique("conquerors", "Tornado of Ashes", false, cooldownHelper.secondsToMiliseconds(300), (player, fruit, code) -> {
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                double jumpstrength = 0.41999998697815;
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 255));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 255));
                target.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    target.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpstrength);
                }, 5 * 20);
            }
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_PURPLE + "You have used the Conqueror's Haki technique");
    });
    static Technique towerofflames = new Technique("tower_flames", "Lover's Blazing Spark", false, cooldownHelper.minutesToMiliseconds(7), (player, item, args) -> {
        for (org.bukkit.entity.Player target : player.getWorld().getPlayers()) {
            if (target.getLocation().distance(player.getLocation()) <= 20) {
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                org.bukkit.Location loc = target.getLocation().clone();
                loc.setY(loc.getY() - 1);
                // create a blue particle effect at the location
                for (int i = 0; i < 100; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * 3;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    loc.add(x, 0, z);
                    target.getWorld().spawnParticle(Particle.WITCH, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, 0, z);
                    target.damage(15);
                }
            }
        }
    });
}
