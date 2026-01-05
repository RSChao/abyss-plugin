package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

import org.bukkit.entity.WitherSkull;

import java.util.List;

public class arcane {
    static final String TECH_ID = "arcane";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, lostsouls);
        TechRegistry.registerTechnique(TECH_ID, towerofflames);
        TechRegistry.registerTechnique(TECH_ID, sonic_boom);
    }

    static Technique lostsouls = new Technique(
        "lostsouls",
        "Lost Souls",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("Shoot 4 wither skulls forward.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for (int i = 0; i < 4; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    WitherSkull skull = player.launchProjectile(org.bukkit.entity.WitherSkull.class);
                    skull.setVelocity(player.getLocation().getDirection().multiply(2));
                    skull.setShooter(player);
                    skull.setCustomName("lostsoul");
                    skull.setCustomNameVisible(true);
                    skull.setYield(7F);
                }, i * 3L);
            }
        }
    );

    static Technique towerofflames = new Technique(
        "towerofflames",
        "Tower of Flames",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Blue particle ring and damage nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for (org.bukkit.entity.Player target : player.getWorld().getPlayers()) {
                if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                if (target.getLocation().distance(player.getLocation()) <= 20) {
                    org.bukkit.Location loc = target.getLocation().clone();
                    loc.setY(loc.getY() - 1);
                    for (int i = 0; i < 100; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = Math.random() * 3;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        loc.add(x, 0, z);
                        target.getWorld().spawnParticle(Particle.WITCH, loc, 1, 0, 0, 0, 0);
                        loc.subtract(x, 0, z);
                    }
                    target.damage(30);
                }
            }
        }
    );

    static Technique sonic_boom = new Technique(
        "sonicbum",
        "Soulles Scream",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Sonic boom effect + heavy debuffs.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location origin = player.getLocation();
            Player target = roaring_soul.getClosestPlayer(origin);
            if (target != null && !target.equals(player) && !PlayerTechniqueManager.isInmune(target.getUniqueId())) {
                Location targetLoc = target.getEyeLocation();
                Vector direction = targetLoc.toVector().subtract(player.getEyeLocation().toVector()).normalize();
                double distance = player.getEyeLocation().distance(targetLoc);
                int steps = (int) (distance * 4);
                for (int i = 0; i <= steps; i++) {
                    Location particleLoc = player.getEyeLocation().clone().add(direction.clone().multiply(i * (distance / (double)Math.max(1, steps))));
                    player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
                }
                target.damage(14.0, player);
                target.setNoDamageTicks(5);
                target.setHealth(Math.max(0, target.getHealth() - 14.0));
                target.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20 * 5, 255));
                target.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(20 * 5, 255));
            }
            hotbarMessage.sendHotbarMessage(player, "Sonic Boom activated!");
        }
    );
}


/*Abyss of the Arcane

Lost Souls (4 projectiles de wither)

Tower of flames (genera un dt de particulas azules bajo todos los jugadores en un rango de 20 bloques)

Souless Scream (Sonic Boom + glich)
*/