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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiveRainbowBump {

    private static Color getBumpColor(ItemStack bump) {
        if (bump.isSimilar(Items.ruby_whacka_bump)) return Color.RED;
        if (bump.isSimilar(Items.bronze_whacka_bump)) return Color.ORANGE;
        if (bump.isSimilar(Items.gold_whacka_bump)) return Color.YELLOW;
        if (bump.isSimilar(Items.emerald_whacka_bump)) return Color.GREEN;
        if (bump.isSimilar(Items.aquamarine_whacka_bump)) return Color.AQUA;
        if (bump.isSimilar(Items.sapphire_whacka_bump)) return Color.BLUE;
        if (bump.isSimilar(Items.amethyst_whacka_bump)) return Color.PURPLE;
        if (bump.isSimilar(Items.silver_whacka_bump)) return Color.SILVER;
        return Color.BLACK; // onyx or unknown
    }
    public static void register() {
        new CommandAPICommand("giverainbowbump")
            .withPermission("whacka.giverainbow")
            .withHelp("Inicia la secuencia para crear el chichón arcoíris", "Requiere los 9 bumps de gema/metal en el inventario del jugador.")
            .withArguments(new LocationArgument("location"), new OnePlayer("player"))
            .executes((sender, args) -> {
                Location baseLoc = ((Location) args.get(0)).clone();
                Player player = (Player) args.get(1);
                baseLoc.getBlock().getLocation().add(0.5, 0.5, 0.5);
                // Lista de los 9 bumps requeridos (onyx + 8)
                List<ItemStack> required = Arrays.asList(
                    Items.onyx_whacka_bump,
                    Items.ruby_whacka_bump,
                    Items.bronze_whacka_bump,
                    Items.gold_whacka_bump,
                    Items.emerald_whacka_bump,
                    Items.aquamarine_whacka_bump,
                    Items.sapphire_whacka_bump,
                    Items.amethyst_whacka_bump,
                    Items.silver_whacka_bump
                );

                // Comprobar si el jugador tiene todos
                for (ItemStack req : required) {
                    if (!player.getInventory().containsAtLeast(req, 1)) {
                        sender.sendMessage("El jugador no tiene todos los bumps requeridos.");
                        return;
                    }
                }

                // Remover los 9 bumps del jugador
                for (ItemStack req : required) {
                    player.getInventory().removeItem(req);
                }

                // Lista para guardar las entidades spawneadas
                List<Item> spawned = new ArrayList<>();

                // Spawn Onyx inmediatamente (no obtenible, sin gravedad)
                Location center = baseLoc.clone();
                Location onyxLoc = center.clone();
                Item onyxItem = center.getWorld().dropItem(onyxLoc, Items.onyx_whacka_bump.clone());
                onyxItem.setPickupDelay(Integer.MAX_VALUE);
                onyxItem.setGravity(false);
                onyxItem.setVelocity(new Vector(0,0,0));
                // flash al aparecer
                center.getWorld().spawnParticle(Particle.FLASH, onyxLoc, 10, 0.3, 0.3, 0.3, 0, Color.BLACK);
                spawned.add(onyxItem);

                // Orden de los 8 que se spawnearán (después de onyx)
                List<ItemStack> order = Arrays.asList(
                    Items.ruby_whacka_bump,
                    Items.bronze_whacka_bump,
                    Items.gold_whacka_bump,
                    Items.emerald_whacka_bump,
                    Items.aquamarine_whacka_bump,
                    Items.sapphire_whacka_bump,
                    Items.amethyst_whacka_bump,
                    Items.silver_whacka_bump
                );

                // Tarea que spawnea cada 40 ticks uno de los 8 en círculo radio 3
                new BukkitRunnable() {
                    int i = 0;
                    final double radius = 3.0;
                    @Override
                    public void run() {
                        if (i >= order.size()) {
                            this.cancel();
                            // Una vez spawneados los 8 (más el onyx = 9), esperar 60 ticks para el final
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // Hacer unas cuantas flashes antes de spawnear el rainbow
                                    new BukkitRunnable() {
                                        int flashes = 0;
                                        @Override
                                        public void run() {
                                            // flash en el centro y en cada bump visible
                                            for (Item it : spawned) {
                                                if (it != null && it.isValid()) {
                                                    Location l = it.getLocation().clone().add(0, 0.5, 0);
                                                    l.getWorld().spawnParticle(Particle.FLASH, l, 8, 0.2, 0.2, 0.2, 0, getBumpColor(it.getItemStack()));
                                                }
                                            }
                                            flashes++;
                                            if (flashes >= 3) {
                                                this.cancel();
                                                // Spawn rainbow, hacerla obtenible y con gravedad
                                                Location rainbowLoc = center.clone().add(0.5, 0.5, 0.5);
                                                Item rainbow = center.getWorld().dropItem(rainbowLoc, Items.rainbow_whacka_bump.clone());
                                                // flash blanco
                                                center.getWorld().spawnParticle(Particle.FLASH, rainbowLoc, 20, 0.4, 0.4, 0.4, 0, Color.WHITE);
                                                // Eliminar los otros items
                                                for (Item it : spawned) {
                                                    if (it != null && it.isValid()) it.remove();
                                                }
                                                rainbow.setVelocity(new Vector(0, 0, 0));
                                                // Configurar rainbow para que caiga y sea obtenible
                                                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                                                    rainbow.setPickupDelay(0);
                                                    rainbow.setGravity(true);
                                                }, 20L); // 20 ticks de retraso antes de que pueda ser recogida
                                            }
                                        }
                                    }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 10);
                                }
                            }.runTaskLater(Plugin.getPlugin(Plugin.class), 60);
                            return;
                        }

                        // Calcular posición circular alrededor del centro (misma Y)
                        double angle = 2 * Math.PI * i / order.size();
                        double x = center.getX() + radius * Math.cos(angle);
                        double z = center.getZ();
                        double y = center.getY() + 0.5 + radius * Math.sin(angle);
                        Location spawnLoc = new Location(center.getWorld(), x + 0.5, y, z + 0.5);

                        // Partícula al aparecer (blanca flash)
                        spawnLoc.getWorld().spawnParticle(Particle.FLASH, spawnLoc, 10, 0.3, 0.3, 0.3, 0, getBumpColor(order.get(i)));

                        Item it = spawnLoc.getWorld().dropItem(spawnLoc, order.get(i).clone());
                        it.setPickupDelay(Integer.MAX_VALUE);
                        it.setGravity(false);
                        it.setVelocity(new Vector(0,0,0));
                        spawned.add(it);

                        i++;
                    }
                }.runTaskTimer(Plugin.getPlugin(Plugin.class), 40, 40); // esperar 40 ticks antes del primer de los 8, y luego cada 40
            })
            .register();
    }
}
