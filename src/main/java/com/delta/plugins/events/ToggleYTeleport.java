package com.delta.plugins.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class ToggleYTeleport  implements Listener {
    public Map<Player, Location> beds = new HashMap<>();
    public static int y_trigger = 0;

    @EventHandler
    void onPlayerRightClickBed(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType().toString().contains("BED")) {
            beds.put(player, event.getClickedBlock().getLocation());
        }
    }
    @EventHandler
    void onPlayerFall(PlayerMoveEvent event) {
        if(y_trigger == -316) {
            event.getHandlers().unregister(this);
            return;
        }
        if(beds.containsKey(event.getPlayer()) && event.getPlayer().getLocation().getY() <= y_trigger) {
            event.getPlayer().teleport(beds.get(event.getPlayer()));
        }
    }
}
