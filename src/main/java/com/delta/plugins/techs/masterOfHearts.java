package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class masterOfHearts {
    static final String TECH_ID = "master_of_hearts";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);
    public static void register() {
        com.delta.plugins.Plugin.registerAbyssID(TECH_ID);
        // Registrar listener para inputs de chat para la técnica ultimate
        TechRegistry.registerTechnique(TECH_ID, heartCannon);
        TechRegistry.registerTechnique(TECH_ID, Whacka_Summon.summonWhackaTech);
        TechRegistry.registerTechnique(TECH_ID, pureheartLaser);
        Bukkit.getPluginManager().registerEvents(new UltimateListener(), plugin);
        TechRegistry.registerTechnique(TECH_ID, ultimateTechnique);

    }

    // Reemplazo: heartCannon (nuevo constructor TechniqueAPI)
    static Technique heartCannon = new Technique(
        "heartcannon",
        "Heart cannon",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Ritual visual y daño frontal.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();

            // 1. Flotar al jugador y fijar velocidad a 0
            Location startLoc = player.getLocation().clone().add(0, 1, 0);
            player.teleport(startLoc);
            player.setAllowFlight(true);
            player.setFlying(true);

            // Mantener la velocidad en 0 durante 6 segundos
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    player.setVelocity(new Vector(0, 0, 0));
                    ticks++;
                    if (ticks > 140) { // 6 segundos
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 1);

            // 2. Aparecer corazones en círculo relativo a la cámara, 2 bloques delante
            List<ItemStack> hearts = Arrays.asList(
                Items.pure_heart_red, Items.pure_heart_brown, Items.pure_heart_blue,
                Items.pure_heart_cyan, Items.pure_heart_purple, Items.pure_heart_pink,
                Items.pure_heart_yellow, Items.pure_heart_grey
            );
            List<Item> spawned = new ArrayList<>();
            Location center = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(2));
            Vector forward = player.getEyeLocation().getDirection().normalize();
            Vector up = new Vector(0, 1, 0);
            Vector right = forward.clone().getCrossProduct(up).normalize();
            up = right.clone().getCrossProduct(forward).normalize(); // up perpendicular a forward y right

            double radius = 1.5;
            int n = hearts.size();
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                Vector circleVec = right.clone().multiply(Math.cos(angle)).add(up.clone().multiply(Math.sin(angle))).multiply(radius);
                Location itemLoc = center.clone().add(circleVec);
                Item item = player.getWorld().dropItem(itemLoc, hearts.get(i));
                item.setPickupDelay(Integer.MAX_VALUE);
                item.setGravity(false);
                item.setVelocity(new Vector(0,0,0));
                spawned.add(item);
            }
            final Vector finalUp = up;

            // 3 segundos después, girar el círculo y acumular partículas en el centro
            new BukkitRunnable() {
                int ticks = 0;
                double spinAngle = 0;
                double spinSpeed = Math.PI / 30; // velocidad inicial
                @Override
                public void run() {
                    if (ticks == 0) {
                        hotbarMessage.sendHotbarMessage(player, "¡El ritual comienza a girar!");
                    }
                    // Cada segundo aumenta la velocidad de giro
                    if (ticks % 20 == 0 && ticks > 0) {
                        spinSpeed *= 1.25;
                    }
                    // Girar los items
                    spinAngle += spinSpeed;
                    for (int i = 0; i < n; i++) {
                        double angle = 2 * Math.PI * i / n + spinAngle;
                        Vector circleVec = right.clone().multiply(Math.cos(angle)).add(finalUp.clone().multiply(Math.sin(angle))).multiply(radius);
                        Location itemLoc = center.clone().add(circleVec);
                        spawned.get(i).teleport(itemLoc);
                    }
                    // Acumular partículas en el centro
                    player.getWorld().spawnParticle(Particle.HEART, center, 2, 0.2, 0.2, 0.2, 0.1);

                    ticks++;
                    if (ticks > 140) { // 7 segundos de giro
                        // 4. Matar entidades vivas 20 bloques delante del jugador
                        Location front = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(10));
                        Vector dir = player.getLocation().getDirection().normalize();
                        for (LivingEntity ent : player.getWorld().getLivingEntities()) {
                            if (ent == player) continue;
                            Vector toEnt = ent.getLocation().toVector().subtract(player.getEyeLocation().toVector());
                            double dot = toEnt.normalize().dot(dir);
                            double dist = ent.getLocation().distance(player.getEyeLocation());
                            if (dot > 0.85 && dist < 50) {
                                ent.damage(9999, player);
                            }
                        }
                        // Eliminar los items
                        for (Item it : spawned) it.remove();
                        // Disparar rayo de partículas
                        Location start = center.clone();
                        Vector rayDir = dir.clone();
                        for (double d = 0; d < 20; d += 0.5) {
                            Location point = start.clone().add(rayDir.clone().multiply(d));
                            player.getWorld().spawnParticle(Particle.END_ROD, point, 2, 0.05, 0.05, 0.05, 0.01);
                        }
                        hotbarMessage.sendHotbarMessage(player, "¡El ritual ha terminado!");
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 60, 1); // empieza tras 3 segundos (60 ticks)
        }
    );

    // Nueva técnica: cataclismo de corazones -> hace girar jugadores, spawnea explosiones/TNT debajo cada tick por 4s y luego los lanza
    static Technique masterCataclysm = new Technique(
        "fuck_yes",
        "Blender of Doom",
        new TechniqueMeta(true, cooldownHelper.hour, List.of("Gira jugadores y crea explosiones masivas.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            World world = player.getWorld();
            Location center = player.getLocation().clone();
            double maxRange = 100.0;
            double orbitRadius = 10.0;
            double baseY = center.getY() + 5.0;

            // Recolectar jugadores afectados (mismo mundo y dentro de rango)
            List<Player> targets = new ArrayList<>();
            for (Player p : world.getPlayers()) {
                if (p.getWorld() != world) continue;
                if(p.equals(player)) continue;
                if (p.getLocation().distance(center) <= maxRange) {
                    targets.add(p);
                }
            }
            if (targets.isEmpty()) {
                hotbarMessage.sendHotbarMessage(player, "No hay jugadores en rango para el ritual.");
                return;
            }

            // Datos por jugador
            Map<Player, Double> angle = new HashMap<>();
            Map<Player, Double> curY = new HashMap<>();
            Map<Player, Double> vy = new HashMap<>();
            Random rand = new Random();

            for (Player p : targets) {
                angle.put(p, rand.nextDouble() * Math.PI * 2.0);
                curY.put(p, baseY);
                vy.put(p, -0.1 + rand.nextDouble() * 0.6); // velocidad vertical inicial aleatoria
            }

            hotbarMessage.sendHotbarMessage(player, "El ritual comienza: ¡prepárense!");

            // Tarea que corre cada tick durante 4s (80 ticks)
            new BukkitRunnable() {
                int ticks = 0;
                double spinSpeed = Math.PI / 6.0; // rotación muy rápida (vuelta completa ~12 ticks)

                @Override
                public void run() {
                    // cada tick: teleport a órbita + oscilación vertical + crear explosión debajo
                    for (Player p : new ArrayList<>(targets)) {
                        if (p == null || !p.isOnline() || p.getWorld() != world) {
                            targets.remove(p);
                            continue;
                        }
                        // actualizar ángulo
                        double a = angle.getOrDefault(p, 0.0) + spinSpeed;
                        angle.put(p, a);

                        // actualizar velocidad vertical aleatoriamente y altura
                        double v = vy.getOrDefault(p, 0.0) + (-0.05 + rand.nextDouble() * 0.1); // pequeña variación por tick
                        // limitar velocidad
                        v = Math.max(-1.5, Math.min(1.5, v));
                        vy.put(p, v);

                        double y = curY.getOrDefault(p, baseY) + v;
                        // mantener rango razonable alrededor de baseY (3..7)
                        y = Math.max(baseY - 2.0, Math.min(baseY + 2.0, y));
                        curY.put(p, y);

                        // calcular posición orbital alrededor del usuario (radio fijo)
                        double x = center.getX() + Math.cos(a) * orbitRadius;
                        double z = center.getZ() + Math.sin(a) * orbitRadius;
                        Location tp = new Location(world, x, y, z, (float) Math.toDegrees(a + Math.PI), 0f);

                        // Teleportar jugador a la posición orbital
                        try {
                            p.teleport(tp);
                        } catch (Exception ex) {
                            // ignore teleport errors
                        }

                        // Generar explosión debajo del jugador (simula TNT); no romper bloques (false) para minimizar destrucción
                        Location explLoc = tp.clone().add(0, -1.0, 0);
                        // seguridad: asegúrate de que la ubicación esté en el mismo mundo
                        if (explLoc.getWorld() == world) {
                            // blast power 50 según petición, sin dañar bloques ni crear fuego
                            world.createExplosion(explLoc, 50.0f, true, true, player);
                        }
                    }

                    ticks++;
                    if (ticks >= 80) { // 4 segundos completados
                        this.cancel();

                        // 1 segundo de espera (20 ticks) antes de lanzar a los jugadores
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Lanzar jugadores radialmente hacia afuera con componente vertical
                                for (Player p : new ArrayList<>(targets)) {
                                    if (p == null || !p.isOnline() || p.getWorld() != world) continue;
                                    // vector horizontal desde el centro hacia el jugador
                                    Vector dir = p.getLocation().toVector().subtract(center.toVector());
                                    dir.setY(0);
                                    if (dir.lengthSquared() < 0.0001) {
                                        // si está exactamente en el centro, dar dirección aleatoria
                                        double ang = rand.nextDouble() * Math.PI * 2;
                                        dir = new Vector(Math.cos(ang), 0, Math.sin(ang));
                                    } else {
                                        dir.normalize();
                                    }
                                    // configurar velocidad horizontal y vertical (ajustable)
                                    Vector launch = dir.multiply(7.0); // velocidad horizontal
                                    launch.setY(2.5 + rand.nextDouble() * 7.5); // impulso vertical
                                    p.setVelocity(launch);
                                }
                            }
                        }.runTaskLater(plugin, 20L);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);

        }
    );

    // Nueva técnica: anillo de purehearts + partículas + láser repetido según absorption y salud
    static Technique pureheartLaser = new Technique(
        "pureheart_laser",
        "Pureheart Barrage",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(1), List.of("Anillos de purehearts y láseres según absorption/salud.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if (!player.isOnline()) return;
            World world = player.getWorld();
            // 1) encontrar jugador objetivo más cercano (no el jugador mismo salvo ausencia)
            Player closest = null;
            double bestDist = Double.MAX_VALUE;
            for (Player p : world.getPlayers()) {
                if (p.equals(player)) continue;
                if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                double d = p.getLocation().distanceSquared(player.getLocation());
                if (d < bestDist) {
                    bestDist = d;
                    closest = p;
                }
            }
            if (closest == null) {
                // no hay otros jugadores en el mismo mundo: permitimos apuntar al propio jugador
                closest = player;
            }
            // 2) determinar repeticiones: 3 + x (x = absorption en corazones). Si x==0 => x=2
            double absorptionPoints = player.getAbsorptionAmount(); // puntos de vida (HP)
            int x = (int)Math.round((absorptionPoints / 2.0)); // convertir a corazones
            if (x == 0) x = 2;
            final int repeats = 3 + x + (new Random()).nextInt(11);

            // 3) parámetros dependientes de salud normal (tener en cuenta max health dinámico)
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
            double healthFraction = 1.0;
            if (maxHealth > 0) healthFraction = Math.min(1.0, Math.max(0.0, player.getHealth() / maxHealth));
            // time1 base en ticks (ej. 40 ticks = 2s). A mayor healthFraction -> menor time1 (más rápido)
            int baseTimeTicks = 40;
            int time1Ticks = Math.max(6, (int)(baseTimeTicks * (1.0 - 0.5 * healthFraction))); // mínimo 6 ticks
            // delay entre repeticiones (ticks), también reduce con más vida normal
            int baseDelay = 30;
            int delayBetween = Math.max(6, (int)(baseDelay * (1.0 - 0.6 * healthFraction)));

            Random rand = new Random();

            // lista de items 'pureheart_*' disponibles
            List<ItemStack> heartItems = Arrays.asList(
                Items.pureheart_red, Items.pureheart_orange, Items.pureheart_yellow,
                Items.pureheart_green, Items.pureheart_blue, Items.pureheart_indigo,
                Items.pureheart_purple, Items.pureheart_white
            );

            // tarea que lanza las repeticiones secuenciales
            Player finalClosest = closest;
            new BukkitRunnable() {
                int iter = 0;
                @Override
                public void run() {
                    if (!player.isOnline() || iter >= repeats) {
                        cancel();
                        return;
                    }
                    // 4) seleccionar coordenadas aleatorias dentro de 20 bloques del jugador objetivo
                    double r = rand.nextDouble() * 20.0;
                    double theta = rand.nextDouble() * Math.PI * 2.0;
                    double tx = finalClosest.getLocation().getX() + r * Math.cos(theta);
                    double tz = finalClosest.getLocation().getZ() + r * Math.sin(theta);
                    // hallar altura segura (superficie)
                    double ty = world.getHighestBlockYAt((int)Math.floor(tx), (int)Math.floor(tz)) + 1;
                    if(ty< player.getLocation().getY()) {
                        ty = player.getLocation().getY() + (new Random()).nextInt(0, 10);
                    }
                    Location center = new Location(world, tx, ty, tz);

                    // 5) crear anillo de items alrededor del centro; orientarlos hacia el target (closest)
                    double radius = 1.5;
                    int n = heartItems.size();
                    List<Item> spawned = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        double angle = 2 * Math.PI * i / n;
                        double ix = center.getX() + Math.cos(angle) * radius;
                        double iz = center.getZ() + Math.sin(angle) * radius;
                        double iy = center.getY();
                        Location itemLoc = new Location(world, ix, iy, iz);
                        Item item = world.dropItem(itemLoc, heartItems.get(i));
                        item.setPickupDelay(Integer.MAX_VALUE);
                        item.setGravity(false);
                        item.setVelocity(new Vector(0,0,0));
                        // orientar el item para "mirar" hacia el jugador objetivo
                        double dx = finalClosest.getLocation().getX() - ix;
                        double dz = finalClosest.getLocation().getZ() - iz;
                        float yaw = (float)Math.toDegrees(Math.atan2(dx, dz));
                        try {
                            item.setRotation(yaw, 0f);
                        } catch (NoSuchMethodError ignored) {
                            // algunos servidores pueden no exponer setRotation; ignorar si no existe.
                        }
                        spawned.add(item);
                    }

                    // 7) al cabo de time1Ticks -> disparar láser hacia el objetivo y aplicar daño
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // trazar rayo desde center hacia la cabeza del objetivo
                            Location targetLoc = finalClosest.getLocation().clone().add(0, finalClosest.getEyeLocation().getY() - finalClosest.getLocation().getY(), 0);
                            Vector dir = targetLoc.toVector().subtract(center.toVector()).normalize();
                            double maxRange = center.distance(targetLoc) + 1.0;
                            boolean hit = false;
                            for (double d = 0; d <= maxRange; d += 0.5) {
                                Location point = center.clone().add(dir.clone().multiply(d));
                                world.spawnParticle(Particle.END_ROD, point, 2, 0.05, 0.05, 0.05, 0.01);
                                // check hit aproximado por distancia al jugador
                                if (!hit && point.distance(finalClosest.getLocation()) < 1.2) {
                                    hit = true;
                                    // aplicar daño: estimado para que con Prot V resulte ~7 corazones (14 HP).
                                    // Consideramos Prot V reduce ~20% -> base ~18
                                    double baseDamage = 18.0*10;
                                    try {
                                        finalClosest.damage(baseDamage, player);
                                    } catch (Throwable ignored) {}
                                }
                            }
                            // limpiar items del anillo
                            for (Item it : spawned) {
                                try { it.remove(); } catch (Throwable ignored) {}
                            }
                        }
                    }.runTaskLater(plugin, time1Ticks);

                    iter++;
                }
            }.runTaskTimer(plugin, 0L, delayBetween);
        }
    );

    // --- Estado y utilidades para la técnica ultimate ---
    private static final String ULT_ID = "ultimate_cataclysm";
    private static final Map<UUID, Integer> phase = new ConcurrentHashMap<>(); // 1=awaiting number,2=expanding,3=awaiting reactivation
    private static final Map<UUID, Integer> targetRadius = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> particleTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> timeoutTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> expansionTicks = new ConcurrentHashMap<>(); // helper if needed

    // --- Nuevos mapas para los anillos de corazones ---
    private static final Map<UUID, List<Item>> heartRingItems = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> heartRingTasks = new ConcurrentHashMap<>();

    // --- NUEVO: mapa para indicar modo "pve" / no-daño-a-jugadores por activación ---
    private static final Map<UUID, Boolean> noPlayerMode = new ConcurrentHashMap<>();

    // --- NUEVOS MAPAS PARA LA ESFERA (GLASS) ---
    private static final Map<UUID, Set<BlockState>> sphereReplacedBlocks = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Block>> sphereBlocksMap = new ConcurrentHashMap<>();
    private static final Map<UUID, AtomicBoolean> sphereRestored = new ConcurrentHashMap<>();

    // Listener para capturar el número en chat durante fase1
    public static class UltimateListener implements Listener {
        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent ev) {
            Player p = ev.getPlayer();
            UUID id = p.getUniqueId();
            if (!phase.containsKey(id) || phase.get(id) != 1) return;
            ev.setCancelled(true); // consumir mensaje
            String msg = ev.getMessage().trim();
            try {
                // Parsear tokens: puede venir "50 pve" o "pve 50" o "50" etc.
                String[] parts = msg.split("\\s+");
                Integer parsedRadius = null;
                boolean parsedNoPlayer = false;
                for (String token : parts) {
                    if(token.equalsIgnoreCase("cancel")){
                        hotbarMessage.sendHotbarMessage(p, "Ultimate cancelled.");
                        CooldownManager.removeCooldown(p, ULT_ID);
                        phase.remove(id);
                        return;
                    }
                    if (token.equalsIgnoreCase("pve") || token.equalsIgnoreCase("noplayer")) {
                        parsedNoPlayer = true;
                        continue;
                    }
                    // intentar parse int
                    try {
                        int val = Integer.parseInt(token);
                        parsedRadius = val;
                    } catch (NumberFormatException ignore) {}
                }
                if (parsedRadius == null) {
                    hotbarMessage.sendHotbarMessage(p, "Please type a number (optionally add 'pve' or 'noplayer').");
                    phase.remove(id);
                    return;
                }
                int val = parsedRadius;
                if (val <= 0) {
                    hotbarMessage.sendHotbarMessage(p, "Radius must be positive.");
                    phase.remove(id);
                    return;
                }
                if (val > 300) val = 300;
                targetRadius.put(id, val);
                noPlayerMode.put(id, parsedNoPlayer); // guardar opción
                phase.put(id, 2); // pasar a fase 2
                // iniciar expansión de partículas cliente-side
                startExpansion(p, val);

                // --- NUEVO: crear esfera de GLASS en hilo principal cuando se especifica el radio ---
                final int chosenRadius = val;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Set<BlockState> replaced = new HashSet<>();
                    Set<Block> sb = new HashSet<>();
                    if (chosenRadius <= 80) {
                        try {
                            Location center = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                            Set<Block> sphere = roaring_soul.sphereAround(center, chosenRadius);
                            for (Block b : sphere) {
                                Material m = b.getType();
                                if (m == Material.AIR || m == Material.WATER || m == Material.BUBBLE_COLUMN || m == Material.LIGHT) {
                                    replaced.add(b.getState());
                                    sb.add(b);
                                    b.setType(Material.GLASS, false);
                                }
                            }
                        } catch (Throwable ignored) {}
                    } else {
                        hotbarMessage.sendHotbarMessage(p, "Radius too large (max 80): sphere not created.");
                    }
                    sphereReplacedBlocks.put(id, replaced);
                    sphereBlocksMap.put(id, sb);
                    sphereRestored.put(id, new AtomicBoolean(false));
                });
                // --- FIN NUEVO ---

            } catch (Throwable ex) {
                hotbarMessage.sendHotbarMessage(p, "Please type a number.");
                phase.remove(id);
            }
        }
    }

    // Crea dos anillos de items alrededor del jugador y una tarea que los actualiza cada tick


    private static void startExpansion(Player player, int radius) {
        UUID id = player.getUniqueId();
        final int totalTicks = 60; // 3 segundos
        expansionTicks.put(id, 0);

        // Cancel any previous particle task for safety
        BukkitTask prev = particleTasks.get(id);
        if (prev != null) prev.cancel();

        BukkitTask task = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    phase.remove(id);
                    particleTasks.remove(id);
                    noPlayerMode.remove(id);
                    // Asegurar limpieza del anillo si existiera
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        BukkitTask t = heartRingTasks.remove(id);
                        if (t != null) t.cancel();
                        List<Item> items = heartRingItems.remove(id);
                        if (items != null) for (Item it : items)
                            try {
                                it.remove();
                            } catch (Throwable ignored) {
                            }
                        // Si existía esfera, restaurarla también (precaución)
                        Set<BlockState> replaced = sphereReplacedBlocks.remove(id);
                        if (replaced != null) {
                            for (BlockState bs : replaced) {
                                try {
                                    bs.update(true, false);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                        sphereBlocksMap.remove(id);
                        sphereRestored.remove(id);
                    });
                    return;
                }
                double progress = (double) tick / totalTicks;
                double curRadius = progress * radius;
                // spawn sphere shell particles client-side
                Location center = player.getLocation().add(0, 1, 0);
                int points = Math.max(40, (int) (Math.PI * 2 * curRadius * 8)); // scalable points
                for (int i = 0; i < points; i++) {
                    double theta = Math.acos(2 * Math.random() - 1);
                    double phi = 2 * Math.PI * Math.random();
                    double x = Math.sin(theta) * Math.cos(phi);
                    double y = Math.cos(theta);
                    double z = Math.sin(theta) * Math.sin(phi);
                    Location loc = center.clone().add(x * curRadius, y * curRadius, z * curRadius);
                    // spawnParticle client-side
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }
                tick++;
                if (tick > totalTicks) {
                    // finished expansion
                    phase.put(id, 3); // esperar reactivación
                    // dar resistance 45 permanentemente (amplifier = level-1)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 44, true, false));
                    // reset cooldown a un tick después (intentando usar cooldownHelper)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        CooldownManager.setCooldown(player, ULT_ID, 0L);
                    }, 1L);
                    // programar timeout de 1 minuto: si no se reactiva, quitar resistance y aplicar daño 5 veces (1 tick cada vez)
                    BukkitTask timeout = new BukkitRunnable() {
                        @Override
                        public void run() {
                            // limpiar anillos y quitar resistance
                            player.removePotionEffect(PotionEffectType.RESISTANCE);

                            // Eliminar anillo si existe (timeout ocurre en hilo principal)
                            BukkitTask hrTask = heartRingTasks.remove(id);
                            if (hrTask != null) hrTask.cancel();
                            List<Item> items = heartRingItems.remove(id);
                            if (items != null) for (Item it : items)
                                try {
                                    it.remove();
                                } catch (Throwable ignored) {
                                }
                            ;

                            // --- NUEVO: restaurar esfera si fue creada al especificar el radio ---
                            Set<BlockState> replaced = sphereReplacedBlocks.remove(id);
                            if (replaced != null) {
                                for (BlockState bs : replaced) {
                                    try {
                                        bs.update(true, false);
                                    } catch (Throwable ignored) {
                                    }
                                }
                            }
                            sphereBlocksMap.remove(id);
                            sphereRestored.remove(id);
                            // --- FIN NUEVO ---

                            new BukkitRunnable() {
                                int times = 0;

                                @Override
                                public void run() {
                                    if (!player.isOnline() || times >= 5) {
                                        cancel();
                                        return;
                                    }
                                    player.damage(1000.0, player);
                                    times++;
                                }
                            }.runTaskTimer(plugin, 1L, 1L);
                            phase.remove(id);
                            particleTasks.remove(id);
                            noPlayerMode.remove(id);
                            targetRadius.remove(id);
                        }
                    }.runTaskLater(plugin, 20L * 60); // 1 minuto
                    timeoutTasks.put(id, timeout);

                    // CREAR anillo de 16 corazones SÍNCRONO (hilo principal)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        // si el jugador desconectó entre tanto, abortar
                        if (!player.isOnline()) return;
                        World w = player.getWorld();
                        List<ItemStack> hearts = Arrays.asList(
                                Items.pure_heart_red, Items.pure_heart_brown, Items.pure_heart_blue,
                                Items.pure_heart_cyan, Items.pure_heart_purple, Items.pure_heart_pink,
                                Items.pure_heart_yellow, Items.pure_heart_grey,
                                Items.pureheart_red, Items.pureheart_orange, Items.pureheart_yellow,
                                Items.pureheart_green, Items.pureheart_blue, Items.pureheart_indigo,
                                Items.pureheart_purple, Items.pureheart_white
                        );
                        final int n = hearts.size();
                        final double ringRadius = 2.0;
                        List<Item> spawned = new ArrayList<>();
                        double[] baseAngles = new double[n];
                        for (int i = 0; i < n; i++) {
                            baseAngles[i] = 2 * Math.PI * i / n;
                            double angle = baseAngles[i];
                            Location loc = player.getLocation().add(0, 1, 0).clone().add(Math.cos(angle) * ringRadius, 0, Math.sin(angle) * ringRadius);
                            Item it = w.dropItem(loc, hearts.get(i));
                            it.setPickupDelay(Integer.MAX_VALUE); // insanely high
                            it.setGravity(false);
                            it.setVelocity(new Vector(0, 0, 0));
                            spawned.add(it);
                        }
                        heartRingItems.put(id, spawned);

                        // tarea que mueve y hace girar los corazones cada tick
                        BukkitTask ringTask = new BukkitRunnable() {
                            double spinAngle = 0;
                            final double spinSpeed = Math.PI / 20.0; // ajustable

                            @Override
                            public void run() {
                                if (!player.isOnline()) {
                                    // cleanup si el jugador se desconecta
                                    heartRingTasks.remove(id);
                                    List<Item> items = heartRingItems.remove(id);
                                    if (items != null) for (Item it : items)
                                        try {
                                            it.remove();
                                        } catch (Throwable ignored) {
                                        }
                                    ;
                                    cancel();
                                    return;
                                }
                                Location centerLoc = player.getLocation().add(0, 1, 0);
                                for (int i = 0; i < spawned.size(); i++) {
                                    Item it = spawned.get(i);
                                    if (it == null || it.isDead() || !it.isValid()) continue;
                                    double angle = baseAngles[i] + spinAngle;
                                    double x = centerLoc.getX() + Math.cos(angle) * ringRadius;
                                    double z = centerLoc.getZ() + Math.sin(angle) * ringRadius;
                                    double y = centerLoc.getY();
                                    try {
                                        it.teleport(new Location(w, x, y, z));
                                    } catch (Throwable ignored) {
                                    }
                                }
                                spinAngle += spinSpeed;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                        heartRingTasks.put(id, ringTask);
                    });

                    // limpiar task tracking
                    particleTasks.remove(id);
                    expansionTicks.remove(id);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L,1L);
        particleTasks.put(id, task);
    };

    // Nueva técnica ultimate
    static Technique ultimateTechnique = new Technique(
        "ultimate_cataclysm",
        "Heart Atomization",
        new TechniqueMeta(true, cooldownHelper.hour*2, List.of("Ultimate multi-fase con anillos y esfera.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            UUID id = player.getUniqueId();
            Integer pphase = phase.get(id);
            if (pphase == null) {
                // fase 1: preguntar y esperar chat input
                phase.put(id, 1);
                hotbarMessage.sendHotbarMessage(player, "How big shall the destruction be? Type 'pve' for no-player damage mode.");
                return;
            }
            if (pphase == 1) {
                hotbarMessage.sendHotbarMessage(player, "Awaiting size input in chat...");
                return;
            }
            if (pphase == 2) {
                hotbarMessage.sendHotbarMessage(player, "Expansion in progress...");
                return;
            }
            if (pphase == 3) {
                // reactivación: cancelar timeout si existe
                BukkitTask timeout = timeoutTasks.remove(id);
                if (timeout != null) timeout.cancel();
                // quitar resistencia ahora para proceder (pero se pedía que se comprobara: se quita más abajo si timeout)
                player.removePotionEffect(PotionEffectType.RESISTANCE);

                Integer radI = targetRadius.get(id);
                if (radI == null) {
                    hotbarMessage.sendHotbarMessage(player, "No radius stored; aborting.");
                    // limpiar estado por seguridad
                    phase.remove(id);
                    noPlayerMode.remove(id);
                    return;
                }
                double radius = radI.doubleValue();
                boolean skipPlayers = noPlayerMode.getOrDefault(id, false);

                // --- MODIFICADO: usar la esfera ya creada al especificar el radio (si existe) ---
                Set<BlockState> replacedBlocks = sphereReplacedBlocks.remove(id);
                Set<Block> sphereBlocks = sphereBlocksMap.remove(id);
                AtomicBoolean restoredFlag = sphereRestored.remove(id);
                if (restoredFlag == null) restoredFlag = new AtomicBoolean(false);
                boolean sphereCreated = (replacedBlocks != null && !replacedBlocks.isEmpty());
                // --- FIN MODIFICADO ---

                // Preparar restauración segura: timeout de 60s pero permitiendo restauración temprana cuando terminen los daños
                AtomicInteger remaining = new AtomicInteger(0);
                AtomicBoolean restored = new AtomicBoolean(false);
                AtomicReference<BukkitTask> restoreTaskRef = new AtomicReference<>(null);

                Runnable restoreNow = () -> {
                    if (!restored.compareAndSet(false, true)) return;
                    // cancelar timeout si existe
                    BukkitTask scheduled = restoreTaskRef.getAndSet(null);
                    if (scheduled != null) {
                        try { scheduled.cancel(); } catch (Throwable ignored) {}
                    }
                    // Restaurar bloques guardados (si existían)
                    if (replacedBlocks != null) {
                        for (BlockState bs : replacedBlocks) {
                            try {
                                bs.update(true, false);
                            } catch (Exception ignored) {}
                        }
                        replacedBlocks.clear();
                    }
                    if (sphereBlocks != null) sphereBlocks.clear();
                };

                // Programar restauración por timeout (60s) SOLO si se creó la esfera
                if (sphereCreated) {
                    restoreTaskRef.set(Bukkit.getScheduler().runTaskLater(plugin, restoreNow, 20L * 60L));
                } else {
                    // marcar ya restaurado para que restoreNow no haga nada si se invoca
                    restored.set(true);
                }
                // --- FIN: creación/restauración de esfera ---

                // Construir lista de entidades objetivo y ajustar contador "remaining"
                Enchantment det = Enchantment.getByKey(NamespacedKey.minecraft("determined"));
                World world = player.getWorld();

                List<LivingEntity> targets = new ArrayList<>();
                for (LivingEntity ent : new ArrayList<>(world.getLivingEntities())) {
                    if (ent == null || !ent.isValid()) continue;
                    if(ent instanceof Player p) {
                        if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    }
                    if (ent.equals(player)) continue;
                    double dist = ent.getLocation().distance(player.getLocation());
                    if (dist > radius) continue;
                    if (ent instanceof Player && skipPlayers) continue;
                    targets.add(ent);
                }
                remaining.set(targets.size());
                // Si no hay objetivos, restaurar inmediatamente
                if (targets.isEmpty()) {
                    restoreNow.run();
                }

                // Por cada LivingEntity objetivo, aplicar efecto/daño y decrementar contador al finalizar
                for (LivingEntity ent : targets) {
                    if (ent instanceof Player) {
                        Player p = (Player) ent;

                        ItemStack off = p.getInventory().getItemInOffHand();
                        int level = 0;
                        if (off != null && off.getType() != org.bukkit.Material.AIR && det != null) {
                            level = off.getEnchantmentLevel(det);
                        }
                        int repeats = 0;
                        if (off == null || off.getType() == org.bukkit.Material.AIR || det == null || level < 3) {
                            repeats = 3;
                        } else {
                            repeats = (3 * level) / 2;
                            if (repeats <= 0) repeats = 1;
                        }
                        final int times = repeats;
                        new BukkitRunnable() {
                            int done = 0;
                            @Override
                            public void run() {
                                if (!p.isOnline() || done >= times) {
                                    // marcar como terminado y comprobar restauración
                                    if (remaining.decrementAndGet() == 0) {
                                        restoreNow.run();
                                    }
                                    cancel();
                                    return;
                                }
                                p.damage(1000.0, player);
                                done++;
                            }
                        }.runTaskTimer(plugin, 0L, 2L);
                    } else {
                        LivingEntity le = ent;
                        final int times = 3;
                        new BukkitRunnable() {
                            int done = 0;
                            @Override
                            public void run() {
                                if (!le.isValid() || done >= times) {
                                    if (remaining.decrementAndGet() == 0) {
                                        restoreNow.run();
                                    }
                                    cancel();
                                    return;
                                }
                                try {
                                    le.damage(1000.0, player);
                                } catch (Throwable ignored) {}
                                done++;
                            }
                        }.runTaskTimer(plugin, 0L, 2L);
                    }
                }

                // cancelar partículas activas si hay alguna
                BukkitTask pt = particleTasks.remove(id);
                if (pt != null) pt.cancel();

                // esperar 1 segundo y luego eliminar corazones del anillo (si existen)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // cancelar y eliminar anillo
                    BukkitTask ring = heartRingTasks.remove(id);
                    if (ring != null) ring.cancel();
                    List<Item> items = heartRingItems.remove(id);
                    if (items != null) for (Item it : items) try { it.remove(); } catch (Throwable ignored) {};
                }, 20L);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    CooldownManager.setCooldown(player, ULT_ID, 48L * 60L * 60L * 1000L); // 48 horas en ms
                }, 1L);

                // finalizar técnica: limpiar estado
                phase.remove(id);
                targetRadius.remove(id);
                timeoutTasks.remove(id);
                noPlayerMode.remove(id);
                hotbarMessage.sendHotbarMessage(player, "Ultimate activated and resolved.");
            }
        }
    );

}
