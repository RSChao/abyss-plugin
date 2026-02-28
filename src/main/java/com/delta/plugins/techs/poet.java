package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.delta.plugins.items.Items;
import com.delta.plugins.projectiles.DeterminationProjectile;
import com.rschao.plugins.fightingpp.events.awakening;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.awakening.Awakening;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.register.TechniqueNameManager;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class poet {
    static final String TECH_ID = "poet";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, dt);
        TechRegistry.registerTechnique(TECH_ID, killme);
        TechRegistry.registerTechnique(TECH_ID, laser);
        TechRegistry.registerTechnique(TECH_ID, omninegate);
        TechRegistry.registerTechnique(TECH_ID, holy_moly);
    }

    static Technique dt = new Technique(
        "dt",
        "Runic Hellfire",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("Launch determination projectiles.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location loc = player.getEyeLocation();
            for(int i = 0; i<10; i++){
                Bukkit.getScheduler().runTaskLater(plugin, ()->{
                    DeterminationProjectile proj = new DeterminationProjectile(player.getLocation(), player);
                    proj.launch();
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            if(!proj.isValid()){
                                this.cancel();
                                return;
                            }
                            for(Player p : Bukkit.getOnlinePlayers()){
                                // Excluir activador e jugadores inmunes
                                if (p == player) continue;
                                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                                if(proj.getDistance(p.getLocation()) < 2 && p != player){
                                    p.setNoDamageTicks(5);
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 2L, 2L);
                }, i*2L);

            }
            hotbarMessage.sendHotbarMessage(player, "&5&lRunic Hellfire Activated!");
        }
    );

    static Technique killme = new Technique(
        "fuckudelta",
        "Pale Rave's Eye",
        new TechniqueMeta(false, cooldownHelper.hour/3, List.of("Attempt to unleash a random technique.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Random rand = new Random();
            int r = rand.nextInt(100);
            player.sendMessage("You feel a strange power...");
            if(r < 75){
                int randomFruit = rand.nextInt(com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs().size());
                String fruitID = com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs().get(randomFruit);
                int tech = rand.nextInt(TechRegistry.getAllTechniques(fruitID).size());
                Technique technique = TechRegistry.getAllTechniques(fruitID).get(tech);
                if(technique != null){
                    if(technique.getMeta().isUltimate()){
                        List<String> awakened = new ArrayList<>();
                        for(String id : com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                            if(Awakening.isAwakened(player.getName(), id)){
                                awakened.add(id);
                                continue;
                            }
                            Awakening.setAwakened(player.getName(), id, true);
                            com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened(player.getName(), id, true);
                            awakening.loadAwakenedFromConfig(player.getName());
                            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                                if(awakened.contains(id)) return;
                                Awakening.setAwakened(player.getName(), id, false);
                                com.rschao.plugins.fightingpp.events.awakening.setFruitAwakened(player.getName(), id, false);
                            }, 200L);
                        }
                    }
                    List<String> text = new ArrayList<>();
                    text.add("&4It is time...");
                    text.add("&4take the power of the Butcher...");
                    text.add("&4and become his vessel.");
                    text.add("&4&lLET US WRECH HAVOC, AS FAR AS THE EYE CAN SEE!");
                    text.add("Origin the butcher of Oblivion has bestowed upon you his power...");
                    text.add("Unleashing " + TechniqueNameManager.getDisplayName(player, technique));
                    new BukkitRunnable(){
                        int ticks = 0;
                        @Override
                        public void run() {
                            if(CooldownManager.isOnCooldown(player, technique.getId())){
                                this.cancel();
                                return;
                            }
                            if(ticks > (2*text.size())) {
                                technique.use(new TechniqueContext(player));
                                this.cancel();
                                return;
                            }
                            if(ticks % 2 == 0){
                                int index = ticks/2;
                                if(index < text.size()){
                                    player.sendMessage(text.get(index).replace("&", "§"));
                                }
                            }
                            ticks += 2;

                        }
                    }.runTaskTimer(plugin, 0, 20L);
                }

            } else {
                int randomFruit = rand.nextInt(Plugin.getAllAbyssIDs().size());
                String fruitID = Plugin.getAllAbyssIDs().get(randomFruit);
                int tech = rand.nextInt(TechRegistry.getAllTechniques(fruitID).size());
                Technique technique = TechRegistry.getAllTechniques(fruitID).get(tech);
                if(technique.getId().equals(TechRegistry.getById("roaringgowhacka"))){
                    tech = rand.nextInt(TechRegistry.getAllTechniques(fruitID).size());
                    technique = TechRegistry.getAllTechniques(fruitID).get(tech);
                    if(technique.getId().equals(TechRegistry.getById("roaringgowhacka"))){
                        technique = null;
                    }
                }
                if(technique != null){
                    if(technique.getMeta().isUltimate()){
                        List<String> awakened = new ArrayList<>();
                        for(String id : com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                            if(Awakening.isAwakened(player.getName(), id)){
                                awakened.add(id);
                                continue;
                            }
                            Awakening.setAwakened(player.getName(), id, true);
                            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                                if(awakened.contains(id)) return;
                                Awakening.setAwakened(player.getName(), id, false);
                            }, 20L);
                        }
                    }
                    List<String> text = new ArrayList<>();
                    text.add("&4It is time...");
                    text.add("&4take the power of the Butcher...");
                    text.add("&4and become his vessel.");
                    text.add("&4&lLET US WRECH HAVOC, AS FAR AS THE EYE CAN SEE!");
                    text.add("Origin the butcher of Oblivion has bestowed upon you his power...");
                    text.add("Unleashing " + TechniqueNameManager.getDisplayName(player, technique));
                    Technique finalTechnique = technique;
                    new BukkitRunnable(){
                        int ticks = 0;
                        @Override
                        public void run() {
                            if(CooldownManager.isOnCooldown(player, finalTechnique.getId())){
                                this.cancel();
                                return;
                            }
                            if(ticks > (2*text.size())) {
                                finalTechnique.use(new TechniqueContext(player));
                                this.cancel();
                                return;
                            }
                            if(ticks % 2 == 0){
                                int index = ticks/2;
                                if(index < text.size()){
                                    player.sendMessage(text.get(index).replace("&", "§"));
                                }
                            }
                            ticks += 2;

                        }
                    }.runTaskTimer(plugin, 0, 20L);
                }
                else {
                    player.sendMessage("But nothing happened...");
                    Bukkit.getScheduler().runTaskLater(plugin, ()->{
                        CooldownManager.setCooldown(player, "fuckudelta", 60*1000);
                    }, 40L);
                    player.sendMessage("You feel relieved...");
                }
            }
        }
    );

    static Technique laser = new Technique(
        "protonblast",
        "Runic Proton Blast",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Fire a long particle/arrow beam.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Location eyeLoc = player.getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize();
            int durationTicks = 60; // 3 segundos
            int arrowStartTick = 20; // después de 1 segundo
            int arrowsToShoot = 20;
            double particleStep = 0.5; // distancia entre partículas
            int particlesPerTick = 16; // cantidad de partículas por tick

            new BukkitRunnable() {
                int tick = 0;
                int arrowsShot = 0;
                @Override
                public void run() {
                    if (tick >= durationTicks) {
                        this.cancel();
                        return;
                    }

                    // Disparar partículas blancas en línea recta
                    for (int i = 0; i < particlesPerTick; i++) {
                        double dist = i * particleStep;
                        Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(dist + tick * particleStep));
                        player.getWorld().spawnParticle(Particle.DUST, particleLoc, 0, new Particle.DustOptions(org.bukkit.Color.WHITE, 1.5f));
                    }

                    // Enviar mensaje "hi" a jugadores en línea recta a <=7 bloques
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p == player) continue;
                        // Excluir jugadores inmunes
                        if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        Location pLoc = p.getLocation().add(0, 1.6, 0); // altura de los ojos
                        Vector toPlayer = pLoc.toVector().subtract(eyeLoc.toVector());
                        double dist = toPlayer.length();
                        if (dist <= 7) {
                            p.setNoDamageTicks(5);
                        }
                    }

                    // Lanzar flechas después de 1 segundo, 1 por tick, hasta 20
                    if (tick >= arrowStartTick && arrowsShot < arrowsToShoot) {
                        Arrow arrow = player.getWorld().spawnArrow(
                            eyeLoc.clone().add(direction.clone().multiply(1.0)), // un poco delante de los ojos
                            direction.clone(),
                            (float) 1.0,
                            0.0f // spread
                        );
                        arrow.setShooter(player);
                        arrow.setDamage(10.0);
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                        arrow.setVelocity(direction.normalize().multiply(5));
                        arrowsShot++;
                    }

                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            hotbarMessage.sendHotbarMessage(player, "&5&lRunic Proton Blast Activated!");
        }
    );

    static Technique omninegate = new Technique(
        "omninegate",
        "Omni Negate",
        new TechniqueMeta(false, cooldownHelper.hour, List.of("Temporarily toggle omni negate.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            new BukkitRunnable(){
                @Override
                public void run() {
                    events.hasOmniNegate.put(player.getUniqueId(), true);
                }
            }.runTaskLater(plugin, 10);
            new BukkitRunnable(){
                @Override
                public void run() {
                    events.hasOmniNegate.put(player.getUniqueId(), false);
                }
            }.runTaskLater(plugin, 40);
            hotbarMessage.sendHotbarMessage(player, "&5&lOmni Negate Activated!");
        }
    );

    static Technique holy_moly = new Technique(
        "holy_moly",
        "Holy Moly",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(60), List.of("Drop a holy moly item.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Item i = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), Items.Moly_holy);
            i.setPickupDelay(0);
            hotbarMessage.sendHotbarMessage(player, "&6&lHoly Moly Obtained!");
        }
    );

    // Nueva helper pública: ejecutar Runic Hellfire desde una entidad (mobs)
    public static void runicHellfireAtEntity(LivingEntity user) {
        if (user == null || user.isDead()) return;
        Location loc = user.getLocation();
        World w = loc.getWorld();

        // Hacemos 10 ráfagas escalonadas que dañan y generan partículas
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // partículas en el centro
                w.spawnParticle(Particle.FLAME, loc.clone().add(0,1,0), 30, 1, 1, 1, 0.08);
                w.playSound(loc, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1f);

                // daño a jugadores cercanos en la torre
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.isOnline()) continue;
                    if (!p.getWorld().equals(w)) continue;
                    if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                    Integer floor = p.getPersistentDataContainer().get(new org.bukkit.NamespacedKey("tower", "floor"), org.bukkit.persistence.PersistentDataType.INTEGER);
                    if (floor == null || floor <= 0) continue;
                    if (p.getLocation().distance(loc) <= 6.0) {
                        try { p.damage(8.0 + idx, user); } catch (Throwable ignored) {}
                        p.setNoDamageTicks(5);
                    }
                }
            }, i * 4L); // ráfagas cada 4 ticks
        }
    }

}
