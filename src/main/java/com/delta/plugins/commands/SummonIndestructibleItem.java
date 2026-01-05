package com.delta.plugins.commands;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class SummonIndestructibleItem {
    public static CommandAPICommand command = new CommandAPICommand("indestructilbeitem")
            .withPermission("delta.items")
            .executesPlayer((player, args) -> {
                Item i = player.getWorld().dropItem(player.getLocation(), player.getInventory().getItemInMainHand());
                i.setInvulnerable(true);
                i.setVelocity(new Vector(0, 0, 0));
                i.setGravity(false);
                i.setPickupDelay(Integer.MAX_VALUE);
                i.setPersistent(true);
                i.setGlowing(true);
                i.setUnlimitedLifetime(true);
            });
}
