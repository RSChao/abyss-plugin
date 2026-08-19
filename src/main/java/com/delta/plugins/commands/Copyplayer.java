package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.rschao.plugins.showdowncore.showdownCore.api.runnables.ShowdownScript;
import com.rschao.plugins.showdowncore.showdownCore.api.runnables.registry.ScriptRegistry;
import com.rschao.plugins.showdowncore.showdownCore.gui.recipe.ActionRegistry;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Copyplayer {

    // Mapa: copiaUuid -> originalUuid
    public static final Map<UUID, UUID> copyOriginalMap = new ConcurrentHashMap<>();

    public static void register() {
        CommandAPICommand cmd = new CommandAPICommand("copyplayer")
                .withPermission("delta.playercopy")
                .withArguments(new OnePlayer("player"))
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

                    ShowdownScript scr = ScriptRegistry.getScript("events:copyplayer");
                    if(scr != null) {
                        scr.setArgs(player, target);
                        scr.run();
                    } else {
                        player.sendMessage("No script found for copyplayer.");
                    }
                });
        cmd.register();
    }
}
