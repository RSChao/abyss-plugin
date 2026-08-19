package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.TechFlagEvents;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class BlenderOfDoom {

    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register(String group){
        TechRegistry.registerTechnique(group, blenderOfDoom);
    }

    static Technique blenderOfDoom = new Technique("supreme:blender_of_doom", "Supreme Magic: Spinning wheel of doom", new TechniqueMeta(true, cooldownHelper.hour, List.of("")), TargetSelectors.radialPlayers(100), (ctx, token) -> {
        List<String> dialogue = List.of(
                "I am thou, and thou art I.",
                "Heed my call, God of Oblivion",
                "May the final doom rain upon us all",
                "Death become my blade once more.",
                "Slay my enemies, and let the world be cleansed.",
                "In the name of " + ctx.caster().getName() + ((ctx.caster().getName().contains("Delta") || ctx.caster().getName().contains("Romero")) ? ", the Second God of Showdown" : ", the one worthy of the Second God's power") + ", i cast",
                ChatColor.BLACK + (ChatColor.BOLD + "Supreme Magic: Spinning Wheel of Doom") + ChatColor.RESET + "!"
        );

        for(int i = 0; i < dialogue.size(); i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(finalI == dialogue.size() - 1){
                        p.sendTitle(dialogue.get(finalI), "", 10, 70, 20);
                    }
                    else {
                        p.sendMessage(dialogue.get(finalI));
                    }
                }

            }, i*30L); // Delay of 30 ticks (1.5 seconds)
        }

            // Start effects after the dialogue finishes
            long startDelay = dialogue.size() * 30L + 20L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                java.util.Collection<org.bukkit.entity.LivingEntity> targets = ctx.targets();
                org.bukkit.entity.LivingEntity caster = ctx.caster();

                java.util.Map<org.bukkit.entity.LivingEntity, org.bukkit.Location> centers = new java.util.HashMap<>();
                for (org.bukkit.entity.LivingEntity ent : targets) {
                    if (ent == null) continue;
                    if (ent.equals(caster)) continue; // exclude caster
                    centers.put(ent, ent.getLocation().clone());
                }

                // Spinner runnable: teleports targets in a circle for ~20 seconds
                new org.bukkit.scheduler.BukkitRunnable() {
                    int ticks = 0;
                    final int durationTicks = 20 * 15; // 20 seconds
                    final double radius = 10.0;
                    final double angularSpeed = 1.0; // radians per tick (fast)

                    @Override
                    public void run() {
                        ticks++;

                        for (java.util.Map.Entry<org.bukkit.entity.LivingEntity, org.bukkit.Location> entry : centers.entrySet()) {
                            org.bukkit.entity.LivingEntity e = entry.getKey();
                            org.bukkit.Location center = entry.getValue();
                            if (e == null || !e.isValid()) continue;

                            double angle = angularSpeed * ticks;
                            double x = center.getX() + Math.cos(angle) * radius;
                            double z = center.getZ() + Math.sin(angle) * radius;

                            org.bukkit.Location loc = center.clone();
                            loc.setX(x);
                            loc.setZ(z);
                            loc.setYaw((float) Math.toDegrees(angle));
                            loc.setPitch(0f);

                            try {
                                e.teleport(loc);
                            } catch (Exception ignored) { }
                        }

                        // Every 5 seconds (100 ticks) deal massive damage and then apply effects the next tick
                        if (ticks % 30 == 0) {
                            for (org.bukkit.entity.LivingEntity e : centers.keySet()) {
                                if (e == null || !e.isValid()) continue;
                                try {
                                    e.damage(1000.0, caster);
                                } catch (Exception ignored) { }

                                org.bukkit.entity.LivingEntity targetForEffects = e;
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    if (targetForEffects == null || !targetForEffects.isValid()) return;
                                    // Darkness for 30s, Nausea (Confusion) level 2 for 10s, Slowness 255 for 5s
                                    targetForEffects.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, 20 * 30, 0));
                                    targetForEffects.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.NAUSEA, 20 * 10, 1));
                                    targetForEffects.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.SLOWNESS, 20 * 5, 254));
                                }, 1L);
                            }
                        }

                        if (ticks >= durationTicks) {
                            // Stop spinning
                            this.cancel();

                            // After the circle ends: placeholder runnable and heal the caster
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                for(LivingEntity e : targets){
                                    TechFlagEvents.techFullBans.put(e.getUniqueId(), true);
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        TechFlagEvents.techFullBans.put(e.getUniqueId(), false);
                                    }, 20*30L); // 30 seconds ban
                                }
                                try {
                                    if (caster != null && caster.isValid()) {
                                        double max = caster.getMaxHealth();
                                        caster.setHealth(max);
                                    }
                                } catch (Exception ignored) { }
                            });
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);

            }, startDelay);
        });



    public static void saveGroup(String group) {
        // Save the group to the config
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        config.set("blender_groups", group);
        plugin.saveConfig();
    }

    public static String getGroup() {
        // Retrieve the group from the config
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        return config.getString("blender_groups", "default_group");
    }
}
