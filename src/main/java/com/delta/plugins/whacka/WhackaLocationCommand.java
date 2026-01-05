package com.delta.plugins.whacka;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;

public class WhackaLocationCommand {
    public static CommandAPICommand command = new CommandAPICommand("addwhackalocation")
        .withPermission("delta.whacka.admin")
        .executesPlayer((player, args) -> {
            WhackaLocationManager.addLocation(player.getLocation());
            player.sendMessage("Whacka spawn location added!");
        });
}
