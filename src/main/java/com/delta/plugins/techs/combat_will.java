package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;

public class combat_will {
    static final String TECH_ID = "combat_will";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, time_stop);
        TechRegistry.registerTechnique(TECH_ID, blackFlash);
        TechRegistry.registerTechnique(TECH_ID, damageGoBack);
        TechRegistry.registerTechnique(TECH_ID, orbitalStrike);
    }


    static Technique time_stop = new Technique("combat_will_time_stop", "Combat Will: Time Stop", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Stops all nearby entities in place for 5 seconds.")), com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors.self(), (techUser, target) -> {;
        for(org.bukkit.entity.Entity entity : techUser.caster().getNearbyEntities(10, 10, 10)) {
            if(entity instanceof LivingEntity le){
                le.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(100, 255));
                le.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    le.removePotionEffect(PotionEffectType.SLOWNESS);
                    le.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.41999998697815);
                }, 100);
            }

        }
        techUser.caster().sendMessage("You have activated Combat Will: Time Stop! Nearby entities are frozen for 5 seconds.");
    });

    static Technique blackFlash = new Technique(
            "punchy",
            "One-Punch Aura",
            new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Temporarily set deadly punch flag.")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                events.hasDeadlyPunch.put(player.getUniqueId(), true);
                hotbarMessage.sendHotbarMessage(player, "You have activated One-Punch Aura! Your next punch will be deadly.");
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                    if (events.hasDeadlyPunch.getOrDefault(player.getUniqueId(), false)) {
                        events.hasDeadlyPunch.put(player.getUniqueId(), false);
                        hotbarMessage.sendHotbarMessage(player, "One-Punch Aura has worn off! Your punches are no longer deadly.");
                    }
                }, 20 * 10);
            }
    );

    static Technique damageGoBack = new Technique(
            "deflecty",
            "Damage Deflect",
            new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Temporarily deflect damage taken.")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                events.hasDeflect.put(player.getUniqueId(), true);
                hotbarMessage.sendHotbarMessage(player, "You have activated Damage Deflect! Lasts 3 seconds.");
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                    if (events.hasDeflect.getOrDefault(player.getUniqueId(), false)) {
                        events.hasDeflect.put(player.getUniqueId(), false);
                    }
                }, 20 * 3);
            }
    );


    static Technique orbitalStrike = new Technique("orbital_boom", "Will of Destruction: Orbital Strike", new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(6), List.of("Calls down an orbital strike on a target location.")), com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors.self(), (techUser, target) -> {
        Player player = techUser.caster();
        // centro de la strike (se usa la posición original del jugador)
        Location center = player.getLocation().clone();
        World world = center.getWorld();
        if (world == null) return;

        // hacer al jugador "flotar": teletransportarlo 3 bloques arriba y darle Slow Falling
        Location up = center.clone().add(0, 3, 0);
        player.teleport(up);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 6, 0, true, false, true));

        // radios a usar para partículas y explosiones
        double[] radii = {3.0, 50.0};

        // tarea que genera partículas en círculo durante 2 segundos (40 ticks), ejecutando cada 2 ticks
        new BukkitRunnable() {
            int runs = 0;
            final double angleStep = Math.PI / 10.0;
            @Override
            public void run() {
                if (runs >= 20) { // 20 ejecuciones * 2 ticks = 40 ticks = 2s
                    cancel();
                    // después de 2 segundos: crear explosiones en cada ángulo para ambos radios
                    for (double angle = 0.0; angle < Math.PI * 2.0 - 1e-6; angle += angleStep) {
                        for (double r : radii) {
                            double x = Math.cos(angle) * r;
                            double z = Math.sin(angle) * r;
                            Location expLoc = center.clone().add(x, 0, z);
                            // power 100, setFire=false, breakBlocks=true (destructivo)
                            world.createExplosion(expLoc, 100f, false, true, player);
                        }
                    }
                    // quitar el efecto de slow falling poco después (para que el jugador vuelva a caer con normalidad)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    }, 20L);
                    return;
                }

                // generar partículas para ambos radios en la misma Y que el centro
                double y = center.getY();
                for (double angle = 0.0; angle < Math.PI * 2.0 - 1e-6; angle += angleStep) {
                    for (double r : radii) {
                        double x = Math.cos(angle) * r;
                        double z = Math.sin(angle) * r;
                        Location pLoc = new Location(world, center.getX() + x, y, center.getZ() + z);
                        world.spawnParticle(Particle.FIREWORK, pLoc, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }

                runs++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.sendMessage("You have activated Will of Destruction: Orbital Strike (charging)!");
    });
}
