package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.TechFlagEvents;
import com.delta.plugins.events.events;
import com.rschao.api.SoulType;
import com.rschao.events.soulEvents;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cancel.CancelReason;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ShowdownMan {
    static final String ID = "showdown_man";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    static Map<Player, Integer> jaronas = new HashMap<>();
    public static Map<Player, Boolean> jaronaUltis = new HashMap<>();

    private static void playTechniqueSound(Player player, String sound, float volume, float pitch) {
        if (jaronaUltis.getOrDefault(player, false)) {
            player.playSound(player, "minecraft:jarona_last", SoundCategory.MASTER, volume, pitch);
        } else {
            player.playSound(player, sound, volume, pitch);
        }
    }

    public static void register(){
        TechRegistry.registerTechnique(ID, jarona);
        TechRegistry.registerTechnique(ID, randomHand);
        TechRegistry.registerTechnique(ID, darkWorld);
        TechRegistry.registerTechnique(ID, integrity);
        TechRegistry.registerTechnique(ID, jaronaRegen);
        TechRegistry.registerTechnique(ID, orbitalBravery);
        TechRegistry.registerTechnique(ID, justice_sacrifice);
        TechRegistry.registerTechnique(ID, last_jarona);
    }

    static Technique jarona = new Technique(
            "jarona",
            "JARONA",
            new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(60), List.of("3-use Dragon onslaught", "does not allow proflight")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                int jaronaCount = jaronas.getOrDefault(player, 0);
                Location location = player.getLocation();
                player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, location, 30);
                playTechniqueSound(player, "minecraft:jarona" + (new Random().nextInt(4)+1), 1, 1);

                for (org.bukkit.entity.Entity entity : location.getWorld().getEntities()) {
                    if(entity.getWorld() != player.getWorld()) continue;
                    if (entity.getLocation().distance(location) <= 20 && entity != player) {
                        if ((entity instanceof Player)) {
                            Player target = (Player) entity;
                            String soundToPlay = jaronaUltis.getOrDefault(player, false) ? "minecraft:jarona_last" : "minecraft:jarona" + (new Random().nextInt(0, 4)+1);
                            target.playSound(target, soundToPlay, 1, 1);
                            target.damage(30);
                        }
                        Vector direction = entity.getLocation().toVector().subtract(location.toVector()).normalize();
                        entity.setVelocity(direction.multiply(3));
                    }
                }
                Vector direction = player.getLocation().getDirection();
                player.setVelocity(direction.multiply(4));
                if(jaronaCount == 3){
                    jaronas.put(player, 0);
                }
                else {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        CooldownManager.removeCooldown(ctx.caster(), "jarona");
                    }, 20);
                }
            }
    );

    static Technique randomHand = new Technique("perseverance_emblem", "Emblem of Perseverance", false, cooldownHelper.minutesToMiliseconds(2), List.of("Uses a random base hand", "ignores cooldown for the hands"), TargetSelectors.self(), (ctx, token) ->{
        final String group_id = "hands";

        List<Technique> techs = TechRegistry.getAllTechniques(group_id);
        int random = new Random().nextInt(0, techs.size());
        Technique t = techs.get(random);
        ctx.caster().sendMessage("Using " + t.getDisplayName());
        playTechniqueSound(ctx.caster(), "minecraft:lend_power", 1, 1);
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            t.getAction().execute(ctx, token);
        }, 20);
    });


    static Technique darkWorld = new Technique(
            "kindness_flare",
            "Kindness Flare",
            new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Create golden aura and buff nearby players (except corrupted soul users).")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                Location center = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                int maxRadius = (events.hasChaosHeart(player) ? 70 : 50);
                Set<Block> sphereBlocks = new HashSet<>();
                Set<BlockState> replacedBlocks = new HashSet<>();

                // Dar visión nocturna y fuerza por 1 minuto
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, (events.hasChaosHeart(player)? 5 : 4), false, false, false));

                playTechniqueSound(player, "minecraft:lend_power", 1, 1);

                // Iniciar el efecto Dark World
                startDarkWorldEffect(player, center, maxRadius, sphereBlocks, replacedBlocks);
            }
    );

    private static void startDarkWorldEffect(Player user, Location center, int radius, Set<Block> sphereBlocks, Set<BlockState> replacedBlocks) {
        World world = center.getWorld();
        Set<Player> affectedPlayers = new HashSet<>();

        Set<Block> sphere = roaring_soul.sphereAround(center, radius);
        for (Block b : sphere) {
            Material m = b.getType();
            if (m == Material.AIR || m == Material.WATER || m == Material.BUBBLE_COLUMN || m == Material.LIGHT) {
                // Guardar estado original y marcar bloque para restauración
                replacedBlocks.add(b.getState());
                sphereBlocks.add(b);
                // Poner vidrio tintado sin física
                b.setType(Material.GREEN_STAINED_GLASS, false);
            }
        }

        BukkitRunnable effectTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // Apply effects to players in radius except user
                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distance(center) <= radius) {
                        // Excluir jugadores inmunes
                        if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        int defaultlvl = 3;
                        if(events.hasChaosHeart(user) && !events.hasPurityHeart(p)) defaultlvl += 1;
                        if(p.hasPermission("gaster.boss")) continue;

                        int soul1 = soulEvents.GetSoulN(p);
                        int soul2 = soulEvents.GetSecondSoulN(p);

                        SoulType soulType1 = SoulType.getById(soul1);
                        SoulType soulType2 = SoulType.getById(soul2);

                        if(!p.equals(user) && ((soulType1.getTier() == 3 || soulType1.equals(SoulType.CHAOSHEART)|| soulType1.equals(SoulType.DARKNESS)) || (soulType2.getTier() == 3 || soulType2.equals(SoulType.CHAOSHEART) || soulType1.equals(SoulType.DARKNESS)))){
                            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, defaultlvl, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, defaultlvl, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, defaultlvl + 1, false, false, true));
                        } else {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, defaultlvl+1, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, defaultlvl, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, defaultlvl, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, defaultlvl, false, false, true));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, defaultlvl, false, false, true));
                        }
                        affectedPlayers.add(p);
                    }
                }
                // Remove enderpearls not from user, and all shulker bullets
                for (Entity e : world.getEntities()) {
                    if (e instanceof EnderPearl) {
                        EnderPearl ep = (EnderPearl) e;
                        if (!(ep.getShooter() instanceof Player) || !ep.getShooter().equals(user)) {
                            if (ep.getShooter() instanceof Player) {
                                Player p = (Player) ep.getShooter();
                                // Excluir inmunes y pure heart
                                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                                if(events.hasPurityHeart(p)) continue;
                            }
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
        effectTask.runTaskTimer(Plugin.getPlugin(Plugin.class), 2L, 2L);
    }

    static Technique integrity = new Technique("roaring_integrity", "Roar of Integrity", false, cooldownHelper.minutesToMiliseconds(6), List.of("Causes several pops. Attracts nearby enemies"), TargetSelectors.radialPlayers(20), (ctx, token) ->{
        Player player = ctx.caster();
        Location location = player.getLocation();
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, location, 30);
        playTechniqueSound(player, "minecraft:lend_power", 1, 1);

        for (Entity entity : ctx.targets()) {
            if (entity.getLocation().distance(location) <= 20 && entity != player) {
                Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(5)); // Adjust speed as needed
                if(entity instanceof Player pl){
                    Bukkit.getScheduler().runTaskLater(plugin, () ->{
                        pl.damage(1000);
                    }, 20);
                    Bukkit.getScheduler().runTaskLater(plugin, () ->{
                        pl.damage(1000);
                    }, 24);
                }
            }
        }

    });

    static Technique jaronaRegen = new Technique("patience_drain", "Patience drain", false, cooldownHelper.minutesToMiliseconds(10), List.of("Allows the user to regenerate health from players around him"), TargetSelectors.self(), (ctx, token)->{
        TechFlagEvents.patienceDrain.put(ctx.caster().getUniqueId(), true);
        playTechniqueSound(ctx.caster(), "minecraft:lend_power", 1, 1);
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            TechFlagEvents.patienceDrain.remove(ctx.caster().getUniqueId());
        }, 20*60*5);
    });

    static Technique orbitalBravery = new Technique("orange_boom", "Bravery Strike", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(6), List.of("Calls down an orbital strike on a target location.")), TargetSelectors.self(), (techUser, token) -> {
        Player player = techUser.caster();
        player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(20*90, 4));
        playTechniqueSound(player, "minecraft:lend_power", 1, 1);
        // centro de la strike (se usa la posición original del jugador)
        Location center = player.getLocation().clone();
        World world = center.getWorld();
        if (world == null) return;
        // radios a usar para partículas y explosiones
        double[] radii = {3.0, 50.0};

        // tarea que genera partículas en círculo durante 2 segundos (40 ticks), ejecutando cada 2 ticks
        new BukkitRunnable() {
            int runs = 0;
            final double angleStep = Math.PI / 10.0;
            @Override
            public void run() {
                if (runs >= 20) { // 20 ejecuciones * 2 ticks = 40 ticks = 2s
                    cancel();
                    // después de 2 segundos: crear explosiones en cada ángulo para ambos radios
                    for (double angle = 0.0; angle < Math.PI * 2.0 - 1e-6; angle += angleStep) {
                        for (double r : radii) {
                            double x = Math.cos(angle) * r;
                            double z = Math.sin(angle) * r;
                            Location expLoc = center.clone().add(x, 0, z);
                            // power 100, setFire=false, breakBlocks=true (destructivo)
                            world.createExplosion(expLoc, 10f, false, true, player);
                        }
                    }
                    // quitar el efecto de slow falling poco después (para que el jugador vuelva a caer con normalidad)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    }, 20L);
                    return;
                }

                // generar partículas para ambos radios en la misma Y que el centro
                double y = center.getY();
                for (double angle = 0.0; angle < Math.PI * 2.0 - 1e-6; angle += angleStep) {
                    for (double r : radii) {
                        double x = Math.cos(angle) * r;
                        double z = Math.sin(angle) * r;
                        Location pLoc = new Location(world, center.getX() + x, y, center.getZ() + z);
                        world.spawnParticle(Particle.FIREWORK, pLoc, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }

                runs++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.sendMessage("You have activated Bravery Strike (charging)!");
    });

    static Technique justice_sacrifice = new Technique(
            "justice_sacrifice",
            "Justice Sacrifice",
            new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Temporarily enchant swords.")),
            TargetSelectors.self(),
            (ctx, token) -> {
                Player player = ctx.caster();
                Whacka_abyss.subtractHealthWhacka(player);
                playTechniqueSound(player, "minecraft:lend_power", 1, 1);
                for(ItemStack item : player.getInventory().getContents()) {
                    if(item == null) continue;
                    if(item.getType().toString().contains("SWORD")) {
                        ItemMeta meta = item.getItemMeta();
                        ItemMeta meta2 = item.getItemMeta();
                        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("oblivion")), 100, true);
                        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("wither")), 100, true);
                        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("glitch")), 100, true);
                        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("geno")), 100, true);
                        item.setItemMeta(meta);
                        TechFlagEvents.yellowHits.put(player.getUniqueId(), 0);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                if(TechFlagEvents.yellowHits.getOrDefault(player.getUniqueId(), 0) >= 3){
                                    item.setItemMeta(meta2);
                                    TechFlagEvents.yellowHits.remove(player.getUniqueId());
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0, 1);
                        return;
                    }
                }
                hotbarMessage.sendHotbarMessage(player, ChatColor.YELLOW + "You have used the Justice Sacrifice technique");
            }
    );

    static Technique last_jarona = new Technique("supreme:omega_showdown", "Supreme Magic: Omega Showdown Man", true, cooldownHelper.hour, List.of("Temporarily grants infinite usage of techniques in this abyss"), TargetSelectors.self(), (ctx, token) ->{
        jaronaUltis.put(ctx.caster(), true);
        Player player = ctx.caster();
        player.playSound(player, "minecraft:powers_combined", 1, 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> jaronaUltis.remove(ctx.caster()), 20*30);
    });
}
