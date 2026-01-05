package com.delta.plugins.particles;

import com.delta.plugins.Plugin;
import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.*;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EffectsHandler {
    static EffectManager em = Plugin.getEffectManager();
    public static void testEffect(Location loc) {
        Effect effect = new DnaEffect(em);
        effect.setLocation(loc);
        effect.setMaxIterations(3);
        effect.iterations = effect.getPeriod();
        effect.start();
    }

    public static void starEffect(Location loc, Player player) {
        Effect e = new StarEffect(em);
        e.color = Color.fromRGB(231, 0, 255);
        //e.setLocation(player.getLocation().add(0,5,0));
        DynamicLocation dl = new DynamicLocation(player);
        dl.addOffset(new Vector(0, 5, 0));
        e.setDynamicOrigin(dl);
        e.setMaxIterations(5);
        e.iterations = e.getPeriod()*7;
        e.start();
    }

    public static void WarpEffect(Player player) {
        Effect e = new WarpEffect(em);
        DynamicLocation dl = new DynamicLocation(player);
        dl.addOffset(new Vector(0, -1, 0));
        e.setDynamicOrigin(dl);
        e.setMaxIterations(5);
        e.iterations = e.period*5;
        e.start();
    }

    public static void atomEffect(Location loc) {
        Effect e = new AtomEffect(em);
        DynamicLocation dl = new DynamicLocation(loc);
        dl.addOffset(new Vector(0, 2, 0));
        e.setDynamicOrigin(dl);
        e.particle = Particle.DUST;
        e.setParticleCount(e.getParticleCount()*10);
        e.setParticleSize(e.getParticleSize()*10);
        e.iterations = e.period*10;
        e.start();
    }


    public static void Bum(Player player) {
        Effect e = new BigBangEffect(em);
        e.setLocation(player.getLocation());
        e.start();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getPlugin(Plugin.class), new Runnable(){
            @Override
            public void run() {
                e.cancel();
            }
        }, 20*3L);
    }

    public static void shieldEffect(Location loc) {
        Effect e = new ShieldEffect(em);
        e.setLocation(loc);
        e.start();
    }

    public static void helixEffect(Location loc) {
        Effect e = new HelixEffect(em);
        e.setLocation(loc);
        e.start();
    }

    public static void tornadoEffect(Location loc) {
        TornadoEffect e = new TornadoEffect(em);
        e.setLocation(loc);
        e.maxTornadoRadius = 7;
        e.start();
    }
}
