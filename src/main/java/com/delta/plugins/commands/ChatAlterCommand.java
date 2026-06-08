package com.delta.plugins.commands;

import com.delta.plugins.events.ChatAlterEvents;
import dev.jorel.commandapi.CommandAPICommand;

public class ChatAlterCommand {

    public static CommandAPICommand command = new CommandAPICommand("chatalter")
            .withPermission("delta.chatalter")
            .withArguments(new dev.jorel.commandapi.arguments.StringArgument("newPerson"))
            .executesPlayer((player, args) -> {
                String newPerson = (String) args.get(0);
                ChatAlterEvents.chatAlterMap.put(player.getUniqueId(), newPerson);
                assert newPerson != null;
                if(newPerson.equals("off")){
                    ChatAlterEvents.chatAlterMap.remove(player.getUniqueId());
                    player.sendMessage("Chat alteration turned off.");
                } else {
                    player.sendMessage("Your chat name has been changed to: " + newPerson);
                }
            });
}
