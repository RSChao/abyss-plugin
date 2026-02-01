package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.items.Items;
// Reemplazadas las antiguas importaciones de techapi por techniqueAPI:
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class chaosWielder {
    static final String id = "chaos_wielder";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register(){
        Plugin.registerAbyssID(id);
        // Registrar la técnica nueva
        TechRegistry.registerTechnique(id, chaosBuff);
        TechRegistry.registerTechnique(id, chaosHeartbeat);
        TechRegistry.registerTechnique(id, resetCooldown);
        TechRegistry.registerTechnique(id, danceOfHearts);
    }

    static Technique chaosBuff = new Technique(
        "chaobuff",
        "Chaos Buff",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Grant chaos-group buffs and strip resistance of nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(90*20, 1));
            player.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(90*20, 1));

            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                if(p.getWorld().equals(player.getWorld())){
                    if(p.getLocation().distance(player.getLocation())<=10){
                        //remove resistance if they have it
                        if(p.hasPotionEffect(PotionEffectType.RESISTANCE)){
                            p.removePotionEffect(PotionEffectType.RESISTANCE);
                        }
                    }
                }
            }
            hotbarMessage.sendHotbarMessage(player, "§c§l¡Has desatado el poder del caos!§r");
        }
    );

    // Nueva técnica: Chaos Heartbeat
    static Technique chaosHeartbeat = new Technique(
        "chaos_heartbeat",
        "Chaos Heartbeat",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Spawn three pulse waves around you.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            // Programar 3 oleadas a 1s, 2s y 3s (20, 40, 60 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnWave(player), 20L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnWave(player), 40L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnWave(player), 60L);
            hotbarMessage.sendHotbarMessage(player, "§c§l¡Has desatado el latido del caos!§r");
        }
    );

    static Technique weak = new Technique(
        "chaos_weak",
        "Chaos Weaknening",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Apply heavy weakness & immobilize nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            // Aplicar debilidad 2 por 10 segundos
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                if(p.getWorld().equals(player.getWorld())){
                    if(p.getLocation().distance(player.getLocation())<=10){
                        p.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(600, 1));
                        double jumpstrength = 0.41999998697815;
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 255));
                        p.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(jumpstrength);
                        }, 5 * 20);
                    }
                }
            }
            hotbarMessage.sendHotbarMessage(player, "§c§l¡Has debilitado a los jugadores cercanos!§r");
        }
    );

    // Nueva técnica ultimate: Dance of Hearts
    static Technique danceOfHearts = new Technique(
        "dance_of_hearts",
        "Dance of the Hearts of Good and Evil",
        new TechniqueMeta(true, cooldownHelper.hour, List.of("Ultimate ritual that gathers and obliterates nearby players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (player == null || !player.isOnline()) return;

            // Duración aproximada de la técnica en ticks (usada para efectos)
            final int totalTicks = 20 * 8; // aproximado (8s) — suficiente para la secuencia programada

            // 1) Hacer invulnerable (Resistencia 255) durante toda la técnica
            player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(totalTicks, 255));
            hotbarMessage.sendHotbarMessage(player, "§d§lDance of Hearts iniciado...§r");

            // 2) Dos flashes de partículas en los ojos separados 5 ticks
            Location eye = player.getEyeLocation().clone();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.FLASH, eye, 10, 0.2, 0.2, 0.2, 0, Color.WHITE);
            }, 0L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.getWorld().spawnParticle(Particle.FLASH, eye, 12, 0.2, 0.2, 0.2, 0, Color.WHITE);
            }, 5L);

            // 3) Inmovilizar jugadores y recopilarlos en targets
            final List<UUID> targets = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOnline()) continue;
                if (!p.getWorld().equals(player.getWorld())) continue;
                if(p.equals(player)) continue;
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
                if (p.getLocation().distance(player.getLocation()) <= 30.0) {
                    // Inmovilizar: aplicar Slowness extremo y ceguera leve para anti-movimiento
                    p.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(totalTicks, 255));
                    p.addPotionEffect(PotionEffectType.JUMP_BOOST.createEffect(totalTicks, 128)); // evita saltos
                    targets.add(p.getUniqueId());
                }
            }

            // 4) 10 ticks después: spawnear items (2 círculos) y Chaos Heart (3 blocks up)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location base = player.getLocation().clone().add(0, 2.0, 0);
                // Preparar listas de ItemStacks (inner = pureheart_*, outer = pure_heart_*)
                ItemStack[] inner = new ItemStack[]{
                    com.delta.plugins.items.Items.pureheart_red,
                    com.delta.plugins.items.Items.pureheart_orange,
                    com.delta.plugins.items.Items.pureheart_yellow,
                    com.delta.plugins.items.Items.pureheart_green,
                    com.delta.plugins.items.Items.pureheart_blue,
                    com.delta.plugins.items.Items.pureheart_indigo,
                    com.delta.plugins.items.Items.pureheart_purple,
                    com.delta.plugins.items.Items.pureheart_white
                };
                ItemStack[] outer = new ItemStack[]{
                    com.delta.plugins.items.Items.pure_heart_red,
                    com.delta.plugins.items.Items.pure_heart_brown,
                    com.delta.plugins.items.Items.pure_heart_blue,
                    com.delta.plugins.items.Items.pure_heart_cyan,
                    com.delta.plugins.items.Items.pure_heart_purple,
                    com.delta.plugins.items.Items.pure_heart_pink,
                    com.delta.plugins.items.Items.pure_heart_yellow,
                    com.delta.plugins.items.Items.pure_heart_grey
                };
                // Chaos heart item
                ItemStack chaosHeartStack = Items.ChaosHeart;

                final List<Item> spawnedItems = new ArrayList<>();

                // parámetros de círculos
                double innerRadius = 1.5;
                double outerRadius = 3.0;
                int countInner = inner.length;
                int countOuter = outer.length;

                // Spawn inner
                for (int i = 0; i < countInner; i++) {
                    double angle = 2 * Math.PI * i / countInner;
                    Location loc = base.clone().add(Math.cos(angle) * innerRadius, 0, Math.sin(angle) * innerRadius);
                    Item it = base.getWorld().dropItem(loc, inner[i].clone());
                    it.setGravity(false);
                    it.setPickupDelay(Integer.MAX_VALUE);
                    it.setInvulnerable(true);
                    it.setVelocity(new Vector(0, 0, 0));
                    spawnedItems.add(it);
                }
                // Spawn outer
                for (int i = 0; i < countOuter; i++) {
                    double angle = 2 * Math.PI * i / countOuter;
                    Location loc = base.clone().add(Math.cos(angle) * outerRadius, 0, Math.sin(angle) * outerRadius);
                    Item it = base.getWorld().dropItem(loc, outer[i].clone());
                    it.setGravity(false);
                    it.setVelocity(new Vector(0, 0, 0));
                    it.setPickupDelay(Integer.MAX_VALUE);
                    it.setInvulnerable(true);
                    spawnedItems.add(it);
                }
                // Spawn chaos heart 3 blocks above player
                Location chaosLoc = player.getLocation().clone().add(0, 3.0, 0);
                Item chaosItem = chaosLoc.getWorld().dropItem(chaosLoc, chaosHeartStack.clone());
                chaosItem.setGravity(false);
                chaosItem.setVelocity(new Vector(0, 0, 0));
                chaosItem.setPickupDelay(Integer.MAX_VALUE);
                chaosItem.setInvulnerable(true);
                spawnedItems.add(chaosItem);

                // 5) 3 segundos después: convertir items en partículas y eliminarlos (animación)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Guardar particle state holders (positions + color)
                    class HeartParticle {
                        Location pos;
                        DustOptions dust;
                        boolean isChaos;
                        double angleOffset;
                        HeartParticle(Location pos, DustOptions dust, boolean isChaos, double angleOffset){
                            this.pos = pos;
                            this.dust = dust;
                            this.isChaos = isChaos;
                            this.angleOffset = angleOffset;
                        }
                    }
                    List<HeartParticle> particles = new ArrayList<>();
                    // Map each spawned item to a color dust and initial pos
                    for (Item it : spawnedItems) {
                        Location l = it.getLocation().clone();
                        // detect chaos by comparing to chaosItem location (y difference)
                        boolean isChaos = Math.abs(l.getY() - chaosLoc.getY()) < 0.1;
                        DustOptions dust;
                        if (isChaos) {
                            dust = new DustOptions(Color.fromRGB(0,0,0), 1.2f);
                        } else {
                            // choose color by item's material/name heuristic (fall back white)
                            ItemStack st = it.getItemStack();
                            // heuristics based on item model names unavailable here; use display name colors if present
                            String name = (!st.getItemMeta().getPersistentDataContainer().isEmpty()) ? st.getItemMeta().getPersistentDataContainer().getKeys().iterator().next().getKey() : "";
                            if (name.contains("rojo") || name.contains("red")) dust = new DustOptions(Color.fromRGB(220,20,60), 1.0f);
                            else if (name.contains("amarillo") || name.contains("yellow")) dust = new DustOptions(Color.fromRGB(255,215,0), 1.0f);
                            else if (name.contains("azul") || name.contains("blue")) dust = new DustOptions(Color.fromRGB(65,105,225), 1.0f);
                            else if (name.contains("verde") || name.contains("green") || name.contains("esmeralda")) dust = new DustOptions(Color.fromRGB(50,205,50), 1.0f);
                            else if (name.contains("rosa") || name.contains("pink")) dust = new DustOptions(Color.fromRGB(255,105,180), 1.0f);
                            else if (name.contains("morado") || name.contains("purple") || name.contains("amatista")) dust = new DustOptions(Color.fromRGB(148,0,211), 1.0f);
                            else if (name.contains("gris") || name.contains("grey") || name.contains("onyx")) dust = new DustOptions(Color.fromRGB(105,105,105), 1.0f);
                            else if (name.contains("marrón") || name.contains("brown")) dust = new DustOptions(Color.fromRGB(139,69,19), 1.0f);
                            else dust = new DustOptions(Color.fromRGB(255,255,255), 1.0f);
                        }
                        double offset = Math.atan2(l.getZ() - base.getZ(), l.getX() - base.getX());
                        particles.add(new HeartParticle(l, dust, isChaos, offset));
                        // remove item entity immediately (replaced by particles)
                        it.remove();
                    }

                    // Animate particles: por 1 segundo (20 ticks) subir y girar; chaos only upwards
                    // animTicks aumentado: original 20 ticks (1s) + 5s adicionales = 120 ticks (6s)
                    final int animTicks = 120;
                    final int[] tickCounter = {0};
                    int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        for (HeartParticle hp : particles) {
                            double t = tickCounter[0] / (double) animTicks;
                            double radius = 0.0;
                            if (!hp.isChaos) {
                                radius = (hp.pos.distance(base) > 0.1) ? hp.pos.distance(base) : 1.0;
                            }
                            double angle = hp.angleOffset + t * Math.PI * 2; // full rotation
                            double y = hp.pos.getY() + tickCounter[0] * 0.05; // chaos rises faster
                            Location spawn = base.clone().add(Math.cos(angle) * radius, y - base.getY(), Math.sin(angle) * radius);
                            spawn.getWorld().spawnParticle(Particle.DUST, spawn, 2, 0, 0, 0, 0, hp.dust);
                        }
                        tickCounter[0]++;
                        if (tickCounter[0] > animTicks) {
                            // cancelar tarea
                            // Esta tarea será cancelada por el scheduler run after creation
                        }
                    }, 0L, 1L).getTaskId();

                    // Cancelar la animación tras animTicks
                    Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getScheduler().cancelTask(taskId), animTicks + 1L);

                    // 1s después de la animación: juntar partículas en un punto y ejecutar teleporte/daño
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Location center = base.clone().add(0, 2.0, 0); // centro final de partículas
                        // spawn final concentrated particle visuals
                        for (HeartParticle hp : particles) {
                            center.getWorld().spawnParticle(Particle.DUST, center.clone().add(0, 0.5, 0), 8, 0.3, 0.3, 0.3, 0, hp.dust);
                        }

                        // Teleportar cada objetivo formando un círculo (3 blocks radius) y aplicar daño masivo
                        List<Player> actualTargets = new ArrayList<>();
                        for (UUID u : targets) {
                            Player tp = Bukkit.getPlayer(u);
                            if (tp != null && tp.isOnline() && tp.getWorld().equals(player.getWorld())) actualTargets.add(tp);
                        }
                        int n = actualTargets.size();
                        for (int i = 0; i < n; i++) {
                            Player tp = actualTargets.get(i);
                            double angle = 2 * Math.PI * i / Math.max(1, n);
                            Location dest = center.clone().add(Math.cos(angle) * 3.0, 0, Math.sin(angle) * 3.0);
                            dest.setYaw((float) Math.toDegrees(angle));
                            tp.teleport(dest);
                            // daño inmenso
                            tp.damage(10000.0);
                        }
                        // Programar segundo daño 20 ticks después
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            for (Player tp : actualTargets) {
                                if (tp != null && tp.isOnline()) tp.damage(10000.0);
                            }
                        }, 20L);

                        // Liberar jugadores y empujarlos fuera (darles un empuje radial)
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            for (Player tp : actualTargets) {
                                if (tp == null || !tp.isOnline()) continue;
                                tp.removePotionEffect(PotionEffectType.SLOWNESS);
                                tp.removePotionEffect(PotionEffectType.JUMP_BOOST);
                                // push away from center
                                Vector dir = tp.getLocation().toVector().subtract(center.toVector()).normalize();
                                if (Double.isNaN(dir.getX()) || Double.isNaN(dir.getY()) || Double.isNaN(dir.getZ())) dir = new Vector(0, 0.5, 0);
                                tp.setVelocity(dir.multiply(1.5).setY(0.5));
                            }
                            // limpiar partículas finales
                            // (No persistent particle entities fueron creadas; la limpieza es conceptual)
                            hotbarMessage.sendHotbarMessage(player, "§d§lDance of Hearts finalizado.§r");
                        }, 40L); // small delay para asegurar 2º daño aplicado
                    }, animTicks + 1L); // after the 1s animation
                }, 60L); // 3 segundos después de que los items aparecieron (60 ticks)
            }, 10L); // 10 ticks después de la inmovilización
        }
    );

    static Technique resetCooldown = new Technique(
        "reset_cooldown_chaos",
        "Chaos Reboot",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(50), List.of("Reset many abyss & fruit cooldowns.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            List<String> excludedTechs = List.of("ultimate_cataclysm", "reset_cooldown_chaos", "reset_cooldown_whacka", "reset_cooldown", "oblivion_atomization", "reset_cooldown_delta");
            for(String id: TechRegistry.getRegisteredFruitIds()){
                for(Technique t: TechRegistry.getAllTechniques(id)){
                    if(!excludedTechs.contains(t.getId())){
                        CooldownManager.removeCooldown(player, t.getId());
                    }
                }
            }
            hotbarMessage.sendHotbarMessage(player, "¡Has reiniciado tus cooldowns!");
        }
    );

    // Función auxiliar: comprueba jugadores a <=30 bloques y crea el círculo expandible de partículas negras
    private static void spawnWave(Player user){
        if (user == null || !user.isOnline()) return;

        // Aplicar daño/substracción de vida a jugadores cercanos (puede ocurrir una vez por oleada)
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOnline()) continue;
            if (!p.getWorld().equals(user.getWorld())) continue;
            if (p.equals(user)) continue;
            if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
            if (p.getLocation().distance(user.getLocation()) <= 30.0) {
                double newHealth = p.getHealth() - 20.0;
                if (newHealth <= 0) {
                    // aplicar daño masivo para forzar muerte si corresponde
                    p.damage(300.0, user);
                } else {
                    // setHealth puede lanzar excepción si fuera de rango; usamos try por seguridad
                    try {
                        p.setHealth(newHealth);
                        p.damage(1, user);
                    } catch (Exception ignore) { /* si falla, intentar infligir daño equivalente */
                        p.damage(20.0, user);
                    }
                }
            }
        }

        // Crear expansión de partículas: crecer de 0 a 30 bloques en ~10 ticks
        final int steps = 10;
        final double maxRadius = 30.0;
        final Location center = user.getLocation().clone().add(0, 1.0, 0); // elevar un poco para visibilidad
        final DustOptions dust = new DustOptions(Color.fromRGB(0,0,0), 1.0f);

        for (int step = 1; step <= steps; step++) {
            final int s = step;
            final double radius = (maxRadius / steps) * s;
            // programar cada paso en ticks relativos (1 tick entre pasos -> total ~10 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (user == null || !user.isOnline()) return;
                Location c = center.clone();
                // generar puntos en el perímetro; resolución depende del radio (más radio -> más puntos)
                double circumference = 2 * Math.PI * Math.max(radius, 1.0);
                double spacing = 0.5; // separación aproximada entre partículas
                int points = Math.max(8, (int)(circumference / spacing));
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI) * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location spawnLoc = c.clone().add(x, 0, z);
                    spawnLoc.getWorld().spawnParticle(Particle.DUST, spawnLoc, 1, 0, 0, 0, 0, dust);
                }
            }, (long) s); // s ticks después de la invocación de la oleada
        }
    }

    // Nueva sobrecarga: spawn de una oleada centrada en una entidad (usada por mobs)
    public static void spawnWaveAtEntity(LivingEntity user){
        if (user == null || user.isDead()) return;

        // Aplicar daño a jugadores en la torre y en rango
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOnline()) continue;
            if (!p.getWorld().equals(user.getWorld())) continue;
            if (p.equals(user)) continue;
            if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue; // excluir inmunes
            if (p.getLocation().distance(user.getLocation()) <= 30.0) {
                double newHealth = p.getHealth() - 20.0;
                if (newHealth <= 0) {
                    p.damage(300.0, user);
                } else {
                    try {
                        p.setHealth(newHealth);
                        p.damage(1, user);
                    } catch (Exception ignore) {
                        p.damage(20.0, user);
                    }
                }
                p.sendMessage("§c§l¡Has sido golpeado por el latido del caos!§r");
            }
        }

        // Partículas en anillos crecientes centrados en la entidad
        final int steps = 10;
        final double maxRadius = 30.0;
        final Location center = user.getLocation().clone().add(0, 1.0, 0);
        final DustOptions dust = new DustOptions(Color.fromRGB(0,0,0), 1.0f);

        for (int step = 1; step <= steps; step++) {
            final int s = step;
            final double radius = (maxRadius / steps) * s;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (user == null || user.isDead()) return;
                Location c = center.clone();
                double circumference = 2 * Math.PI * Math.max(radius, 1.0);
                double spacing = 0.5;
                int points = Math.max(8, (int)(circumference / spacing));
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI) * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location spawnLoc = c.clone().add(x, 0, z);
                    spawnLoc.getWorld().spawnParticle(Particle.DUST, spawnLoc, 1, 0, 0, 0, 0, dust);
                }
            }, s);
        }
    }
}
