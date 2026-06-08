package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Void {

    static final String id = "void";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register() {
        Bukkit.getPluginManager().registerEvents(new Events(), plugin);

        TechRegistry.registerTechnique(id, voidTech);
    }


    static Technique voidTech = new Technique("void", "Void Termina", new TechniqueMeta(true, 0, List.of("Ends all worlds")), TargetSelectors.radialPlayers(500), (ctx, token) -> {
        // Prepare targets (make mutable and include caster)
        HashSet<LivingEntity> targets = new HashSet<>(ctx.targets());
        targets.add(ctx.caster());
        ctx.setTargets(targets);

        // center location (caster)
        if (!(ctx.caster() instanceof Player casterPlayer)) return;
        final Player caster = casterPlayer;
        final Location center = caster.getLocation().clone();
        final World world = center.getWorld();

        // Teleport all targets to caster
        for (LivingEntity t : targets) {
            try {
                if(t instanceof Player p) {
                    for(ItemStack tumadre : p.getInventory().getContents()){
                        if(tumadre != null && (tumadre.getType().equals(Material.BLAZE_POWDER) || tumadre.getType().equals(Material.HEART_OF_THE_SEA))) p.getInventory().remove(tumadre);
                    }
                } else t.teleport(center);
            } catch (Exception ignored) {                                                                                                                                }
        }

        // 1) Charge up particles for 3 seconds (60 ticks)
        final int chargeTicks = 60;
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                // draw a small particle charge at caster location
                world.spawnParticle(Particle.END_ROD, center, 50, 0.5, 1.0, 0.5, 0.02);
                world.spawnParticle(Particle.DUST, center, 20, 0.5, 0.5, 0.5, 0.02, new Particle.DustOptions(Color.BLACK, 1));
                tick++;
                if (tick >= chargeTicks) {
                    this.cancel();
                    // Launch beam up 40 blocks then start sphere
                    launchBeamAndStartSphere();
                }
            }

            private void launchBeamAndStartSphere() {
                // Create an upward beam of particles for 40 blocks
                new BukkitRunnable() {
                    int step = 0;
                    final int maxSteps = 40;

                    @Override
                    public void run() {
                        double y = center.getY() + step;
                        Location loc = new Location(world, center.getX(), y, center.getZ());
                        world.spawnParticle(Particle.END_ROD, loc, 100, 0.2, 0.2, 0.2, 0.05);
                        world.spawnParticle(Particle.DUST, loc, 20, 0.5, 0.5, 0.5, 0.02, new Particle.DustOptions(Color.BLACK, 1));
                        step++;
                        if (step > maxSteps) {
                            this.cancel();
                            // Start black sphere growth
                            Bukkit.getLogger().severe("[Void Termina] Creating sphere...");
                            startGrowingSphere();
                            for(Player p : world.getPlayers()) p.setGameMode(GameMode.ADVENTURE);
                            Set<Block> blocksToReplace = roaring_soul.sphereAround(center, 50);
                            for(Block b : blocksToReplace){
                                b.setType(Material.BLACK_CONCRETE);
                                BlockPlaceEvent event = new BlockPlaceEvent(b, b.getState(), b, new ItemStack(Material.BLACK_CONCRETE), caster, true);
                                Bukkit.getPluginManager().callEvent(event);
                            }
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void startGrowingSphere() {
                final int growPerTick = 2; // blocks per tick
                final int maxRadius = 800;
                Bukkit.getLogger().severe("[Void Termina] Creating growing sphere (deprecated)...");
                new BukkitRunnable() {
                    int radius = 0;

                    @Override
                    public void run() {
                        radius += growPerTick;
                        // Apply nausea to players inside
                        for (Player p : world.getPlayers()) {
                            if (p.getLocation().distance(center) <= radius) {
                                // 1 minute = 1200 ticks, amplifier 20
                                PotionEffectType nausea = PotionEffectType.NAUSEA;
                                if (nausea != null) p.addPotionEffect(new PotionEffect(nausea, 1200, 20, true, false, false));
                            }
                        }

                        if (radius >= maxRadius) {
                            this.cancel();
                            // Start TNT phase
                            startTNTPhase(maxRadius);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }

            private void spawnSphereParticles(Location center, int radius, World world) {
                // Approximate sphere by drawing a few rings at different latitudes
                int rings = 12;
                for (int i = 0; i < rings; i++) {
                    double theta = Math.PI * (double) i / (rings - 1) - Math.PI/2.0; // -pi/2..pi/2
                    double rAtTheta = radius * Math.cos(theta);
                    double y = center.getY() + radius * Math.sin(theta);
                    int points = Math.max(16, (int)(2 * Math.PI * rAtTheta / 1.0));
                    for (int j = 0; j < points; j++) {
                        double phi = 2 * Math.PI * j / points;
                        double x = center.getX() + rAtTheta * Math.cos(phi);
                        double z = center.getZ() + rAtTheta * Math.sin(phi);
                        Location loc = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, loc, 20, 0.5, 0.5, 0.5, 0.02, new Particle.DustOptions(Color.BLACK, 1));
                    }
                }
            }

            private void startTNTPhase(int sphereRadius) {
                Bukkit.getLogger().severe("[Void Termina] Deleting inventories...");
                // Clear players' inventories and give resistance 255 for 5 minutes
                for (LivingEntity le : targets) {
                        if (le instanceof Player p) {
                        p.getInventory().clear();
                        PotionEffectType resist = PotionEffectType.RESISTANCE;
                        if (resist != null) p.addPotionEffect(new PotionEffect(resist, 5 * 60 * 20, 255, true, false, false));
                    }
                }

                final Random rnd = new Random();
                final int tntPhaseTicks = 20 ; // 20 seconds -> 400 ticks

                // Repeating task every tick for tntPhaseTicks: spawn explosions scheduled with random short delay
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (t >= tntPhaseTicks) {
                            this.cancel();
                            // After TNT phase: break some blocks and teleport targets
                            // teleport each target via console command
                            Bukkit.getLogger().severe("[Void Termina] Teleporting targets to void...");
                            for (LivingEntity le : targets) {
                                if (le instanceof Player p) {
                                    String cmd = "tpworld void " + p.getName();
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                }
                                // clear their potion effects
                                if (le instanceof Player pl) {
                                    for (PotionEffect eff : pl.getActivePotionEffects()) {
                                        pl.removePotionEffect(eff.getType());
                                    }
                                }
                            }
                            return;
                        }

                        // Each tick spawn a randomized explosion inside the sphere
                        double r = rnd.nextDouble() * sphereRadius;
                        double cosTheta = 2 * rnd.nextDouble() - 1;
                        double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
                        double phi = 2 * Math.PI * rnd.nextDouble();
                        double dx = r * sinTheta * Math.cos(phi);
                        double dy = r * sinTheta * Math.sin(phi);
                        double dz = r * cosTheta;

                        double x = center.getX() + dx;
                        double y = center.getY() + dy;
                        double z = center.getZ() + dz;

                        // Clamp Y to world bounds
                        y = Math.max(1, Math.min(y, world.getMaxHeight() - 2));

                        Location explLoc = new Location(world, x, y, z);

                        int delayTicks = 4 + rnd.nextInt(7); // 4..10
                        final Location scheduledLoc = explLoc;
                        // schedule explosion after small delay
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            // create a big explosion (power 500)
                            try {
                                world.createExplosion(scheduledLoc.getX(), scheduledLoc.getY(), scheduledLoc.getZ(), 50F, false, true);
                            } catch (Exception ignored) {}
                        }, delayTicks);

                        t++;

                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }

        }.runTaskTimer(plugin, 0L, 2L); // charge particles every 2 ticks

    });

    static class Events implements Listener{

        @EventHandler
        void onPlayerChat(PlayerChatEvent ev){
            if(ev.getMessage().contains(voidTech.getDisplayName()) && ev.getPlayer().isOp() && ev.getPlayer().hasPermission("showdown.void_termina")){
                Player p = ev.getPlayer();
                voidTech.use(new TechniqueContext(p));
            }
        }
    }
}
