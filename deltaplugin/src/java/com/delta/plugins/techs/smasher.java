package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class smasher {
    static final String TECH_ID = "smasher";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, mazinger);
        TechRegistry.registerTechnique(TECH_ID, petasion);
        TechRegistry.registerTechnique(TECH_ID, gorila);
    }
    static Technique mazinger = new Technique("punetaso", "Heavy Fist", false, cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {
        events.hasCritDamage.put(player.getUniqueId(), true);
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            events.hasCritDamage.put(player.getUniqueId(), false);
        }, 20L * (60*3));
    });
    static Technique petasion = new Technique("petasion", "Bulked up body", false, cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 120, 0));
    });

    static Technique gorila = new Technique("gorila", "Gorilla Press", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            Location location = player.getLocation();
            //get the direction towards the closest player
            Vector direction = player.getEyeLocation().getDirection().normalize();
            Location launch = location.add(direction.multiply(2));

            for (int i = 0; i < 3; i++) {

                Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                    org.bukkit.entity.Fireball fireball = player.getWorld().spawn(launch, org.bukkit.entity.Fireball.class);
                    fireball.setDirection(direction);
                    fireball.setYield(4F);
                    fireball.setIsIncendiary(true);
                    fireball.setShooter(player);
                    fireball.setVelocity(direction.multiply(4));
                    fireball.setCustomName("determinationBall");
                }, i * 3);
            }
        }, 40);
    });
}
