package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

public class pureHeartPillar {
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
        heartMap.put("chaos", com.rschao.items.Items.ChaosHeart);
        heartMap.put("purity", com.rschao.items.Items.PurityHeart);

    }

    public static void register() {
        new CommandAPICommand("pureheartpillar")
                .withPermission("pureheart.spawn")
                .withArguments(new StringArgument("color").replaceSuggestions(ArgumentSuggestions.strings(heartMap.keySet().toArray(new String[0]))))
                .withArguments(new OnePlayer("holder"), new LocationArgument("location"))
                .executes((sender, args) -> {
                    String color = (String) args.get(0);
                    Location loc = ((Location) args.get(2)).clone();
                    Player holder = (Player) args.get(1);
                    for(ItemStack heart : heartMap.values()) {
                        if(heart == null) {
                            sender.sendMessage("§cEl color " + color + " no es válido.");
                            return;
                        }
                    }
                    boolean hasHeart = false;
                    for(ItemStack item: holder.getInventory().getContents()) {
                        if(item != null && item.isSimilar(heartMap.get(color))) {
                            item.setAmount(item.getAmount() - 1);
                            hasHeart = true;
                            break;
                        }
                    }
                    if(!hasHeart) {
                        sender.sendMessage("§cEl jugador no tiene un corazón de ese color.");
                        return;
                    }


                    loc.setX(Math.floor(loc.getX()) + 0.5);
                    loc.setZ(Math.floor(loc.getZ()) + 0.5);

                    // Calcula la dirección en la que mira el jugador y coloca el corazón 3 bloques delante
                    Vector direction = holder.getLocation().getDirection().normalize();
                    Location spawnLoc = holder.getLocation().clone().add(direction.multiply(2));
                    spawnLoc.add(0, 1, 0);

                    Item droppedItem = spawnLoc.getWorld().dropItemNaturally(spawnLoc, heartMap.get(color));
                    droppedItem.setGravity(false);
                    droppedItem.setPickupDelay(Integer.MAX_VALUE);

                    new BukkitRunnable() {
                        double y = 0;
                        @Override
                        public void run() {
                            if (y >= 5) {
                                droppedItem.remove();
                                // Respawn 5 bloques arriba de la ubicación original
                                Location aboveLoc = loc.clone().add(0, 5, 0);
                                Item fallingItem = aboveLoc.getWorld().dropItemNaturally(aboveLoc, heartMap.get(color));
                                fallingItem.setGravity(false);
                                fallingItem.setPickupDelay(Integer.MAX_VALUE);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        // Desciende lentamente hasta la ubicación original
                                        Location current = fallingItem.getLocation();
                                        if (current.getY() <= loc.getY() + 0.1) {
                                            fallingItem.setVelocity(new Vector(0, 0, 0));
                                            fallingItem.teleport(loc);
                                            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), ()->{
                                                // Partícula de destello
                                                loc.getWorld().spawnParticle(Particle.FLASH, loc, 10, 0.5, 0.5, 0.5, 0, Color.WHITE);
                                                fallingItem.remove();
                                                Block b = loc.clone().subtract(0, 5, 0).getBlock();
                                                Material type = b.getType();
                                                b.setType(Material.AIR);

                                                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), ()->{
                                                    b.setType(type);
                                                }, 40);
                                            }, 40);
                                            this.cancel();
                                            return;
                                        }
                                        fallingItem.setVelocity(new Vector(0, -0.05, 0));
                                    }
                                }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 1);

                                this.cancel();
                                return;
                            }
                            droppedItem.setVelocity(new Vector(0, 0.1, 0));
                            spawnLoc.getWorld().spawnParticle(Particle.HEART, droppedItem.getLocation().add(0, -0.1, 0), 1, 0.2, 0.2, 0.2, 0);
                            y += 0.1;
                        }
                    }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 1);
                })
                .register();
    }
}
