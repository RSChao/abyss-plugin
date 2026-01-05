package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.specialEvents.GravesManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetGraveCommand {
    public static void register() {
        new CommandAPICommand("setgrave")
            .withPermission("delta.grave.set")
            .withArguments(new StringArgument("name"))
            .withArguments(new StringArgument("activator"))
            .withOptionalArguments(new LocationArgument("location"))
            .executes((sender, args) -> {
                String name = (String) args.get(0);
                String activator = (String) args.get(1);
                Location loc = null;
                if (args.count() > 2 && args.get(2) != null) {
                    loc = (Location) args.get(2);
                } else {
                    if (sender instanceof Player) {
                        loc = ((Player) sender).getLocation().clone();
                    } else {
                        sender.sendMessage("Debe especificar una ubicación si no es un jugador.");
                        return;
                    }
                }
                // normalize to center of block
                loc.setX(loc.getBlockX() + 0.5);
                loc.setY(loc.getBlockY() + 0.5);
                loc.setZ(loc.getBlockZ() + 0.5);
                GravesManager.saveGrave(name, loc, activator);
                sender.sendMessage("Tumba '" + name + "' guardada con activador '" + activator + "'. Chaos Heart spawneado 3 bloques arriba.");
            })
            .register();
    }
}

