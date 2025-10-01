package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.mobs.MobSpawner;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class StartTower {
    public static CommandAPICommand command = new CommandAPICommand("starttower")
            .withArguments(new PlayerArgument("player"))
            .executes((player, args) -> {
                Player p = (Player) args.get(0);
                PitEvents.startPit(p);

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()) return;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                int x = config.getInt("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".x");
                int y = config.getInt("tower." + (PitEvents.getFloor(p)) + ".y");
                int z = config.getInt("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".z");
                String world = config.getString("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".world");

                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                p.teleport(location);

                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class),() ->{
                    (new MobSpawner()).spawnMob(p.getWorld(), p);
                }, 2);
            });
    public static CommandAPICommand nextFloor = new CommandAPICommand("nextfloor")
            .withArguments(new PlayerArgument("player"))
            .executes((player, args) -> {
                Player p = (Player) args.get(0);
                PitEvents.nextFloor(p);

                File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "towerdata.yml");
                if(!file.exists()) return;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);


                int x = config.getInt("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".x");
                int y = config.getInt("tower." + (PitEvents.getFloor(p)) + ".y");
                int z = config.getInt("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".z");
                String world = config.getString("tower." + ((Integer) (PitEvents.getFloor(p)/50)) + ".world");



                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                p.teleport(location);
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class),() ->{
                    (new MobSpawner()).spawnMob(p.getWorld(), p);
                }, 2);
            });
    public static CommandAPICommand resetfloor = new CommandAPICommand("resetfloor")
            .withArguments(new PlayerArgument("player"))
            .executes((player, args) -> {
                Player p = (Player) args.get(0);
                PitEvents.resetFloor(p);
            });
}
