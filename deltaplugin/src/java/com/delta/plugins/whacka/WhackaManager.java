package com.delta.plugins.whacka;

import com.delta.plugins.events.PitEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class WhackaManager {
    private static Silverfish whacka = null;
    private static Location currentSpawn = null;
    private static long nextSpawnTime = 0L; // system time in ms

    public static void init() {
    }

    public static void assignRandomSpawn() {
        List<Location> locs = WhackaLocationManager.getLocations();
        if (locs.isEmpty()) {
            currentSpawn = null;
            return;
        }
        currentSpawn = locs.get(new Random().nextInt(locs.size()));
    }

    public static Location getCurrentSpawn() {
        return currentSpawn;
    }

    public static void trySpawnWhackaIfPlayerNearby() {
        if (currentSpawn == null) return;
        if (whacka != null && !whacka.isDead() && whacka.isValid()) return;
        if (System.currentTimeMillis() < nextSpawnTime) return; // cooldown check
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(currentSpawn.getWorld()) && p.getLocation().distance(currentSpawn) <= 50) {
                if(PitEvents.getFloor(p) != 0) continue; // don't spawn if any player is in the Pit
                spawnWhacka(currentSpawn);
                break;
            }
        }
    }

    public static void spawnWhacka(Location loc) {
        if (whacka != null && whacka.isValid()) whacka.remove();
        whacka = loc.getWorld().spawn(loc, Silverfish.class, e -> {
            e.setCustomName("Whacka");
            e.setCustomNameVisible(true);
            e.setRemoveWhenFarAway(false);
            e.setSilent(true);
            e.setGlowing(true);
            e.setPersistent(true);
            e.setAI(true);
            e.setInvisible(false);
            e.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100.0);
            e.setHealth(100.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
            e.getAttribute(Attribute.SCALE).setBaseValue(10.0);
        });
    }


    public static Entity spawnWhackaEntity(Location loc) {
        whacka = loc.getWorld().spawn(loc, Silverfish.class, e -> {
            e.setCustomName("Whacka");
            e.setCustomNameVisible(true);
            e.setRemoveWhenFarAway(false);
            e.setSilent(true);
            e.setGlowing(true);
            e.setPersistent(true);
            e.setAI(true);
            e.setInvisible(false);
            e.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100.0);
            e.setHealth(100.0);
            e.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
            e.getAttribute(Attribute.SCALE).setBaseValue(10.0);
        });

        return whacka;
    }

    public static Silverfish getWhacka() {
        return (whacka != null && whacka.isValid() && !whacka.isDead()) ? whacka : null;
    }

    public static void removeWhacka() {
        if (whacka != null) whacka.remove();
        whacka = null;
    }

    public static void setCooldown(long millis) {
        nextSpawnTime = System.currentTimeMillis() + millis;
    }
}
