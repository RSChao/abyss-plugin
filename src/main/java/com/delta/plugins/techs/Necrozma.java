package com.delta.plugins.techs;

import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Necrozma {
    static final String TECH_KEY = "god_of_light";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);

    public static void registerTechs() {
        // Registration logic for Necrozma techs would go here
        TechRegistry.registerTechnique(TECH_KEY, lightPull);
        // Registrar nueva técnica: Flash Blind
        TechRegistry.registerTechnique(TECH_KEY, flashBlind);
        // Registrar nueva técnica: Blink Look (teleporta al bloque que mira, max 50)
        TechRegistry.registerTechnique(TECH_KEY, blinkLook);
        // Registrar Ultimate: Light that burns the Sky
        TechRegistry.registerTechnique(TECH_KEY, lightThatBurns);
    }

    // Técnica: Light Pull
    static Technique lightPull = new Technique(
        "light_pull",
        "Light Pull",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(20), List.of("Pull targets into orbit and damage.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (!player.isOnline()) return;
            World world = player.getWorld();
            Location centerBase = player.getLocation();
            Collection<Entity> nearby = world.getNearbyEntities(centerBase, 15, 15, 15);

            // Determinar si hay otros jugadores en el radio (si los hay, ignorar mobs)
            boolean hasOtherPlayers = false;
            for (Entity e : nearby) {
                if (e instanceof Player && !e.equals(player)) { hasOtherPlayers = true; break; }
            }

            // Recolectar objetivos (LivingEntity) según la regla: si hay players -> solo players (excluyendo caster),
            // si no hay players -> todos los LivingEntity excepto el caster.
            List<LivingEntity> targets = new ArrayList<>();
            for (Entity e : nearby) {
                if (e == null) continue;
                if (e.equals(player)) continue; // siempre ignorar al usuario
                if (!(e instanceof LivingEntity)) continue;
                if (hasOtherPlayers) {
                    if (e instanceof Player) targets.add((LivingEntity)e);
                } else {
                    targets.add((LivingEntity)e);
                }
            }

            if (targets.isEmpty()) {
                hotbarMessage.sendHotbarMessage(player, "No hay objetivos en 15 bloques.");
                return;
            }

            // Preparar ángulos base para cada objetivo
            Random rnd = new Random();
            int n = targets.size();
            double[] baseAngles = new double[n];
            for (int i = 0; i < n; i++) baseAngles[i] = rnd.nextDouble() * Math.PI * 2;

            final double orbitRadius = 10.0;
            final double heightOffset = 3.0;
            final double spinSpeed = Math.PI / 30.0; // ajuste de velocidad angular
            final int totalTicks = 60; // 3 segundos

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!player.isOnline()) { cancel(); return; }
                    Location center = player.getLocation().clone().add(0, heightOffset, 0);
                    World w = center.getWorld();

                    // Para cada target: teletransportar a la posición circular y generar la línea de partículas
                    for (int i = 0; i < targets.size(); i++) {
                        LivingEntity t = targets.get(i);
                        if (t == null || !t.isValid()) continue;
                        if(t instanceof Player p) {
                            if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        }

                        double angle = baseAngles[i] + spinSpeed * tick;
                        double tx = center.getX() + Math.cos(angle) * orbitRadius;
                        double tz = center.getZ() + Math.sin(angle) * orbitRadius;
                        double ty = center.getY();
                        Location tp = new Location(w, tx, ty, tz);
                        try { t.teleport(tp); } catch (Throwable ignored) {}

                        // Dibujar línea de partículas desde los ojos del jugador hasta la entidad (segmentos cada 0.5)
                        Location from = player.getEyeLocation();
                        Location to = tp.clone();
                        double dist = from.distance(to);
                        if (dist > 0.1) {
                            Vector dir = to.toVector().subtract(from.toVector()).normalize();
                            for (double d = 0; d <= dist; d += 0.5) {
                                Location point = from.clone().add(dir.clone().multiply(d));
                                try { w.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0); } catch (Throwable ignored) {}
                            }
                        }
                    }

                    tick++;
                    if (tick > totalTicks) {
                        // Fin del periodo: aplicar 100 de daño base a cada objetivo y finalizar
                        for (LivingEntity t : targets) {
                            if (t == null || !t.isValid()) continue;
                            try { t.damage(100.0, player); } catch (Throwable ignored) {}
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    );

    // Técnica: Flash Blind
    static Technique flashBlind = new Technique(
        "flash_blind",
        "Flash Blind",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(20), List.of("Blind players by emitting particles.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (!player.isOnline()) return;
            World world = player.getWorld();
            Location center = player.getLocation();

            // Buscar jugadores en 40 bloques, ignorando al caster
            Collection<Entity> nearby = world.getNearbyEntities(center, 40, 40, 40);
            List<Player> targets = new ArrayList<>();
            for (Entity e : nearby) {
                if (e instanceof Player) {
                    Player p = (Player) e;
                    if (p.equals(player)) continue; // ignorar al usuario
                    targets.add(p);
                }
            }

            if (targets.isEmpty()) {
                hotbarMessage.sendHotbarMessage(player, "No hay jugadores en 40 bloques.");
                return;
            }

            // Duración en ticks (10 segundos)
            int durationTicks = 20 * 10;

            // Para cada objetivo: generar partículas en frente suyo y aplicar ceguera
            for (Player tp : targets) {
                if (tp == null || !tp.isOnline()) continue;
                if(PlayerTechniqueManager.isInmune(tp.getUniqueId())) continue;
                Location eye = tp.getEyeLocation().clone();
                Vector dir = eye.getDirection().normalize();
                World w = eye.getWorld();

                // Generar una "explosión" de partículas blancas en una pequeña línea delante del jugador
                for (double d = 0.5; d <= 2.0; d += 0.25) {
                    Location point = eye.clone().add(dir.clone().multiply(d));
                    try {
                        w.spawnParticle(Particle.END_ROD, point, 8, 0.2, 0.2, 0.2, 0.01);
                    } catch (Throwable ignored) {}
                }

                // Aplicar ceguera
                try {
                    tp.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 1, false, false, false));
                } catch (Throwable ignored) {}
            }

            // Dar invisibilidad al caster por la misma duración
            try {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 1, false, false, false));
            } catch (Throwable ignored) {}
        }
    );

    // Nueva helper pública: aplicar Flash Blind centrado en una entidad (mobs)
    public static void flashBlindAtEntity(LivingEntity user) {
        if (user == null || user.isDead()) return;
        World world = user.getWorld();
        Location center = user.getLocation();

        Collection<Entity> nearby = world.getNearbyEntities(center, 40, 40, 40);
        List<Player> targets = new ArrayList<>();
        for (Entity e : nearby) {
            if (e instanceof Player) {
                Player p = (Player) e;
                if (p.equals(user)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                Integer floor = p.getPersistentDataContainer().get(new org.bukkit.NamespacedKey("tower", "floor"), org.bukkit.persistence.PersistentDataType.INTEGER);
                if (floor == null || floor <= 0) continue;
                targets.add(p);
            }
        }

        if (targets.isEmpty()) return;

        int durationTicks = 20 * 10; // 10s
        for (Player tp : targets) {
            if (tp == null || !tp.isOnline()) continue;
            Location eye = tp.getEyeLocation().clone();
            Vector dir = eye.getDirection().normalize();
            World w = eye.getWorld();
            for (double d = 0.5; d <= 2.0; d += 0.25) {
                Location point = eye.clone().add(dir.clone().multiply(d));
                try { w.spawnParticle(Particle.END_ROD, point, 8, 0.2, 0.2, 0.2, 0.01); } catch (Throwable ignored) {}
            }
            try { tp.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 1, false, false, false)); } catch (Throwable ignored) {}
        }
    }

    // Técnica: Blink — teleporta al bloque que mira (máx 50)
    static Technique blinkLook = new Technique(
        "blink",
        "Blink",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(15), List.of("Teleport to looked block.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (!player.isOnline()) return;
            // Intentar obtener bloque objetivo (máx 50) — usar getTargetBlockExact y fallback a rayTrace
            Block target = null;
            try {
                target = player.getTargetBlockExact(50);
            } catch (Throwable ignored) {}
            if (target == null) {
                try {
                    RayTraceResult res = player.rayTraceBlocks(50);
                    if (res != null) target = res.getHitBlock();
                } catch (Throwable ignored) {}
            }

            if (target == null) {
                hotbarMessage.sendHotbarMessage(player, "No hay bloque visible dentro de 50 bloques.");
                return;
            }

            // Buscar posición segura encima del bloque (hasta 3 bloques hacia arriba)
            Location dest = target.getLocation().add(0.5, 1.0, 0.5);
            boolean found = false;
            for (int y = 1; y <= 3; y++) {
                Block check = target.getRelative(0, y, 0);
                if (check == null) continue;
                if (check.isPassable()) {
                    dest = check.getLocation().add(0.5, 0.0, 0.5);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // si no se encontró espacio pasable encima, usar la posición por defecto (un bloque encima)
                dest = target.getLocation().add(0.5, 1.0, 0.5);
            }

            // Verificar seguridad final (no dentro de bloque sólido)
            if (!dest.getBlock().isPassable()) {
                hotbarMessage.sendHotbarMessage(player, "Destino inseguro para teletransporte.");
                return;
            }

            try {
                player.teleport(dest);
            } catch (Throwable ex) {
                hotbarMessage.sendHotbarMessage(player, "Error al teletransportarse.");
            }
        }
    );

    // Técnica Ultimate: Light that burns the Sky
    static Technique lightThatBurns = new Technique(
        "light_burns_sky",
        "Light that burns the Sky",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(10), List.of("Massive multi-pulse ultimate.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (player == null || !player.isOnline()) return;
            World world = player.getWorld();

            // 1) Forzar noche
            try { world.setTime(13000L); } catch (Throwable ignored) {}

            // 2) Explosión visual inicial: MUCHAS partículas END_ROD en un círculo radio=1 en los ojos del jugador
            Location eye = player.getEyeLocation().clone();
            final int points = 360;
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                Location spawn = eye.clone().add(Math.cos(angle) * 1.0, 0.0, Math.sin(angle) * 1.0);
                try { world.spawnParticle(Particle.END_ROD, spawn, 8, 0.15, 0.15, 0.15, 0.01); } catch (Throwable ignored) {}
            }

            // Parámetros generales de los pulsos
            final int pulseCount = 2; // dos pulsos que dañan
            final long pulseInterval = 30L; // 1 segundo entre pulsos
            final double pulseRange = 30.0;
            final double pulseDamage = 40.0; // daño doble

            // Función que realiza un pulso: genera partículas y daña jugadores cercanos
            Runnable doPulse = () -> {
                if (!player.isOnline()) return;
                World w = player.getWorld();
                Location center = player.getLocation().clone().add(0, 1.0, 0);
                // Partículas en anillo creciente (visuales)
                for (double r = 1.0; r <= 8.0; r += 1.0) {
                    int ringPoints = Math.max(12, (int)(2 * Math.PI * r * 4));
                    for (int i = 0; i < ringPoints; i++) {
                        double a = 2 * Math.PI * i / ringPoints;
                        Location p = center.clone().add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
                        try { w.spawnParticle(Particle.END_ROD, p, 2, 0, 0, 0, 0); } catch (Throwable ignored) {}
                    }
                }

                // Daño a jugadores cercanos (excluye caster)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p == null || !p.isOnline()) continue;
                    if (p.equals(player)) continue;
                    if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    if (!p.getWorld().equals(player.getWorld())) continue;
                    if (p.getLocation().distance(player.getLocation()) <= pulseRange) {
                        try { p.damage(pulseDamage, player); } catch (Throwable ignored) {}
                    }
                }
            };

            // Programar los dos pulsos: a 1s y 2s
            Bukkit.getScheduler().runTaskLater(plugin, doPulse, pulseInterval);
            Bukkit.getScheduler().runTaskLater(plugin, doPulse, pulseInterval * 2);

            // En el "tercer pulso" (a 3s) crear explosión grande en la ubicación del usuario que NO rompe bloques
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                Location loc = player.getLocation().clone();
                World w = loc.getWorld();
                // Visual: un anillo masivo de partículas blancas
                for (double r = 1.0; r <= 50.0; r += 1.0) {
                    int ringPoints = Math.max(12, (int)(2 * Math.PI * r / 0.5));
                    for (int i = 0; i < ringPoints; i++) {
                        double a = 2 * Math.PI * i / ringPoints;
                        Location p = loc.clone().add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
                        try { w.spawnParticle(Particle.END_ROD, p, 1, 0, 0, 0, 0); } catch (Throwable ignored) {}
                    }
                }
                // Explosión potente, pero que no rompe bloques ni prende fuego
                try {
                    for(Player p: Bukkit.getOnlinePlayers()){
                        if(p == null || !p.isOnline()) continue;
                        if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        if(!p.getWorld().equals(player.getWorld())) continue;
                        if(p.getLocation().distance(loc) <= 100.0){
                            try { p.damage(1000.0, player); } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }, pulseInterval * 3);

        }
    );
}
