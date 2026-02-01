package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.techs.OriginDepleter;
import com.rschao.boss_battle.InvManager;
import com.rschao.boss_battle.bossEvents;
import com.rschao.events.definitions.BossChangeEvent;
import com.rschao.events.soulEvents;
import com.rschao.plugins.techniqueAPI.tech.cancel.SimpleCancellationToken;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    @EventHandler
    void handleOriginBoss(BossChangeEvent ev){
        String bossName = ev.getBossName();
        if(!bossName.equalsIgnoreCase("s3lore.origin.origin")) return;
        int phase = ev.getPhase();
        if(phase >= 4){
            OriginDepleter.addP4Techs();
            TechRegistry.getById("reset_cooldown_chaos").getAction().execute(new TechniqueContext(ev.getBossPlayer()), new SimpleCancellationToken());
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            if(soulEvents.hasSoul(p, 19) && !p.equals(ev.getBossPlayer())){
                FileConfiguration configuration = bossEvents.getBossFile("s3lore.origin.minion");
                //load inv, souls, fruits and abysses
                InvManager.LoadInventory(p, configuration.getString("boss.world." + phase + ".kit"));
                p.teleport(ev.getBossPlayer());
                soulEvents.setSouls(p, 19, 66);
                for(String fruitId : configuration.getStringList("boss.world." + phase + ".fruits")){
                    com.rschao.plugins.fightingpp.events.events.saveFruitToConfig(p.getName(), fruitId);
                    com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened(p.getName(), fruitId, true);
                }
                List<String> abyss = configuration.getStringList("boss.world." + phase + ".abyss");
                if(!abyss.isEmpty()) return;

                Plugin.getPlugin(Plugin.class).getConfig().set(p.getName() + ".groupids", abyss);
                Plugin.getPlugin(Plugin.class).saveConfig();
                Plugin.getPlugin(Plugin.class).reloadConfig();
            }
        }
    }
}
