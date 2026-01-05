package com.delta.plugins.darkworld;

import com.delta.plugins.Plugin;
import com.delta.plugins.commands.makeDarkWorld;
import com.rschao.items.Items;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.UUID;

public class DarkWorldEvents implements Listener {
    @EventHandler
    void onPlayerInteract(PlayerInteractEvent ev){
        ItemStack itemStack = ev.getItem();
        if(itemStack == null) return;
        if(itemStack.getItemMeta() == null) return;
        // --- NUEVO: guardar portal de entrada ---
        if(itemStack.isSimilar(DarkWorldRegistry.entrancePortal)) {
            ev.setCancelled(true);
            File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml");
            if(!file.exists()){
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            FileConfiguration configuration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            try {
                configuration.load(file);
            } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
                e.printStackTrace();
            }
            if(ev.getPlayer().getTargetBlockExact(5) == null){
                return;
            }
            Location loc = ev.getPlayer().getTargetBlockExact(5).getLocation().add(0, 1, 0);
            configuration.set(ev.getPlayer().getName() + ".entrance_portal_loc", loc);
            try {
                configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            ev.getPlayer().sendMessage("§aHas marcado la ubicación del Portal de Entrada en: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " en el mundo " + loc.getWorld().getName());
            return;
        }
        // --- NUEVO: guardar portal de salida ---
        if(itemStack.isSimilar(DarkWorldRegistry.exitPortal)) {
            ev.setCancelled(true);
            File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml");
            if(!file.exists()){
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            FileConfiguration configuration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            try {
                configuration.load(file);
            } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
                e.printStackTrace();
            }
            if(ev.getPlayer().getTargetBlockExact(5) == null){
                return;
            }
            Location loc = ev.getPlayer().getTargetBlockExact(5).getLocation().add(0, 1, 0);
            configuration.set(ev.getPlayer().getName() + ".exit_portal_loc", loc);
            try {
                configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            ev.getPlayer().sendMessage("§aHas marcado la ubicación del Portal de Salida en: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " en el mundo " + loc.getWorld().getName());
            return;
        }
        if(!itemStack.isSimilar(DarkWorldRegistry.locationComp)) return;
        {
            ev.setCancelled(true);
            File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml");
            if(!file.exists()){
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            FileConfiguration configuration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            try {
                configuration.load(file);
            } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
                e.printStackTrace();
            }
            if(ev.getPlayer().getTargetBlockExact(5) == null){
                return;
            }
            Location loc = ev.getPlayer().getTargetBlockExact(5).getLocation().add(0, 1, 0);
            configuration.set(ev.getPlayer().getName() + ".fountain_loc", loc);
            // Save the config
            try {
                configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            ev.getPlayer().sendMessage("§aHas marcado la ubicación de la Fuente Oscura en: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " en el mundo " + loc.getWorld().getName());
        }
    }

    @EventHandler
    void onKnifeUseOnBedrock(PlayerInteractEvent ev) {
        // Solo continuar si el jugador hace clic derecho en un bloque
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack itemStack = ev.getItem();
        if (itemStack == null) return;
        if (itemStack.getItemMeta() == null) return;
        // Verifica si el ítem es el cuchillo especial
        if (!itemStack.isSimilar(DarkWorldRegistry.knife)) return;
        Block clickedBlock = ev.getClickedBlock();
        if (clickedBlock == null) return;
        // Verifica si el bloque es bedrock
        if (clickedBlock.getType() != Material.BEDROCK) return;
        Location blockLocation = clickedBlock.getLocation();
        if(blockLocation.getY() < DarkWorldRegistry.getMinYLevel(blockLocation.getWorld()) || blockLocation.getY() > DarkWorldRegistry.getMaxYLevel(blockLocation.getWorld())){
            ev.getPlayer().sendMessage("§cNo puedes usar el Dark World Knife aquí.");
            return;
        }
        // Aquí puedes poner la lógica que desees al usar el cuchillo en bedrock
        ev.setCancelled(true);
        ev.getPlayer().sendMessage("§5Has usado el Dark World Knife sobre un bloque de bedrock.");

        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml");
        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration configuration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        try {
            configuration.load(file);
        } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
        if(configuration.contains(ev.getPlayer().getName() + ".fountain_loc")){
            Location fountainLoc = configuration.getLocation(ev.getPlayer().getName() + ".fountain_loc");
            Location entrancePortalLoc = configuration.getLocation(ev.getPlayer().getName() + ".entrance_portal_loc");
            Location exitPortalLoc = configuration.getLocation(ev.getPlayer().getName() + ".exit_portal_loc");
            // --- NUEVO: chequeo de todas las ubicaciones necesarias ---
            if (fountainLoc == null || fountainLoc.getWorld() == null ||
                entrancePortalLoc == null || entrancePortalLoc.getWorld() == null ||
                exitPortalLoc == null || exitPortalLoc.getWorld() == null) {
                ev.getPlayer().sendMessage("§cDebes marcar la Fuente Oscura, el Portal de Entrada y el Portal de Salida antes de crear el Dark World.");
                return;
            }
            // Teletransporta al jugador a la ubicación de la fuente oscura
            makeDarkWorld.playDarkWorldAnimation(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), clickedBlock.getLocation().add(0.5, 0, 0.5), fountainLoc, fountainLoc.getWorld().getName());
            Bukkit.getScheduler().runTaskLater(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), () -> {
                UUID uuid = UUID.randomUUID();
                configuration.set("darkworlds." + uuid + ".fountain", fountainLoc);
                configuration.set("darkworlds." + uuid + ".knife", clickedBlock.getLocation().add(0.5, 0, 0.5));
                configuration.set("darkworlds." + uuid + ".owners", java.util.Collections.singletonList(ev.getPlayer().getName()));
                // Guardar portales si existen
                if (entrancePortalLoc != null) {
                    configuration.set("darkworlds." + uuid + ".entrance_portal", entrancePortalLoc);
                }
                if (exitPortalLoc != null) {
                    configuration.set("darkworlds." + uuid + ".exit_portal", exitPortalLoc);
                }
                configuration.set(ev.getPlayer().getName() + ".fountain_loc", null);
                configuration.set(ev.getPlayer().getName() + ".entrance_portal_loc", null);
                configuration.set(ev.getPlayer().getName() + ".exit_portal_loc", null);
                try {
                    configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }, 160L);
            //ev.getPlayer().sendMessage("§aTe has teletransportado a la Fuente Oscura en: " + fountainLoc.getBlockX() + ", " + fountainLoc.getBlockY() + ", " + fountainLoc.getBlockZ() + " en el mundo " + fountainLoc.getWorld().getName());
        } else {
            ev.getPlayer().sendMessage("§cNo tienes una ubicación de Fuente Oscura marcada. Usa el Dark World Location Component primero.");
        }
    }


    static BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml");
            if(!file.exists()){
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            FileConfiguration configuration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            try {
                configuration.load(file);
            } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
                e.printStackTrace();
            }
            if(configuration.contains("darkworlds")){
                for(String key : configuration.getConfigurationSection("darkworlds").getKeys(false)){
                    Location fountainLoc = configuration.getLocation("darkworlds." + key + ".fountain");
                    Location knifeLoc = configuration.getLocation("darkworlds." + key + ".knife");
                    Location entrancePortalLoc = configuration.getLocation("darkworlds." + key + ".entrance_portal");
                    Location exitPortalLoc = configuration.getLocation("darkworlds." + key + ".exit_portal");
                    if(fountainLoc != null && fountainLoc.getWorld() != null){
                        // Corriente vertical en 0, 2, 4 y 6 bloques arriba
                        for (int yOffset : new int[]{0, 2, 4, 6}) {
                            Location spawnLoc = fountainLoc.clone().add(0.5, yOffset, 0.5);
                            for(int i = 0; i < 20; i++){
                                double x = 0;
                                double z = 0;
                                double y = Math.random() * 0.1 + 0.08;
                                //Vector velocity = new Vector(x, y, z);
                                spawnLoc.getWorld().spawnParticle(
                                    Particle.LARGE_SMOKE,
                                    spawnLoc,
                                    1,
                                    0.05, 0, 0.05,
                                    0.01,
                                    null
                                );
                            }
                        }
                        for(Entity ent : fountainLoc.getWorld().getNearbyEntities(fountainLoc, 3, 3, 3)){
                            if(ent instanceof Item) {
                                Item item = (Item) ent;
                                ItemStack stack = item.getItemStack();
                                ItemMeta meta = stack.getItemMeta();
                                if (meta != null && stack.isSimilar(Items.SoulContainer)){
                                    //remove the fountain and knife locations from config
                                    configuration.set("darkworlds." + key + ".fountain", null);

                                    try {
                                        configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
                                    } catch (java.io.IOException e) {
                                        e.printStackTrace();
                                    }
                                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
                                        for(Entity e : fountainLoc.getWorld().getNearbyEntities(fountainLoc, 10, 10, 10)){
                                            if(e instanceof org.bukkit.entity.Player){
                                                org.bukkit.entity.Player p = (org.bukkit.entity.Player) e;
                                                p.sendMessage("§aLa Fuente Oscura ha sido sellada con un Soul Container.");
                                                Location tpLoc = configuration.getLocation("darkworlds." + key + ".knife");
                                                configuration.set("darkworlds." + key + ".knife", null);
                                                // --- CAMBIO: teleporta al portal de entrada si existe ---
                                                Location portalLoc = tpLoc;
                                                if(portalLoc != null && portalLoc.getWorld() != null){
                                                    p.getInventory().addItem(Items.SoulContainer);
                                                    item.remove();
                                                    p.teleport(portalLoc.clone().add(0, 1, 0));
                                                }
                                                configuration.set("darkworlds." + key, null);
                                                try {
                                                    configuration.save(new java.io.File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), "darkworlds/darkworlds.yml"));
                                                } catch (java.io.IOException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                    }, 20);
                                }
                            }
                        }
                        // --- NUEVO: teletransporte al portal de salida si el jugador está en la fuente y no está sellada ---
                        for (org.bukkit.entity.Player player : fountainLoc.getWorld().getPlayers()) {
                            if (exitPortalLoc != null && player.getLocation().distance(fountainLoc) <= 3) {
                                // Solo si la fuente no está sellada (existe fountainLoc)
                                player.teleport(exitPortalLoc.clone().add(0, 1, 0));
                                player.sendMessage("§7Has sido expulsado del Dark World.");
                            }
                        }
                    }
                    if(knifeLoc != null && knifeLoc.getWorld() != null){

                        // Corriente vertical en 1, 3, 5 y 7 bloques arriba (manteniendo el +1 original)
                        for (int yOffset : new int[]{1, 3, 5, 7}) {
                            Location spawnLoc = knifeLoc.clone().add(0, yOffset, 0);
                            for(int i = 0; i < 20; i++){
                                double x = 0;
                                double z = 0;
                                double y = Math.random() * 0.1 + 0.08;
                                Vector velocity = new Vector(x, y, z);
                                spawnLoc.getWorld().spawnParticle(
                                    Particle.LARGE_SMOKE,
                                    spawnLoc,
                                    1,
                                    0.05, 0, 0.05,
                                    0.01,
                                    null
                                );
                            }
                        }
                        for(Entity ent : knifeLoc.getWorld().getNearbyEntities(knifeLoc, 3, 3, 3)){
                            if(ent instanceof Player) {
                                Player player = (Player) ent;
                                if(player.getLocation().distance(knifeLoc)<= 3){
                                    player.teleport(entrancePortalLoc);
                                    player.sendMessage("§7Has entrado a un Dark World.");
                                }

                            }
                        }
                    }
                    java.util.List<String> owners = configuration.getStringList("darkworlds." + key + ".owners");
                    if (fountainLoc != null && fountainLoc.getWorld() != null && owners != null && !owners.isEmpty()) {
                        for (String ownerName : owners) {
                            org.bukkit.entity.Player owner = Bukkit.getPlayerExact(ownerName);
                            if (owner != null && owner.getWorld().equals(fountainLoc.getWorld())) {
                                if (owner.getLocation().distance(fountainLoc) <= 200) {
                                    owner.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.STRENGTH, 40, 3, true, false, false));
                                    owner.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.SPEED, 40, 3, true, false, false));
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    public static void runDarkWorldParticles(){
        task.runTaskTimer(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class), 0L, 1L);
    }
}
