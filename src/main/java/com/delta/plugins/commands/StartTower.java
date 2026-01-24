package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.mobs.MobSpawner;
import com.rschao.boss_battle.InvManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartTower {
    public static CommandAPICommand command = new CommandAPICommand("starttower")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .withOptionalArguments(new IntegerArgument("floor"), new StringArgument("inv"))
            .executes((sender, args) -> {
                Player p = (Player) args.get(0);
                Integer floorArg = args.get("floor") != null ? (Integer) args.get("floor") : null;
                String invKey = args.get("inv") != null ? (String) args.get("inv") : null;

                // iniciar pit si necesario y establecer piso si se indicó
                if(floorArg != null){
                    PitEvents.startPit(p);
                    PitEvents.setFloorAbsolute(p, floorArg);
                } else {
                    PitEvents.startPit(p);
                }

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()) {
                    // crear archivo si no existe (mantener compatibilidad)
                    try{ file.createNewFile(); } catch (Exception ignored){}
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                int floor = PitEvents.getFloor(p);
                Location configured = config.getLocation("tower." + floor + ".spawn");
                Location location = configured != null ? configured : p.getLocation();

                // backup del inventario actual
                InvManager.SaveInventory(p, "tower.backup." + p.getUniqueId(),  false);

                // teleport y efectos
                p.teleport(location);
                p.setGameMode(GameMode.ADVENTURE);
                p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(PotionEffect.INFINITE_DURATION, 0));

                // cargar inventario: si se pasó una clave usarla, si no usar "tower" por compatibilidad
                if(invKey != null && !invKey.isEmpty()){
                    InvManager.LoadInventory(p, invKey, false);
                } else {
                    InvManager.LoadInventory(p, "tower", false);
                }

                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class),() -> (new MobSpawner()).spawnMob(p.getWorld(), p), 2);
            });
    public static CommandAPICommand nextFloor = new CommandAPICommand("nextfloor")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .withOptionalArguments(new IntegerArgument("amount"))
            .executes((sender, args) -> {
                Player p = (Player) args.get(0);
                int amount = args.get("amount") != null ? (Integer) args.get("amount") : 1;
                if(args.get("amount") == null)  {
                    PitEvents.nextFloor(p);
                } else {
                    PitEvents.setFloor(p, amount);
                }

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()) return;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                int floor = PitEvents.getFloor(p);
                Location configured = config.getLocation("tower." + floor + ".spawn");
                Location location = configured != null ? configured : p.getLocation();

                p.teleport(location);
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class),() ->{
                    (new MobSpawner()).spawnMob(p.getWorld(), p);
                }, 2);
            });
    public static CommandAPICommand resetfloor = new CommandAPICommand("resetfloor")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                Player p = (Player) args.get(0);
                PitEvents.resetFloor(p);
            });

    // Nuevo comando: settowerspawn <floor> -> guarda la ubicación del ejecutor como tower.<floor>.spawn
    public static CommandAPICommand setTowerSpawn = new CommandAPICommand("settowerspawn")
            .withArguments(new IntegerArgument("floor"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                if(!(sender instanceof Player)) return;
                Player p = (Player) sender;
                int floor = (Integer) args.get(0);

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("tower." + floor + ".spawn", p.getLocation());
                try{
                    config.save(file);
                    p.sendMessage("Spawn de la torre guardado para el piso " + floor);
                } catch (Exception e){
                    e.printStackTrace();
                    p.sendMessage("Error al guardar el spawn.");
                }
            });

    // Nuevo comando: addmobspot <floor> -> añade la ubicación del ejecutor a tower.<floor>.mobs
    public static CommandAPICommand addMobSpot = new CommandAPICommand("addmobspot")
            .withArguments(new IntegerArgument("floor"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                if(!(sender instanceof Player)) return;
                Player p = (Player) sender;
                int floor = (Integer) args.get(0);

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                List<Location> list = (List<Location>) config.get("tower." + floor + ".mobs", new ArrayList<Location>());
                if(list == null) list = new ArrayList<>();
                list.add(p.getLocation());
                config.set("tower." + floor + ".mobs", list);
                try{
                    config.save(file);
                    p.sendMessage("Punto de spawn para mobs añadido al piso " + floor);
                } catch (Exception e){
                    e.printStackTrace();
                    p.sendMessage("Error al guardar el punto de mobs.");
                }
            });

    // Comando para guardar checkpoint del jugador (inventario + piso)
    public static CommandAPICommand saveCheckpoint = new CommandAPICommand("savetower")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                Player p = (Player) args.get(0);
                // guardar inventario con la clave tower.checkpoint.<player>
                InvManager.SaveInventory(p, "tower.checkpoint." + p.getName(), false);

                // guardar piso en towerdata.yml con la misma clave
                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()){
                    try{ file.createNewFile(); } catch (Exception ignored){}
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("tower.checkpoint." + p.getName(), PitEvents.getFloor(p));
                try{
                    config.save(file);
                    p.sendMessage("Checkpoint de la torre guardado (inv + piso).");
                } catch (Exception e){
                    e.printStackTrace();
                    p.sendMessage("Error al guardar el checkpoint.");
                }
            });

    // Comando para cargar checkpoint: inicia la torre en el piso guardado y carga inventario guardado
    public static CommandAPICommand loadCheckpoint = new CommandAPICommand("loadtowercheckpoint")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                Player p = (Player) args.get(0);

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()){
                    if(sender instanceof Player) ((Player)sender).sendMessage("No hay datos de checkpoint.");
                    return;
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String key = "tower.checkpoint." + p.getName();
                if(!config.contains(key)){
                    if(sender instanceof Player) ((Player)sender).sendMessage("No hay checkpoint guardado para " + p.getName());
                    return;
                }

                int savedFloor = config.getInt(key, 0);
                // cargar inventario guardado
                InvManager.LoadInventory(p, "tower.checkpoint." + p.getName(), false);

                // iniciar pit y colocar al jugador en el piso guardado
                PitEvents.startPit(p);
                PitEvents.setFloorAbsolute(p, savedFloor);

                Location configured = config.getLocation("tower." + savedFloor + ".spawn");
                Location location = configured != null ? configured : p.getLocation();

                p.teleport(location);
                p.setGameMode(GameMode.ADVENTURE);
                p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 0));

                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class),() -> (new MobSpawner()).spawnMob(p.getWorld(), p), 2);
            });
    // Nuevo comando: /giveextrakey <player>
    public static CommandAPICommand giveExtraKey = new CommandAPICommand("giveextrakey")
            .withArguments(new OnePlayer("player"))
            .withPermission("tower.admin")
            .executes((sender, args) -> {
                Player target = (Player) args.get(0);
                if(target == null) return;

                int have = PitEvents.countKeys(target);
                if(have >= 1){
                    PitEvents.setExtraKeyEffect(target, true);
                } else {
                    if(sender instanceof Player) sender.sendMessage(target.getName() + " no tiene ninguna llave. El efecto sólo se puede aplicar si ya posee al menos una llave.");
                }
            });
}
