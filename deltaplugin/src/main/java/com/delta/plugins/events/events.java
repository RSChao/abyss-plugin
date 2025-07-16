package com.delta.plugins.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

public class events implements Listener {
    public static final Map<UUID, Boolean> hasCritDamage = new HashMap<>();
    public static final Map<UUID, Integer> playerTechniques = new HashMap<>();
    // Track which group id index the player is currently using (0-2)
    public static final Map<UUID, Integer> playerGroupIdIndex = new HashMap<>();

    @EventHandler
    void onPlayerUseTech(PlayerInteractEvent ev){
        Player p = ev.getPlayer();
        if(ev.getItem() == null) return;
        if(ev.getItem().getType() == null) return;
        if(!ev.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "channeler"), PersistentDataType.BOOLEAN)) return;
        // Get current group id index for player (default 0)
        int groupIndex = playerGroupIdIndex.getOrDefault(p.getUniqueId(), 0);
        String groupId = getGroupId(p, groupIndex);
        if (groupId == null || groupId.equals("none")) {
            p.sendMessage("You have no group ids set.");
            return;
        }

        // Get current technique index for this group id
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);

        if(ev.getAction().toString().contains("LEFT")){
            if(p.isSneaking()){
                // Cycle group id index (0-2)
                int maxGroups = getGroupIdCount(p);
                if (maxGroups == 0) {
                    p.sendMessage("You have no group ids to switch to!");
                    return;
                }
                int newIndex = (groupIndex + 1) % maxGroups;
                playerGroupIdIndex.put(p.getUniqueId(), newIndex);
                String newGroupId = getGroupId(p, newIndex);
                p.sendMessage("You have switched to group id: " + newGroupId);
            }
            else{
                Technique technique = TechRegistry.getAllTechniques(groupId).get(techIndex);
                if(technique == null) return;
                technique.use(p, ev.getItem(), Technique.nullValue());
            }
        }
        else if(ev.getAction().toString().contains("RIGHT")){
            if(p.isSneaking()){
                if (techIndex == 0) {
                    PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, TechRegistry.getAllTechniques(groupId).size() - 1);
                } else {
                    PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, techIndex - 1);
                }
            }
            else{
                PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, (techIndex + 1) % TechRegistry.getAllTechniques(groupId).size());
                techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
            }
            p.sendMessage("You have switched to technique: " + TechRegistry.getAllTechniques(groupId).get(techIndex).getName());
        }
    }

    // Get the group id for a player at a given index (0-2)
    public String getGroupId(Player p, int index){
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        java.util.List<String> groupIds = config.getStringList(p.getName() + ".groupids");
        if (groupIds == null || groupIds.isEmpty() || index < 0 || index >= groupIds.size()) {
            return "none";
        }
        return groupIds.get(index);
    }

    // Get the number of group ids a player has
    public int getGroupIdCount(Player p) {
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        java.util.List<String> groupIds = config.getStringList(p.getName() + ".groupids");
        return groupIds == null ? 0 : groupIds.size();
    }

    @EventHandler
    public void mobOuchie(EntityDamageByEntityEvent event) {
        //check if the player has crit damage enabled
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID playerId = player.getUniqueId();
            if (hasCritDamage.getOrDefault(playerId, false)) {
                // Apply crit damage logic here
                double damage = event.getDamage();
                event.setDamage(damage * 1.5); // Example: increase damage by 50%
                player.sendMessage("You dealt critical damage!");
            }
        }
        
    }
}
