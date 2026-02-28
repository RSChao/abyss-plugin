package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SummonChaosHeart {
    private static final Map<String, ItemStack> heartMap = new LinkedHashMap<>();
    static {
        heartMap.put("red", Items.pureheart_red);
        heartMap.put("orange", Items.pureheart_orange);
        heartMap.put("yellow", Items.pureheart_yellow);
        heartMap.put("green", Items.pureheart_green);
        heartMap.put("blue", Items.pureheart_blue);
        heartMap.put("indigo", Items.pureheart_indigo);
        heartMap.put("purple", Items.pureheart_purple);
        heartMap.put("white", Items.pureheart_white);
    }

    public static void register(){
        new CommandAPICommand("chaosheartsequence")
                .withPermission("chaosheart.sequence")
                .withHelp("Secuencia de corazones puros y Chaos Heart", "Secuencia especial de corazones puros y aparición animada del Chaos Heart.")
                .withArguments(new LocationArgument("location"), new IntegerArgument("radius", 1, 10))
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
                    } else {
                        for(ItemStack heart : heartMap.values()){
                            player.getInventory().removeItem(heart);
                        }
                    }


                    int radius = (int) args.get("radius");
                    // Posiciones predeterminadas para los corazones
                    List<Location> heartLocs = Arrays.asList(
                            baseLoc.clone().add(-radius, 0, radius),
                            baseLoc.clone().add(-radius, 0, 0),
                            baseLoc.clone().add(-radius, 0, -radius),
                            baseLoc.clone().add(0, 0, radius),
                            baseLoc.clone().add(0, 0, -radius),
                            baseLoc.clone().add(radius, 0, radius),
                            baseLoc.clone().add(radius, 0, 0),
                            baseLoc.clone().add(radius, 0, -radius)
                    );
                    Map<String, Location> heartPosMap = new LinkedHashMap<>();
                    int idx = 0;
                    for (String color : heartMap.keySet()) {
                        if (idx < heartLocs.size())
                            heartPosMap.put(color, heartLocs.get(idx));
                        idx++;
                    }

                    Set<Block> removedBlocks = new HashSet<>();
                    List<Item> spawnedHearts = new ArrayList<>();

                    // Secuencia de aparición de corazones
                    new BukkitRunnable() {
                        int i = 0;
                        @Override
                        public void run() {
                            if (i >= heartPosMap.size()) {
                                // Esperar 30 ticks y luego aparecer el Chaos Heart animado
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Location chaosStart = baseLoc.clone().add(0, -2, 0);
                                        Location chaosEnd = baseLoc.clone();
                                        chaosStart.setX(chaosStart.getBlockX() + 0.5);
                                        chaosStart.setY(chaosStart.getBlockY() + 0.5);
                                        chaosStart.setZ(chaosStart.getBlockZ() + 0.5);
                                        chaosEnd.setX(chaosEnd.getBlockX() + 0.5);
                                        chaosEnd.setY(chaosEnd.getBlockY() + 0.5);
                                        chaosEnd.setZ(chaosEnd.getBlockZ() + 0.5);

                                        ItemStack chaosHeart = com.rschao.items.Items.ChaosHeart.clone();
                                        Item chaosItem = chaosStart.getWorld().dropItem(chaosStart, chaosHeart);
                                        chaosItem.setGravity(false);
                                        chaosItem.setVelocity(new org.bukkit.util.Vector(0, 0.1, 0));


                                        new BukkitRunnable() {
                                            int tick = 0;
                                            @Override
                                            public void run() {
                                                if (!chaosItem.isValid() || chaosItem.isDead()) {
                                                    this.cancel();
                                                    return;
                                                }
                                                Location loc = chaosItem.getLocation();
                                                loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 0.2, 0), 10, Math.random()/2, Math.random()/2, Math.random()/2, new Particle.DustOptions(Color.BLACK, 1));
                                            }
                                        }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 1);

                                        // Animar subida en 40 ticks (2 segundos)
                                        new BukkitRunnable() {
                                            int tick = 0;
                                            @Override
                                            public void run() {
                                                if (!chaosItem.isValid() || chaosItem.isDead()) {
                                                    this.cancel();
                                                    return;
                                                }
                                                double progress = Math.min(1.0, tick / 40.0);
                                                double newY = chaosStart.getY() + (chaosEnd.getY() - chaosStart.getY()) * progress;
                                                chaosItem.teleport(new Location(
                                                        chaosStart.getWorld(),
                                                        chaosStart.getX(),
                                                        newY,
                                                        chaosStart.getZ()
                                                ));
                                                tick++;
                                                if (tick > 40) {
                                                    // Flotar en la posición final
                                                    chaosItem.teleport(chaosEnd);
                                                    this.cancel();
                                                }
                                            }
                                        }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 1);

                                        // Eliminar corazones visuales y restaurar bloques tras 5s
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                for (Item item : spawnedHearts) {
                                                    if (item.isValid()) item.remove();
                                                }
                                                for (Block b : removedBlocks) {
                                                    b.setType(Material.GRASS_BLOCK);
                                                }
                                            }
                                        }.runTaskLater(Plugin.getPlugin(Plugin.class), 100);
                                    }
                                }.runTaskLater(Plugin.getPlugin(Plugin.class), 30);
                                this.cancel();
                                return;
                            }
                            String color = (String) heartPosMap.keySet().toArray()[i];
                            Location loc = heartPosMap.get(color).clone();
                            loc.setX(loc.getBlockX() + 0.5);
                            loc.setY(loc.getBlockY() + 0.5);
                            loc.setZ(loc.getBlockZ() + 0.5);
                            loc.add(0, 0.5, 0);

                            /*Block below = loc.clone().add(0, -5.5, 0).getBlock();
                            if (below.getType() != Material.AIR) {
                                removedBlocks.add(below);
                                below.setType(Material.AIR);
                            }*/
                            // Partículas y aparición
                            loc.getWorld().spawnParticle(Particle.FLASH, loc, 10, 0.3, 0.3, 0.3, 0, Color.BLACK);
                            ItemStack heart = heartMap.get(color);

                            // Usar Item flotante en vez de ItemDisplay
                            Item item = loc.getWorld().dropItem(loc, heart.clone());
                            item.setPickupDelay(Integer.MAX_VALUE);
                            item.setGravity(false);
                            item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                            spawnedHearts.add(item);

                            i++;
                        }
                    }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 20);
                })
                .register();
    }

    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != location.getWorld()) continue; // Skip players in different worlds
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;}
        }
        return closestPlayer;
    }
}
