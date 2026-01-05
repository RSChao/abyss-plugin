package com.delta.plugins.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class makeDarkWorld {

    public static void register(JavaPlugin plugin) {
        new CommandAPICommand("darkworld")
                .withArguments(new LocationArgument("animationLocation"))
                .withArguments(new LocationArgument("teleportLocation"))
                .withOptionalArguments(new StringArgument("world"))
                .executes((sender, args) -> {
                    Location animationLoc = (Location) args.get("animationLocation");
                    Location teleportLoc = (Location) args.get("teleportLocation");
                    String worldName = (String) args.getOrDefault("world", teleportLoc.getWorld().getName());

                    playDarkWorldAnimation(plugin, animationLoc, teleportLoc, worldName);
                })
                .register();
    }

    /**
     * Ejecuta la animación de Dark World en una ubicación y teletransporta jugadores cercanos a otra.
     * @param plugin Plugin instance
     * @param animationLocation Centro de la animación
     * @param teleportLocation Destino del teletransporte
     * @param worldName Nombre del mundo destino (puede ser nulo)
     */
    public static void playDarkWorldAnimation(JavaPlugin plugin, Location animationLocation, Location teleportLocation, String worldName) {
        Location centerLoc = animationLocation.clone();
        World world = centerLoc.getWorld();

        // 1. Animación de partículas negras en bloque frente al centro
        Vector direction = centerLoc.getDirection();
        if (direction.lengthSquared() == 0) direction = new Vector(0, 0, 1); // Por si no hay dirección
        Location front = centerLoc.clone().add(direction.setY(0).normalize());
        Location left = front.clone().add(direction.clone().rotateAroundY(Math.toRadians(90)).normalize());
        Location right = front.clone().add(direction.clone().rotateAroundY(Math.toRadians(-90)).normalize());

        Location[] positions = new Location[] { left, front, right };

        // Spawn partículas negras en 10 ticks (1 cada 5 ticks)
        for (int i = 0; i < positions.length; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                world.spawnParticle(Particle.LARGE_SMOKE, positions[idx], 3, 0.3, 0.5, 0.3, 0.01);
            }, i * 5L);
        }

        // 2. Esperar 20 ticks, luego explosión de humo durante 3 segundos (60 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks++ >= 140) {
                        this.cancel();
                        return;
                    }
                    Particle mainParticle = ticks < 80 ? Particle.CLOUD : Particle.LARGE_SMOKE;

                    // Explosión grande ascendente
                    for (int i = 0; i < 100; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = Math.random() * 1.5;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = Math.random() * 0.5 + 1.5; // velocidad vertical alta
                        Vector velocity = new Vector(x * 0.2, y, z * 0.2); // lateral lento, arriba rápido
                        Location spawnLoc = centerLoc.clone().add(x, 1, z);
                        world.spawnParticle(
                            mainParticle,
                            spawnLoc,
                            0,
                            velocity.getX(), velocity.getY(), velocity.getZ(), 1
                        );
                    }

                    // Explosión pequeña en la base
                    for (int i = 0; i < 30; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = Math.random() * 0.5;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = Math.random() * 0.2;
                        Vector velocity = new Vector(x * 0.1, y, z * 0.1);
                        Location spawnLoc = centerLoc.clone().add(x, 0.5, z);
                        world.spawnParticle(
                            mainParticle,
                            spawnLoc,
                            0,
                            velocity.getX(), velocity.getY(), velocity.getZ(), 1
                        );
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }, 20L + 10L);

        // 3. Dos segundos después de la animación de humo, teletransportar jugadores
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location tpLoc = teleportLocation.clone();
            if (worldName != null) {
                World newWorld = Bukkit.getWorld(worldName);
                if (newWorld != null) {
                    tpLoc.setWorld(newWorld);
                }
            }
            List<Player> toTeleport = world.getPlayers().stream()
                    .filter(p -> p.getLocation().distance(centerLoc) <= 20)
                    .collect(Collectors.toList());
            toTeleport.forEach(p -> p.teleport(tpLoc));
        }, 150L); // 10 ticks (partículas) + 20 (espera) + 60 (humo) + 40 (2s)
    }
}
