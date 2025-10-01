package com.delta.plugins.mobs;


import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.items.PitItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Random;

public class MobSpawner {

    private final Random random = new Random();

    public void spawnMob(World world, Player p) {
        // Find a random location within a reasonable range
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml"));
        Location startLocation = getRandomSpawnLocation(world, new Location(Bukkit.getWorld("survival"), config.getInt("tower.center."+ ((int) (PitEvents.getFloor(p)/50)) + ".x"), p.getLocation().getY(), config.getInt("tower.center." + ((int) (PitEvents.getFloor(p)/50)) + ".z")));
        if (startLocation == null) {
            p.sendMessage("null location");
            return; // Could not find a valid spawn location
        }
        boolean found = false;

        for (int i = 0; i < 10; i++) { // Try up to 10 times to find a valid location
            double distance = 3 + (11 * random.nextDouble()); // Random distance between 3 and 14
            double angle = random.nextDouble() * 2 * Math.PI; // Random angle

            // Calculate new location
            double xOffset = distance * Math.cos(angle);
            double zOffset = distance * Math.sin(angle);
            Location newLocation = startLocation.clone().add(xOffset, 0, zOffset);
            if(!isValidSpawnLocation(newLocation)) continue;

            int floor = PitEvents.getFloor(p);
            MobManager.getEntitiesByFloor(startLocation, floor);
            found = true;
            break;

        }
        if(found) return;
        Location loc = p.getLocation();
        if(isValidSpawnLocation(loc)) {
            int floor = PitEvents.getFloor(p);
            MobManager.getEntitiesByFloor(loc, floor);
            return;
        }
        else {
            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), PitItems.floor_key);
            p.sendMessage("No se pudo spawnear mobs, se te ha dado una llave.");
        }

        return; // Could not spawn mob after multiple attempts
    }

    private Location getRandomSpawnLocation(World world, Location loc) {
        // Get a random X and Z coordinate within a reasonable range
        int x = (int) loc.getX()+ random.nextInt(-13, 13); // Example range: -100 to 100
        int z = (int) loc.getZ()+ random.nextInt(-13, 13);
        int y = (int) loc.getY();
        if (y <= 0) {
            return null; // No valid spawn location
        }
        return new Location(world, x, y, z); // Return the random location
    }

    private boolean isValidSpawnLocation(Location location) {
        // Check if the block is not solid and the mob can spawn there
        return location.getBlock().getType() == Material.AIR &&
                location.getBlock().getRelative(0, -1, 0).getType().isSolid() &&
                location.getWorld().getEntities().stream()
                        .noneMatch(entity -> entity.getLocation().distance(location) < 1); // Ensure no other entities are too close
    }
}