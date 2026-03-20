package com.delta.plugins.events.definition;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KatanaSheathEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final Player player;

    public KatanaSheathEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
