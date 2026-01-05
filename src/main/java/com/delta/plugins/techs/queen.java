package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.fightingpp.events.awakening;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class queen {

    static final String TECH_ID = "queen";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, bless);
        TechRegistry.registerTechnique(TECH_ID, protonBlastTriple);
        TechRegistry.registerTechnique(TECH_ID, laugh);
        TechRegistry.registerTechnique(TECH_ID, fire_res);
    }

    static Technique bless = new Technique(
        "bless",
        "Blessing of the Queen",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Remove harmful effects and grant buffs.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for(PotionEffect effect : player.getActivePotionEffects()){
                if(effect.getType().getCategory() == PotionEffectTypeCategory.HARMFUL){
                    player.removePotionEffect(effect.getType());
                }
            }
            player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(90*20, 1));

            hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lBlessing of the Queen§a!");
        }
    );

    static Technique protonBlastTriple = new Technique(
        "protonblast_triple",
        "Trident Hell Blast",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Three-directional trident blast.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location eyeLoc = player.getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize();

            // Calcula las direcciones rotadas a +30º y -30º horizontalmente
            Vector dirMain = direction.clone();
            Vector dirLeft = rotateYaw(direction.clone(), -30);
            Vector dirRight = rotateYaw(direction.clone(), 30);

            Vector[] dirs = new Vector[] { dirMain, dirLeft, dirRight };

            int durationTicks = 60;
            int arrowStartTick = 20;
            int arrowsToShoot = 20;
            double particleStep = 0.5;
            int particlesPerTick = 16;

            for (Vector dir : dirs) {
                new BukkitRunnable() {
                    int tick = 0;
                    int arrowsShot = 0;
                    @Override
                    public void run() {
                        if (tick >= durationTicks) {
                            this.cancel();
                            return;
                        }
                        for (int i = 0; i < particlesPerTick; i++) {
                            double dist = i * particleStep;
                            Location particleLoc = eyeLoc.clone().add(dir.clone().multiply(dist + tick * particleStep));
                            player.getWorld().spawnParticle(Particle.DUST, particleLoc, 0, new Particle.DustOptions(Color.SILVER, 1.5f));
                        }
                        // Lanzar flechas después de 1 segundo, 1 por tick, hasta 20
                        if (tick >= arrowStartTick && arrowsShot < arrowsToShoot) {
                            Arrow arrow = player.getWorld().spawnArrow(
                                eyeLoc.clone().add(dir.clone().multiply(1.0)),
                                dir.clone(),
                                (float) 1.0,
                                0.0f
                            );
                            arrow.setShooter(player);
                            arrow.setDamage(10.0);
                            arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                            arrow.setVelocity(dir.normalize().multiply(5));
                            arrowsShot++;
                        }
                        tick++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
            hotbarMessage.sendHotbarMessage(player, "&5&lTrident Hell Blast Activated!");
        }
    );

    // Rota un vector horizontalmente (alrededor del eje Y) por grados
    public static Vector rotateYaw(Vector vec, double degrees) {
        double yaw = Math.atan2(vec.getZ(), vec.getX());
        double pitch = Math.asin(vec.getY() / vec.length());
        double newYaw = yaw + Math.toRadians(degrees);
        double xzLen = Math.cos(pitch) * vec.length();
        double x = xzLen * Math.cos(newYaw);
        double z = xzLen * Math.sin(newYaw);
        double y = Math.sin(pitch) * vec.length();
        return new Vector(x, y, z).normalize();
    }

    static Technique laugh = new Technique(
        "lol",
        "The Queen's way",
        new TechniqueMeta(false, cooldownHelper.hour, List.of("Terrify and severely damage nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for (org.bukkit.entity.Entity entity : player.getNearbyEntities(20, 20, 20)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    // Excluir jugadores inmunes
                    if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 255));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 255));
                    target.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH).setBaseValue(0);
                    double jumpstrength = 0.41999998697815;
                    Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                        target.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH).setBaseValue(jumpstrength);
                        target.damage(400);
                    }, 3 * 20);
                }
            }
            hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Queen's way technique");
        }
    );


    static Technique fire_res = new Technique(
        "fire_res",
        "Fire Resistance",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("Grant fire resistance indefinitely.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, false));
            hotbarMessage.sendHotbarMessage(player, "§aYou used §6§lFire Resistance§a!");
        }
    );
}
