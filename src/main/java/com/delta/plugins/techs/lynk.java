package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager; // added

// Nuevos imports necesarios de Bukkit
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

// Nuevos imports para efectos de poción
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Nuevos imports para config y secciones
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.World;

import java.util.List;

public class lynk {

    static final String TECH_ID = "lynk";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, wall);
        TechRegistry.registerTechnique(TECH_ID, paralyze);
        TechRegistry.registerTechnique(TECH_ID, clon);
        TechRegistry.registerTechnique(TECH_ID, ganonUltimate);
    }

    static Technique wall = new Technique(
        "lapare",
        "Las manos arriba, las manos abajo, y te comes el muro",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Throw a player into a wall.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Block target = player.getTargetBlockExact(80);
            if (target == null) return;

            Location targetLoc = target.getLocation().add(0.5, 1.0, 0.5);
            double d = player.getLocation().distance(targetLoc);
            if (d >= 80) return;

            Player closestP = null;
            double minDist = Double.MAX_VALUE;
            for (Player p : player.getWorld().getPlayers()) {
                if (p.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                double distToUser = player.getLocation().distance(p.getLocation());
                if (distToUser < minDist) {
                    minDist = distToUser;
                    closestP = p;
                }
            }
            final Player closest = closestP;
            if (closest == null) return;
            if (minDist >= 40) return;

            Vector toTarget = targetLoc.toVector().subtract(closest.getLocation().toVector()).normalize();
            closest.setVelocity(toTarget.multiply(2.0).add(new Vector(0, 0.6, 0)));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try { closest.teleport(targetLoc); } catch (Exception ignored) {}
                double damage = 400.0 / Math.max(0.001, d);
                try { closest.damage(damage, player); } catch (Exception e) { closest.damage(damage); }
                try { closest.setVelocity(new Vector(0,0,0)); } catch (Exception ignored) {}
            }, 8L);
        }
    );

    static Technique paralyze = new Technique(
        "paralyze",
        "Kieto parao",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("Paralyze nearest player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Player closestP = null;
            double minDist = Double.MAX_VALUE;
            for (Player p : player.getWorld().getPlayers()) {
                if (p.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                double distToUser = player.getLocation().distance(p.getLocation());
                if (distToUser < minDist) {
                    minDist = distToUser;
                    closestP = p;
                }
            }
            final Player closest = closestP;
            if (closest == null) return;
            if (minDist >= 40) return;

            int durationTicks = 20 * 10;
            int slownessAmplifier = 255;
            try {
                PotionEffect slow = new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, slownessAmplifier, false, false);
                closest.addPotionEffect(slow, true);
            } catch (Exception ignored) {}

            try { closest.setVelocity(new Vector(0,0,0)); } catch (Exception ignored) {}

            try {
                PotionEffect strength = new PotionEffect(PotionEffectType.STRENGTH, durationTicks, 1, false, false);
                player.addPotionEffect(strength, true);
            } catch (Exception ignored) {}
        }
    );

    // Agregado: técnica clon migrada y adaptada a TechniqueAPI
    static Technique clon = new Technique(
        "zombie_summon",
        "Magia seika",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Invoca un clon zombi basado en el jugador.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Player closest = roaring_soul.getClosestPlayer(player.getLocation());
            if (closest == null || closest.isDead() || closest.getLocation().distance(player.getLocation()) > 16) {
                closest = player;
            }

            // Obtener salud del jugador
            double health = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();

            // Obtener arma del jugador (preferir netherite sword)
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon == null || !weapon.getType().name().endsWith("_SWORD") || weapon.getType() != Material.NETHERITE_SWORD) {
                weapon = null;
                for (ItemStack is : player.getInventory().getContents()) {
                    if (is != null && is.getType().name().endsWith("_SWORD")) {
                        weapon = is;
                        break;
                    }
                }
            }

            // Obtener armadura
            PlayerInventory inv = player.getInventory();
            ItemStack[] armor = inv.getArmorContents();

            // Spawn 1 zombie (ajustable)
            for (int i = 0; i < 1; i++) {
                Location spawnLoc = player.getLocation().clone();
                double angle = Math.toRadians(0);
                spawnLoc.add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);

                Zombie zombie = (Zombie) player.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.setCustomName("Guerrero de " + player.getName());
                zombie.setCustomNameVisible(true);
                zombie.setPersistent(true);
                zombie.setRemoveWhenFarAway(false);

                // Set health
                try {
                    zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                    zombie.setHealth(Math.min(health, maxHealth));
                } catch (Exception ignored) {}

                // Set weapon and armor
                org.bukkit.entity.Entity zEnt = zombie;
                org.bukkit.inventory.EntityEquipment eq = zombie.getEquipment();
                if (weapon != null && eq != null) eq.setItemInMainHand(weapon.clone());
                if (eq != null) eq.setArmorContents(armor);
                if (eq != null) {
                    eq.setItemInMainHandDropChance(0);
                    eq.setHelmetDropChance(0);
                    eq.setChestplateDropChance(0);
                    eq.setLeggingsDropChance(0);
                    eq.setBootsDropChance(0);
                }

                // Set target
                zombie.setTarget(closest);
                // Forzar targeting por si la IA no lo toma
                Bukkit.getPluginManager().callEvent(new org.bukkit.event.entity.EntityTargetLivingEntityEvent(zombie, closest, org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
            }

            hotbarMessage.sendHotbarMessage(player, "¡Has invocado un clon!");
        }
    );

    // Nueva técnica: envía jugadores a la ubicación de config "ganon_dim.loc" por 5 minutos y aplica Wither II a todos excepto el usuario
    static Technique ganonUltimate = new Technique(
        "ganon_ultimate",
        "Ganon's Banish",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(60), List.of("Send nearby players to configured location and apply Wither.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            final long durationTicks = 20L * 60L * 5L;
            Location target = getGanonLocationFromConfig();
            if (target == null) {
                hotbarMessage.sendHotbarMessage(player, "ganon_dim.loc no está configurado correctamente. Habla con un admin");
                return;
            }
            for (Player p : player.getWorld().getPlayers()) {
                if (p.equals(player)) continue;
                double dist = player.getLocation().distance(p.getLocation());
                if (dist <= 60.0) {
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    final Location original = p.getLocation().clone();
                    try { p.teleport(target); } catch (Exception ignored) {}
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try { if (!p.isDead()) p.teleport(original); } catch (Exception ignored) {}
                    }, durationTicks);
                }
            }

            int witherAmp = 1;
            int witherDuration = (int) durationTicks;
            PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, witherDuration, witherAmp, false, false);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(online.getUniqueId())) continue;
                try { online.addPotionEffect(wither, true); } catch (Exception ignored) {}
            }

            hotbarMessage.sendHotbarMessage(player, "¡Ganon ha enviado a los cercanos al abismo!");
        }
    );

    // Helper para parsear "ganon_dim.loc" desde config. Acepta:
    // - una sección con keys world, x, y, z, (opcional) yaw, pitch
    // - o una string CSV "world,x,y,z[,yaw,pitch]"
    private static Location getGanonLocationFromConfig() {
        try {
            if (plugin.getConfig().isConfigurationSection("ganon_dim.loc")) {
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("ganon_dim.loc");
                String worldName = sec.getString("world");
                double x = sec.getDouble("x");
                double y = sec.getDouble("y");
                double z = sec.getDouble("z");
                float yaw = (float) sec.getDouble("yaw", 0.0);
                float pitch = (float) sec.getDouble("pitch", 0.0);
                World w = Bukkit.getWorld(worldName);
                if (w == null) return null;
                return new Location(w, x, y, z, yaw, pitch);
            }

            String raw = plugin.getConfig().getString("ganon_dim.loc", "").trim();
            if (raw.isEmpty()) return null;
            String[] parts = raw.split("[,\\s]+");
            if (parts.length < 4) return null;
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = 0f, pitch = 0f;
            if (parts.length >= 6) {
                yaw = Float.parseFloat(parts[4]);
                pitch = Float.parseFloat(parts[5]);
            }
            World w = Bukkit.getWorld(worldName);
            if (w == null) return null;
            return new Location(w, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
}
