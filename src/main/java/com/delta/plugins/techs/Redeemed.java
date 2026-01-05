package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Redeemed {
    static final String TECH_ID = "redeemed";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, chains);
        TechRegistry.registerTechnique(TECH_ID, darkWorld);
        TechRegistry.registerTechnique(TECH_ID, redeemedMF);
    }


    static Technique chains = new Technique(
        "deltateodiodos",
        "Blessed Chains",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Teleport and buff a nearby player repeatedly.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Player closestPlayer = chao.getClosestPlayer(player.getLocation());
            // Excluir objetivo inmune
            if (closestPlayer != null && PlayerTechniqueManager.isInmune(closestPlayer.getUniqueId())) {
                player.sendMessage("No players nearby to launch.");
                return;
            }
            if (closestPlayer != null) {
                for (int i = 0; i < 100; i++) {
                    int t = i % 5;
                    Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(20);
                        Location targetLocation = player.getEyeLocation().add(direction);
                        closestPlayer.teleport(targetLocation);

                        if (t == 0) {
                            // Excluir inmunes (seguridad redundante)
                            if (!PlayerTechniqueManager.isInmune(closestPlayer.getUniqueId())) {
                                closestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1, false, false));
                                closestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1, false, false));
                            }
                        }
                    }, i);
                }
                hotbarMessage.sendHotbarMessage(player, "You have used the Nightmare Chains technique!");
            } else {
                player.sendMessage("No players nearby to launch.");
            }
        }
    );

    static Technique darkWorld = new Technique(
        "why",
        "Golden Flare",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Create golden aura and buff nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
            int maxRadius = (events.hasChaosHeart(player) ? 70 : 50);
            Set<Block> sphereBlocks = new HashSet<>();
            Set<BlockState> replacedBlocks = new HashSet<>();

            // Dar visión nocturna y fuerza por 1 minuto
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, (events.hasChaosHeart(player)? 5 : 4), false, false, false));
            for(Entity e : player.getNearbyEntities(10, 10, 10)){
                if(e instanceof Player){
                    Player p = (Player) e;
                    p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, (events.hasPurityHeart(p)? 5 : 4), false, false, false));
                }
            }

            // Iniciar el efecto Dark World
            startDarkWorldEffect(player, center, maxRadius, sphereBlocks, replacedBlocks);
        }
    );

    private static void startDarkWorldEffect(Player user, Location center, int radius, Set<Block> sphereBlocks, Set<BlockState> replacedBlocks) {
        World world = center.getWorld();
        Set<Player> affectedPlayers = new HashSet<>();
        // 1. Apply effects and start repeating task
        BukkitRunnable effectTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // Apply effects to players in radius except user
                for (Player p : world.getPlayers()) {
                    if (!p.equals(user) && p.getLocation().distance(center) <= radius) {
                        // Excluir jugadores inmunes
                        if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        int defaultlvl = 3;
                        if(events.hasChaosHeart(user) && !events.hasPurityHeart(p)) defaultlvl += 1;
                        if(p.hasPermission("gaster.boss")) continue;


                        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, defaultlvl+1, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, defaultlvl, false, false, true));

                        affectedPlayers.add(p);
                    }
                }
                // Remove enderpearls not from user, and all shulker bullets
                for (Entity e : world.getEntities()) {
                    if (e instanceof EnderPearl) {
                        EnderPearl ep = (EnderPearl) e;
                        if (!(ep.getShooter() instanceof Player) || !ep.getShooter().equals(user)) {
                            if (ep.getShooter() instanceof Player) {
                                Player p = (Player) ep.getShooter();
                                // Excluir inmunes y pure heart
                                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                                if(events.hasPurityHeart(p)) continue;
                            }
                            e.remove();
                        }
                    }
                }
                ticks += 2;
                for(Player p : affectedPlayers){
                    if(ticks <31 && p.getLocation().distance(center) >= radius){
                        p.teleport(center);
                    }
                }
                if (ticks >= 20 * 60) { // 1 minute
                    this.cancel();
                }
            }
        };
        effectTask.runTaskTimer(Plugin.getPlugin(Plugin.class), 2L, 2L);
    }

    static Technique redeemedMF = new Technique(
        "redeemedmf",
        "Redeemed Blade",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(7), List.of("Temporarily boost attack speed and grant AoE buffs.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
           double l = player.getAttribute(Attribute.ATTACK_SPEED).getBaseValue();
           player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(999);
           for(Player p : Bukkit.getOnlinePlayers()){
               if(p.getLocation().distance(player.getLocation()) < 20){
                   // Excluir jugadores inmunes
                   if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                   int defaultlvl = 3;
                   p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120*20, defaultlvl+1, false, false, true));
                   p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120*20, defaultlvl, false, false, true));
                   p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 120*20, defaultlvl, false, false, true));
                   p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120*20, defaultlvl, false, false, true));
                   p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120*20, defaultlvl, false, false, true));
               }
           }
           Bukkit.getScheduler().runTaskLater(plugin, () ->{
               player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(l);
           }, 120*20);
        }
    );
}
