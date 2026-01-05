package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class manticore {
    static final String TECH_ID = "manticore";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, barbs);
        TechRegistry.registerTechnique(TECH_ID, toss);
        TechRegistry.registerTechnique(TECH_ID, llubia);
    }

    static Technique barbs = new Technique("barbas", "Thousand Poison Barbs", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {
        for (int i = 0; i < 15; i++) {
            Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                Vector direction = player.getEyeLocation().getDirection().normalize();
                Vector randomOffset = new Vector(
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                );
                Vector finalDirection = direction.add(randomOffset).multiply(5); // Adjust speed multiplier as needed
                org.bukkit.entity.Arrow arrow = player.launchProjectile(org.bukkit.entity.Arrow.class, finalDirection);
                arrow.setDamage(20.0); // Set base damage to 50
                arrow.setGravity(false);
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1), true); // Apply Wither effect for 5 seconds
                arrow.setVelocity(finalDirection);
                arrow.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED); // Prevent pickup
            }, i * 2L); // Slight delay between each arrow
        }
    });
    static Technique toss = new Technique("toss", "Seismic Toss", false, cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {
        Vector dir = player.getEyeLocation().getDirection().normalize().multiply(2);
        player.setVelocity(dir);

        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            Location location = player.getLocation();
            //get the direction towards the closest player
            Vector direction = player.getEyeLocation().getDirection().normalize();
            Location launch = location.add(direction.multiply(2));

            for (int i = 0; i < 3; i++) {

                Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                    org.bukkit.entity.Fireball fireball = player.getWorld().spawn(launch, org.bukkit.entity.Fireball.class);
                    fireball.setDirection(direction);
                    fireball.setYield(2F);
                    fireball.setIsIncendiary(true);
                    fireball.setShooter(player);
                    fireball.setVelocity(direction.multiply(4));
                    fireball.setCustomName("determinationBall");
                }, i * 3);
            }
        }, 40);
    });
    static Technique llubia = new Technique("yubiaputera", "Accid rain", false, cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {
        Player target = chao.getClosestPlayer(player.getLocation());
        if (target == null) target = player;
        // Si target es inmune, volver al propio jugador
        if (PlayerTechniqueManager.isInmune(target.getUniqueId())) target = player;
        final Player finalTarget = target;
        final Location playerLoc = player.getLocation();
        final World world = player.getWorld();
        final int minArrows = 30;
        final int maxArrows = (events.hasPurityHeart(finalTarget) ? 30 : 50);
        final int durationTicks = 100;
        final int totalArrows = minArrows + (int) (Math.random() * (maxArrows - minArrows + 1));
        final double arrowsPerTick = (double) totalArrows / durationTicks;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new BukkitRunnable() {
                int ticks = 0;
                double arrowAccumulator = 0.0;

                @Override
                public void run() {
                    if (ticks++ >= durationTicks || !player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    arrowAccumulator += arrowsPerTick;
                    int arrowsThisTick = (int) arrowAccumulator;
                    arrowAccumulator -= arrowsThisTick;
                    float yaw = playerLoc.getYaw();
                    for (int i = 0; i < arrowsThisTick; i++) {
                        // Spawn arrows in a vertical circle (ring) above the player
                        double phi = Math.random() * 2 * Math.PI; // 0 to 2PI (circle)
                        double radius = 12 + Math.random() * 10;
                        double heightAbove = 8 + Math.random() * 4; // height above player

                        // Spherical to Cartesian (with fixed theta)
                        double x = radius * Math.cos(phi);
                        double y = heightAbove;
                        double z = radius * Math.sin(phi);

                        // Rotate around Y axis by player's yaw
                        double yawRad = Math.toRadians(-yaw);
                        double rotatedX = x * Math.cos(yawRad) - z * Math.sin(yawRad);
                        double rotatedZ = x * Math.sin(yawRad) + z * Math.cos(yawRad);

                        Location spawnLoc = playerLoc.clone().add(rotatedX, y, rotatedZ);

                        Vector direction = finalTarget.getLocation().add(0, 1, 0).toVector().subtract(spawnLoc.toVector()).normalize();
                        Arrow arrow = world.spawnArrow(spawnLoc, direction, 2.5f, 0.1f);
                        arrow.setShooter(player);
                        if(!events.hasPurityHeart(finalTarget)){
                            arrow.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255), true);
                            arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 255), true);
                        }
                        arrow.setCritical(true);
                        arrow.setDamage(7);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        arrow.setGravity(false);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
            hotbarMessage.sendHotbarMessage(player, ChatColor.GOLD + "Accid rain unleashed!");
        }, 100L); // 5 seconds (100 ticks)
    });
}
