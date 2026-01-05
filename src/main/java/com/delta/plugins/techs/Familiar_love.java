package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
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
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Familiar_love {

    static final String TECH_ID = "Familiar_love";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    static final Map<Player, Boolean> equivocarseEsDelitoActive = new HashMap<>();
    public static final Map<Player, Boolean> OstiacionActive = new HashMap<>();
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, undying_cat);
        TechRegistry.registerTechnique(TECH_ID, lazy_brick);
        TechRegistry.registerTechnique(TECH_ID, equivocarse_es_delito);
        TechRegistry.registerTechnique(TECH_ID, ostiacion);
        TechRegistry.registerTechnique(TECH_ID, wolf_pack);
        TechRegistry.registerTechnique(TECH_ID, siete_por_ocho);
        TechRegistry.registerTechnique(TECH_ID, cat_tramper);
        TechRegistry.registerTechnique(TECH_ID, iron_gramps);
        TechRegistry.registerTechnique(TECH_ID, familiar_love);
    }

    static Technique undying_cat = new Technique(
        "unded_cat",
        "Poder del gato inmortal",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("Restore C-heart from inventory/offhand or remove cooldown.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            boolean offhandUsed = false;
            ItemStack item = player.getInventory().getItemInOffHand();
            if(item == null) offhandUsed = false;
            if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER)) {
                int uses = item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, 0);
                if(uses > 0) {
                    offhandUsed = true;
                    // Restaurar el C-heart del jugador
                    uses--;
                    item.getItemMeta().getPersistentDataContainer().set(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, uses);
                    player.sendMessage("C-heart restaurado desde la offhand. Usos restantes: " + uses);
                }
            }
            if(offhandUsed) return;
            boolean inventoryUsed = false;
            // Buscar el primer C-heart en el inventario
            for(ItemStack invItem : player.getInventory().getContents()) {
                if(invItem != null && invItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER)) {
                    int uses = invItem.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, 0);
                    if(uses > 0) {
                        uses--;
                        invItem.getItemMeta().getPersistentDataContainer().set(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, uses);
                        player.sendMessage("C-heart restaurado desde el inventario. Usos restantes: " + uses);
                        inventoryUsed = true;
                        break;
                    }
                }
            }
            if(inventoryUsed) return;

            // Si no hay C-hearts usados, fijar cooldown a la mitad
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    CooldownManager.removeCooldown(player, "unded_cat");
                }
            }, 1L); // Ejecutar en el siguiente tick para evitar conflictos
            hotbarMessage.sendHotbarMessage(player, "Poder del gato inmortal usado!");
        }
    );

    static Technique lazy_brick = new Technique(
        "lazy_brick",
        "Ladrillo al vago",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Slow nearest players then launch arrow after delay.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            List<Player> players = player.getWorld().getPlayers();
            for (Player p : players) {
                if (p.getLocation().distance(player.getLocation()) <= 10 && p != player) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.SLOWNESS, 200, 1)); // Lentitud 2 por 10 segundos
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (p.getLocation().distance(player.getLocation()) <= 10 + (equivocarseEsDelitoActive.getOrDefault(player, false) ? 10 : 0)) {
                            // Lanzar flecha con 50 de daño base crítica
                            org.bukkit.entity.Arrow arrow = player.launchProjectile(org.bukkit.entity.Arrow.class);
                            arrow.setCritical(true);
                            arrow.setDamage(50.0);
                        }
                    }, 100L); // Esperar 5 segundos antes de verificar la posición
                }
            }
            hotbarMessage.sendHotbarMessage(player, "Ladrillo lanzado!");
        }
    );

    static Technique equivocarse_es_delito = new Technique(
        "equivocarse_es_delito",
        "Equivocarse es delito",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Temporary buff flag for player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            equivocarseEsDelitoActive.put(player, true);
            hotbarMessage.sendHotbarMessage(player, "Equivocarse es delito activado!");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                equivocarseEsDelitoActive.put(player, false);
                hotbarMessage.sendHotbarMessage(player, "Equivocarse es delito ha terminado.");
            }, 600L); // 30 segundos
        }
    );

    static Technique ostiacion = new Technique(
        "ostiacion",
        "Ostiación",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(15), List.of("Massive hit to closest player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            OstiacionActive.put(player, true);
            Player p = roaring_soul.getClosestPlayer(player.getLocation());
            if(p.getLocation().distance(player.getLocation()) <= 10 + (equivocarseEsDelitoActive.getOrDefault(p, false) ? 10 : 0)) {
                p.damage(800);
            }
            hotbarMessage.sendHotbarMessage(player, "Ostiación activada!");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                OstiacionActive.put(player, false);
                hotbarMessage.sendHotbarMessage(player, "Ostiación ha terminado.");
            }, 1200L); // 1 minuto
        }
    );

    static Technique wolf_pack = new Technique(
        "wolf_pack",
        "Manada de lobos",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(7), List.of("Summon 3 wolves with boosted stats.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            // Invoca 3 lobos con Fuerza 4, resistencia 3 y su salud máxima siendo 2X la del jugador. No despawnean.
            for(int i = 0; i<3; i++){
                Wolf w = player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Wolf.class);
                w.setOwner(player);
                w.setTamed(true);
                w.setAngry(true);
                w.setTarget(roaring_soul.getClosestPlayer(player.getLocation()));
                w.setCustomName(player.getName() + "'s Wolf");
                w.addPotionEffect(PotionEffectType.STRENGTH.createEffect(9999999, 3)); // Fuerza 4
                w.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(9999999, 2)); // Resistencia 3
                w.setMaxHealth(player.getHealth() * 2);
                w.setHealth(player.getHealth() * 2);
            }
            hotbarMessage.sendHotbarMessage(player, "Manada de lobos invocada!");
        }
    );

    static Technique siete_por_ocho = new Technique(
        "siete_por__ocho",
        "¿¡7x8!?",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("Deal heavy AoE damage.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            double damage = 56.0;
            if(player.getHealth() < 56.0) {
                damage /= 4.0;
            }
            List<Player> players = player.getWorld().getPlayers();
            for (Player p : players) {
                if (p.getLocation().distance(player.getLocation()) <= 5) {
                    p.setHealth(Math.max(p.getHealth() - damage, 1));
                    if(p.getHealth() < damage) {
                        p.damage(4);
                    }
                }
            }
            hotbarMessage.sendHotbarMessage(player, "¿¡7x8!? activado!");
        }
    );

    static Technique cat_tramper = new Technique(
        "cat_tramper",
        "Cat tramper",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Summon a cat that may explode.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            // Invoca un gato que explota a los varios segundos. Este gato sigue al target más cercano al jugador.
            Player target = roaring_soul.getClosestPlayer(player.getLocation());
            org.bukkit.entity.Cat cat = player.getWorld().spawn(target.getLocation(), org.bukkit.entity.Cat.class);
            cat.setTarget(target);
            cat.setOwner(player);
            cat.setTamed(true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                int rng = (int) (Math.random() * 100);
                if (rng < 20) {
                    cat.getWorld().createExplosion(cat.getLocation(), 500.0f, false, false);
                    return;
                }

                cat.remove();
            }, 100L); // Explota después de 5 segundos
            hotbarMessage.sendHotbarMessage(player, "Cat tramper invocado!");
        }
    );

    static Technique iron_gramps = new Technique(
        "iron_gramps",
        "Iron Gramps",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Temporary resistance and regen.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(1200, 0)); // Resistencia 1 por 1 minuto
            player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(1200, 1)); // Regeneración 2 por 1 minuto
            hotbarMessage.sendHotbarMessage(player, "Iron Gramps activado!");
        }
    );

    // Nueva técnica ultimate: Familiar Love
    static Technique familiar_love = new Technique(
        "familiar_love",
        "Amor familiar",
        new TechniqueMeta(true, cooldownHelper.hour, List.of("Ultimate: spawn hearts, particles and massive AoE damage.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (player == null || !player.isOnline()) return;
            hotbarMessage.sendHotbarMessage(player, "§d§lAmor familiar iniciado...§r");

            // 1) copiar flashes iguales a chaosWielder
            Location eye = player.getEyeLocation().clone();
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.getWorld().spawnParticle(Particle.FLASH, eye, 10, 0.2, 0.2, 0.2, 0, Color.WHITE), 0L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.getWorld().spawnParticle(Particle.FLASH, eye, 12, 0.2, 0.2, 0.2, 0, Color.WHITE), 5L);

            // 2) spawnear outer pure hearts en un círculo centrado en el jugador
            Location base = player.getLocation().clone().add(0, 2.0, 0);
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
            final List<Item> spawnedItems = new ArrayList<>();
            double outerRadius = 3.0;
            int countOuter = outer.length;
            for (int i = 0; i < countOuter; i++) {
                double angle = 2 * Math.PI * i / countOuter;
                Location loc = base.clone().add(Math.cos(angle) * outerRadius, 0, Math.sin(angle) * outerRadius);
                Item it = base.getWorld().dropItem(loc, outer[i].clone());
                it.setGravity(false);
                it.setPickupDelay(Integer.MAX_VALUE);
                it.setInvulnerable(true);
                it.setVelocity(new Vector(0, 0, 0));
                spawnedItems.add(it);
            }

            // 3) 2 segundos después: convertir items en partículas y hacerlos girar rápido (3s)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Colores RGB codificados manualmente para cada heart index
                Color[] colors = new Color[]{
                    Color.fromRGB(220,20,60),   // red
                    Color.fromRGB(139,69,19),   // brown
                    Color.fromRGB(65,105,225),  // blue
                    Color.fromRGB(0,255,255),   // cyan
                    Color.fromRGB(148,0,211),   // purple
                    Color.fromRGB(255,105,180), // pink
                    Color.fromRGB(255,215,0),   // yellow
                    Color.fromRGB(105,105,105)  // grey
                };

                class HeartParticle { double radius; double angle; DustOptions dust; }
                List<HeartParticle> particles = new ArrayList<>();

                // Crear HeartParticle desde los items y eliminar entidades de item
                for (int i = 0; i < spawnedItems.size(); i++) {
                    Item it = spawnedItems.get(i);
                    Location l = it.getLocation().clone();
                    double ang = Math.atan2(l.getZ() - base.getZ(), l.getX() - base.getX());
                    HeartParticle hp = new HeartParticle();
                    hp.radius = Math.max(0.5, l.distance(base));
                    hp.angle = ang;
                    hp.dust = new DustOptions(colors[i % colors.length], 1.0f);
                    particles.add(hp);
                    it.remove();
                }

                // Animación de giro rápido: 60 ticks (3s)
                final int spinTicks = 60;
                final int[] tick = {0};
                final double angularSpeed = 1.2; // rad/tick -> giro rápido
                final int spinTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (HeartParticle hp : particles) {
                        hp.angle += angularSpeed;
                        Location spawn = base.clone().add(Math.cos(hp.angle) * hp.radius, 0, Math.sin(hp.angle) * hp.radius);
                        spawn.getWorld().spawnParticle(Particle.DUST, spawn, 3, 0, 0, 0, 0, hp.dust);
                    }
                    tick[0]++;
                    if (tick[0] >= spinTicks) {
                        // la tarea se cancelará luego mediante cancelTask fuera (programada abajo)
                    }
                }, 0L, 1L).getTaskId();

                // cancelar la animación tras spinTicks
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getScheduler().cancelTask(spinTaskId), spinTicks + 1L);

                // 4) Tras 3s de spinning: spawn same particles alrededor de cada jugador a <=20 bloques (mismo mundo)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // cancelar cualquier partícula local conceptual (ya cancelada); ahora crear efectos persistentes por jugador
                    List<Integer> perPlayerParticleTasks = new ArrayList<>();
                    List<Player> affected = new ArrayList<>();
                    for (Player p : player.getWorld().getPlayers()) {
                        if (!p.isOnline()) continue;
                        if (p.equals(player)) continue;
                        if (p.getLocation().distance(player.getLocation()) > 20.0) continue;
                        affected.add(p);
                        Location center = p.getLocation().clone().add(0, 1.5, 0);
                        // crear tarea repetida que dibuja los mismos 8 colores en círculo alrededor del jugador
                        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                            double localAngle = 0;
                            @Override
                            public void run() {
                                // una pasada: dibujar 8 partículas en círculo
                                double r = 1.8;
                                for (int i = 0; i < colors.length; i++) {
                                    double a = localAngle + 2 * Math.PI * i / colors.length;
                                    Location spawn = center.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r);
                                    spawn.getWorld().spawnParticle(Particle.DUST, spawn, 4, 0, 0, 0, 0, new DustOptions((colors[i]), 1.0f));
                                    // note: DustOptions accepts Color directly
                                    spawn.getWorld().spawnParticle(Particle.DUST, spawn, 0, 0, 0, 0, 0, new DustOptions(colors[i], 1.0f));
                                }
                                localAngle += 0.4;
                            }
                        }, 0L, 2L).getTaskId();
                        perPlayerParticleTasks.add(taskId);
                    }

                    // Programar los daños: 1º daño 20 ticks después, 2º daño 40 ticks después
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : affected) {
                            if (p != null && p.isOnline()) p.damage(700.0, player);
                        }
                    }, 20L);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : affected) {
                            if (p != null && p.isOnline()) p.damage(700.0, player);
                        }
                        // después del segundo daño, limpiar todas las tareas de partículas por jugador
                        for (int id : perPlayerParticleTasks) {
                            try { Bukkit.getScheduler().cancelTask(id); } catch (Exception ignored) {}
                        }
                        hotbarMessage.sendHotbarMessage(player, "§d§lAmor familiar finalizado.§r");
                    }, 40L);

                }, spinTicks + 1L); // ejecutar después de que termine el spin
            }, 40L); // 2 segundos después de spawnear items
        }
    );

}
