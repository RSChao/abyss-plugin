package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.techs.OriginDepleter;
import com.rschao.boss_battle.BossAPI;
import com.rschao.boss_battle.InvManager;
import com.rschao.boss_battle.api.BossHandler;
import com.rschao.boss_battle.api.BossInstance;
import com.rschao.boss_battle.bossEvents;
import com.rschao.events.definitions.BossChangeEvent;
import com.rschao.events.definitions.BossEndEvent;
import com.rschao.events.soulEvents;
import com.rschao.plugins.techniqueAPI.tech.cancel.SimpleCancellationToken;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class BossEvents implements Listener {
    @EventHandler
    void onBossChange(BossChangeEvent ev){
        String playerName = ev.getBossPlayer().getName();
        FileConfiguration bossConfig = ev.config;
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        List<String> abyss = BossAPI.getAddon(bossConfig, ev.getPhase(), "abyss");
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
        FileConfiguration configuration = BossHandler.loadBoss("s3lore.origin.minion");
        if(!configuration.contains("boss.world")){
            Bukkit.getLogger().severe("Boss configuration for s3lore.origin.minion is missing 'boss.world'! Please check your boss configurations.");
            return;
        }
        //load inv, souls, fruits and abysses
        Bukkit.getLogger().warning(BossAPI.getKit(configuration, phase));
        InvManager.LoadInventory(Bukkit.getPlayer("Doritospro"), BossAPI.getKit(configuration, phase));
        Bukkit.getPlayer("Doritospro").teleport(ev.getBossPlayer());
        soulEvents.setSouls(Bukkit.getPlayer("Doritospro"), 19, 66);
        for(String fruitId : BossAPI.getAddon(configuration, phase, "fruits")){
            com.rschao.plugins.fightingpp.events.events.saveFruitToConfig("Doritospro", fruitId);
            com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened("Doritospro", fruitId, true);
        }
        List<String> abyss = BossAPI.getAddon(configuration, phase, "abyss");
        if(abyss.isEmpty()) return;

        Plugin.getPlugin(Plugin.class).getConfig().set("Doritospro.groupids", abyss);
        Plugin.getPlugin(Plugin.class).saveConfig();
        Plugin.getPlugin(Plugin.class).reloadConfig();
    }
}
