// java
package com.delta.plugins.mobs;

import com.delta.plugins.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TowerBossHandler {

    public static void startBoss(String bossName, Player host) {
        // Lógica existente para iniciar el boss
        host.performCommand("switchboss " + bossName);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), ()-> host.performCommand("boss"), 20L);
    }
}
