package com.delta.plugins.events.techEvents;

import com.delta.plugins.Plugin;
import com.rschao.events.definitions.PlayerPopHeartEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DestinyBondEvents implements Listener {
    private static final Set<UUID> hasPopped = new HashSet<>();
    private static final Map<UUID, Integer> destinyBondPops = new HashMap<>();
    private static final Map<UUID, UUID> destinyBonded = new HashMap<>();

    @EventHandler
    public void onPlayerPop(PlayerPopHeartEvent e){
        Player victim = e.getPlayer();
        if (hasPopped.contains(victim.getUniqueId())) return;
        if (!checkDestinyBondVictim(victim)) return;

        UUID casterUuid = getDestinyBondCaster(victim);
        if (casterUuid == null) return;

        int pops = destinyBondPops.getOrDefault(casterUuid, 0);
        destinyBondPops.put(casterUuid, pops + 1);

        Player caster = victim.getServer().getPlayer(casterUuid);
        if (caster != null && caster.isOnline() && !hasPopped.contains(casterUuid)){
            // Marcar al caster para evitar reentradas cuando se le aplique daño / efecto
            hasPopped.add(casterUuid);
            // limpiar la marca en el siguiente tick
            new BukkitRunnable(){
                @Override public void run() { hasPopped.remove(casterUuid); }
            }.runTaskLater(Plugin.getPlugin(Plugin.class), 1L);

            caster.damage(9999, victim);
        }

        if (pops + 1 >= 9){
            destinyBondPops.remove(casterUuid);
            destinyBonded.remove(casterUuid);
            victim.sendMessage("Your Destiny Bond has been fulfilled.");
            if (caster != null) caster.sendMessage("Your Destiny Bond has been fulfilled.");
        }
    }

    @EventHandler
    public void onCasterPop(PlayerPopHeartEvent e){
        Player caster = e.getPlayer();
        if (hasPopped.contains(caster.getUniqueId())) return;
        if (!checkDestinyBondCaster(caster)) return;

        UUID victimUuid = destinyBonded.get(caster.getUniqueId());
        if (victimUuid == null) return;

        int pops = destinyBondPops.getOrDefault(caster.getUniqueId(), 0);
        destinyBondPops.put(caster.getUniqueId(), pops + 1);

        Player victim = caster.getServer().getPlayer(victimUuid);
        if (victim != null && victim.isOnline() && !hasPopped.contains(victimUuid)){
            // Marcar al victim para evitar reentradas
            hasPopped.add(victimUuid);
            new BukkitRunnable(){
                @Override public void run() { hasPopped.remove(victimUuid); }
            }.runTaskLater(Plugin.getPlugin(Plugin.class), 1L);

            victim.damage(9999, caster);
        }

        if (pops + 1 >= 9){
            destinyBondPops.remove(caster.getUniqueId());
            destinyBonded.remove(caster.getUniqueId());
            caster.sendMessage("Your Destiny Bond has been fulfilled.");
            if (victim != null) victim.sendMessage("Your Destiny Bond has been fulfilled.");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player victim = e.getEntity();
        if (hasPopped.contains(victim.getUniqueId())) return;
        if(!checkDestinyBondVictim(victim)) return;

        UUID casterUuid = getDestinyBondCaster(victim);
        if(casterUuid == null) return;

        Player caster = victim.getServer().getPlayer(casterUuid);
        if(caster != null && caster.isOnline() && !hasPopped.contains(casterUuid)){
            victim.sendMessage("Your Destiny Bond has been fulfilled.");
            caster.sendMessage("Your Destiny Bond has been fulfilled.");

            hasPopped.add(casterUuid);
            new BukkitRunnable(){
                @Override public void run() { hasPopped.remove(casterUuid); }
            }.runTaskLater(Plugin.getPlugin(Plugin.class), 1L);
            caster.setHealth(0);
        }
        destinyBondPops.remove(casterUuid);
        destinyBonded.remove(casterUuid);
    }

    @EventHandler
    public void onCasterDeath(PlayerDeathEvent e){
        Player caster = e.getEntity();
        if (hasPopped.contains(caster.getUniqueId())) return;
        if(!checkDestinyBondCaster(caster)) return;

        UUID victimUuid = destinyBonded.get(caster.getUniqueId());
        if(victimUuid == null) return;

        Player victim = caster.getServer().getPlayer(victimUuid);
        if(victim != null && victim.isOnline()&& !hasPopped.contains(victimUuid)){
            caster.sendMessage("Your Destiny Bond has been fulfilled.");
            victim.sendMessage("Your Destiny Bond has been fulfilled.");
            hasPopped.add(victimUuid);
            new BukkitRunnable(){
                @Override public void run() { hasPopped.remove(victimUuid); }
            }.runTaskLater(Plugin.getPlugin(Plugin.class), 1L);
            victim.setHealth(0);
        }
        destinyBondPops.remove(caster.getUniqueId());
        destinyBonded.remove(caster.getUniqueId());
    }

    static boolean checkDestinyBondCaster(Player p){
        UUID pUUID = p.getUniqueId();
        if(!destinyBonded.containsKey(pUUID)) return false;
        return true;
    }
    static boolean checkDestinyBondVictim(Player p){
        UUID pUUID = p.getUniqueId();
        if(!destinyBonded.containsValue(pUUID)) return false;
        return true;
    }
    static UUID getDestinyBondCaster(Player p){
        UUID pUUID = p.getUniqueId();
        for(Map.Entry<UUID, UUID> entry : destinyBonded.entrySet()){
            if(entry.getValue().equals(pUUID)){
                return entry.getKey();
            }
        }
        return null;
    }

    public static void addDestinyBond(Player p, Player target){
        UUID pUUID = p.getUniqueId();
        destinyBondPops.put(pUUID, 0);
        destinyBonded.put(pUUID, target.getUniqueId());
    }
}
