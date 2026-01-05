package com.delta.plugins.mobs;


import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.items.PitItems;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobSpawner {

    private final Random random = new Random();

    public void spawnMob(World world, Player p) {
        // Find a random location within a reasonable range
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml"));

        int floor = PitEvents.getFloor(p);

        // Obtiene lista de puntos de mobs y spawn principal desde config (tower.<floor>.mobs y tower.<floor>.spawn)
        List<Location> mobSpots = (List<Location>) config.get("tower." + floor + ".mobs", new ArrayList<Location>());
        Location configuredSpawn = config.getLocation("tower." + floor + ".spawn");
        for(Location loc : mobSpots) {
            if (configuredSpawn != null && Math.abs(loc.getY() - configuredSpawn.getY()) > 10) {
                p.sendMessage("La ubicación de spawn configurada no coincide con los spawner de mobs.");
                p.sendMessage("Revisar configuración de towerdata.yml");
            }
        }

        // Intentar distribuir mobs a través de todas las ubicaciones configuradas (scatter)
        if (!mobSpots.isEmpty()) {
            try {
                List<org.bukkit.entity.Entity> spawned = MobManager.getEntitiesByFloor(mobSpots, floor);
                if (spawned != null && !spawned.isEmpty()) {
                    return; // Éxito en scatter
                }
                // si spawned está vacío, caerá a la lógica de spawn anterior
            } catch (Exception ex) {
                // Si ocurre cualquier error, hacemos fallback al sistema actual
            }
        }

        // Si no hay mobSpots o scatter falló, proceder con el comportamiento previo:
        Location startLocation = null;
        if (!mobSpots.isEmpty()) {
            startLocation = mobSpots.get(random.nextInt(mobSpots.size()));
            MobManager.getEntitiesByFloor(mobSpots, floor);
        } else if (configuredSpawn != null) {
            startLocation = configuredSpawn;
        }
        if (startLocation != null) {
            MobManager.getEntitiesByFloor(startLocation, floor);
            return;
        }
        boolean found = false;

        double tower = Math.floor(PitEvents.getFloor(p)/ 50);
        int x = config.getInt("tower.center." + (int) tower + ".x", 0);
        int z = config.getInt("tower.center." + (int) tower + ".z", 0);
        startLocation = new Location(world, x, world.getHighestBlockYAt(x, z), z); // evitar NPE usando una Y válida

        for (int i = 0; i < 10; i++) { // Try up to 10 times to find a valid location
            double distance = 3 + (11 * random.nextDouble()); // Random distance between 3 and 14
            double angle = random.nextDouble() * 2 * Math.PI; // Random angle

            // Calculate new location based on chosen startLocation
            double xOffset = distance * Math.cos(angle);
            double zOffset = distance * Math.sin(angle);
            Location newLocation = startLocation.clone().add(xOffset, 0, zOffset);
            if(!isValidSpawnLocation(newLocation)) continue;

            MobManager.getEntitiesByFloor(newLocation, floor);
            found = true;
            break;

        }
        if(found) return;
        Location loc = p.getLocation();
        if(isValidSpawnLocation(loc)) {
            MobManager.getEntitiesByFloor(loc, floor);
            return;
        }
        else {
            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), PitItems.floor_key);
            p.sendMessage("No se pudo spawnear mobs, se te ha dado una llave.");
        }

        return; // Could not spawn mob after multiple attempts
    }

    private Location getRandomSpawnLocation(Location loc) {
        // ahora no se usa en la implementación principal pero lo dejo por compatibilidad si lo quieres reutilizar
        int x = (int) loc.getX() + random.nextInt(-13, 13);
        int z = (int) loc.getZ() + random.nextInt(-13, 13);
        int y = (int) loc.getY();
        if (y <= 0) {
            return null; // No valid spawn location
        }
        return new Location(loc.getWorld(), x, y, z); // Return the random location
    }

    private boolean isValidSpawnLocation(Location location) {
        // Check if the block is not solid and the mob can spawn there
        return (!location.getBlock().getType().isSolid()); // Ensure no other entities are too close
    }
}