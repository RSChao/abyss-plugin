package com.delta.plugins.whacka;

import com.delta.plugins.items.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class WhackaListener implements Listener {

    private static final String KILLS_KEY = "whacka.kills";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        WhackaManager.removeWhacka();
        WhackaManager.assignRandomSpawn();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        WhackaManager.removeWhacka();
        WhackaManager.assignRandomSpawn();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        WhackaManager.trySpawnWhackaIfPlayerNearby();
    }

    @EventHandler
    public void onWhackaDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Silverfish sf) || !"Whacka".equals(entity.getCustomName())) return;
        Player killer = sf.getKiller();
        if (killer == null) {
            // Find closest player within 10 blocks
            double minDist = 10.0;
            Player closest = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(entity.getWorld())) {
                    double dist = p.getLocation().distance(entity.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = p;
                    }
                }
            }
            killer = closest;
        }
        if (killer != null) {
            int kills = getKills(killer.getUniqueId());
            kills++;
            if (kills >= 8) {
                event.getDrops().clear();
                event.getEntity().getWorld().dropItemNaturally(entity.getLocation(), Items.rare_whacka_bump.clone());
                setKills(killer.getUniqueId(), 0);
                WhackaManager.assignRandomSpawn();
            } else {
                event.getDrops().clear();
                event.getEntity().getWorld().dropItemNaturally(entity.getLocation(), Items.whacka_bump.clone());
                setKills(killer.getUniqueId(), kills);
            }
        }
        WhackaManager.removeWhacka();
        WhackaManager.setCooldown(300_000L); // 5 minutes in ms
    }

    private int getKills(UUID uuid) {
        FileConfiguration config = JavaPlugin.getProvidingPlugin(WhackaListener.class).getConfig();
        return config.getInt(KILLS_KEY + "." + uuid, 0);
    }

    private void setKills(UUID uuid, int kills) {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(WhackaListener.class);
        plugin.getConfig().set(KILLS_KEY + "." + uuid, kills);
        plugin.saveConfig();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().getPersistentDataContainer().has(
                    Items.whacka_bump.getItemMeta().getPersistentDataContainer().getKeys().iterator().next(),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                // Whacka Bump
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 600, 4));
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 5));
                            player.setSaturation(Math.min(20f, player.getSaturation() + 12f));
                        }
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), 1L);
                }, 1L);
            } else if (item.getItemMeta().getPersistentDataContainer().has(
                    Items.rare_whacka_bump.getItemMeta().getPersistentDataContainer().getKeys().iterator().next(),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                // Rare Whacka Bump
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 600, 6));
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            player.setFoodLevel(20);
                            player.setSaturation(20f);
                        }
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), 1L);
                }, 1L);
            }
        }
    }
}
