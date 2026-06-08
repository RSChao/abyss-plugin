package com.delta.plugins.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;

public class ChatAlterEvents implements Listener {

    public static Map<UUID, String> chatAlterMap;

    @EventHandler
    void onChat(AsyncPlayerChatEvent ev){
        if(chatAlterMap.containsKey(ev.getPlayer().getUniqueId())){
            ev.setCancelled(true);
            String newPerson = chatAlterMap.get(ev.getPlayer().getUniqueId());
            String message = ev.getMessage();
            String formattedMessage = "<" + newPerson + "> " + message;
            for(Player p : ev.getRecipients()){
                p.sendMessage(formattedMessage);
            }
        }
    }

}
