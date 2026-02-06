package com.delta.plugins.whacka;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.items.Items;
import com.delta.plugins.mobs.custom.Whacka_1_12_10;
import com.rschao.events.soulEvents;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        if(event.getPlayer().getName().equals("RSChao1HM")){
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> event.getPlayer().sendMessage("Whacka spawns at: " + WhackaManager.getCurrentSpawn()), 100);
        }
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

    static BukkitRunnable whackaTask = new BukkitRunnable() {
        @Override
        public void run() {
            for(Player player : Bukkit.getOnlinePlayers()){
                for(Entity entity : player.getNearbyEntities(50, 50, 50)){
                    if(entity instanceof LivingEntity le){
                        if(le.getPersistentDataContainer().has(Whacka_1_12_10.WHACKA_KEY)){
                            entity.getLocation().getWorld().spawnParticle(Particle.DUST, entity.getLocation().add(0,1,0), 10, 0.5, 0.5, 0.5, 0.01, new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 20, 147), 1));
                        }
                    }
                }
            }
        }
    };

    @EventHandler
    public void onWhackaDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Silverfish sf)) return;
        if (!sf.getPersistentDataContainer().has(Whacka_1_12_10.WHACKA_KEY)) return;
        Player killer = sf.getKiller();
        if (killer == null) {
            if(PitEvents.getFloor(killer) >0) return; // don't award if in the Pit
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

            }
            event.setDroppedExp(9000);
            setKills(killer.getUniqueId(), kills%8);
            WhackaManager.assignRandomSpawn();
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
                }, 1L);
            } else if (item.getItemMeta().getPersistentDataContainer().has(
                    Items.rare_whacka_bump.getItemMeta().getPersistentDataContainer().getKeys().iterator().next(),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                // Rare Whacka Bump
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 600, 6));
                }, 1L);
            }else if (item.getItemMeta().getPersistentDataContainer().has(
                    Items.Moly_holy.getItemMeta().getPersistentDataContainer().getKeys().iterator().next(),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                // Moly Holy
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), () -> {
                    PlayerTechniqueManager.setInmune(player.getUniqueId(), true, 60);
                    player.sendMessage("§6You feel a divine protection surround you!");
                }, 1L);
            }else if (item.getItemMeta().getPersistentDataContainer().has(
                    Items.voidBump().getItemMeta().getPersistentDataContainer().getKeys().iterator().next(),
                    org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                // Void Bump
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("deltaplugin"), () -> {
                    if(soulEvents.hasSoul(player, 19)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1));
                        player.sendMessage("§5You feel the void's embrace...");
                    } else {
                         player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 60, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 60, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 60, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 1));
                    }
                }, 1L);
            }
        }
    }
}
