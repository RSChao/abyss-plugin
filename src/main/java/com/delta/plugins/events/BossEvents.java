package com.delta.plugins.events;

import com.rschao.events.definitions.BossChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.List;

public class BossEvents implements Listener {
    @EventHandler
    void onBossChange(BossChangeEvent ev){
        String bossName = ev.getBossName();
        String playerName = ev.getBossPlayer().getName();
        FileConfiguration bossConfig = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("bossfight").getDataFolder() + "/bosses/", bossName + ".yml"));
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        List<String> abyss = bossConfig.getStringList("boss.world." + ev.getPhase() + ".abyss");
        if(abyss.isEmpty()) {
            return;
        }
        if(abyss.size() == 1 && abyss.get(0).equalsIgnoreCase(playerName)) return;
        config.set(playerName + ".groupids", abyss);
        com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).saveConfig();
        com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
        Bukkit.getLogger().info("Abyss group ids for " + playerName + " set to " + abyss);


    }
}
