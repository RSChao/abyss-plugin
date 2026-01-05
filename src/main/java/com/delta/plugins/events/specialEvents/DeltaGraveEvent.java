package com.delta.plugins.events.specialEvents;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeltaGraveEvent implements Listener {
    private final JavaPlugin plugin = Plugin.getPlugin(Plugin.class);

    // Nuevo: seguimiento por tumba de task ids y jugadores "atrapados"
    private final Map<String, Integer> graveTaskIds = new HashMap<>();
    private final Map<String, Set<UUID>> graveTrappedPlayers = new HashMap<>();

    public DeltaGraveEvent() {
        // Spawn hearts for existing graves and start periodic check
        Bukkit.getScheduler().runTask(plugin, () -> GravesManager.ensureHeartsSpawned());
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Map<String, GravesManager.GraveEntry> graves = GravesManager.getGraves();
        if (graves.isEmpty()) return;
        Location dest = com.rschao.Plugin.getPlugin(com.rschao.Plugin.class).getConfig().getLocation("dimension_d.loc");
        if (dest == null) {
            // si falta la loc destino, no hacemos nada
            return;
        }
        for (Map.Entry<String, GravesManager.GraveEntry> e : graves.entrySet()) {
            String graveName = e.getKey();
            GravesManager.GraveEntry ge = e.getValue();
            if (ge.activator == null || ge.location == null) continue;
            Player activator = Bukkit.getPlayerExact(ge.activator);
            if (activator == null || !activator.isOnline()) continue;
            if (!activator.getWorld().equals(ge.location.getWorld())) continue;
            double dist = activator.getLocation().distance(ge.location);
            if (dist <= 3.0) {
                List<ItemStack> requiredItems = List.of(Items.pureheart_blue, Items.pureheart_red, Items.pureheart_green, Items.pureheart_yellow, Items.pureheart_purple, Items.pureheart_white, Items.pureheart_indigo, Items.pureheart_orange, Items.ruby_whacka_bump);
                boolean hasAll = false;
                // check if activator has all required items
                Map<Integer, ItemStack> foundItems = new HashMap<>();
                for (ItemStack is : activator.getInventory().getContents()) {
                    if (is == null) continue;
                    for (ItemStack req : requiredItems) {
                        if (is.isSimilar(req) && !foundItems.containsValue(req)) {
                            foundItems.put(activator.getInventory().first(is), req);
                        }
                    }
                }
                hasAll = (foundItems.size() == requiredItems.size());
                if (!hasAll) {
                    // not all items present, skip
                    return;
                }
                // Transferir abysses del activador y reemplazar por "???" (se guarda en graves.yml)
                GravesManager.transferAbysses(ge.activator, graveName);

                // teleport activator and every player within 20 blocks of the grave location
                Set<UUID> teleported = new HashSet<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.isOnline()) continue;
                    if (!p.getWorld().equals(ge.location.getWorld())) continue;
                    if (p.getLocation().distance(ge.location) <= 20.0) {
                        p.teleport(dest);
                        teleported.add(p.getUniqueId());
                    }
                }

                // actualizar lista de atrapados para esta tumba
                graveTrappedPlayers.put(graveName, teleported);

                // Si no hay tarea de spawn para esta tumba, crearla
                if (!graveTaskIds.containsKey(graveName)) {
                    int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> spawnZombiesForGrave(graveName, dest), 0L, 20L * 30).getTaskId();
                    graveTaskIds.put(graveName, taskId);
                }
            }
        }
    }


    // Spawn periodic zombies for given graveName at destination location
    private void spawnZombiesForGrave(String graveName, Location dest) {
        Set<UUID> trapped = graveTrappedPlayers.getOrDefault(graveName, Collections.emptySet());
        if (trapped.isEmpty()) {
            // no hay jugadores atrapados, cancelar tarea
            Integer tid = graveTaskIds.remove(graveName);
            if (tid != null) Bukkit.getScheduler().cancelTask(tid);
            return;
        }

        // Elegir jugador al azar entre los atrapados que esté online y en el mismo mundo
        List<Player> candidates = new ArrayList<>();
        for (UUID u : trapped) {
            Player p = Bukkit.getPlayer(u);
            if (p != null && p.isOnline() && p.getWorld().equals(dest.getWorld())) candidates.add(p);
        }
        if (candidates.isEmpty()) {
            // cancelar si ya no hay jugadores válidos
            Integer tid = graveTaskIds.remove(graveName);
            if (tid != null) Bukkit.getScheduler().cancelTask(tid);
            return;
        }

        Player source = candidates.get(new Random().nextInt(candidates.size()));

        // posición aleatoria en radio <=10, misma Y
        double r = Math.random() * 10.0;
        double ang = Math.random() * Math.PI * 2.0;
        double x = dest.getX() + Math.cos(ang) * r;
        double z = dest.getZ() + Math.sin(ang) * r;
        Location spawnLoc = new Location(dest.getWorld(), x, dest.getY(), z);

        // Spawn zombie y copiar armor/health del jugador fuente
        Zombie zed = dest.getWorld().spawn(spawnLoc, Zombie.class);
        try {
            ItemStack[] armor = source.getInventory().getArmorContents();
            zed.getEquipment().setArmorContents(armor);
            zed.getEquipment().setItemInMainHand(source.getInventory().getItemInMainHand());
        } catch (Throwable ignored) {}
        try {
            double sourceMax = source.getAttribute(Attribute.MAX_HEALTH) != null ? source.getAttribute(Attribute.MAX_HEALTH).getValue() : Math.max(20.0, source.getMaxHealth());
            double sourceHealth = source.getHealth();
            if (zed.getAttribute(Attribute.MAX_HEALTH) != null) {
                zed.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Math.max(1.0, sourceMax));
            }
            zed.setHealth(Math.min( (zed.getAttribute(Attribute.MAX_HEALTH) != null ? zed.getAttribute(Attribute.MAX_HEALTH).getValue() : zed.getMaxHealth()), Math.max(1.0, sourceHealth)));
        } catch (Throwable t) {
            // si falla al setear salud, ignorar
        }
        // pequeñas propiedades para que el zombie persista visible
        try {
            zed.setRemoveWhenFarAway(false);
        } catch (Throwable ignored) {}
    }
}
