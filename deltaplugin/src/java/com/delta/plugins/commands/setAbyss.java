package com.delta.plugins.commands;

import java.util.List;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.configuration.file.FileConfiguration;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class setAbyss {
    public static CommandAPICommand command = new CommandAPICommand("setabyss")
            .withArguments(new StringArgument("id"), new PlayerArgument("player"))
            .executesPlayer((player, args) -> {
                String id = (String) args.get(0);
                org.bukkit.entity.Player targetPlayer = (org.bukkit.entity.Player) args.get(1);
                FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
                if(targetPlayer == null) {
                    player.sendMessage("Invalid player specified.");
                    return;
                }
                // Get the player's group ids
                List<String> groupIds = config.getStringList(targetPlayer.getName() + ".groupids");
                if (groupIds.contains(id)) {
                    player.sendMessage("Player already has this group id.");
                    return;
                }
                if(groupIds.size() >= 3){
                    player.sendMessage("Player already has 3 group ids.");
                    return;
                }
                groupIds.add(id);
                config.set(targetPlayer.getName() + ".groupids", groupIds);
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).saveConfig();
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
                player.sendMessage("Abyss group id '" + id + "' set for " + targetPlayer.getName());
                targetPlayer.sendMessage("You have been assigned a new abyss group id: " + id);
            });
    public static CommandAPICommand command2 = new CommandAPICommand("sizeabyss")
            .withArguments(new StringArgument("id"))
            .executesPlayer((player, args) -> {
                String id = (String) args.get(0);
                List<Technique> techs = TechRegistry.getAllTechniques(id);
                if(techs.size() == 0){
                    player.sendMessage("No techniques found for abyss id: " + id);
                    return;
                }
                player.sendMessage("Techniques for abyss id '" + id + "':");
                for(Technique tech : techs){
                    player.sendMessage("- " + tech.getName());
                }
            });
}
