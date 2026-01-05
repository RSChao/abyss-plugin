package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class end_boss {
    static final String TECH_ID = "end_boss";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register(){
        TechRegistry.registerTechnique(TECH_ID, blackFlash);
        TechRegistry.registerTechnique(TECH_ID, laserSweep);
        TechRegistry.registerTechnique(TECH_ID, blackHole);
        TechRegistry.registerTechnique(TECH_ID, dimentioProj);
        Plugin.registerAbyssID(TECH_ID);
    }

    static Technique blackFlash = new Technique(
        "flash",
        "Black Flash",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Temporarily set flash damage flag.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            events.hasFlashDamage.put(player.getUniqueId(), true);
            hotbarMessage.sendHotbarMessage(player, "You have activated Black Flash!");
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                if (!events.hasFlashDamage.getOrDefault(player.getUniqueId(), false)) {
                    events.hasFlashDamage.put(player.getUniqueId(), false);
                    hotbarMessage.sendHotbarMessage(player, "Black Flash has ended.");
                }
            }, 20 * 10);
        }
    );

    static Technique laserSweep = new Technique(
            "lasersweep",
            "Laser Sweep",
            new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(60), List.of("Sweep a laser beam that damages in path.")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                Location center = player.getLocation().clone();
                World world = center.getWorld();
                int radius = (events.hasChaosHeart(player) ? 80 : 30);
                int sweepSteps = 100;
                double sweepAngle = 2 * Math.PI;

                new BukkitRunnable() {
                    int step = 0;
                    @Override
                    public void run() {
                        if (step >= sweepSteps) {
                            this.cancel();
                            return;
                        }
                        double angle = (sweepAngle * step) / sweepSteps;
                        for (double dist = 2; dist < radius; dist += 2) {
                            double x = center.getX() + Math.cos(angle) * dist;
                            double z = center.getZ() + Math.sin(angle) * dist;
                            double y = center.getY() + 1.5;
                            Location beamLoc = new Location(world, x, y, z);
                            world.spawnParticle(org.bukkit.Particle.END_ROD, beamLoc, 2, 0, 0, 0, 0.01);

                            for (Player p : world.getPlayers()) {
                                if (!p.equals(player)
                                        && p.getLocation().distance(center) <= radius
                                        && p.getLocation().distance(beamLoc) < 2) {
                                    p.damage(events.hasPurityHeart(p) ? 10 : 20, player);
                                }
                            }
                        }
                        step++;
                    }
                }.runTaskTimer(plugin, 0L, 2L);
            }
    );

    static Technique blackHole = new Technique(
            "blackhole",
            "Black Hole",
            new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(180), List.of("Create a Black Hole effect.")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                Location center = player.getLocation().clone().add(0, 5, 0);
                World world = center.getWorld();
                int blockCount = 15;
                int radius = 8;
                List<FallingBlock> fallingBlocks = new ArrayList<>();
                Random random = new Random();

                // Partículas del agujero negro
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks++ > 100) {
                            this.cancel();
                            return;
                        }
                        world.spawnParticle(Particle.LARGE_SMOKE, center, 40, 1.5, 1.5, 1.5, 0.01);
                        world.spawnParticle(org.bukkit.Particle.PORTAL, center, 20, 1, 1, 1, 0.05);
                    }
                }.runTaskTimer(plugin, 0L, 2L);

                // Raycast para absorber bloques, uno por tick
                new BukkitRunnable() {
                    int absorbed = 0;
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (absorbed >= blockCount || ticks++ > 100) {
                            this.cancel();
                            return;
                        }
                        // Dirección aleatoria
                        double yaw = Math.toRadians(random.nextInt(360));
                        double pitch = Math.toRadians(-60 + random.nextInt(121)); // -60 a 60
                        org.bukkit.util.Vector dir = new org.bukkit.util.Vector(
                                Math.cos(pitch) * Math.cos(yaw),
                                Math.sin(pitch),
                                Math.cos(pitch) * Math.sin(yaw)
                        ).normalize();

                        // Raycast hasta 40 bloques
                        Location rayLoc = center.clone();
                        Block hitBlock = null;
                        for (int i = 0; i < 40; i++) {
                            rayLoc.add(dir);
                            Block b = world.getBlockAt(rayLoc);
                            if (b.getType() != Material.AIR && b.getType().isSolid()) {
                                hitBlock = b;
                                break;
                            }
                        }
                        if (hitBlock != null) {
                            Material mat = hitBlock.getType();
                            Location blockLoc = hitBlock.getLocation();

                            hitBlock.setType(Material.AIR);

                            FallingBlock fb = world.spawnFallingBlock(blockLoc.add(0.5, 0.5, 0.5), mat.createBlockData());
                            fb.setGravity(false);
                            fb.setDropItem(false);
                            fallingBlocks.add(fb);

                            // Movimiento hacia el centro
                            new BukkitRunnable() {
                                int t = 0;
                                @Override
                                public void run() {
                                    if (t++ > 100 || !fb.isValid()) {
                                        this.cancel();
                                        return;
                                    }
                                    org.bukkit.util.Vector toCenter = center.clone().subtract(fb.getLocation()).toVector().normalize().multiply(0.2);
                                    fb.setVelocity(toCenter);
                                }
                            }.runTaskTimer(plugin, 0L, 2L);

                            absorbed++;
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);

                // Eliminar FallingBlocks tras 5 segundos
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (FallingBlock fb : fallingBlocks) {
                        if (fb.isValid()) fb.remove();
                    }
                }, 100L);

                // Disparar flechas tras 6 segundos
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (int i = 0; i < fallingBlocks.size(); i++) {
                        for (int j = 0; j < 5; j++) { // 2 flechas por bloque
                            double yaw = Math.toRadians(random.nextInt(360));
                            // Pitch entre 0 (horizontal) y 60 (hacia abajo)
                            double pitch = Math.toRadians(random.nextInt(-30, 81)); // 0 a 60
                            org.bukkit.util.Vector dir = new org.bukkit.util.Vector(
                                    Math.cos(pitch) * Math.cos(yaw),
                                    -Math.sin(pitch), // Negativo para que apunte hacia abajo
                                    Math.cos(pitch) * Math.sin(yaw)
                            ).normalize();
                            org.bukkit.entity.Arrow arrow = world.spawnArrow(center, dir.multiply(3), 10f, 0f);
                            arrow.setDamage(50);
                            arrow.setShooter(player);
                            arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                        }
                    }
                }, 160L);
            }
    );

    static Technique dimentioProj = new Technique(
        "dimentio",
        "Dimensional Projectile",
        new TechniqueMeta(true, cooldownHelper.secondsToMiliseconds(30), List.of("Spawn shulker bullets at closest players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            final double projectileDamage = 50.0;
            final String metaKey = "dimentio_thingy";
            List<Player> players = new ArrayList<>(getClosestPlayers(player.getLocation(), 2));
            for(Player p : players){
                ShulkerBullet bullet = player.getWorld().spawn(player.getLocation(), ShulkerBullet.class);
                bullet.setShooter(player);
                bullet.setTarget(p);

                bullet.setMetadata(metaKey, new FixedMetadataValue(plugin, projectileDamage));
                Bukkit.getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onPotionEffect(org.bukkit.event.entity.EntityPotionEffectEvent event) {
                        if (event.getEntity() instanceof Player && event.getNewEffect() != null
                                && event.getNewEffect().getType().equals(PotionEffectType.LEVITATION)) {
                            event.setCancelled(true);
                        }
                    }
                    @EventHandler
                    public void onShulkerBulletDamage(EntityDamageByEntityEvent event) {
                        if (event.getDamager() instanceof ShulkerBullet) {
                            ShulkerBullet bullet = (ShulkerBullet) event.getDamager();
                            if (bullet.hasMetadata(metaKey)) {
                                double dmg = bullet.getMetadata(metaKey).get(0).asDouble();
                                event.setDamage(dmg);
                            }
                        }
                    }
                }, plugin);
            }
        }
    );

    public static List<Player> getClosestPlayers(Location location, int amount) {
        if (location == null || amount <= 0) {
            return new ArrayList<>(); // Return empty list for invalid input
        }

        // Get all online players
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        // Sort players by distance to the given location
        List<Player> sortedPlayers = onlinePlayers.stream()
                .filter(player -> player.getLocation().getWorld() != null && player.getLocation().distanceSquared(location) > 0 && player.getWorld() != location.getWorld() && !PlayerTechniqueManager.isInmune(player.getUniqueId())) // Added null check
                .sorted(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(location)))
                .collect(Collectors.toList());

        // Return the specified amount of closest players
        return sortedPlayers.stream().limit(amount).collect(Collectors.toList());
    }
}
