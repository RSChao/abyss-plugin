package com.delta.plugins.whacka;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class WhackaLocationManager {
    private static final String LOCATIONS_KEY = "whacka.locations";
    private static FileConfiguration config() {
        return JavaPlugin.getProvidingPlugin(WhackaLocationManager.class).getConfig();
    }

    public static void addLocation(Location loc) {
        List<String> locs = getLocationStrings();
        locs.add(serialize(loc));
        config().set(LOCATIONS_KEY, locs);
        save();
    }

    public static List<Location> getLocations() {
        List<Location> result = new ArrayList<>();
        for (String s : getLocationStrings()) {
            result.add(deserialize(s));
        }
        return result;
    }

    private static List<String> getLocationStrings() {
        List<String> locs = config().getStringList(LOCATIONS_KEY);
        return locs == null ? new ArrayList<>() : new ArrayList<>(locs);
    }

    private static void save() {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(WhackaLocationManager.class);
        plugin.saveConfig();
        plugin.reloadConfig();
    }

    private static String serialize(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private static Location deserialize(String s) {
        String[] parts = s.split(",");
        return new Location(
            Bukkit.getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3])
        );
    }
}
