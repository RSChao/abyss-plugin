package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import dev.jorel.commandapi.arguments.LocationArgument;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SummonPurityHeart {
    private static final Map<String, ItemStack> heartMap = new LinkedHashMap<>();
    static {
        heartMap.put("red", Items.pure_heart_red);
        heartMap.put("brown", Items.pure_heart_brown);
        heartMap.put("blue", Items.pure_heart_blue);
        heartMap.put("cyan", Items.pure_heart_cyan);
        heartMap.put("purple", Items.pure_heart_purple);
        heartMap.put("pink", Items.pure_heart_pink);
        heartMap.put("yellow", Items.pure_heart_yellow);
        heartMap.put("grey", Items.pure_heart_grey);

    }
    public static void register(){
        new CommandAPICommand("pureheartsequence")
                .withPermission("pureheart.sequence")
                .withHelp("Secuencia de corazones puros", "Secuencia especial de corazones puros en posiciones predeterminadas.")
                .withArguments(new LocationArgument("location"))
                .withOptionalArguments(new OnePlayer("player"))
                .executes((sender, args) -> {
                    Location baseLoc = ((Location) args.get(0)).clone();
                    Player player;
                    if (args.count() > 1 && args.get(1) != null) {
                        player = (Player) args.get(1);
                    } else {
                        player = getClosestPlayer(baseLoc);
                    }

                    // Chequear si el jugador tiene todos los corazones puros
                    boolean hasAll = true;
                    for (ItemStack heart : heartMap.values()) {
                        if (!player.getInventory().containsAtLeast(heart, 1)) {
                            hasAll = false;
                            break;
                        }
                    }
                    if (!hasAll) {
                        sender.sendMessage("El jugador no tiene todos los corazones puros.");
                        return;
                    }
                    else{
                        for(ItemStack heart : heartMap.values()){
                            player.getInventory().removeItem(heart);
                        }
                    }

                    // Posiciones predeterminadas para los corazones
                    List<Location> heartLocs = Arrays.asList(
                            baseLoc.clone().add(-1, 0, 1),
                            baseLoc.clone().add(-1, 0, 0),
                            baseLoc.clone().add(-1, 0, -1),
                            baseLoc.clone().add(0, 0, 1),
                            baseLoc.clone().add(0, 0, -1),
                            baseLoc.clone().add(1, 0, 1),
                            baseLoc.clone().add(1, 0, 0),
                            baseLoc.clone().add(1, 0, -1)
                            /*
                            * F: -X
                            * B: +X
                            * L: +Z
                            * R: -Z
                            * */
                    );
                    Map<String, Location> heartPosMap = new LinkedHashMap<>();
                    int idx = 0;
                    for (String color : heartMap.keySet()) {
                        if (idx < heartLocs.size())
                            heartPosMap.put(color, heartLocs.get(idx));
                        idx++;
                    }

                    //Set<Block> removedBlocks = new HashSet<>();
                    List<Item> spawnedHearts = new ArrayList<>();
                    BukkitRunnable[] sequenceTask = new BukkitRunnable[1];
                    BukkitRunnable[] monitorTask = new BukkitRunnable[1];

                    // Secuencia de aparición de corazones
                    sequenceTask[0] = new BukkitRunnable() {
                        int i = 0;
                        @Override
                        public void run() {
                            if (i >= heartPosMap.size()) {
                                // Esperar 20 ticks antes de iniciar el monitor de distancia
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        monitorTask[0] = new BukkitRunnable() {
                                            boolean triggered = false;
                                            @Override
                                            public void run() {
                                                double dist = player.getLocation().distance(baseLoc);
                                                if (!triggered && dist <= 6) {
                                                    triggered = true;
                                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawnpureheart purity " + baseLoc.getBlockX() + " " + (baseLoc.getBlockY()-1) + " " + baseLoc.getBlockZ() + " " + player.getWorld().getName());
                                                    // Destruir corazones y reponer bloques tras 5s
                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            for (Item item : spawnedHearts) {
                                                                if (item.isValid()) item.remove();
                                                            }

                                                        }
                                                    }.runTaskLater(Plugin.getPlugin(Plugin.class), 100);
                                                    this.cancel();
                                                } else if (!triggered && dist >= 7) {
                                                    // Cancelar secuencia, eliminar corazones y restaurar bloques
                                                    for (Item item : spawnedHearts) {
                                                        if (item.isValid()) item.remove();
                                                    }
                                                    for(ItemStack heart : heartMap.values()){
                                                        player.getInventory().addItem(heart);
                                                    }
                                                    this.cancel();
                                                }
                                            }
                                        };
                                        monitorTask[0].runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 10);
                                    }
                                }.runTaskLater(Plugin.getPlugin(Plugin.class), 20);
                                this.cancel();
                                return;
                            }
                            String color = (String) heartPosMap.keySet().toArray()[i];
                            Location loc = heartPosMap.get(color).clone();
                            loc.setX(loc.getBlockX() + 0.5);
                            loc.setY(loc.getBlockY() + 0.5);
                            loc.setZ(loc.getBlockZ() + 0.5);
                            loc.add(0, 0.5, 0);
                            // Partículas y aparición
                            loc.getWorld().spawnParticle(Particle.FLASH, loc, 10, 0.3, 0.3, 0.3, 0, Color.WHITE);
                            ItemStack heart = heartMap.get(color);

                            // Usar Item flotante en vez de ItemDisplay
                            Item item = loc.getWorld().dropItem(loc, heart.clone());
                            item.setPickupDelay(Integer.MAX_VALUE);
                            item.setGravity(false);
                            item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                            spawnedHearts.add(item);

                            i++;
                        }
                    };
                    // Lanzar secuencia, 1 corazón por segundo
                    sequenceTask[0].runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 20);
                })
                .register();
    }
    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = 7;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != location.getWorld()) continue; // Skip players in different worlds
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }
}
