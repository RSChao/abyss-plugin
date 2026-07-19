package com.delta.plugins.commands;

import com.delta.plugins.techs.BlenderOfDoom;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;

public class RegisterBlender {
        public static CommandAPICommand cnd = new CommandAPICommand("registerblender")
                .withPermission("delta.registerblender")
                .withArguments(new StringArgument("group"))
                .executesPlayer((player, args) -> {
                    String group = (String) args.get(0);
                    player.sendMessage("Registering blender for group: " + group);
                    // Here you would add the logic to register the blender for the specified group
                    BlenderOfDoom.saveGroup(group);
                    BlenderOfDoom.register(group);
                });
}
