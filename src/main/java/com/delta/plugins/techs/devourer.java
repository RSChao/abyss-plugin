package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.rschao.boss_battle.bossEvents;
import com.rschao.events.soulEvents;
import com.rschao.plugins.fightingpp.techs.chao;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class devourer {
    static final String TECH_ID = "devourer";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, chains);
        TechRegistry.registerTechnique(TECH_ID, laugh);
        TechRegistry.registerTechnique(TECH_ID, maus);
        TechRegistry.registerTechnique(TECH_ID, soulstorm);
    }
    static Technique chains = new Technique("deltateodio", "Nightmare Chains", false,cooldownHelper.minutesToMiliseconds(5), (player, item, args) -> {
        // Create a chain of diamond blocks around the player
        Player closestPlayer = chao.getClosestPlayer(player.getLocation());
        // Excluir objetivo inmune
        if (closestPlayer != null && PlayerTechniqueManager.isInmune(closestPlayer.getUniqueId())) {
            player.sendMessage("No hay jugadores válidos cerca para usar Nightmare Chains.");
            return;
        }
        if (closestPlayer != null) {
            for (int i = 0; i < 80; i++) {
                int t = i % 5;
                Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                    Vector direction = player.getEyeLocation().getDirection().normalize().multiply(20);
                    Location targetLocation = player.getEyeLocation().add(direction);
                    closestPlayer.teleport(targetLocation);

                    if (t == 0) {
                        closestPlayer.damage(40);
                        closestPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1, false, false));
                    }
                }, i);
            }
            hotbarMessage.sendHotbarMessage(player, "You have used the Nightmare Chains technique!");
        } else {
            player.sendMessage("No players nearby to launch.");
        }
    });

    static Technique laugh = new Technique("lololol", "Chained Explosion", false, cooldownHelper.hour, (player, fruit, code) -> {
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(20, 20, 20)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                // Excluir objetivos inmunes
                if (PlayerTechniqueManager.isInmune(target.getUniqueId())) continue;
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 255));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 255));
                target.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH).setBaseValue(0);
                double jumpstrength = 0.41999998697815;
                Bukkit.getScheduler().runTaskLater(com.rschao.plugins.fightingpp.Plugin.getPlugin(com.rschao.plugins.fightingpp.Plugin.class), () -> {
                    target.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH).setBaseValue(jumpstrength);
                    target.damage(400);
                }, 3 * 20);
            }
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Chained Explosion technique");
    });
    static Technique maus = new Technique("queputasesesto", "Blade of Chained Jaws", false, cooldownHelper.minutesToMiliseconds(5), (player, fruit, code) -> {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 90, 1, false, false));
        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null) continue;
            if(item.getType().toString().contains("SWORD")) {

                ItemMeta meta = item.getItemMeta();
                ItemMeta meta2 = item.getItemMeta();
                meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("attraction")), 2, true);
                meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("drain")), 2, true);
                meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft("geno")), 2, true);
                item.setItemMeta(meta);
                Bukkit.getScheduler().runTaskLater(plugin, ()->{
                    item.setItemMeta(meta2);
                }, 20 * 90);
                return;
            }
        }
        hotbarMessage.sendHotbarMessage(player, ChatColor.DARK_GRAY + "You have used the Blade of Chained Jaws technique");
    });



    static Technique soulstorm = new Technique("void", "Worlds' Last Breath", true, cooldownHelper.hour*3, (player, fruit, code) -> {
        boolean boss = bossEvents.bossActive;
        if(boss && player.hasPermission("gaster.boss")){
            int soul1 = soulEvents.GetSoulN(player);
            int soul2 = soulEvents.GetSecondSoulN(player);

            if(!(soul1 == 19 && soul2 == 66)){
                player.sendMessage(ChatColor.RED + "You need souls Void and Chaos Heart to use this technique during a boss fight.");
            }
        }


        // Paso 1: Flotar y quitar gravedad
        Location startLoc = player.getLocation().clone();
        startLoc.setY(startLoc.getY() + 1);
        player.teleport(startLoc);
        player.setGravity(false);
        player.setVelocity(new Vector(0, 0.1, 0));
        List<Player> enemies = new ArrayList<>();
        for (Player p : player.getWorld().getPlayers()) {
            if (!p.equals(player) && p.getLocation().distance(startLoc) <= 80) {
                // Excluir jugadores inmunes
                if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                enemies.add(p);
            }
        }
        // Paso 2: Fijar posición tras 2 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location animLoc = player.getLocation().clone();
            // Teletransporte constante
            BukkitRunnable tpTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    player.teleport(animLoc);
                }
            };
            tpTask.runTaskTimer(plugin, 0L, 1L);

            // Paso 3: Invocar 8 pure hearts en círculo y el chaos heart encima del jugador
            ItemStack[] pureHearts = {
                com.delta.plugins.items.Items.pureheart_red,
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
                final int idx = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double angle = 2 * Math.PI * idx / 8;
                    double radius = 1.5;
                    double x = circleCenter.getX() + radius * Math.cos(angle);
                    double y = circleCenter.getY();
                    double z = circleCenter.getZ() + radius * Math.sin(angle);
                    Location itemLoc = new Location(world, x, y, z);

                    ItemStack stack = pureHearts[idx].clone();
                    Item itemEntity = world.dropItem(itemLoc, stack);
                    itemEntity.setPickupDelay(Integer.MAX_VALUE);
                    itemEntity.setGravity(false);
                    itemEntity.setVelocity(new Vector(0, 0, 0));
                    spawnedItems.add(itemEntity);
                }, 20L * idx); // 1 segundo entre cada ítem
            }

            // Invocar el chaos heart (nightmare) encima del jugador tras los pure hearts
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location chaosLoc = animLoc.clone().add(0, 2.2, 0);
                ItemStack chaosHeart = com.rschao.items.Items.ChaosHeart.clone();
                Item chaosEntity = world.dropItem(chaosLoc, chaosHeart);
                chaosEntity.setPickupDelay(Integer.MAX_VALUE);
                chaosEntity.setGravity(false);
                chaosEntity.setVelocity(new Vector(0, 0, 0));
                spawnedItems.add(chaosEntity);

                // Paso 4: Partículas de agujero negro (negro y púrpura) absorbiéndose al chaos heart
                BukkitRunnable particleTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!chaosEntity.isValid()) {
                            this.cancel();
                            return;
                        }
                        Location center = chaosEntity.getLocation().clone().add(0, 0.2, 0);
                        int points = 32;
                        double radius = 4.5;
                        for (int i = 0; i < points; i++) {
                            double angle = 2 * Math.PI * i / points;
                            double px = center.getX() + radius * Math.cos(angle);
                            double py = center.getY() + (Math.random()-0.5)*1.5;
                            double pz = center.getZ() + radius * Math.sin(angle);
                            Location particleLoc = new Location(world, px, py, pz);
                            Vector dir = center.toVector().subtract(particleLoc.toVector()).normalize().multiply(0.25 + Math.random()*0.15);
                            // Partículas negras y púrpuras
                            world.spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, 1, new Particle.DustOptions(Color.PURPLE, 1.5f));
                            world.spawnParticle(Particle.DUST, particleLoc, 0, dir.getX(), dir.getY(), dir.getZ(), 0.08);
                            // Opcional: partículas negras
                            world.spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, new org.bukkit.Particle.DustOptions(org.bukkit.Color.BLACK, 1.5f));
                            for(Player p : enemies){
                                //get resistance potion effect
                                PotionEffect[] effects = p.getActivePotionEffects().toArray(new PotionEffect[0]);
                                if(effects.length > 0){
                                    for(PotionEffect effect : effects){
                                        if(effect.getType().equals(PotionEffectType.RESISTANCE)){
                                            if(effect.getAmplifier() > 5){
                                                p.getActivePotionEffects().remove(effect);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                particleTask.runTaskTimer(plugin, 0L, 2L);

                // Paso 5: Explosiones y daño tras aparecer los ítems
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    BukkitRunnable explosionTask = new BukkitRunnable() {
                        int tick = 0;

                        @Override
                        public void run() {
                            if (tick % 20 == 0) {
                                for (Player enemy : enemies) {
                                    player.sendMessage(enemy.getName());
                                    Location feet = enemy.getLocation().clone().add(0, 1, 0);
                                    Bukkit.getScheduler().runTaskLater(plugin, () ->{
                                        enemy.damage(100.0, player);
                                    }, 2);
                                    world.createExplosion(feet.add(0, 1, 0), 7.0F, false, false, player);
                                }
                            }
                            tick += 1;
                            if (tick >= 40) {
                                // Eliminar ítems y restaurar gravedad
                                for (Item item : spawnedItems) {
                                    if (!item.isDead()) item.remove();
                                }
                                player.setGravity(true);
                                tpTask.cancel();
                                particleTask.cancel();
                                this.cancel();
                            }
                        }
                    };
                    explosionTask.runTaskTimer(plugin, 0L, 1L);
                }, 20L * 2); // Espera 2 segundos tras el chaos heart
            }, 20L * 8); // Espera a que aparezcan los 8 pure hearts
        }, 40L); // 2 segundos después de iniciar
    });

}
