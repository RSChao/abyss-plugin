package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.enchant.techs.PrimalKatana;
import com.delta.plugins.events.definition.KatanaSheathEvent;
import com.delta.plugins.techs.OriginDepleter;
import com.rschao.boss_battle.BossAPI;
import com.rschao.boss_battle.InvManager;
import com.rschao.boss_battle.api.BossHandler;
import com.rschao.boss_battle.api.BossInstance;
import com.rschao.boss_battle.bossEvents;
import com.rschao.enchants.OblivionEnchant;
import com.rschao.events.definitions.BossChangeEvent;
import com.rschao.events.definitions.BossEndEvent;
import com.rschao.events.soulEvents;
import com.rschao.plugins.techniqueAPI.tech.cancel.SimpleCancellationToken;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossEvents implements Listener {

    public static Map<Player, Boolean> isSheathTechOn = new HashMap<>();
    public static Map<Player, Boolean> isGlitchTechOn = new HashMap<>();
    @EventHandler
    void onBossChange(BossChangeEvent ev){
        String playerName = ev.getBossPlayer().getName();
        FileConfiguration bossConfig = ev.config;
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        List<String> abyss = BossAPI.getAddon(bossConfig, ev.getPhase(), "abyss");
        if(abyss.isEmpty()) {
            return;
        }
        if(abyss.size() == 1 && abyss.get(0).equalsIgnoreCase(playerName)) return;
        config.set(playerName + ".groupids", abyss);
        com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).saveConfig();
        com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).reloadConfig();
        Bukkit.getLogger().info("Abyss group ids for " + playerName + " set to " + abyss);
    }

    @EventHandler
    void handleOriginBoss(BossChangeEvent ev){
        String bossName = ev.getBossName();
        if(!bossName.equalsIgnoreCase("s3lore.origin.origin")) return;
        int phase = ev.getPhase();
        if(phase >= 4){
            OriginDepleter.addP4Techs();
            TechRegistry.getById("reset_cooldown_chaos").getAction().execute(new TechniqueContext(ev.getBossPlayer()), new SimpleCancellationToken());
        }
        FileConfiguration configuration = BossHandler.loadBoss("s3lore.origin.minion");
        if(!configuration.contains("boss.world")){
            Bukkit.getLogger().severe("Boss configuration for s3lore.origin.minion is missing 'boss.world'! Please check your boss configurations.");
            return;
        }
        //load inv, souls, fruits and abysses
        Bukkit.getLogger().warning(BossAPI.getKit(configuration, phase));
        InvManager.LoadInventory(Bukkit.getPlayer("Doritospro"), BossAPI.getKit(configuration, phase));
        Bukkit.getPlayer("Doritospro").teleport(ev.getBossPlayer());
        soulEvents.setSouls(Bukkit.getPlayer("Doritospro"), 19, 66);
        for(String fruitId : BossAPI.getAddon(configuration, phase, "fruits")){
            com.rschao.plugins.fightingpp.events.events.saveFruitToConfig("Doritospro", fruitId);
            com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened("Doritospro", fruitId, true);
        }
        List<String> abyss = BossAPI.getAddon(configuration, phase, "abyss");
        if(abyss.isEmpty()) return;

        Plugin.getPlugin(Plugin.class).getConfig().set("Doritospro.groupids", abyss);
        Plugin.getPlugin(Plugin.class).saveConfig();
        Plugin.getPlugin(Plugin.class).reloadConfig();
    }


    @EventHandler
    void onKatanaSheath(KatanaSheathEvent ev){
        Player p = ev.getPlayer();
        if(isSheathTechOn.getOrDefault(p, false)){
            isSheathTechOn.put(p, false);
            Location eye = p.getEyeLocation();
            Vector dir = eye.getDirection().clone().normalize();
            PrimalKatana.spawnSlashEffect(p);
            for (Player t : p.getWorld().getPlayers()) {
                if (t == null || !t.isValid() || t.equals(p)) continue;
                if (t.getLocation().distance(p.getLocation()) > 20) continue;

                Vector to = t.getEyeLocation().toVector().subtract(eye.toVector());
                if (to.lengthSquared() < 0.0001) continue;
                to = to.normalize();
                double dot = dir.dot(to);
                if (Double.isNaN(dot)) continue;
                double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
                if (angle <= 45) {
                    try {
                        t.damage(3000.0, p);
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    @EventHandler
    void onPlayerGlitchTech(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!isGlitchTechOn.getOrDefault(damager, false)) return;
        Entity target = e.getEntity();
        if (!(target instanceof LivingEntity le)) return;
        le.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20*20, 234));
        le.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(20*20, 234));
    }
}
