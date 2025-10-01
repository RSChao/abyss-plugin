package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.delta.plugins.projectiles.DeterminationProjectile;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class offspring {

    static final String TECH_ID = "offspring";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, protonBlastTriple);
        TechRegistry.registerTechnique(TECH_ID, carnage);
        TechRegistry.registerTechnique(TECH_ID, darkWorld);
        TechRegistry.registerTechnique(TECH_ID, fire_res);
    }

    static Technique protonBlastTriple = new Technique("hellfire", "Hellfire", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {
        for(int i = 0; i<30; i++){
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                DeterminationProjectile proj = new DeterminationProjectile(player.getLocation(), player);
                proj.launch();
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(!proj.isValid()){
                            this.cancel();
                            return;
                        }
                        for(Player p : Bukkit.getOnlinePlayers()){
                            if(proj.getDistance(p.getLocation()) < 2 && p != player){
                                p.setNoDamageTicks(5);
                            }
                        }
                    }
                }.runTaskTimer(plugin, 2L, 2L);
            }, i*2L);

        }
    });
    static Technique carnage = new Technique("carnage", "Hatred Carnage", false, cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {
        Location location = player.getLocation();
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, location, 30);
        player.getWorld().playSound(location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);

        for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
            if (entity.getLocation().distance(location) <= 20 && entity != player) {
                if ((entity instanceof Player)) {
                    Player target = (Player) entity;
                    target.damage((events.hasPurityHeart(target)) ? 10 : 30, player);
                    target.addPotionEffect(PotionEffectType.WITHER.createEffect(100, 1));
                }
                Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();
                entity.setVelocity(direction.multiply(3));
            }
        }
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(4));

        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lCarnage§a!");
    });

    static Technique darkWorld = new Technique("dfisdis", "Hope Vanquisher", false, cooldownHelper.minutesToMiliseconds(10), (player, item, args) -> {

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
    });

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
                        int defaultlvl = 0;
                        if(events.hasChaosHeart(user) && !events.hasPurityHeart(p)) defaultlvl += 1;

                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, defaultlvl + 1, false, false, true));

                        affectedPlayers.add(p);
                    }
                }
                // Remove enderpearls not from user, and all shulker bullets
                for (Entity e : world.getEntities()) {
                    if (e instanceof EnderPearl) {
                        EnderPearl ep = (EnderPearl) e;
                        if (!(ep.getShooter() instanceof Player) || !ep.getShooter().equals(user)) {
                            Player p = (Player) ep.getShooter();
                            if(events.hasPurityHeart(p)) return;
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

    static Technique fire_res = new Technique("fire_res", "Fire Resistance", false, cooldownHelper.minutesToMiliseconds(2), (player, item, args) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, false));
        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lFire Resistance§a!");
    });
}
