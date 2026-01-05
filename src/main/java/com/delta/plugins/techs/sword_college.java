package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class sword_college {
    static final String TECH_ID = "college_of_swords";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, etherealblade);
        TechRegistry.registerTechnique(TECH_ID, resetCooldown);
        TechRegistry.registerTechnique(TECH_ID, zippit);
        TechRegistry.registerTechnique(TECH_ID, noAttackCooldown);
        TechRegistry.registerTechnique(TECH_ID, ulti);

    }

    static Technique etherealblade = new Technique(
    "etherealblade",
    "Ethereal Blade",
    new TechniqueMeta(false, 60000, List.of("Teleport behind a target and create explosions.")),
    TargetSelectors.self(),
    (ctx, token) -> {
        Player player = ctx.caster();

        // 1. Find random target within 200 blocks
        Player target = roaring_soul.getClosestPlayer(player.getLocation());
        if(target == null) {
            player.sendMessage("No target found within 200 blocks.");
            return;
        }
        if (PlayerTechniqueManager.isInmune(target.getUniqueId()) || target.getLocation().distance(player.getLocation()) > 200) {
            player.sendMessage("No target found within 200 blocks.");
            return;
        }
        /*PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission("yes",true);*/

        // 2. Apply blindness 255 for 5s, slowness 255 for 1s
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 254, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 2, 254, false, false, false));

        // 3. Teleport user 2 blocks behind target, facing them
        Location behind = target.getLocation().clone();
        Location playerLoc = player.getLocation().clone();
        behind.setDirection(target.getLocation().getDirection().multiply(-1));
        behind = behind.add(behind.getDirection().normalize().multiply(2));
        behind.setYaw(target.getLocation().getYaw() + 180);
        player.teleport(behind);
        player.teleport(player.getLocation().setDirection(target.getLocation().subtract(player.getLocation()).toVector()));

        // Nuevo: generar explosiones potencia 7 en cada bloque entre playerLoc y behind
        {
            Vector dir = behind.toVector().subtract(playerLoc.toVector());
            double distance = dir.length();
            if (distance > 0) {
                Vector step = dir.clone().normalize();
                int steps = (int) Math.ceil(distance);
                // usar el mundo de playerLoc
                if (playerLoc.getWorld() != null) {
                    for (int i = 0; i <= steps; i++) {
                        Location exLoc = playerLoc.clone().add(step.clone().multiply(i));
                        playerLoc.getWorld().createExplosion(exLoc, 7F, false, false, player);
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> CooldownManager.setCooldown(player, "geno", cooldownHelper.minutesToMiliseconds(5)), 2);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), true), 7);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), false), 18);
    });



    static Technique resetCooldown = new Technique(
    "reset_cooldown_delta",
    "Floritures go brrr",
    new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(30), List.of("Remove all cooldowns.")),
    TargetSelectors.self(),
    (ctx, token) -> {
        Player player = ctx.caster();
        CooldownManager.removeAllCooldowns(player);
        CooldownManager.setCooldown(player, "reset_cooldown_delta", cooldownHelper.minutesToMiliseconds(30));
        hotbarMessage.sendHotbarMessage(player, "¡Has reiniciado tus cooldowns!");
    });

    // zippit: proxy a la acción de otra técnica
    static Technique zippit = new Technique(
        "zippit",
        "Zippit",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Proxy to cooldownfunny action.")),
        TargetSelectors.self(),
        TechRegistry.getById("cooldownfunny").getAction()
    );

    // ulti convertido al nuevo patrón
    static Technique ulti = new Technique(
        "danza",
        "Ascent to Heaven",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(60), List.of("Send nearby players skyward and explode them.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            List<Player> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != player && p.getLocation().distance(player.getLocation()) < 100) {
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                    players.add(p);
                }
            }
            int days = (int) (Bukkit.getWorlds().get(0).getTime() / 24000);
            Bukkit.getWorlds().get(0).setTime(24000 * (days + 1) + 14000);
            for (Player p : players) {
                p.setVelocity(new Vector(0, 500, 0));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    p.getWorld().createExplosion(p.getLocation(), 20, false, false);
                    p.setVelocity(new Vector(0, 0, 0));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Bukkit.getWorlds().get(0).setTime(24000 * (days + 1));
                    }, 5 * 20);
                }, 20);
            }
            hotbarMessage.sendHotbarMessage(player, "¡Has ascendido al cielo!");
        }
    );

    // noAttackCooldown convertido
    static Technique noAttackCooldown = new Technique(
        "no_attack_cooldown",
        "*swish* Olé!",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(5), List.of("Temporarily set attack speed very high.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(999);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4);
                }, 60 * 20);
            }, 2);
            hotbarMessage.sendHotbarMessage(player, "¡Ataque sin cooldown activado!");
        }
    );

}
