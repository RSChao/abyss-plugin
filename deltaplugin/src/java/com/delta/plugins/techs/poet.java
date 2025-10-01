package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.delta.plugins.projectiles.DeterminationProjectile;
import com.rschao.plugins.fightingpp.events.awakening;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.awakening.Awakening;
import com.rschao.plugins.techapi.tech.cooldown.CooldownManager;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
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
    }

    static Technique dt = new Technique("dt", "Runic Hellfire", false, cooldownHelper.minutesToMiliseconds(2), (player, item, args)->{
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
                            if(proj.getDistance(p.getLocation()) < 2 && p != player){
                                p.setNoDamageTicks(5);
                            }
                        }
                    }
                }.runTaskTimer(plugin, 2L, 2L);
            }, i*2L);

        }
        hotbarMessage.sendHotbarMessage(player, "&5&lRunic Hellfire Activated!");
    });

    static Technique killme = new Technique("fuckudelta", "Pale Rave's Eye", false, cooldownHelper.hour/3, (player, item, args)->{
        Random rand = new Random();
        int r = rand.nextInt(100);
        player.sendMessage("You feel a strange power...");
        if(r < 75){
            int randomFruit = rand.nextInt(com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs().size());
            String fruitID = com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs().get(randomFruit);
            int tech = rand.nextInt(TechRegistry.getAllTechniques(fruitID).size());
            Technique technique = TechRegistry.getAllTechniques(fruitID).get(tech);
            if(technique != null){
                if(technique.isUltimate()){
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
                text.add("Unleashing " + technique.getName());
                new BukkitRunnable(){
                    int ticks = 0;
                    @Override
                    public void run() {
                        if(CooldownManager.isOnCooldown(player, technique.getId())){
                            this.cancel();
                            return;
                        }
                        if(ticks > (2*text.size())) {
                            technique.use(player, player.getInventory().getItemInMainHand(), Technique.nullValue());
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
            if(technique != null){
                if(technique.isUltimate()){
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
                text.add("Unleashing " + technique.getName());
                new BukkitRunnable(){
                    int ticks = 0;
                    @Override
                    public void run() {
                        if(CooldownManager.isOnCooldown(player, technique.getId())){
                            this.cancel();
                            return;
                        }
                        if(ticks > (2*text.size())) {
                            technique.use(player, player.getInventory().getItemInMainHand(), Technique.nullValue());
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
        }
    });

    static Technique laser = new Technique("protonblast", "Runic Proton Blast", false, cooldownHelper.minutesToMiliseconds(3), (player, item, args)->{
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
    });

    static Technique omninegate = new Technique("omninegate", "Omni Negate", false, cooldownHelper.hour, (player, item, args)->{
        new BukkitRunnable(){
            @Override
            public void run() {
                events.hasOmniNegate.put(player.getUniqueId(), false);
            }
        }.runTaskLater(plugin, 40);
        new BukkitRunnable(){
            @Override
            public void run() {
                events.hasOmniNegate.put(player.getUniqueId(), true);
            }
        }.runTaskLater(plugin, 10);
        hotbarMessage.sendHotbarMessage(player, "&5&lOmni Negate Activated!");
    });

}
