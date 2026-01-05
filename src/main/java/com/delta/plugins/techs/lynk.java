package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager; // added

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

public class lynk {

    static final String TECH_ID = "hyrule_hero";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, wall);
        TechRegistry.registerTechnique(TECH_ID, paralyze);
        TechRegistry.registerTechnique(TECH_ID, clon);
        TechRegistry.registerTechnique(TECH_ID, ganonUltimate);
    }

    static Technique wall = new Technique("lapare", "Las manos arriba, las manos abajo, y te comes el muro", false, cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {
        // Obtener el bloque objetivo hasta 80 bloques
        Block target = player.getTargetBlockExact(80);
        if (target == null) return;

        Location targetLoc = target.getLocation().add(0.5, 1.0, 0.5); // punto sobre el bloque
        double d = player.getLocation().distance(targetLoc);
        if (d >= 80) return; // requiere menor que 80

        // Buscar el jugador más cercano en el mismo mundo (excluyendo al usuario)
        Player closestP = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;
            // Excluir jugadores inmunes
            if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
            double distToUser = player.getLocation().distance(p.getLocation());
            if (distToUser < minDist) {
                minDist = distToUser;
                closestP = p;
            }
        }
        final Player closest = closestP;
        if (closest == null) return;
        if (minDist >= 40) return; // requiere menor que 40

        // Empujar al jugador hacia el bloque: primer impulso para moverlo, luego teletransportarlo y aplicar daño
        Vector toTarget = targetLoc.toVector().subtract(closest.getLocation().toVector()).normalize();
        // Impulso inicial (lleva al jugador hacia el bloque)
        closest.setVelocity(toTarget.multiply(2.0).add(new Vector(0, 0.6, 0)));

        // Esperar un pequeño tiempo para simular impacto y luego fijar la posición y aplicar daño
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Teletransportar al jugador justo contra el bloque y bloquear su movimiento momentáneamente
            try {
                closest.teleport(targetLoc);
            } catch (Exception ignored) {}

            // Calcular daño: 400 / d
            double damage = 400.0 / Math.max(0.001, d);
            // Aplicar daño atribuido al usuario (si la API soporta atacante)
            try {
                closest.damage(damage, player);
            } catch (Exception e) {
                // fallback sin atacante
                closest.damage(damage);
            }

            // Opcional: detener velocidad residual
            closest.setVelocity(new Vector(0, 0, 0));
        }, 8L); // ~0.4s (8 ticks) de espera para el "smash"
    });


    static Technique paralyze = new Technique("paralyze", "Kieto parao", false, cooldownHelper.minutesToMiliseconds(20), (player, item, args) -> {
        // Buscar el jugador más cercano en el mismo mundo (excluyendo al usuario)
        Player closestP = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;
            // Excluir jugadores inmunes
            if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
            double distToUser = player.getLocation().distance(p.getLocation());
            if (distToUser < minDist) {
                minDist = distToUser;
                closestP = p;
            }
        }
        final Player closest = closestP;
        if (closest == null) return;
        if (minDist >= 40) return; // requiere menor que 40

        // Aplicar parálisis: Slowness nivel 255 por 10 segundos (200 ticks)
        int durationTicks = 20 * 10;
        int slownessAmplifier = 255; // intensidad solicitada
        try {
            PotionEffect slow = new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, slownessAmplifier, false, false);
            closest.addPotionEffect(slow, true);
        } catch (Exception ignored) {}

        // Asegurar que el objetivo no se mueva inmediatamente
        try { closest.setVelocity(new Vector(0,0,0)); } catch (Exception ignored) {}

        // Dar fuerza al usuario por el mismo tiempo (ej. Strength I)
        try {
            PotionEffect strength = new PotionEffect(PotionEffectType.STRENGTH, durationTicks, 1, false, false);
            player.addPotionEffect(strength, true);
        } catch (Exception ignored) {}
    });

    // Nueva técnica: envía jugadores a la ubicación de config "ganon_dim.loc" por 5 minutos y aplica Wither II a todos excepto el usuario
    static Technique ganonUltimate = new Technique("ganon_ultimate", "Ganon's Banish", true, cooldownHelper.minutesToMiliseconds(60), (player, item, args) -> {
        // Duración en ticks (5 minutos)
        final long durationTicks = 20L * 60L * 5L; // 6000L

        // Obtener la ubicación objetivo desde la config
        Location target = getGanonLocationFromConfig();
        if (target == null) {
            hotbarMessage.sendHotbarMessage(player, "ganon_dim.loc no está configurado correctamente. Habla con un admin");
            return;
        }

        // Teletransportar todos los jugadores en el mismo mundo y dentro de 60 bloques
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;
            double dist = player.getLocation().distance(p.getLocation());
            if (dist <= 60.0) {
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                final Location original = p.getLocation().clone();
                // Teleportar al destino
                try { p.teleport(target); } catch (Exception ignored) {}
                // Programar retorno después de 5 minutos
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        if (!p.isDead()) p.teleport(original);
                    } catch (Exception ignored) {}
                }, durationTicks);
            }
        }

        // Aplicar Wither II a todos los jugadores en línea excepto el usuario por 5 minutos
        int witherAmp = 1; // amplifier 1 => Wither II
        int witherDuration = (int) durationTicks;
        PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, witherDuration, witherAmp, false, false);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            // Excluir jugadores inmunes
            if (PlayerTechniqueManager.isInmune(online.getUniqueId())) continue;
            try { online.addPotionEffect(wither, true); } catch (Exception ignored) {}
        }

        hotbarMessage.sendHotbarMessage(player, "¡Ganon ha enviado a los cercanos al abismo!");
    });

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

    static Technique clon = new Technique("zombie_summon", "Magia seika", false, cooldownHelper.minutesToMiliseconds(10), (player, item, args) -> {
        Player closest = roaring_soul.getClosestPlayer(player.getLocation());
        if(closest == null || closest.isDead() || closest.getLocation().distance(player.getLocation()) > 16){
            closest = player;
        }


        // Get player's health
        double health = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();

        // Get player's weapon (prefer netherite sword, else any sword in inventory)
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType() != Material.NETHERITE_SWORD) {
            weapon = null;
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.getType().name().endsWith("_SWORD")) {
                    weapon = is;
                    break;
                }
            }
        }

        // Get player's armor
        PlayerInventory inv = player.getInventory();
        ItemStack[] armor = inv.getArmorContents();

        // Spawn 2 zombies, 2 blocks away from player
        for (int i = 0; i < 1; i++) {
            Location spawnLoc = player.getLocation().clone();
            double angle = Math.toRadians(0); // 0 and 180 degrees
            spawnLoc.add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);

            Zombie zombie = (Zombie) player.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.setCustomName("Guerrero de " + player.getName());
            zombie.setCustomNameVisible(true);
            zombie.setPersistent(true);
            zombie.setRemoveWhenFarAway(false);

            // Set health
            zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
            zombie.setHealth(Math.min(health, maxHealth));

            // Set weapon and armor
            EntityEquipment eq = zombie.getEquipment();
            if (weapon != null) eq.setItemInMainHand(weapon.clone());
            eq.setArmorContents(armor);
            eq.setItemInMainHandDropChance(0);
            eq.setHelmetDropChance(0);
            eq.setChestplateDropChance(0);
            eq.setLeggingsDropChance(0);
            eq.setBootsDropChance(0);

            // Set target
            zombie.setTarget(closest);
            // Force targeting in case AI doesn't pick up
            Bukkit.getPluginManager().callEvent(new org.bukkit.event.entity.EntityTargetLivingEntityEvent(zombie, closest, EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
        }

        hotbarMessage.sendHotbarMessage(player, "¡Has invocado un clon!");
    });
}
