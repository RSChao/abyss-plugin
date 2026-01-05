package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class arcane {
    static final String TECH_ID = "arcane";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, lostsouls);
        TechRegistry.registerTechnique(TECH_ID, towerofflames);
        TechRegistry.registerTechnique(TECH_ID, sonic_boom);
    }

    static Technique lostsouls = new Technique("lostsouls", "Lost Souls", false, cooldownHelper.minutesToMiliseconds(2), (player, item, args) -> {
        for(int i = 0; i < 4; i++) {
            // shoot a wither skull in the direction the player is looking
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                org.bukkit.entity.WitherSkull skull = player.launchProjectile(org.bukkit.entity.WitherSkull.class);
                skull.setVelocity(player.getLocation().getDirection().multiply(2));
                skull.setShooter(player);
                skull.setCustomName("lostsoul");
                skull.setCustomNameVisible(true);
                skull.setYield(7F);
            }, i * 3L);
        }
    });
    static Technique towerofflames = new Technique("towerofflames", "Tower of Flames", false, cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {
        for (org.bukkit.entity.Player target : player.getWorld().getPlayers()) {
            // Excluir jugadores inmunes
            if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
            if (target.getLocation().distance(player.getLocation()) <= 20) {
                org.bukkit.Location loc = target.getLocation().clone();
                loc.setY(loc.getY() - 1);
                // create a blue particle effect at the location
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
    });

    static Technique sonic_boom = new Technique("sonicbum", "Soulles Scream", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args) -> {
        Location origin = player.getLocation();
        Player target = roaring_soul.getClosestPlayer(origin);
        // Excluir objetivo nulo, el activador y objetivos inmunes
        if (target != null && !target.equals(player) && !PlayerTechniqueManager.isInmune(target.getUniqueId())) {
            Location targetLoc = target.getEyeLocation();
            Vector direction = targetLoc.toVector().subtract(player.getEyeLocation().toVector()).normalize();
            // Generar partículas de sonic boom del warden
            double distance = player.getEyeLocation().distance(targetLoc) ;
            int steps = (int) (distance * 4); // más pasos = más partículas
            for (int i = 0; i <= steps; i++) {
                Location particleLoc = player.getEyeLocation().clone().add(direction.clone().multiply(i * (distance / steps)));
                player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLoc, 1, 0, 0, 0, 0);
            }



            target.damage(14.0, player); // ignora armadura si el plugin/servidor lo permite
            // Para ignorar armadura completamente, puedes usar:
            target.setNoDamageTicks(5); // permite daño inmediato
            target.setHealth(Math.max(0, target.getHealth() - 14.0));
            target.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20*5, 255));
            target.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(20*5, 255));
        }
        hotbarMessage.sendHotbarMessage(player, "Sonic Boom activated!");
    });
}


/*Abyss of the Arcane

Lost Souls (4 projectiles de wither)

Tower of flames (genera un dt de particulas azules bajo todos los jugadores en un rango de 20 bloques)

Souless Scream (Sonic Boom + glich)
*/