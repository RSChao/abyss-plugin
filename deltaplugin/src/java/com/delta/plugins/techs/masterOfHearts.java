package com.delta.plugins.techs;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import com.delta.plugins.items.Items;
import java.util.*;

public class masterOfHearts {
    static final String TECH_ID = "masterOfHearts";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);
    public static void register() {
        //com.delta.plugins.Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, heartCannon);
    }

    static Technique heartCannon = new Technique("heartcannon", "Heart cannon", false, cooldownHelper.secondsToMiliseconds(60), (player, channeler, args) -> {
        // 1. Flotar al jugador y fijar velocidad a 0
        Location startLoc = player.getLocation().clone().add(0, 1, 0);
        player.teleport(startLoc);
        player.setAllowFlight(true);
        player.setFlying(true);

        // Mantener la velocidad en 0 durante 6 segundos
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                player.setVelocity(new Vector(0, 0, 0));
                ticks++;
                if (ticks > 120) { // 6 segundos
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        // 2. Aparecer corazones en círculo relativo a la cámara, 2 bloques delante
        List<ItemStack> hearts = Arrays.asList(
            Items.pure_heart_red, Items.pure_heart_brown, Items.pure_heart_blue,
            Items.pure_heart_cyan, Items.pure_heart_purple, Items.pure_heart_pink,
            Items.pure_heart_yellow, Items.pure_heart_grey
        );
        List<Item> spawned = new ArrayList<>();
        Location center = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(2));
        Vector forward = player.getEyeLocation().getDirection().normalize();
        Vector up = new Vector(0, 1, 0);
        Vector right = forward.clone().getCrossProduct(up).normalize();
        up = right.clone().getCrossProduct(forward).normalize(); // up perpendicular a forward y right

        double radius = 1.5;
        int n = hearts.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            Vector circleVec = right.clone().multiply(Math.cos(angle)).add(up.clone().multiply(Math.sin(angle))).multiply(radius);
            Location itemLoc = center.clone().add(circleVec);
            Item item = player.getWorld().dropItem(itemLoc, hearts.get(i));
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setGravity(false);
            item.setVelocity(new Vector(0,0,0));
            spawned.add(item);
        }
        final Vector finalUp = up;

        // 3 segundos después, girar el círculo y acumular partículas en el centro
        new BukkitRunnable() {
            int ticks = 0;
            double spinAngle = 0;
            double spinSpeed = Math.PI / 30; // velocidad inicial
            @Override
            public void run() {
                if (ticks == 0) {
                    hotbarMessage.sendHotbarMessage(player, "¡El ritual comienza a girar!");
                }
                // Cada segundo aumenta la velocidad de giro
                if (ticks % 20 == 0 && ticks > 0) {
                    spinSpeed *= 1.25;
                }
                // Girar los items
                spinAngle += spinSpeed;
                for (int i = 0; i < n; i++) {
                    double angle = 2 * Math.PI * i / n + spinAngle;
                    Vector circleVec = right.clone().multiply(Math.cos(angle)).add(finalUp.clone().multiply(Math.sin(angle))).multiply(radius);
                    Location itemLoc = center.clone().add(circleVec);
                    spawned.get(i).teleport(itemLoc);
                }
                // Acumular partículas en el centro
                player.getWorld().spawnParticle(Particle.HEART, center, 2, 0.2, 0.2, 0.2, 0.1);

                ticks++;
                if (ticks > 140) { // 7 segundos de giro
                    // 4. Matar entidades vivas 20 bloques delante del jugador
                    Location front = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(10));
                    Vector dir = player.getLocation().getDirection().normalize();
                    for (LivingEntity ent : player.getWorld().getLivingEntities()) {
                        if (ent == player) continue;
                        Vector toEnt = ent.getLocation().toVector().subtract(player.getEyeLocation().toVector());
                        double dot = toEnt.normalize().dot(dir);
                        double dist = ent.getLocation().distance(player.getEyeLocation());
                        if (dot > 0.85 && dist < 20) {
                            ent.damage(9999, player);
                        }
                    }
                    // Eliminar los items
                    for (Item it : spawned) it.remove();
                    // Disparar rayo de partículas
                    Location start = center.clone();
                    Vector rayDir = dir.clone();
                    for (double d = 0; d < 20; d += 0.5) {
                        Location point = start.clone().add(rayDir.clone().multiply(d));
                        player.getWorld().spawnParticle(Particle.END_ROD, point, 2, 0.05, 0.05, 0.05, 0.01);
                    }
                    hotbarMessage.sendHotbarMessage(player, "¡El ritual ha terminado!");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 60, 1); // empieza tras 3 segundos (60 ticks)
    });
}
