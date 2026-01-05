package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.fightingpp.techs.fly;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager; // added
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class leader {

    static final String TECH_ID = "leader";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, flyMeToTheMoon);
        TechRegistry.registerTechnique(TECH_ID, plumayubia);
        TechRegistry.registerTechnique(TECH_ID, talon);
        TechRegistry.registerTechnique(TECH_ID, fallingtwopointo);
    }


    static Technique flyMeToTheMoon = new Technique("soraingskeis", "Soaring Skies", false, 10000, (player, fruit, code) -> {
        if (player.isGliding()) {
            fly.EnableProFlight(player);
        } else {
            Location location = player.getLocation();
            player.getWorld().spawnParticle(Particle.EXPLOSION, location, 30);
            int multiply = 4;
            Vector direction = player.getLocation().getDirection();
            player.setVelocity(direction.multiply(multiply));
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.LIGHT_PURPLE + "You have used the Soaring Skies technique!");
    });

    static Technique plumayubia = new Technique("lluvia", "Feather Rain", false, cooldownHelper.minutesToMiliseconds(10), ((player, itemStack, objects) -> {
        Player p = player;
        Location center = p.getLocation();
        World world = p.getWorld();

        // 1. Teleport all players within 50 blocks (excluding p) to 5 blocks in front of p
        Vector direction = p.getLocation().getDirection().normalize();

        // 2. Schedule arrow spreads
        // Horizontal spread after 2 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = -2; i <= 2; i++) {
                    Vector spread = direction.clone().rotateAroundY(i * Math.PI / 16);
                    Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), spread, 2.0f, 0.1f);
                    arrow.setPierceLevel(7);
                }
            }
        }.runTaskLater(plugin, 2L);

        // Diagonal spread after 2+15=17 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector up = new Vector(0, 0.2, 0);
                for (int i = -2; i <= 2; i++) {
                    Vector diag = direction.clone().rotateAroundY(i * Math.PI / 16).add(up.clone().multiply(i * 0.2));
                    Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), diag, 2.0f, 0.1f);
                    arrow.setPierceLevel(7);
                }
            }
        }.runTaskLater(plugin, 17L);

        // Combined spread after 2+15+15=32 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                int xp = p.getLevel();
                //if xp == 0, set it to 1
                if (xp == 0) xp = 1;
                double baseDamage = xp;
                Vector up = new Vector(0, 0.2, 0);
                // Horizontal
                for (int i = -2; i <= 2; i++) {
                    Vector spread = direction.clone().rotateAroundY(i * Math.PI / 16);
                    Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), spread, 2.0f, 0.1f);
                    arrow.setPierceLevel(7);
                    arrow.setDamage(baseDamage);
                }
                // Diagonal (up left/down right)
                for (int i = -2; i <= 2; i++) {
                    Vector diag = direction.clone().rotateAroundY(i * Math.PI / 16).add(up.clone().multiply(i * 0.2));
                    Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), diag, 2.0f, 0.1f);
                    arrow.setPierceLevel(7);
                    arrow.setDamage(baseDamage);
                }
                // Vertical spread
                for (int i = -2; i <= 2; i++) {
                    Vector vert = direction.clone().add(new Vector(0, i * 0.2, 0));
                    Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), vert, 2.0f, 0.1f);
                    arrow.setPierceLevel(7);
                    arrow.setDamage(baseDamage);
                }
            }
        }.runTaskLater(plugin, 35L);
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_PURPLE + "You have used the Feather Rain technique");
    }));
    static Technique talon = new Technique("talonflame", "Reaction Talons", false, cooldownHelper.minutesToMiliseconds(3), ((player, itemStack, objects) -> {
        Location location = player.getLocation();
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, location, 30);
        player.getWorld().playSound(location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);

        for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
            if (entity.getLocation().distance(location) <= 20 && entity != player) {
                if ((entity instanceof Player)) {
                    Player target = (Player) entity;
                    // Excluir jugadores inmunes
                    if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                    target.damage(30);
                }
                Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();
                entity.setVelocity(direction.multiply(3));
            }
        }
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(4));
        Location launch = location.add(direction.multiply(2));

        for (int i = 0; i < 3; i++) {

            Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                org.bukkit.entity.Fireball fireball = player.getWorld().spawn(launch, org.bukkit.entity.Fireball.class);
                fireball.setDirection(direction);
                fireball.setYield(2F);
                fireball.setIsIncendiary(true);
                fireball.setShooter(player);
                fireball.setCustomName("determinationBall");
            }, i * 3);
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Reaction Talon technique");
    }));

    static Technique fallingtwopointo = new Technique("deltastopcopyinshit", "Revolution Fireworks", false, cooldownHelper.minutesToMiliseconds(10), (player, fruit, code) -> {
        Location center = player.getLocation().clone();
        World world = center.getWorld();
        int radius = 30; // Set radius to 10 blocks
        Random rand = new Random();
        int stars = ((com.delta.plugins.events.events.hasChaosHeart(player)) ? 40 : 30) + rand.nextInt(11);
        int speedX = rand.nextInt(-1, 1);
        int speedZ = rand.nextInt(-1, 1);
        int speedY = rand.nextInt(-1, 7);
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= stars) {
                    this.cancel();
                    return;
                }
                double angle = rand.nextDouble() * 2 * Math.PI;
                double dist = 5 + rand.nextDouble() * (radius - 5); // Adjust distance based on radius
                double x = center.getX() + Math.cos(angle) * dist;
                double z = center.getZ() + Math.sin(angle) * dist;
                double y = center.getY() + 30 + rand.nextDouble() * 10;
                Location spawnLoc = new Location(world, x, y, z);

                // Visual: falling ender crystal (use falling block for effect)
                FallingBlock fb = world.spawnFallingBlock(spawnLoc, Material.OBSIDIAN.createBlockData());
                Vector v = fb.getVelocity();
                v.setX(speedX);
                v.setZ(speedZ);
                v.setY(-speedY);
                fb.setVelocity(v);
                spawnLoc.setX(spawnLoc.getX() + speedX);
                spawnLoc.setZ(spawnLoc.getZ() + speedZ);
                // Damage on landing
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location landLoc = spawnLoc.clone();
                        landLoc.setY(center.getY());
                        for (Player p : world.getPlayers()) {
                            if (!p.equals(player)
                                    && p.getLocation().distance(center) <= radius
                                    && p.getLocation().distance(landLoc) < 3) {
                                // Excluir jugadores inmunes
                                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                                p.damage((events.hasPurityHeart(p) ? 3 : 7), player);

                            }
                        }
                        landLoc.getBlock().setType(Material.AIR);
                        player.getWorld().createExplosion(landLoc, 7.0f, true, true);
                    }
                }.runTaskLater(plugin, 25L); // ~1.25s fall time

                count++;
            }
        }.runTaskTimer(plugin, 0L, 6L);
        hotbarMessage.sendHotbarMessage(player, ChatColor.GOLD + "You have used the Revolution Fireworks technique!");
    });

}
