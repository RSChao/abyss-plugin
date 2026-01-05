package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.boss_battle.bossEvents;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class griffon {
    static final String TECH_ID = "griffon";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, Slizing_Ambush);
        TechRegistry.registerTechnique(TECH_ID, Razor_Tail_Flurry);
        TechRegistry.registerTechnique(TECH_ID, Feather_Tornado);
    }
    static Technique Slizing_Ambush = new Technique("slizing_ambush", "Slizing Ambush", false, cooldownHelper.minutesToMiliseconds(1), (player, item, args) -> {

            Location location = player.getLocation();
            player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, location, 30);
            player.getWorld().playSound(location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);

            for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
                if (entity.getLocation().distance(location) <= 20 && entity != player) {
                    if ((entity instanceof Player)) {
                        Player target = (Player) entity;
                        target.damage((events.hasPurityHeart(target) ? 15 : 30));
                    }
                    Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();
                    entity.setVelocity(direction.multiply(7));
                }
            }
            Vector direction = player.getLocation().getDirection();
            player.setVelocity(direction.multiply(4));
            hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Slizing Ambush technique");
    });
    static Technique Razor_Tail_Flurry = new Technique("razor_tail_flurry", "Razor Tail Flurry", false, cooldownHelper.minutesToMiliseconds(2), (player, item, args) -> {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(999);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4);
            }, 60 * 20);
        }, 2);
        hotbarMessage.sendHotbarMessage(player, ChatColor.GRAY + "You have used the Razor Tail Flurry technique");
    });
    static Technique Feather_Tornado = new Technique("feather_tornado", "Feather Tornado", false, cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {
        Player user = player;

        // 2. Gather players in 60-block radius
        Location center = user.getLocation().add(0, 3, 0);
        List<Player> targets;
        if (bossEvents.bossActive && !player.hasPermission("gaster.boss")) {
            // Solo jugadores con permiso gaster.boss
            targets = user.getWorld().getPlayers().stream()
                    .filter(p -> !p.equals(user) && p.getLocation().distance(user.getLocation()) <= 60)
                    .filter(p -> p.hasPermission("gaster.boss"))
                    .filter(p -> !events.hasPurityHeart(p))
                    .collect(Collectors.toList());
        } else {
            // Todos los jugadores en el radio, excepto el usuario
            targets = user.getWorld().getPlayers().stream()
                    .filter(p -> !p.equals(user) && p.getLocation().distance(user.getLocation()) <= 60)
                    .filter(p -> !events.hasPurityHeart(p))
                    .collect(Collectors.toList());
        }

        // 3. Teleport all to center (+3 up)
        for (Player p : targets) {
            p.teleport(center);
        }

        // 4. Spiral push: schedule repeating task for 5 seconds (100 ticks)
        int spiralDuration = 100;
        int numPlayers = targets.size();
        double spiralRadius = 8; // distance from center
        double spiralHeight = 10; // total height to push up
        double spiralTurns = 3; // number of spiral turns

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= spiralDuration || targets.isEmpty()) {
                    this.cancel();
                    return;
                }
                double angleStep = 2 * Math.PI / Math.max(1, numPlayers);
                for (int i = 0; i < targets.size(); i++) {
                    Player p = targets.get(i);
                    double angle = angleStep * i + spiralTurns * 2 * Math.PI * tick / spiralDuration;
                    double y = spiralHeight * tick / spiralDuration;
                    double x = spiralRadius * Math.cos(angle);
                    double z = spiralRadius * Math.sin(angle);
                    Location spiralLoc = center.clone().add(x, y, z);
                    Vector vel = spiralLoc.toVector().subtract(p.getLocation().toVector()).multiply(0.2);
                    if(tick%10 == 0) {
                        p.damage(10); // Damage per tick
                    }
                    p.setVelocity(vel);

                }
                tick++;
            }
        }.runTaskTimer(plugin, 0, 1);
        hotbarMessage.sendHotbarMessage(player, ChatColor.LIGHT_PURPLE + "You have used the Feather Tornado technique");
    });
}
