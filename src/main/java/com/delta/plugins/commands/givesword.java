package com.delta.plugins.commands;

import com.delta.plugins.items.Items;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.util.List;

public class givesword {
    public static CommandAPICommand command = new CommandAPICommand("channeler")
            .withPermission("delta.channeler")
            .executesPlayer((player, args) -> {
                // Assuming Items.espada_uno is a valid ItemStack
                player.getInventory().addItem(Items.abyss_test);
                hotbarMessage.sendHotbarMessage(player, "You have been given a channeler!");
            });
    public static CommandAPICommand container = new CommandAPICommand("container")
            .withPermission("delta.channeler")
            .withArguments(new StringArgument("id"))
            .executesPlayer((player, args) -> {
                player.getInventory().addItem(Items.abyssContainer((String) args.get(0)));
                hotbarMessage.sendHotbarMessage(player, "You have been given a container with id " + args.get(0)+ "!");
            });
    public static CommandAPICommand withdraw = new CommandAPICommand("abysswithdraw")
            .withArguments(new IntegerArgument("number", 1, 4))
            .executesPlayer((player, args) -> {
                int amount = (int) args.get(0);
                List<String> ids = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig().getStringList(player.getName() + ".groupids");
                if(ids.size() < amount) {
                    hotbarMessage.sendHotbarMessage(player, "You do not posses so many abysses.");
                    return;
                }
                String groupId = ids.get(amount - 1);
                player.getInventory().addItem(Items.abyssContainer(groupId));
                ids.remove(amount - 1);
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig().set(player.getName() + ".groupids", ids);
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).saveConfig();
                com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
                hotbarMessage.sendHotbarMessage(player, "You have withdrawn an abyss (ID: " + groupId + ") from your list.");
            });
}
