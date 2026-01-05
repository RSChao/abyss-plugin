package com.delta.plugins.events;

import com.delta.plugins.items.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;

public class itemDisplayEvents implements Listener {

    // Lista de ItemStacks permitidos (ejemplo)
    private static final List<ItemStack> ALLOWED_ITEMS = Arrays.asList(
            Items.pure_heart_red,
            Items.pure_heart_brown,
            Items.pure_heart_blue,
            Items.pure_heart_cyan,
            Items.pure_heart_purple,
            Items.pure_heart_pink,
            Items.pure_heart_yellow,
            Items.pure_heart_grey
    );

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        boolean allowed = ALLOWED_ITEMS.stream().anyMatch(allowedItem ->
                allowedItem.isSimilar(item)
        );
        if (!allowed) return;

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) return;

        Location dropLoc = targetBlock.getLocation().add(0.5, 1.0, 0.5);

        // Eliminar 1 item del inventario del jugador
        ItemStack toRemove = item.clone();
        toRemove.setAmount(1);
        player.getInventory().removeItem(toRemove);

        // Dropear el item como entidad flotante
        Item dropped = player.getWorld().dropItem(dropLoc, toRemove);
        dropped.setVelocity(new Vector(0, 0, 0)); // Sin velocidad inicial
        dropped.setGravity(false);
        dropped.setUnlimitedLifetime(true);
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setPersistent(true); // No envejece/desaparece

        event.setCancelled(true);
    }
}
