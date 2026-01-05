package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpawnPureHeart {
    private static final Map<String, ItemStack> heartMap = new LinkedHashMap<>();
    static void setMap(){

        heartMap.put("red", Items.pure_heart_red);
        heartMap.put("brown", Items.pure_heart_brown);
        heartMap.put("blue", Items.pure_heart_blue);
        heartMap.put("cyan", Items.pure_heart_cyan);
        heartMap.put("purple", Items.pure_heart_purple);
        heartMap.put("pink", Items.pure_heart_pink);
        heartMap.put("yellow", Items.pure_heart_yellow);
        heartMap.put("grey", Items.pure_heart_grey);
        heartMap.put("chaos", com.rschao.items.Items.ChaosHeart);
        heartMap.put("purity", com.rschao.items.Items.PurityHeart);
    }
    public static void register() {
        setMap();
        new CommandAPICommand("spawnpureheart")
            .withPermission("pureheart.spawn")
            .withHelp("Genera un corazón puro", "Genera un corazón puro en la ubicación especificada con un color específico.")
            .withArguments(new StringArgument("color").replaceSuggestions(ArgumentSuggestions.strings(heartMap.keySet().toArray(new String[0]))))
            .withArguments(new LocationArgument("location"))
            .withOptionalArguments(new StringArgument("world"))
            .executes((sender, args) -> {
                String color = (String) args.get(0);
                Location loc = ((Location) args.get(1)).clone();
                // Si se especifica el mundo, usarlo
                if (args.count() > 2 && args.get(2) != null) {
                    String worldName = (String) args.get(2);
                    if (Bukkit.getWorld(worldName) == null) {
                        sender.sendMessage("El mundo especificado no existe.");
                        return;
                    }
                    loc.setWorld(Bukkit.getWorld(worldName));
                }
                loc.setX(loc.getBlockX() + 0.5);
                loc.setY(loc.getBlockY() + 0.5);
                loc.setZ(loc.getBlockZ() + 0.5);
                loc.add(0, 0.5, 0);
                ItemStack heart = heartMap.get(color);
                if (heart == null) {
                    sender.sendMessage("Color de corazón puro inválido.");
                    return;
                }

                // Primer y segundo destello
                for (int i = 0; i < 2; i++) {
                    int delay = i * 5;
                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                        loc.getWorld().spawnParticle(Particle.FLASH, loc, 10, 0.3, 0.3, 0.3, 0, Color.WHITE);
                    }, delay);
                }

                // Tercer destello y aparición del corazón puro al mismo tiempo (20 ticks)
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                    loc.getWorld().spawnParticle(Particle.FLASH, loc, 20, 0.3, 0.3, 0.3, 0, Color.WHITE);
                    Location spawnLoc = loc.clone();
                    Item dropped = spawnLoc.getWorld().dropItem(spawnLoc, heart.clone());
                    dropped.setVelocity(dropped.getVelocity().zero());
                    dropped.setGravity(false);
                    // Opcional: hacer que flote suavemente
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (!dropped.isValid() || dropped.isOnGround() || ticks > 40) {
                                dropped.setGravity(true);
                                this.cancel();
                                return;
                            }
                            dropped.setVelocity(dropped.getVelocity().zero());
                            ticks++;
                        }
                    }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 1);
                }, 20);
            })
            .register();
    }
}
