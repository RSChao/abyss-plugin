package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

import java.util.List;

public class Copyplayer {
    public static void register() {
        CommandAPICommand cmd = new CommandAPICommand("copyplayer")
                .withPermission("delta.admin")
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    if (args.count() < 1) {
                        sender.sendMessage("Usage: /copyplayer <playername>");
                        return;
                    }
                    Player target = (Player) args.get(0);
                    if (target == null) {
                        sender.sendMessage("Player not found: " + target.getName());
                        return;
                    }
                    String playerName = target.getName();
                    if (!(sender instanceof org.bukkit.entity.Player)) {
                        sender.sendMessage("This command can only be run by a player.");
                        return;
                    }
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

                    // Copy inventory
                    player.getInventory().setContents(target.getInventory().getContents());
                    player.getInventory().setArmorContents(target.getInventory().getArmorContents());
                    player.updateInventory();

                    int soul1 = com.rschao.events.soulEvents.GetSoulN(target);
                    int soul2 = com.rschao.events.soulEvents.GetSecondSoulN(target);
                    com.rschao.events.soulEvents.setSouls(player, soul1, soul2);


                    com.rschao.plugins.fightingpp.events.events events = new com.rschao.plugins.fightingpp.events.events();
                    List<String> fruits = events.getPlayerFruits(playerName);
                    for (String fruit : fruits) {
                        com.rschao.plugins.fightingpp.events.events.saveFruitToConfig(playerName, fruit);
                        com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened(playerName, fruit, true);
                    }
                    List<String> abyss = Plugin.getPlugin(Plugin.class).getConfig().getStringList(target.getName() + ".groupids");
                    Plugin.getPlugin(Plugin.class).getConfig().set(player.getName() + ".groupids", abyss);

                });
        cmd.register();
    }
}
