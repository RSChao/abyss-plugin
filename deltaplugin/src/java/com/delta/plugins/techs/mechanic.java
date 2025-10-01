package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.CooldownManager;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class mechanic {
    static final String TECH_ID = "mechanic";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, gearshift);
        TechRegistry.registerTechnique("mechanic", engine);
        TechRegistry.registerTechnique("mechanic", buzzsaws);
        TechRegistry.registerTechnique("mechanic", machinegun);
    }

    static Technique gearshift = new Technique("gearshift", "Gearshift", true, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {;
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(50*20, 2));
        player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(50*20, 4));
        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lGearshift§a!");
    });

    static Technique engine = new Technique("engine", "Engine Rush", true, cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {;
            Player p = player;
            for(String id :com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                for(Technique t : TechRegistry.getNormalTechniques(id)){
                    long cooldown = CooldownManager.getRemaining(player, t.getId());
                    CooldownManager.setCooldown(p, t.getId(), events.hasPurityHeart(p)? cooldown/4 : cooldown/2);
                }
            }
            for(String id : Plugin.getAllAbyssIDs()){
                for(Technique t : TechRegistry.getNormalTechniques(id)){
                    long cooldown = CooldownManager.getRemaining(player, t.getId());
                    CooldownManager.setCooldown(p, t.getId(), events.hasPurityHeart(p)? cooldown/4 : cooldown/2);
                }
            }


        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lEngine Rush§a!");
    });
    static Technique buzzsaws = new Technique("buzzsaws", "Buzz saws!", true, cooldownHelper.minutesToMiliseconds(4), (player, item, args) -> {;

        //shoot a bunch of arrows in a circle around the player
        for (int i = 0; i < 360; i += 5) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * 5; // Radius of the circle
            double z = Math.sin(angle) * 5; // Radius of the circle
            Vector direction = player.getLocation().getDirection().normalize();
            direction.setX(direction.getX() + x);
            direction.setZ(direction.getZ() + z);
            // Spawn the arrow at the player's location, slightly above ground
            Arrow arrow = player.getWorld().spawnArrow(player.getLocation().add(x, 1, z),
                    direction, 1.0f, 12.0f);
            arrow.setDamage(20);
            arrow.setVelocity(direction.multiply(5));

        }

        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lBuzzsaws");
    });

    static Technique machinegun = new Technique("machinegun", "RATATATATATA", true, cooldownHelper.minutesToMiliseconds(6), (player, item, args) -> {;
        for (int i = 0; i < 15; i++) {
            Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                Vector direction = player.getEyeLocation().getDirection().normalize();
                Vector randomOffset = new Vector(
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2,
                        (Math.random() - 0.5) * 0.2
                );
                Vector finalDirection = direction.add(randomOffset).multiply(5); // Adjust speed multiplier as needed
                org.bukkit.entity.Arrow arrow = player.launchProjectile(org.bukkit.entity.Arrow.class, finalDirection);
                arrow.setDamage(20.0); // Set base damage to 50
                arrow.setGravity(false);
                arrow.setVelocity(finalDirection);
                arrow.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED); // Prevent pickup
            }, i * 2L); // Slight delay between each arrow
        }

        hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lRATATATA!");
    });


}
