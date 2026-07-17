package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.TechFlagEvents;
import com.delta.plugins.mobs.MobManager;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.delta.plugins.techs.roaring_soul.sphereAround;

public class Creator_of_Showdown {

    static final String ID = "creator_of_showdown";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register(){
        TechRegistry.registerTechnique(ID, hajiHika);
        TechRegistry.registerTechnique(ID, owaKage);
        TechRegistry.registerTechnique(ID, dtWord);
        TechRegistry.registerTechnique(ID, lifeBreath);
        TechRegistry.registerTechnique(ID, isekaiPrayer);
        TechRegistry.registerTechnique(ID, koritsuCurse);
        TechRegistry.registerTechnique(ID, aiBlessing);
        TechRegistry.registerTechnique(ID, powerOfTwo);
    }

    // aqui van las técnicas del Creator_of_Showdown
    static Technique hajiHika = new Technique("light_of_beginning", "Hajimari no Hikari", new TechniqueMeta(false, cooldownHelper.hour, List.of("Unleash a divine light that harms foes.")), TargetSelectors.radialPlayers(25), (ctx, token) ->{
        List<LivingEntity> targets = new ArrayList<>(List.of(ctx.targets().toArray(new LivingEntity[0])));
        List<String> dialogue = List.of(
                "God of Showdown! Heed my call!",
                "Let the light of beginning shine upon this battlefield!"
        );
        for(int i = 0; i < targets.size(); i++) {

            int finalI = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (LivingEntity target : targets) {
                    target.sendMessage(dialogue.get(finalI % dialogue.size()));
                }
            }, i*30L);
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                //particles: a ray of light from the sky down to the user
                Player p = ctx.caster();
                org.bukkit.Location playerLoc = p.getLocation();
                
                // Create light ray particle effect from sky to player
                org.bukkit.Location rayStart = playerLoc.clone().add(0, 50, 0);
                for (int y = 50; y >= 0; y--) {
                    org.bukkit.Location particleLoc = playerLoc.clone().add(0, y, 0);
                    playerLoc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 3, 0.3, 0, 0.3, 0.05);
                    playerLoc.getWorld().spawnParticle(Particle.GLOW, particleLoc, 2, 0.2, 0, 0.2, 0);
                }
                
                // Explosion effect at player location
                playerLoc.getWorld().spawnParticle(Particle.EXPLOSION, playerLoc, 5, 0.5, 0.5, 0.5, 0.1);
                playerLoc.getWorld().spawnParticle(Particle.FLASH, playerLoc, 2, Color.WHITE);

