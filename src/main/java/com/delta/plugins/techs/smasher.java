package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class smasher {
    static final String TECH_ID = "smasher";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, mazinger);
        TechRegistry.registerTechnique(TECH_ID, petasion);
        TechRegistry.registerTechnique(TECH_ID, gorila);
    }
    static Technique mazinger = new Technique(
        "punetaso",
        "Heavy Fist",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Temporary crit damage flag.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            events.hasCritDamage.put(player.getUniqueId(), true);
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                events.hasCritDamage.put(player.getUniqueId(), false);
            }, 20L * (60*3));
        }
    );
    static Technique petasion = new Technique(
        "petasion",
        "Bulked up body",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Grant resistance buff.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 120, 0));
        }
    );

    static Technique gorila = new Technique(
        "gorila",
        "Gorilla Press",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Launch fireball volley after delay.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                Location location = player.getLocation();
                Vector direction = player.getEyeLocation().getDirection().normalize();
                Location launch = location.add(direction.multiply(2));

                for (int i = 0; i < 3; i++) {
                    int delay = i * 3;
                    Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                        org.bukkit.entity.Fireball fireball = player.getWorld().spawn(launch, org.bukkit.entity.Fireball.class);
                        fireball.setDirection(direction);
                        fireball.setYield(4F);
                        fireball.setIsIncendiary(true);
                        fireball.setShooter(player);
                        fireball.setVelocity(direction.multiply(4));
                        fireball.setCustomName("determinationBall");
                    }, delay);
                }
            }, 40);
        }
    );
}
