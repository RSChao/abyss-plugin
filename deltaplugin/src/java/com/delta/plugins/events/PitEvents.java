package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.delta.plugins.items.PitItems;
import com.rschao.plugins.fightingpp.techs.chao;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.*;

public class PitEvents implements Listener {
    static Map<String, Integer> floor = new HashMap<>();
    public static Map<String, Integer> kills = new HashMap<>();
    public static void startPit(Player p){
        floor.put(p.getName(), 1);
        p.sendMessage("Has comenzado las Cien Pruebas. Estás en el piso 1.");
    }
    public static int getFloor(Player p){
        return floor.getOrDefault(p.getName(), 0);
    }
    public static void nextFloor(Player p){
        int currentFloor = getFloor(p);
        if(currentFloor == 0){
            p.sendMessage("No has comenzado las Cien Pruebas.");
            return;
        }
        if(currentFloor == 100){
            p.sendMessage("Has completado las Cien Pruebas. Felicidades.");
            floor.put(p.getName(), 0);
            return;
        }
        currentFloor++;
        floor.put(p.getName(), currentFloor);
        p.sendMessage("Has avanzado al piso " + currentFloor + " de las Cien Pruebas.");
    }
    public static void resetFloor(Player p){
        floor.put(p.getName(), 0);
    }

    @EventHandler
    void onPlayerChat(AsyncPlayerChatEvent ev){
        if(ev.getMessage().equals("!keyhole")){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.key_hole);
            p.sendMessage("Has recibido un agujero para llave.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!key")){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.floor_key);
            p.sendMessage("Has recibido una llave.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!coin")){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.coin);
            p.sendMessage("Has recibido una moneda.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!kinektos")){
            Player p = ev.getPlayer();
            p.getInventory().addItem(Items.hoe());
            p.sendMessage("Hi, kinektos");
            ev.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent ev){
        ItemStack item = ev.getItem();
        if(item == null) return;
        if(item.getItemMeta() == null) return;
        if(ev.getClickedBlock() == null || ev.getClickedBlock().getType().equals(Material.AIR)) return;
        if(!ev.getClickedBlock().getType().equals(Material.IRON_TRAPDOOR)) return;
        if(ev.getItem().isSimilar(PitItems.floor_key)){
            if(ev.getClickedBlock() == null) return;
            if(!CheckPitKeyHole(ev.getClickedBlock().getLocation())) return;
            Player p = ev.getPlayer();
            p.sendMessage("Has usado una llave para abrir la puerta.");
            item.setAmount(item.getAmount()-1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nextfloor " + p.getName());
        }
        else if (item.getType().equals(Material.TRIAL_KEY)){
            if(item.getItemMeta().getLore().isEmpty()) return;
            Block b = ev.getClickedBlock();
            if(!CheckKeyHole(b.getLocation(), item.getItemMeta().getLore().get(0))) return;

            // Guarda tipo y datos ANTES de cambiar a aire
            Material originalType = b.getType();
            org.bukkit.block.data.BlockData originalData = b.getBlockData();

            b.setType(Material.AIR);
            b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1, 1);
            ev.getPlayer().sendMessage("Has abierto la puerta con la llave.");
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                b.setType(originalType);
                b.setBlockData(originalData);
                b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1, 1);
            }, 20*5);
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent ev){
        ItemStack item = ev.getItemInHand();
        if(item.getItemMeta() == null) return;
        if(item.getType().equals(Material.IRON_TRAPDOOR)){
            if(item.equals(PitItems.key_hole)){
                SavePitKeyHole(ev.getBlockPlaced().getLocation());
            }
            else {
                SaveKeyHole(ev.getBlockPlaced().getLocation(), item.getItemMeta().getDisplayName());
            }

        }
    }


    @EventHandler
    void onMobDed(EntityDeathEvent ev){
        Player p = ev.getEntity().getKiller();
        if(p == null) return;
        if(!floor.containsKey(p.getName())) return;
        if(floor.get(p.getName()) < 1) return;
        if(p.getName() == null) return;
        ev.getDrops().clear();
        kills.put(p.getName(), kills.getOrDefault(p.getName(), 0) + 1);
        int rng = (new Random()).nextInt(0, 6);
        for(int i = 0; i < rng; i++){
            ev.getDrops().add(PitItems.coin);
        }
        if(ev.getEntity() instanceof Slime s && s.getSize() < 2){
            ev.getDrops().clear();
        }

        if(ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("tower", "key"))){
            ev.getDrops().add(PitItems.floor_key);
            p.sendMessage("Has recibido una llave.");
        }

    }
    @EventHandler
    void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event){
        Player p = event.getEntity();
        if(getFloor(p) > 0){
            p.sendMessage("Has muerto en el piso " + getFloor(p) + " de la torre.");
            resetFloor(p);
        }
    }
    static void SavePitKeyHole(Location loc){
        String s = "pit_keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Location> list = (List<Location>) config.get("keyholes", new ArrayList<>());
        list.add(loc);
        config.set("keyholes", list);
        try{
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    static void SaveKeyHole(Location loc, String pass){
        String s = "keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        UUID id = UUID.randomUUID();
        config.set("keyholes." + id.toString() + ".loc", loc);
        config.set("keyholes." + id.toString() + ".pass", pass);
        try{
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    static boolean CheckPitKeyHole(Location loc){
        String s = "pit_keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Location> list = (List<Location>) config.get("keyholes", new ArrayList<>());
        if(list.contains(loc)) return true;

        return false;
    }
    static boolean CheckKeyHole(Location loc, String pass){
        String s = "keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.isConfigurationSection("keyholes")) return false;

        for(String key : config.getConfigurationSection("keyholes").getKeys(false)){
            Object savedLoc = config.get("keyholes." + key + ".loc");
            String savedPass = config.getString("keyholes." + key + ".pass");
            if(savedLoc != null && savedLoc.equals(loc) && savedPass != null && savedPass.equals(pass)) return true;
        }
        return false;
    }


}
