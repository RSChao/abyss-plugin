package com.delta.plugins.techs;

import com.rschao.events.soulEvents;
import com.rschao.items.weapons;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cancel.SimpleCancellationToken;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class OriginDepleter {
    static final String TECH_ID = "oblivion_depleter";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, breath);
        TechRegistry.registerTechnique(TECH_ID, chunk);
        TechRegistry.registerTechnique(TECH_ID, slashes);
        Bukkit.getPluginManager().registerEvents(new UltimateListener(), plugin);
    }
    private static boolean registeredP4Techs = false;
    public static void addP4Techs(){
        if(registeredP4Techs) return;
        TechRegistry.registerTechnique(TECH_ID, present);
        TechRegistry.registerTechnique(TECH_ID, ultimateTechnique);
        TechRegistry.registerTechnique(TECH_ID, chaos);
        registeredP4Techs = true;
    }

    static Technique breath = new Technique("breath", "Oblivions breath", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Heals and gives Origin 1 extra pop.")), TargetSelectors.self(), (ctx, token) ->{
        for(Player p : Bukkit.getOnlinePlayers()){
            if(soulEvents.hasSoul(p, 19)){
                ItemStack item = p.getInventory().getItemInOffHand();
                if(item.hasItemMeta()){
                    //check for enchant minecraft:determined
                    double level = item.getEnchantmentLevel(Enchantment.getByKey(NamespacedKey.minecraft("determined")));
                    if(level <= 0) return;
                    ItemMeta meta = item.getItemMeta();
                    int t = meta.getPersistentDataContainer().get(weapons.CHKey, PersistentDataType.INTEGER);
                    if(t>0) {
                        meta.getPersistentDataContainer().set(weapons.CHKey, PersistentDataType.INTEGER, t - 1);
                        if(t-1 == 0){
                            List<String> lore = weapons.CorruptedHeart.getItemMeta().getLore();
                            meta.setLore(lore);
                        }
                        else {
                            List<String> lore = List.of("Times used: " + (t - 1));
                            meta.setLore(lore);
                        }
                        item.setItemMeta(meta);
                        p.getInventory().setItemInOffHand(item);
                        p.sendMessage("§a§l[Origin Depleter] §r§aYou have used Oblivions breath! Remaining pops: " + (t - 1));
                    }

                }
            }
        }
    });
    static Technique chunk = new Technique("chunk", "Oblivion chunk", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(15), List.of("Deals damage and applies chained and nightmare effects.")), TargetSelectors.self(), (ctx, token) ->{
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
                    t.addPotionEffect(PotionEffectType.WITHER.createEffect(2, 0));
                    t.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(2, 0));
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
                        try { t.damage(1000.0, player); } catch (Throwable ignored) {}
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    });

    static Technique slashes = new Technique("heavenly_slashes", "Heavenly rain of oblivion", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(30), List.of("Ascension attack dealing massive damage.")), TargetSelectors.self(), TechRegistry.getById("ascension_of_abundance").getAction());


    public static Technique present = new Technique("present", "Oblivion's present", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(25), List.of("Applies various buffs if the player has Origin.")), TargetSelectors.self(), (ctx, token) ->{
        Player p = ctx.caster();
        if (!p.isOnline()) return;
        TechRegistry.getById("darkworld").getAction().execute(new TechniqueContext(p), new SimpleCancellationToken());
        Bukkit.getScheduler().runTaskTimer(plugin, new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if(tick > 20*60) {
                    this.cancel();
                    return;
                }
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(soulEvents.hasSoul(p, 19)){
                        for(PotionEffect pe : p.getActivePotionEffects()) {
                            if(pe.getType().getCategory().equals(PotionEffectTypeCategory.HARMFUL)) p.removePotionEffect(pe.getType());
                        }
                        p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(200, 2));
                        p.addPotionEffect(PotionEffectType.SPEED.createEffect(200, 2));
                        p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(200, 2));
                        p.addPotionEffect(PotionEffectType.STRENGTH.createEffect(200, 2));
                    }
                }
            }
        }, 1, 0); // 10 seconds later
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

        }, 20*60); // 10 seconds later
        if(soulEvents.hasSoul(p, 19)){
            p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(200, 1));
            p.addPotionEffect(PotionEffectType.SPEED.createEffect(200, 1));
            p.addPotionEffect(PotionEffectType.REGENERATION.createEffect(200, 1));
            p.addPotionEffect(PotionEffectType.STRENGTH.createEffect(200, 1));
            hotbarMessage.sendHotbarMessage(p, "§a§l[Origin Depleter] §r§aYou have used Oblivions present!");
        }
    });


    private static final String ULT_ID = "oblivion_atomization";
    private static final Map<UUID, Integer> phase = new ConcurrentHashMap<>(); // 1=awaiting number,2=expanding,3=awaiting reactivation
    private static final Map<UUID, Integer> targetRadius = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> particleTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> timeoutTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> expansionTicks = new ConcurrentHashMap<>(); // helper if needed

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
                                    b.setType(Material.TINTED_GLASS, false);
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
                        // El anillo de corazones ha sido eliminado; no hay limpieza necesaria aquí.
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
                        for(Player p : player.getWorld().getPlayers()) {
                            if(p.equals(player)) continue;
                            if(soulEvents.hasSoul(p, 19)) return;
                            if(p.getLocation().distance(player.getLocation()) <= radius) {
                                for(int i = 0; i < 2; i++){
                                    p.damage(1000.0, player);
                                }

                            }
                        }
                    }, 1L);
                    // programar timeout de 1 minuto: si no se reactiva, quitar resistance y aplicar daño 5 veces (1 tick cada vez)
                    BukkitTask timeout = new BukkitRunnable() {
                        @Override
                        public void run() {
                            // limpiar anillos y quitar resistance
                            player.removePotionEffect(PotionEffectType.RESISTANCE);

                            // El anillo de corazones fue eliminado: no hay items que limpiar.

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
                    // El anillo de corazones ha sido eliminado del diseño: no se crean items.

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
    public static Technique ultimateTechnique = new Technique(
            ULT_ID,
            "Oblivion Nova",
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
                            double repeats = (level*(2.0/3.0));

                            final double times = repeats;
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
                    // El anillo de corazones no existe; no hay limpieza necesaria.

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

    public static Technique chaos = new Technique("chaos", "Original chaos", new TechniqueMeta(true, cooldownHelper.hour*24, List.of("Unleashes the chaos the universe hides.")), TargetSelectors.self(), (ctx, token) ->{
        Player p = ctx.caster();
        if (!p.isOnline()) return;
        Location center = p.getLocation().clone();
        World world = p.getWorld();
        hotbarMessage.sendHotbarMessage(p, "§a§l[Origin Depleter] §r§aYou have used Original chaos!");

        // 1) Recolectar jugadores en mismo mundo y dentro de 100 bloques (excluir caster)
        List<Player> trapped = new ArrayList<>();
        for (Player pl : world.getPlayers()) {
            if (pl.equals(p)) continue;
            try {
                if (pl.getLocation().distance(center) <= 100.0) trapped.add(pl);
            } catch (Throwable ignored) {}
        }

        // 2) Iniciar expansión del "agujero negro": +1 bloque por segundo (hasta 100)
        AtomicInteger radius = new AtomicInteger(0);
        BukkitTask expandTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) { cancel(); return; }
                int r = radius.incrementAndGet(); // aumenta 1 por segundo
                // partículas esféricas para visual
                int points = Math.max(40, (int)(Math.PI * 2 * Math.max(1, r) * 8));
                for (int i = 0; i < points; i++) {
                    double theta = Math.acos(2 * Math.random() - 1);
                    double phi = 2 * Math.PI * Math.random();
                    double x = Math.sin(theta) * Math.cos(phi);
                    double y = Math.cos(theta);
                    double z = Math.sin(theta) * Math.sin(phi);
                    Location loc = center.clone().add(x * r, y * r, z * r);
                    try { world.spawnParticle(Particle.SMOKE, loc, 0, 0, 0, 0, 0.02); } catch (Throwable ignored) {}
                }
                // límite de seguridad
                if (r >= 100) cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L); // cada 20 ticks = 1s

        // 3) Si hay jugadores, crear posiciones en círculo y mantenerlos ahí durante 3 segundos
        if (!trapped.isEmpty()) {
            final int n = trapped.size();
            final double trapRadius = 3.0;
            final List<Location> positions = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                positions.add(center.clone().add(Math.cos(angle) * trapRadius, 0.0, Math.sin(angle) * trapRadius));
            }

            BukkitTask trapTask = new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    // mantener teletransporte cada tick durante 60 ticks (3s)
                    if (tick >= 60) {
                        // 4) Tras 3s: eliminar ítems con "minecraft:determined" hasta sumar nivel >=5
                        Enchantment det = Enchantment.getByKey(NamespacedKey.minecraft("determined"));
                        for (Player t : trapped) {
                            if (t == null || !t.isOnline()) continue;
                            int accumulated = 0;
                            // MAIN INVENTORY
                            PlayerInventory inv = t.getInventory();
                            ItemStack[] contents = inv.getContents();
                            for (int s = 0; s < contents.length && accumulated < 5; s++) {
                                ItemStack it = contents[s];
                                if (it == null) continue;
                                int lvl = det == null ? 0 : it.getEnchantmentLevel(det);
                                if (lvl > 0) {
                                    accumulated += lvl;
                                    inv.setItem(s, null);
                                }
                            }
                            // ARMOR
                            ItemStack[] armor = inv.getArmorContents();
                            for (int a = 0; a < armor.length && accumulated < 5; a++) {
                                ItemStack it = armor[a];
                                if (it == null) continue;
                                int lvl = det == null ? 0 : it.getEnchantmentLevel(det);
                                if (lvl > 0) {
                                    accumulated += lvl;
                                    armor[a] = null;
                                }
                            }
                            inv.setArmorContents(armor);
                            // OFFHAND
                            if (accumulated < 5) {
                                ItemStack off = inv.getItemInOffHand();
                                if (off != null) {
                                    int lvl = det == null ? 0 : off.getEnchantmentLevel(det);
                                    if (lvl > 0) {
                                        accumulated += lvl;
                                        inv.setItemInOffHand(null);
                                    }
                                }
                            }
                            // Actualizar inventario del jugador
                            try { t.updateInventory(); } catch (Throwable ignored) {}
                        }
                        // limpiar: detener expansión si sigue activa y cancelar tarea
                        try { if (expandTask != null) expandTask.cancel(); } catch (Throwable ignored) {}
                        cancel();
                        TechRegistry.unregisterTechnique(TECH_ID, TechRegistry.getById("chaos"));
                        return;
                    }

                    // teletransportar y aplicar efectos para "atrapar"
                    for (int i = 0; i < trapped.size(); i++) {
                        Player t = trapped.get(i);
                        if (t == null || !t.isOnline()) continue;
                        Location pos = positions.get(i).clone().add(0, 0.5, 0);
                        try { t.teleport(pos); } catch (Throwable ignored) {}
                        // efectos breves (se reaplican cada tick)
                        try {
                            t.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(40, 10)); // fuerte slow
                            t.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.WITHER.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.DARKNESS.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.MINING_FATIGUE.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.HUNGER.createEffect(40, 1));
                            t.addPotionEffect(PotionEffectType.NAUSEA.createEffect(40, 1));
                        } catch (Throwable ignored) {}
                    }
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L); // cada tick
        } else {
            // si no hay jugadores atrapados, detener la expansión tras 5s de demostración
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try { expandTask.cancel(); } catch (Throwable ignored) {}
            }, 20L * 5L);
        }
    });
}
