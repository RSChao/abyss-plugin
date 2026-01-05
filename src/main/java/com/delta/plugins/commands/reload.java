package com.delta.plugins.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;

public class reload {
    @SuppressWarnings("deprecation")
    public static CommandAPICommand command = new CommandAPICommand("reloadabyss")
        .executesPlayer((player, args) -> {
            com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
            player.sendMessage("Abyss plugin configuration reloaded.");
            //set all players's groupIds to 0
            //get all keys from section "" in the config
            FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
            for (String key : config.getKeys(false)) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(key);
                if (offlinePlayer.isOnline()) {
                    Player onlinePlayer = offlinePlayer.getPlayer();
                    if (onlinePlayer != null) {
                        // Reset group id index for this player
                        com.delta.plugins.events.events.playerGroupIdIndex.put(onlinePlayer.getUniqueId(), 0);
                        onlinePlayer.sendMessage("Your group id index has been reset to 0.");
                    }
                }
            }
        });
}