                //effects
                p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(20*60*5, 3));
                p.addPotionEffect(PotionEffectType.SPEED.createEffect(20*60*5, 1));
                p.addPotionEffect(PotionEffectType.STRENGTH.createEffect(20*60*5, 3));
                p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(20*60*5, 3));
                p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(20*60*5, 4));

                for(LivingEntity target : targets){
                    if(!target.equals(p)){
                        target.setHealth(1);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            target.damage(1000);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                target.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(10*20, 255));
                            }, 1);
                        }, 2);
                    }
                }
            }
        }.runTaskLater(plugin, 30L * 2);
    });

    static Technique owaKage = new Technique("shadow_of_end", "Owari no Kage", new TechniqueMeta(false, cooldownHelper.hour, List.of("Trap enemies with shadow soldiers")), TargetSelectors.radialPlayers(50), (ctx, token) ->{

        List<LivingEntity> targets = new ArrayList<>(List.of(ctx.targets().toArray(new LivingEntity[0])));
        List<String> dialogue = List.of(
                "God of Showdown! Heed my call!",
                "Let the shadow of end consume this battlefield!"
        );

        for(int i = 0; i < targets.size(); i++) {

            int finalI = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (LivingEntity target : targets) {
                    target.sendMessage(dialogue.get(finalI % dialogue.size()));
                }
            }, i*30L); // Delay of 1.5 seconds (30 ticks)
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ctx.caster().addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(20*60, 0));

            Location center = ctx.caster().getLocation().clone();
            int radius = 50;
            Set<Block> sphereBlocks = new HashSet<>();
            Set<BlockState> replacedBlocks = new HashSet<>();
            Set<Block> sphere = sphereAround(center, radius);
            for (Block b : sphere) {
                replacedBlocks.add(b.getState());
                sphereBlocks.add(b);
                // Poner vidrio tintado sin física
                b.setType(Material.BEDROCK, false);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (BlockState bs : replacedBlocks) {
                    try {
                        // Restaurar estado original sin física
                        bs.update(true, false);
                    } catch (Exception ignored) {}
                }
                replacedBlocks.clear();
                sphereBlocks.clear();
            }, 20L * 60);

            for(LivingEntity target : targets){
                TechFlagEvents.kageTechBans.put(target.getUniqueId(), true);
                if(!target.equals(ctx.caster())){
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        List<Function<Location, Entity>> floor;
                        do{
                            floor = MobManager.buildSpawnFunctions(98);
                            if(floor.isEmpty()){
                            }
                        } while (floor.isEmpty());

                        for(Function<Location, Entity> func : floor){
                            Entity e = func.apply(target.getLocation());
                            if(e instanceof LivingEntity le){
                                le.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(Integer.MAX_VALUE, 0));
                            }
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if(e.isValid() && !e.isDead()){
                                    e.remove();
                                }
                                try{
                                    TechFlagEvents.kageTechBans.remove(target.getUniqueId());
                                }
                                catch (Exception ignored) {
                                }
                            }, 20*60L);
                        }
                    }, 20*3);
                }
            }
        }, 30L * 2);
    });

    static Technique dtWord = new Technique("word_of_determination", "Ketsui no Kotoba", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Brings forth the user's will, granting them power.")), TargetSelectors.self(), (ctx, token) ->{
        Player p = ctx.caster();
        p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(20*90, 1));
        p.addPotionEffect(PotionEffectType.SPEED.createEffect(20*90, 1));
        p.addPotionEffect(PotionEffectType.STRENGTH.createEffect(20*90, 1));
        p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(20*90, 1));
        hotbarMessage.sendHotbarMessage(p, "§a[Creator of Showdown] §7You feel a surge of determination!");
    });

    static Technique lifeBreath = new Technique("breath_of_life", "Inochi no Ibuki", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Heals the caster.")), TargetSelectors.self(), (ctx, token) ->{
        Player p = ctx.caster();
        p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(20*120, 9));
        p.setSaturation(20);
        p.addPotionEffect(PotionEffectType.SATURATION.createEffect(20*120, 4));
    });

    static Technique isekaiPrayer = new Technique("prayer_of_reincarnation", "Tensei no Inori", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("Pray for reincarnation and restore a fallen C-heart.")), TargetSelectors.self(), (ctx, token) ->{
        Player player = ctx.caster();
        boolean offhandUsed = false;
        ItemStack item = player.getInventory().getItemInOffHand();
        if(item == null) offhandUsed = false;
        if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER)) {
            int uses = item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, 0);
            if(uses > 0) {
                offhandUsed = true;
                // Restaurar el C-heart del jugador
                if(uses == 1) uses--;
                else uses-= 2; // Restaurar 2 usos
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
                    if(uses == 1) uses--;
                    else uses-= 2; // Restaurar 2 usos
                    invItem.getItemMeta().getPersistentDataContainer().set(new NamespacedKey("weapon", "corrupted_heart"), PersistentDataType.INTEGER, uses);
                    player.sendMessage("C-heart restaurado desde el inventario. Usos restantes: " + uses);
                    inventoryUsed = true;
                    break;
                }
            }
        }
        if(inventoryUsed) return;

        // Si no hay C-hearts usados, fijar cooldown a la mitad
        Bukkit.getScheduler().runTaskLater(plugin, () -> CooldownManager.removeCooldown(player, "prayer_of_reincarnation"), 1L); // Ejecutar en el siguiente tick para evitar conflictos
    });

    static Technique koritsuCurse = new Technique("curse_of_isolation", "Koritsu no Noroi", new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(30), List.of("Isolate targets from their allies.")), TargetSelectors.radialPlayers(50), (ctx, token) ->{
        List<LivingEntity> targets = new ArrayList<>(List.of(ctx.targets().toArray(new LivingEntity[0])));
        for(LivingEntity target : targets){
            if(!target.equals(ctx.caster())){
                TechFlagEvents.koritsuTargets.put(target.getUniqueId(), true);
                target.sendMessage("§c[Koritsu] §7You have been cursed by " + ctx.caster().getName() + ". You cannot move too close to other Koritsu targets for 1 minute.");
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if(TechFlagEvents.koritsuTargets.containsKey(target.getUniqueId())){
                        TechFlagEvents.koritsuTargets.remove(target.getUniqueId());
                    }
                }, 20*60);
            }
        }
    });

    static Technique aiBlessing = new Technique("ai_blessing", "Ai no Shukufuku", new TechniqueMeta(false, cooldownHelper.hour*4, List.of("Bless allies with protection.")), TargetSelectors.radialPlayers(20), (ctx, token) ->{
        Player caster = ctx.caster();
        caster.sendMessage("§a[Ai no Shukufuku] §7Type the name of the player you wish to bless in chat.");
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            void onPlayerChat(PlayerChatEvent ev){
                String msg = ev.getMessage();
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(msg.equals(p.getName())){
                        ev.setCancelled(true);
                        TechFlagEvents.aiTargets.put(p.getUniqueId(), caster.getUniqueId());
                        p.sendMessage("§a[Ai no Shukufuku] §7You have been blessed by " + caster.getName() + ". You are now protected from their ultimate.");
                        caster.sendMessage("§a[Ai no Shukufuku] §7You have blessed " + p.getName() + ". They are now protected from your ultimate.");
                    }
                }
                ev.getHandlers().unregister(this);
            }
        }, plugin);
    });

    static Technique powerOfTwo = new Technique("supreme:power_of_two", "Shikō no mahō: Futari no Chikara", new TechniqueMeta(true, cooldownHelper.hour*2, List.of("Combine powers of two techniques.")), TargetSelectors.self(), (ctx, token) ->{
        Player caster = ctx.caster();
        caster.sendMessage("§a[Power of Two] §7Type the names of the two techniques you wish to combine in chat.");
        List<String> ids = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            void onPlayerChat(AsyncPlayerChatEvent ev){
                ev.setCancelled(true);
                String msg = ev.getMessage();
                int startingSize = ids.size();
                boolean found = false;
                for(String id : TechRegistry.getRegisteredFruitIds()){
                    for(Technique t : TechRegistry.getAllTechniques(id)){
                        if(t.getDisplayName().equals(msg) && !ids.contains(msg)){
                            ids.add(t.getId());
                            found = true;
                            break;
                        }
                    }
                    if(found) break;
                }
                if(!found){
                    caster.sendMessage("§c[Power of Two] §7Invalid selection: " + msg);
                    CooldownManager.setCooldown(caster, "supreme:power_of_two", cooldownHelper.minutesToMiliseconds(10));
                    ev.getHandlers().unregister(this);
                    return;
                }
                if(ids.size() > startingSize){
                    caster.sendMessage("§a[Power of Two] §7You have selected: " + msg);
                }
                else {
                    caster.sendMessage("§c[Power of Two] §7Invalid selection: " + msg);
                    //set cooldown to 10 minutes after 1 tick, then unregister the event and return
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        CooldownManager.setCooldown(caster, "supreme:power_of_two", cooldownHelper.minutesToMiliseconds(10));
                        ev.getHandlers().unregister(this);
                    }, 1L);
                    return;
                }
                if(ids.size() >= 2){
                    List<String> selectedTechniques = new ArrayList<>();
                    if(ids.size() > 2){
                        caster.sendMessage("§c[Power of Two] §7You have selected more than 2 techniques. Only the first two will be used.");
                        selectedTechniques = ids.subList(0, 2);
                    }
                    else {
                        selectedTechniques = ids;
                    }
                    List<String> dialogue = List.of(
                            "I am thou, and thou art I.",
                            "Heed my call, world of Showdown",
                            "May the divinity of the creator become one with my will.",
                            "May death and destruction become my blade once more.",
                            "In the name of " + ctx.caster().getName() + ((ctx.caster().getName().startsWith("RSChao")) ? ", the Creator of Showdown" : ", the one worthy of the Creator's power") + ", i summon",
                            ChatColor.DARK_RED + (ChatColor.BOLD + "Supreme Magic: Power of Two") + ChatColor.RESET + "!"
                    );

                    for(int i = 0; i < dialogue.size(); i++) {
                        int finalI = i;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            for(Player p : Bukkit.getOnlinePlayers()){
                                if(finalI == dialogue.size() - 1){
                                    p.sendTitle(dialogue.get(finalI), "", 10, 70, 20);
                                }
                                else {
                                    p.sendMessage(dialogue.get(finalI));
                                }
                            }

                        }, i*30L); // Delay of 30 ticks (1.5 seconds)
                    }
                    List<String> finalSelectedTechniques = selectedTechniques;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        for(String id : finalSelectedTechniques){
                            Technique t = TechRegistry.getById(id);
                            if(t == null){
                                caster.sendMessage("§c[Power of Two] §7Invalid technique: " + id);
                                continue;
                            }
                            caster.sendMessage("§a[Power of Two] §7Using technique: " + t.getDisplayName());
                            if(!CooldownManager.isOnCooldown(caster, t.getId())){
                                Bukkit.getScheduler().runTask(plugin, () -> {

                                    t.use(caster);
                                    if(id.equals("heart_atomization")){
                                        caster.sendMessage("§a[Power of Two] §7Heart Atomization will be finalized after 10 seconds.");
                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                            t.use(caster);
                                        }, 10*20L);
                                        if(id.equals("roaringgowhacka")){
                                            caster.sendMessage("§a[Power of Two] §7Roaring Gowhacka will be finalized after 30 seconds.");
                                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                                t.use(caster);
                                            }, 30*20L);
                                        }
                                    }
                                });
                            }
                        }
                        ev.getHandlers().unregister(this);
                    }, dialogue.size()*30L + 20L); // Execute after the dialogue is done
                }
            }
        }, plugin);
    });

}
