package com.delta.plugins.techs;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.CooldownManager;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.Listener;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;

public class roaring_soul implements Listener {
    static final String TECH_ID = "roaring_soul";

    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, geno);
        TechRegistry.registerTechnique(TECH_ID, zombieRain);
    }

    static Technique geno = new Technique("geno", "Dreaded Darkness", false, 500, (player, item, args) ->{
        // 1. Find random target within 200 blocks
        Player target = getClosestPlayer(player.getLocation());
        if(target.getLocation().distance(player.getLocation()) > 200 || target == null) {
            player.sendMessage("No target found within 200 blocks.");
            return;
        }

        // 2. Apply blindness 255 for 5s, slowness 255 for 1s
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 254, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 1, 254, false, false, false));

        // 3. Teleport user 2 blocks behind target, facing them
        Location behind = target.getLocation().clone();
        behind.setDirection(target.getLocation().getDirection().multiply(-1));
        behind = behind.add(behind.getDirection().normalize().multiply(2));
        behind.setYaw(target.getLocation().getYaw() + 180);
        player.teleport(behind);
        player.teleport(player.getLocation().setDirection(target.getLocation().subtract(player.getLocation()).toVector()));
        
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
            CooldownManager.setCooldown(player, "geno", cooldownHelper.minutesToMiliseconds(5));
        }, 2);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
            events.hasGenoDamage.put(player.getUniqueId(), true);
        }, 7);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
            events.hasGenoDamage.put(player.getUniqueId(), false);
        }, 13);
        
    });

    static Technique zombieRain = new Technique("zombierain", "Zombie Rain", false, cooldownHelper.hour, (player, item, args) -> {
        Location spawnLoc = player.getLocation();
        World world = spawnLoc.getWorld();
        for(int i=0;i<30;i++){
            Zombie zombie = (Zombie) world.spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
                zombie.getEquipment().setHelmet(com.rschao.smp.items.Items.OPHelm);
                zombie.getEquipment().setChestplate(com.rschao.smp.items.Items.OPChest);
                zombie.getEquipment().setLeggings(com.rschao.smp.items.Items.OPLeggs);
                zombie.getEquipment().setBoots(com.rschao.smp.items.Items.OPBoots);
                zombie.getEquipment().setItemInMainHand(com.rschao.events.events.buffsword);
                zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60);
        }
        player.sendMessage("Zombies have been summoned!");
    });
    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }
    
    
}
