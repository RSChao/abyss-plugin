package com.delta.plugins.projectiles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import com.rschao.Plugin;

public class arcaneball {

    private final Player owner;
    private boolean valid = false;
    private Fireball fireball;

    public arcaneball(Location location, Player owner) {
        this.owner = owner;
    }

    public void launch() {
        fireball = (Fireball) owner.getWorld().spawnEntity(owner.getEyeLocation(), EntityType.FIREBALL);
        valid = true;
        fireball.setDirection(owner.getEyeLocation().getDirection().normalize().multiply(10));
        fireball.setIsIncendiary(false);
        fireball.setVisibleByDefault(false);
        fireball.setYield(0);
        fireball.setFireTicks(0);
        startParticleTrail(fireball);

        fireball.setCustomName("determinationBall");
        fireball.setCustomNameVisible(false);
    }
    private void startParticleTrail(Fireball fireball) {
        Bukkit.getScheduler().runTaskTimer(Plugin.getPlugin(Plugin.class), new Runnable() {
            @Override
            public void run() {
                if (!fireball.isValid() || fireball.isDead()) {
                    valid = false;
                    return; // Stop if the fireball is no longer valid
                }

                // Spawn particle trail at the fireball's location
                fireball.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, fireball.getLocation(), 30);
            }
        }, 0L, 1L); // Adjust the delay as needed (here, it's every tick)
    }
    public boolean isValid() {
        return valid;
    }
    public long getDistance(Location loc){
        if(fireball == null) return Long.MAX_VALUE;
        return (long) fireball.getLocation().distance(loc);
    }

}
