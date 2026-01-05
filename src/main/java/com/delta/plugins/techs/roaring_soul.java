package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.delta.plugins.items.Items;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class roaring_soul implements Listener {
    static final String TECH_ID = "roaring_soul";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static final Map<UUID, Integer> phase = new HashMap<>();
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, geno);
        TechRegistry.registerTechnique(TECH_ID, fallingStars);
        TechRegistry.registerTechnique(TECH_ID, darkWorld);
        TechRegistry.registerTechnique(TECH_ID, cooldownFucker);
        TechRegistry.registerTechnique(TECH_ID, explosionBox);
        TechRegistry.registerTechnique(TECH_ID, roaringgowhacka);
        Plugin.registerAbyssID(TECH_ID);
    }

    // --- GENO ---
    static Technique geno = new Technique(
        "geno",
        "Dreaded Darkness",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Teleport behind a target and apply blindness.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            // 1. Find random target within 200 blocks
            Player target = getClosestPlayer(player.getLocation());
            if(target == null) {
                player.sendMessage("No target found within 200 blocks.");
                return;
            }
            if(target.getLocation().distance(player.getLocation()) > 200) {
                player.sendMessage("No target found within 200 blocks.");
                return;
            }

            // 2. Apply blindness 255 for 5s, slowness 255 for 1s
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 254, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 2, 254, false, false, false));

            // 3. Teleport user 2 blocks behind target, facing them
            Location behind = target.getLocation().clone();
            behind.setDirection(target.getLocation().getDirection().multiply(-1));
            behind = behind.add(behind.getDirection().normalize().multiply(2));
            behind.setYaw(target.getLocation().getYaw() + 180);
            player.teleport(behind);
            player.teleport(player.getLocation().setDirection(target.getLocation().subtract(player.getLocation()).toVector()));

            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> CooldownManager.setCooldown(player, "geno", cooldownHelper.minutesToMiliseconds(5)), 2);
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), true), 7);
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> events.hasGenoDamage.put(player.getUniqueId(), false), 18 + (Familiar_love.OstiacionActive.getOrDefault(player, false) ? 10 : 0));
        }
    );

    // --- DARK WORLD ---
    static Technique darkWorld = new Technique(
        "darkworld",
        "Dark World",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Create a tinted-glass sphere and debuff players inside.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
            int maxRadius = (events.hasChaosHeart(player) ? 70 : 50);
            Set<Block> sphereBlocks = new HashSet<>();
            Set<BlockState> replacedBlocks = new HashSet<>();

            // Dar visión nocturna y fuerza por 1 minuto
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, 1, false, false, false));

            // Iniciar el efecto Dark World
            startDarkWorldEffect(player, center, maxRadius, sphereBlocks, replacedBlocks);
        }
    );

    private static void startDarkWorldEffect(Player user, Location center, int radius, Set<Block> sphereBlocks, Set<BlockState> replacedBlocks) {
        World world = center.getWorld();
        Set<Player> affectedPlayers = new HashSet<>();

        // --- NUEVO: construir la esfera de TINTED_GLASS y guardar los BlockState originales ---
        Set<Block> sphere = sphereAround(center, radius);
        for (Block b : sphere) {
            Material m = b.getType();
            if (m == Material.AIR || m == Material.WATER || m == Material.BUBBLE_COLUMN || m == Material.LIGHT) {
                // Guardar estado original y marcar bloque para restauración
                replacedBlocks.add(b.getState());
                sphereBlocks.add(b);
                // Poner vidrio tintado sin física
                b.setType(Material.TINTED_GLASS, false);
            }
        }
        // --- FIN: creación de la esfera ---

        // 1. Apply effects and start repeating task
        BukkitRunnable effectTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // Apply effects to players in radius except user
                for (Player p : world.getPlayers()) {
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    if (!p.equals(user) && p.getLocation().distance(center) <= radius) {
                        int defaultlvl = 0;
                        if(events.hasChaosHeart(user) && !events.hasPurityHeart(p)) defaultlvl += 1;

                        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, defaultlvl, false, false, true));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, defaultlvl + 1, false, false, true));

                        affectedPlayers.add(p);
                    }
                }
                // Remove enderpearls not from user
                for (Entity e : world.getEntities()) {
                    if (e instanceof EnderPearl) {
                        EnderPearl ep = (EnderPearl) e;
                        if (!(ep.getShooter() instanceof Player) || !ep.getShooter().equals(user)) {
                            Player p = (Player) ep.getShooter();
                            if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                            if(events.hasPurityHeart(p)) return;
                            e.remove();
                        }
                    }
                }
                ticks += 2;
                for(Player p : affectedPlayers){
                    if(ticks <31 && p.getLocation().distance(center) >= radius){
                        p.teleport(center);
                    }
                }
                if (ticks >= 20 * 60) { // 1 minute
                    // --- RESTAURAR BLOQUES ---
                    for (BlockState bs : replacedBlocks) {
                        try {
                            // Restaurar estado original sin física
                            bs.update(true, false);
                        } catch (Exception ignored) {}
                    }
                    replacedBlocks.clear();
                    sphereBlocks.clear();
                    // Cancelar tarea
                    this.cancel();
                }
            }
        };
        effectTask.runTaskTimer(plugin, 2L, 2L);
    }
    // --- FALLING STARS ---
    static Technique fallingStars = new Technique(
        "fallingstars",
        "Falling Stars",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Call falling obsidian shards that explode on impact.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location center = player.getLocation().clone();
            World world = center.getWorld();
            int radius = 30; // Set radius to 10 blocks
            Random rand = new Random();
            int stars = ((events.hasChaosHeart(player)) ? 40 : 30) + rand.nextInt(11);
            int speedX = rand.nextInt(-1, 1);
            int speedZ = rand.nextInt(-1, 1);
            int speedY = rand.nextInt(-1, 7);
            new BukkitRunnable() {
                int count = 0;
                @Override
                public void run() {
                    if (count >= stars) {
                        this.cancel();
                        return;
                    }
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double dist = 5 + rand.nextDouble() * (radius - 5); // Adjust distance based on radius
                    double x = center.getX() + Math.cos(angle) * dist;
                    double z = center.getZ() + Math.sin(angle) * dist;
                    double y = center.getY() + 30 + rand.nextDouble() * 10;
                    Location spawnLoc = new Location(world, x, y, z);

                    // Visual: falling ender crystal (use falling block for effect)
                    FallingBlock fb = world.spawnFallingBlock(spawnLoc, Material.OBSIDIAN.createBlockData());
                    Vector v = fb.getVelocity();
                    v.setX(speedX);
                    v.setZ(speedZ);
                    v.setY(-speedY);
                    fb.setVelocity(v);
                    spawnLoc.setX(spawnLoc.getX() + speedX);
                    spawnLoc.setZ(spawnLoc.getZ() + speedZ);
                    // Damage on landing
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location landLoc = spawnLoc.clone();
                            landLoc.setY(center.getY());
                            for (Player p : world.getPlayers()) {
                                if (!p.equals(player)
                                        && p.getLocation().distance(center) <= radius
                                        && p.getLocation().distance(landLoc) < 3
                                        && !PlayerTechniqueManager.isInmune(p.getUniqueId())) {
                                    p.damage((events.hasPurityHeart(p) ? 3 : 7), player);

                                }
                            }
                            landLoc.getBlock().setType(Material.AIR);
                            if(fb.isValid()) {
                                fb.remove();
                            }
                            player.getWorld().createExplosion(landLoc, 7.0f, false, false);
                        }
                    }.runTaskLater(plugin, 25L); // ~1.25s fall time

                    count++;
                }
            }.runTaskTimer(plugin, 0L, 6L);
        }
    );

    // --- EXPLOSION BOX (Magic Trap) ---
    static Technique explosionBox = new Technique(
        "blowupbox",
        "Magic Trap",
        new TechniqueMeta(true, cooldownHelper.secondsToMiliseconds(180), List.of("Creates a barrier box and triggers explosions.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            //creates a box of barriers around the closest player 10 ticks after used, then 5 explosions of power 60 happen in the box (each 10 ticks appart and starting 40 ticks after the box is created)
            World world = player.getWorld();
            Player target = null;
            double minDist = 200;
            for (Player p : world.getPlayers()) {
                if(PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                if (!p.equals(player)) {
                    double dist = p.getLocation().distance(player.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        target = p;
                    }
                }
            }
            if (target == null) {
                hotbarMessage.sendHotbarMessage(player, "No hay jugadores cercanos para atrapar.");
                return;
            }

            Location loc = target.getLocation().getBlock().getLocation().add(0.5, -1, 0.5);
            int radius = (new Random()).nextInt(2, 6);
            int height = 5;
            Set<Block> replaced = new HashSet<>();
            if(!events.hasPurityHeart(target)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100 * 10, 255, false, false, false));
            }
            // Crear la caja de barreras después de 10 ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int y = -1; y < height; y++) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            boolean isEdge = Math.abs(x) == radius || Math.abs(z) == radius || y == 0 || y == height - 1;
                            if (isEdge) {
                                Block b = loc.clone().add(x, y, z).getBlock();
                                if (b.getType() == Material.AIR || b.getType() == Material.SHORT_GRASS || b.getType() == Material.TALL_GRASS || b.getType() == Material.WATER || b.getType() == Material.BUBBLE_COLUMN || b.getType() == Material.LAVA) {
                                    replaced.add(b);
                                    b.setType(Material.BARRIER, false);
                                }
                            }
                        }
                    }
                }

                Location blowupLoc = loc.getBlock().getLocation().add(0.5, 2, 0.5);
                // Explosiones: 5 explosiones, cada 10 ticks, comenzando 40 ticks después de la caja
                for (int i = 0; i < 5; i++) {
                    int delay = 40 + (i * 10);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        world.createExplosion(blowupLoc, 5F, false, false, player);
                    }, delay);
                }

                // Limpiar la caja después de 80 ticks tras la última explosión
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Block b : replaced) {
                        b.setType(Material.AIR, false);
                    }
                }, 40 + 5 * 10 + 80);

            }, 10);

            hotbarMessage.sendHotbarMessage(player, "You have set a Magic Trap!");
        }
    );

    // --- COOLDOWN FUCKER ---
    static Technique cooldownFucker = new Technique(
        "cooldownfunny",
        "Technique Fucker",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Sets cooldowns on other players' techniques.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Player p = getClosestPlayer(player.getLocation());
            List<Player> players = new ArrayList<>();

            if(events.hasChaosHeart(player)){
                int rng = (new Random()).nextInt(0, 100);
                if(rng < 25){
                    players.add(p);
                }
                else {
                    players.addAll(end_boss.getClosestPlayers(player.getLocation(), 2));
                }
            }
            else {
                players.add(p);
            }
            if(p.equals(player)) return;
            if(p.getLocation().distance(player.getLocation())>30) return;
            int cooldown = ((new Random()).nextInt(60, 81))*1000;
            for(String id :com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                for(Technique t : TechRegistry.getNormalTechniques(id)){
                    int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                    for(Player pl : players){
                        CooldownManager.setCooldown(pl, t.getId(), events.hasPurityHeart(p)? cooldownFinal/2 : cooldownFinal);
                    }
                }
            }
            for(String id : Plugin.getAllAbyssIDs()){
                for(Technique t : TechRegistry.getNormalTechniques(id)){
                    int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                    for(Player pl : players){
                        CooldownManager.setCooldown(pl, t.getId(), events.hasPurityHeart(p)? cooldownFinal/2 : cooldownFinal);
                    }
                }
            }

            hotbarMessage.sendHotbarMessage(player, "You have set all techniques of " + ((players.size() > 1) ? (players.get(0).getName() + "and" + players.get(1).getName()) : (p.getName())) + " on cooldown for " + (cooldown/1000) + " seconds!" );
        }
    );

    // --- ROARING GOW HACKA (multi-phase) ---
    static Technique roaringgowhacka = new Technique(
        "roaringgowhacka",
        "Day of the Roaring of Darkness",
        new TechniqueMeta(true, cooldownHelper.hour * 3, List.of("Two-phase world-ending ritual.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            UUID uuid = player.getUniqueId();
            Integer currentPhase = phase.get(uuid);
            List<Location> savedExplosions = new ArrayList<>();
            Location phase1Center = player.getLocation().clone();

            if (currentPhase == null || currentPhase == 2) {
                phase.put(uuid, 1);
            } else if (currentPhase == 1) {
                phase.put(uuid, 2);
            }

            int phaseNow = phase.get(uuid);

            if (phaseNow == 1) {
                // Summon pure hearts and chaos heart instantly (no particles)
                Location animLoc = player.getLocation().clone();
                ItemStack[] pureHearts = {
                    Items.pureheart_red,
                    Items.pureheart_orange,
                    Items.pureheart_yellow,
                    Items.pureheart_green,
                    Items.pureheart_blue,
                    Items.pureheart_indigo,
                    Items.pureheart_purple,
                    Items.pureheart_white
                };
                List<Item> spawnedItems = new ArrayList<>();
                World world = animLoc.getWorld();
                Vector backDir = animLoc.getDirection().normalize().multiply(-1);
                Location circleCenter = animLoc.clone().add(backDir);

                for (int i = 0; i < 8; i++) {
                    double angle = 2 * Math.PI * i / 8;
                    double radius = 1.5;
                    double x = circleCenter.getX() + radius * Math.cos(angle);
                    double y = circleCenter.getY();
                    double z = circleCenter.getZ() + radius * Math.sin(angle);
                    Location itemLoc = new Location(world, x, y, z);

                    ItemStack stack = pureHearts[i].clone();
                    Item itemEntity = world.dropItem(itemLoc, stack);
                    itemEntity.setPickupDelay(Integer.MAX_VALUE);
                    itemEntity.setGravity(false);
                    itemEntity.setVelocity(new Vector(0, 0, 0));
                    spawnedItems.add(itemEntity);
                }

                // Chaos heart above player
                Location chaosLoc = animLoc.clone().add(0, 2.2, 0);
                ItemStack chaosHeart = com.rschao.items.Items.ChaosHeart.clone();
                Item chaosEntity = world.dropItem(chaosLoc, chaosHeart);
                chaosEntity.setPickupDelay(Integer.MAX_VALUE);
                chaosEntity.setGravity(false);
                chaosEntity.setVelocity(new Vector(0, 0, 0));
                spawnedItems.add(chaosEntity);

                List<String> dialogue = List.of(
                    "The world shall be engulfed in darkness...",
                    "Feel the roar of the abyss!",
                    "Embrace the chaos within!",
                    "Let the shadows consume you!",
                    "The end is nigh, and darkness reigns!"
                );

                for(int i = 0; i < dialogue.size(); i++) {
                    String line = dialogue.get(i);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : player.getWorld().getPlayers()) {
                            if(p.getLocation().distance(player.getLocation()) <= 20) p.sendMessage(line);
                        }
                    }, i * 20L); // 2 seconds apart
                }

                // After 40 ticks, do the explosion cascade
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Random rand = new Random();
                    int locCount = rand.nextInt(20, 41); // 30-50
                    List<Location> locs = new ArrayList<>();
                    Location base = player.getLocation();
                    for (int i = 0; i < locCount; i++) {
                        double angle = rand.nextDouble() * 2 * Math.PI;
                        double dist = rand.nextDouble() * 20;
                        double x = base.getX() + Math.cos(angle) * dist;
                        double z = base.getZ() + Math.sin(angle) * dist;
                        double y = base.getY();
                        locs.add(new Location(base.getWorld(), x, y, z));
                    }

                    List<Location> explosionLocs = new ArrayList<>();
                    for (Location loc : locs) {
                        Location found = null;
                        World w = loc.getWorld();
                        int baseY = loc.getBlockY();
                        // Search up and down for air block within 10 blocks
                        for (int dy = 0; dy <= 10; dy++) {
                            Location up = loc.clone().add(0, dy, 0);
                            Location down = loc.clone().add(0, -dy, 0);
                            if (w.getBlockAt(up).getType() == Material.AIR) {
                                found = up;
                                break;
                            }
                            if (w.getBlockAt(down).getType() == Material.AIR) {
                                found = down;
                                break;
                            }
                        }
                        if (found == null) {
                            // fallback: highest block at x,z
                            int highestY = w.getHighestBlockYAt(loc);
                            found = new Location(w, loc.getX(), highestY, loc.getZ());
                        }
                        explosionLocs.add(found);
                    }


                    for (Location exLoc : explosionLocs) {
                        int power = 3 * (rand.nextInt(4) + 1);
                        World exworld = exLoc.getWorld();
                        exworld.createExplosion(exLoc, power, false, false, player);
                        savedExplosions.add(exLoc.clone());

                        // Reemplazado: en lugar de generar partículas periódicas, spawnea un ítem 5 bloques encima
                        Location itemSpawn = exLoc.clone().add(0, 5, 0);
                        ItemStack stack = Items.gold_whacka_bump.clone();
                        Item dropped = exworld.dropItem(itemSpawn, stack);
                        dropped.setVelocity(new Vector(0, 0, 0));
                        dropped.setUnlimitedLifetime(true);
                        dropped.setPickupDelay(Integer.MAX_VALUE);
                        dropped.setGravity(false);
                    }
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            CooldownManager.setCooldown(player, "roaringgowhacka", 0);
                        }
                    }.runTaskLater(plugin, 2);
                }, 40 + (dialogue.size() * 20L)); // after dialogue

            } else if (phaseNow == 2) {
                // --- PHASE 2 LOGIC START ---
                // 1. Remove phase data so particle cascades stop, then re-add after 1 tick
                phase.remove(uuid);
                Bukkit.getScheduler().runTaskLater(plugin, () -> phase.put(uuid, 2), 1L);

                // 2. Respawn chaos heart at the original center
                Location chaosLoc = phase1Center.clone().add(0, 2.2, 0);
                ItemStack chaosHeart = com.rschao.items.Items.ChaosHeart.clone();
                World world = chaosLoc.getWorld();
                Item chaosEntity = world.dropItem(chaosLoc, chaosHeart);
                chaosEntity.setPickupDelay(Integer.MAX_VALUE);
                chaosEntity.setGravity(false);
                chaosEntity.setVelocity(new Vector(0, 0, 0));

                List<String> dialogue = List.of(
                        "And now, for the grand finale...",
                        "Let us all end our games...",
                        "Let this wicked power consume the world!",
                        "With my life as sacrifice, let us end this",
                        "I cast Roar of Darkness!"
                );

                for(int i = 0; i < dialogue.size(); i++) {
                    String line = dialogue.get(i);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : player.getWorld().getPlayers()) {
                            if(p.getLocation().distance(player.getLocation()) <= 20) p.sendMessage(line);
                        }
                    }, i * 20L); // 2 seconds apart
                }

                // 4. After 20 ticks, destroy chaos heart and damage nearby players
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    chaosEntity.remove();

                    // Find all players within 20 blocks of phase1Center, except user
                    List<Player> affected = new ArrayList<>();
                    for (Player p : world.getPlayers()) {
                        if (!p.equals(player) && p.getLocation().distance(phase1Center) <= 20 && !PlayerTechniqueManager.isInmune(p.getUniqueId())) {
                            affected.add(p);
                        }
                    }

                    // For each, save resistance effect if any (unless amplifier >= 5), remove it, then damage for 499
                    Map<UUID, PotionEffect> resistanceMap = new HashMap<>();
                    for (Player p : affected) {
                        PotionEffect eff = p.getPotionEffect(PotionEffectType.RESISTANCE);
                        if (eff != null && eff.getAmplifier() < 5) {
                            resistanceMap.put(p.getUniqueId(), eff);
                            p.removePotionEffect(PotionEffectType.RESISTANCE);
                        }
                        // else: skip removal if amplifier >= 5
                        p.damage(499, player);
                    }

                    // 20 ticks later, damage again and restore resistance if needed, then remove phase data
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : affected) {
                            p.damage(499, player);
                            PotionEffect eff = resistanceMap.get(p.getUniqueId());
                            if (eff != null) {
                                p.addPotionEffect(eff);
                            }
                        }
                        phase.remove(uuid);
                    }, 20L);

                    // 5. Apply Technique Fucker effect to user, doubled cooldown, skip "Fly me to the moon"
                    Player p = player;
                    List<Player> players = new ArrayList<>();
                    players.add(p);
                    int cooldown = ((new Random()).nextInt(120, 161)) * 1000; // double original
                    for (String id : com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()) {
                        for (Technique t : TechRegistry.getAllTechniques(id)) {
                            if (t.getDisplayName().equalsIgnoreCase("Fly me to the moon")) continue;
                            int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                            for (Player pl : players) {
                                CooldownManager.setCooldown(pl, t.getId(), cooldownFinal);
                            }
                        }
                    }
                    for (String id : Plugin.getAllAbyssIDs()) {
                        for (Technique t : TechRegistry.getAllTechniques(id)) {
                            int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                            for (Player pl : players) {
                                CooldownManager.setCooldown(pl, t.getId(), cooldownFinal);
                            }
                        }
                    }

                    // 6. Damage user for 499
                    player.damage(499, player);

                }, 20L + (dialogue.size() * 20L)); // after dialogue

                // --- PHASE 2 LOGIC END ---
            }
        }
    );

    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != location.getWorld()) continue; // Skip players in different worlds
            if(PlayerTechniqueManager.isInmune(player.getUniqueId())) continue;
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;

            }
        }

        return closestPlayer;
    }

    public static Set<Block> sphereAround(Location location, int radius) {
        Set<Block> sphere = new HashSet<Block>();
        Block center = location.getBlock();
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    Block b = center.getRelative(x, y, z);
                    double dist = center.getLocation().distance(b.getLocation());
                    if(dist <= radius && dist > (radius - 2)) {
                        sphere.add(b);
                    }
                }
            }
        }
        return sphere;
    }
    
}
