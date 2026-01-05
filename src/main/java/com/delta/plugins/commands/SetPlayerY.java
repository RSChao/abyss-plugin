package com.delta.plugins.commands;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.ToggleYTeleport;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.Bukkit;

public class SetPlayerY {
    public static void setLowestY(){
        CommandAPICommand check = new CommandAPICommand("check")
                .executes((sender, args) -> {
                    sender.sendMessage("Current safety height value: " + ((ToggleYTeleport.y_trigger ==-316)? "Disabled" : ToggleYTeleport.y_trigger));
                });
        CommandAPICommand set = new CommandAPICommand("set")
                .withArguments(new IntegerArgument("y"))
                .executes((sender, args) -> {
                    int y = (Integer) args.get(0);
                    ToggleYTeleport.y_trigger = y;
                    Bukkit.getServer().getPluginManager().registerEvents(new ToggleYTeleport(), Plugin.getPlugin(Plugin.class));
                });
        CommandAPICommand start = new CommandAPICommand("register")
                .executes((sender, args) -> {
                    Bukkit.getServer().getPluginManager().registerEvents(new ToggleYTeleport(), Plugin.getPlugin(Plugin.class));
                });

        CommandAPICommand cmd = new CommandAPICommand("safetyheight")
                .withPermission("delta.safetyheight")
                .withSubcommands(check, set, start)
                .executes((sender, args) -> {
                    sender.sendMessage("Choose option");
                });
        cmd.register();
    }
}
