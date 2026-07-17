package com.delta.plugins.events;

import com.rschao.plugins.techniqueAPI.event.TechniquePreRunEvent;
import com.rschao.plugins.techniqueAPI.event.TechniqueRunEvent;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TechFlagEvents implements Listener {
    public static final Map<UUID, Boolean> kageTechBans = new HashMap<>();
    public static final Map<UUID, Boolean> koritsuTargets = new HashMap<>();
    public static final Map<UUID, UUID> aiTargets = new HashMap<>();


    @EventHandler
    void onTechUse(TechniqueRunEvent ev){
        if(!kageTechBans.getOrDefault(ev.getPlayer().getUniqueId(), false)) return;
        boolean cancelled = false;
        String id = ev.getTechnique().getId();
        if(TechRegistry.getAllTechniques("creator_of_showdown").contains(ev.getTechnique())){
            cancelled = true;
        }
        switch (id){
            case "floor_summon", "heart_atomization", "poof", "dimensional_gather", "dimensional_expulsion", "ganon_ultimate":
                cancelled = true;
                break;
            default:
                break;

        }
        if(cancelled){
            ev.setCancelled(true);
            hotbarMessage.sendHotbarMessage(ev.getPlayer(), "§c[Creator of Showdown] §7You are banned from using this technique.");
        }
    }

    @EventHandler
    void onEntityMove(PlayerMoveEvent ev){
        if(!koritsuTargets.getOrDefault(ev.getPlayer().getUniqueId(), false)) return;
        List<Player> players = ev.getPlayer().getWorld().getPlayers();
        for(Player p : players) {
            if(ev.getPlayer().equals(p)) continue;
            if(!koritsuTargets.containsKey(p.getUniqueId()) || !koritsuTargets.get(p.getUniqueId())) continue;
            assert ev.getTo() != null;
            if(ev.getTo().distance(ev.getPlayer().getLocation()) < 10){
                ev.setCancelled(true);
                hotbarMessage.sendHotbarMessage(ev.getPlayer(), "§c[Koritsu] §7You cannot move too close to another Koritsu target.");
                return;
            }
        }
    }
    @EventHandler
    void koritsuTechBans(TechniqueRunEvent ev){
        if(!koritsuTargets.getOrDefault(ev.getPlayer().getUniqueId(), false)) return;
        boolean cancelled = false;
        String id = ev.getTechnique().getId();
        switch (id){
            case "dimensional_gather", "dimensional_expulsion", "ganon_ultimate":
                cancelled = true;
                break;
            default:
                break;

        }
        if(cancelled){
            ev.setCancelled(true);
            hotbarMessage.sendHotbarMessage(ev.getPlayer(), "§c[Creator of Showdown] §7You are banned from using this technique.");
        }
    }
    @EventHandler
    void aiTechDetarget(TechniquePreRunEvent ev){
        if(aiTargets.getOrDefault(ev.getPlayer().getUniqueId(), null) == null) return;
        if(ev.getTechnique().getMeta().isUltimate()){
            for(Player p : ev.getPlayer().getWorld().getPlayers()){
                if(p.equals(ev.getPlayer())) continue;
                if(!aiTargets.containsKey(p.getUniqueId())) continue;
                PlayerTechniqueManager.setInmune(p.getUniqueId(), true, 1);
                p.sendMessage("§c[Creator of Showdown] §7The Blessing of Love has protected you from " + ev.getPlayer().getName() + "'s ultimate.");
            }
        }

    }
    @EventHandler
    void aiVanillaDamage(EntityDamageByEntityEvent ev){
        if(!(ev.getEntity() instanceof Player)) return;
        Player p = (Player) ev.getEntity();
        if(aiTargets.getOrDefault(p.getUniqueId(), null) == null) return;
        if(ev.getDamager() instanceof Player) {
            Player damager = (Player) ev.getDamager();
            if(aiTargets.getOrDefault(p.getUniqueId(), null) == damager.getUniqueId()){
                ev.setCancelled(true);
                hotbarMessage.sendHotbarMessage(damager, "§c[Creator of Showdown] §7You cannot damage one you have blessed.");
            }
        }
    }
    @EventHandler
    void aiDeath(PlayerDeathEvent ev){
        Player p = ev.getEntity();
        if(aiTargets.containsKey(p.getUniqueId())){
            UUID damager = aiTargets.get(p.getUniqueId());
            Player d = Bukkit.getPlayer(damager);
            if(d != null){
                d.sendMessage("§c[Creator of Showdown] §7Your blessed player " + p.getName() + " has died.");
                CooldownManager.setCooldown(p, "blessing_of_love", cooldownHelper.hour);
            }
            aiTargets.remove(p.getUniqueId());
        }
        if(aiTargets.containsValue(p.getUniqueId())){
            UUID target = null;
            for(Map.Entry<UUID, UUID> entry : aiTargets.entrySet()){
                if(entry.getValue().equals(p.getUniqueId())){
                    target = entry.getKey();
                    break;
                }
            }
            if(target != null){
                Player t = Bukkit.getPlayer(target);
                if(t != null){
                    t.sendMessage("§c[Creator of Showdown] §7Your blesser " + p.getName() + " has died.");
                    CooldownManager.setCooldown(t, "blessing_of_love", cooldownHelper.hour);
                }
                aiTargets.remove(target);
            }
        }
    }
}
