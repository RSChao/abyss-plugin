package com.delta.plugins.commands;

import com.delta.plugins.items.Items;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import dev.jorel.commandapi.CommandAPICommand;

public class givesword {
    public static CommandAPICommand command = new CommandAPICommand("channeler")
        .executesPlayer((player, args) -> {
            // Assuming Items.espada_uno is a valid ItemStack
            player.getInventory().addItem(Items.abyss_test);
            hotbarMessage.sendHotbarMessage(player, "You have been given a sword!");
        });
}
