package com.delta.plugins.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import org.bukkit.configuration.file.FileConfiguration;

public class removeAbyss {
    public static CommandAPICommand command = new CommandAPICommand("clearabyss")
            .withArguments(new OnePlayer("player"))
            .executesPlayer((player, args) -> {
                org.bukkit.entity.Player targetPlayer = (org.bukkit.entity.Player) args.get(0);
                FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
                if(targetPlayer == null) {
                    player.sendMessage("Invalid player specified.");
                    return;
                }
                config.set(targetPlayer.getName() + ".groupids", null);
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).saveConfig();
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
                player.sendMessage("Cleared abyss IDs for " + targetPlayer.getName());
                targetPlayer.sendMessage("Your abyss IDs have been cleared");
            });
}
