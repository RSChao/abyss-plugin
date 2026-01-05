package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techapi.tech.cooldown.CooldownManager;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class clowns {

    static final String TECH_ID = "clowns";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, merrygoround);
        TechRegistry.registerTechnique(TECH_ID, geno);
        TechRegistry.registerTechnique(TECH_ID, swapPlayersNearby); // Register new technique
    }

    static Technique merrygoround = new Technique(
        "merrygoround",
        "Up and down on this merry-go-round",
        false,
        20 * 60*1000, // 1 minute cooldown
        (player, item, args) -> {
            Location center = player.getLocation().clone().add(0, 3, 0);
            World world = player.getWorld();
            List<Player> targets = new ArrayList<>();
            for (Player p : world.getPlayers()) {
                if (!p.equals(player) && p.getLocation().distance(player.getLocation()) <= 20) {
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                    targets.add(p);
                }
            }
            int n = targets.size();
            if (n == 0) {
                player.sendMessage("No players nearby to spin!");
                return;
            }
            // Store original y offsets for each player for off-sync
            double[] phaseOffsets = new double[n];
            for (int i = 0; i < n; i++) {
                phaseOffsets[i] = Math.random() * Math.PI * 2;
            }
            // Apply levitation to prevent falling
            for (Player p : targets) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 120, 1, false, false));
            }
            // Teleport players to initial circle
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                double x = center.getX() + 5 * Math.cos(angle);
                double z = center.getZ() + 5 * Math.sin(angle);
                double y = center.getY();
                targets.get(i).teleport(new Location(world, x, y, z, (float)Math.toDegrees(angle + Math.PI), 0));
            }
            // Start the merry-go-round task
            Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (tick > 100) return;
                    for (int i = 0; i < n; i++) {
                        Player p = targets.get(i);
                        if (!p.isOnline()) continue;
                        double angle = 2 * Math.PI * i / n + tick * 0.2;
                        double x = center.getX() + 5 * Math.cos(angle);
                        double z = center.getZ() + 5 * Math.sin(angle);
                        // Off-sync up/down using sine wave
                        double y = center.getY() + Math.sin(tick * 0.25 + phaseOffsets[i]) * 1.5;
                        p.teleport(new Location(world, x, y, z, (float)Math.toDegrees(angle + Math.PI), 0));
                        if (tick % 10 == 0 && tick > 0 && tick <= 100) {
                            p.damage(20.0, player);
                        }
                    }
                    tick++;
                    if (tick > 100) {
                        // Remove levitation effect
                        for (Player p : targets) {
                            p.removePotionEffect(PotionEffectType.LEVITATION);
                        }
                    }
                }
            }, 0L, 1L);
        }
    );

    static Technique geno = new Technique("nyehehe", "Amusing Trick", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) ->{
        // 1. Find random target within 200 blocks
        Player target = roaring_soul.getClosestPlayer(player.getLocation());
        if(target == null) {
            player.sendMessage("No target found within 200 blocks.");
            return;
        }
        if(PlayerTechniqueManager.isInmune(target.getUniqueId()) || target.getLocation().distance(player.getLocation()) > 200) {
            player.sendMessage("No target found within 200 blocks.");
            return;
        }

        // 2. Apply blindness 255 for 5s, slowness 255 for 1s
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 254, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 2, 254, false, false, false));

        // 3. Teleport user 2 blocks behind target, facing them
        Location behind = target.getLocation().clone();
        behind.setDirection(target.getLocation().getDirection().multiply(-1));
        behind = behind.add(behind.getDirection().normalize().multiply(2));
        behind.setYaw(target.getLocation().getYaw() + 180);
        player.teleport(behind);
        player.teleport(player.getLocation().setDirection(target.getLocation().subtract(player.getLocation()).toVector()));

        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> CooldownManager.setCooldown(player, "geno", cooldownHelper.minutesToMiliseconds(5)), 2);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), true), 7);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), false), 18 + (Familiar_love.OstiacionActive.getOrDefault(player, false) ? 10 : 0));

    });

    static Technique swapPlayersNearby = new Technique(
        "swapPlayersNearby",
        "Deck Shuffle",
        false,
        20 * 30*1000, // 30 seconds cooldown
        (player, item, args) -> {
            List<Player> targets = new ArrayList<>();
            World world = player.getWorld();
            for (Player p : world.getPlayers()) {
                if (!p.equals(player) && p.getLocation().distance(player.getLocation()) <= 50) {
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                    targets.add(p);
                }
            }
            int n = targets.size();
            if (n < 2) {
                player.sendMessage("Not enough players nearby to swap!");
                return;
            }
            // Store original locations
            List<Location> locations = new ArrayList<>();
            for (Player p : targets) {
                locations.add(p.getLocation().clone());
            }
            // Shuffle locations
            java.util.Collections.shuffle(locations);
            // Teleport each player to a new location
            for (int i = 0; i < n; i++) {
                targets.get(i).teleport(locations.get(i));
            }
            player.sendMessage("Swapped the positions of all nearby players!");
        }
    );
}
