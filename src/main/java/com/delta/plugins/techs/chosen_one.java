package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.rschao.boss_battle.bossEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

public class chosen_one {
    static final String id = "chosen_one";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    public static void register(){
        Plugin.registerAbyssID(id);
        TechRegistry.registerTechnique(id, potionArrows);
        TechRegistry.registerTechnique(id, stickman);
        TechRegistry.registerTechnique(id, tp);
        TechRegistry.registerTechnique(id, resetCooldown);
        TechRegistry.registerTechnique(id, ultimateZombieSummon);
    }

    static Technique potionArrows = new Technique(
        "weird_arrows",
        "Ráfaga de locura",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Fire potion-effect arrows.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            List<PotionEffectType> effectList = List.of(PotionEffectType.values());
            PotionEffectType effect = effectList.get((int)(Math.random() * effectList.size()));

            for(int i = 0; i < 5; i++){
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Location loc = player.getEyeLocation();
                    Arrow arrow = player.getWorld().spawn(loc, Arrow.class);
                    arrow.setShooter(player);
                    arrow.setVelocity(loc.getDirection().multiply(5));
                    arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                    if(effect.equals(PotionEffectType.HEALTH_BOOST)){
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5*20, 255), true);
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 255), true);
                    }
                    else if(effect.equals(PotionEffectType.LUCK)){
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 5*20, 1), true);
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 5*20, 1), true);
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5*20, 1), true);
                        arrow.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5*20, 1), true);
                    }
                    arrow.addCustomEffect(new PotionEffect(effect, 5*20, 1), true);
                }, i * 5);
            }
            hotbarMessage.sendHotbarMessage(player, "Has lanzado una ráfaga de flechas de " + effect.getName().toLowerCase().replace("_", " "));
        }
    );

    static Technique stickman = new Technique(
        "darioqueputas",
        "Super Stickman",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Charge and explode at closest player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            new BukkitRunnable(){
                int count = 0;
                @Override
                public void run() {
                    if(count >= 100){
                        this.cancel();
                        return;
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2, 2));
                    Player p = roaring_soul.getClosestPlayer(player.getLocation());
                    if (p == null || PlayerTechniqueManager.isInmune(p.getUniqueId())) {
                        count++;
                        return;
                    }
                    if(player.getWorld().equals(p.getWorld()))
                    if(p.getLocation().distance(player.getLocation()) < 20){
                        this.cancel();
                        p.getWorld().createExplosion(p.getLocation(), (events.hasPurityHeart(p)? 15: 40), false, false, player);
                    }
                    count++;
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    );

    static Technique tp = new Technique(
        "tp",
        "¡Y esos hacks!",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(4), List.of("Teleport to boss or coordinates item.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            if(bossEvents.bossActive){
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(p.hasPermission("gaster.boss") && p != player){
                        if (PlayerTechniqueManager.isInmune(p.getUniqueId())) continue;
                        player.teleport(p.getLocation());
                        hotbarMessage.sendHotbarMessage(player, "¡Has teletransportado a " + p.getName() + "!");
                        return;
                    }
                }
            }
            else{
                ItemStack tpItem = player.getInventory().getItem(35-8);
                if(tpItem == null || !tpItem.hasItemMeta() || !tpItem.getItemMeta().hasDisplayName()){
                    hotbarMessage.sendHotbarMessage(player, "No se detectó objeto de teletransporte");
                    return;
                }
                String targetName = tpItem.getItemMeta().getDisplayName();
                String[] parts = targetName.split(" ");
                if(parts.length <3){
                    if(parts.length == 1){
                        Player target = Bukkit.getPlayerExact(parts[0]);
                        if(target != null){
                            player.teleport(target.getLocation());
                            hotbarMessage.sendHotbarMessage(player, "¡Te has teletransportado a " + target.getName() + "!");
                        }
                        else{
                            hotbarMessage.sendHotbarMessage(player, "No se encontró al jugador " + parts[0]);
                        }
                    }
                }
                else {
                    Location loc = new Location(player.getWorld(), 0,0,0);
                    try{
                        loc.setX(Double.parseDouble(parts[0]));
                        loc.setY(Double.parseDouble(parts[1]));
                        loc.setZ(Double.parseDouble(parts[2]));
                        player.teleport(loc);
                        hotbarMessage.sendHotbarMessage(player, "¡Te has teletransportado a " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "!");
                    }catch(Exception e){
                        hotbarMessage.sendHotbarMessage(player, "Coordenadas inválidas");
                    }
                }
            }
        }
    );

    static Technique resetCooldown = new Technique(
        "reset_cooldown",
        "Cooldowns? Eso se come?",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(30), List.of("Reset many cooldowns.")),
        TargetSelectors.self(), TechRegistry.getById("reset_cooldown_chaos").getAction()
    );

    static Technique ultimateZombieSummon = new Technique(
        "ultimate_zombie_summon",
        "Legión de la Muerte",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(10), List.of("Summon zombie warriors based on player.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Player closest = roaring_soul.getClosestPlayer(player.getLocation());
            if(closest == null || closest.isDead() || PlayerTechniqueManager.isInmune(closest.getUniqueId()) || closest.getLocation().distance(player.getLocation()) > 16){
                closest = player;
            }

            double health = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();

            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getType() != Material.NETHERITE_SWORD) {
                weapon = null;
                for (ItemStack is : player.getInventory().getContents()) {
                    if (is != null && is.getType().name().endsWith("_SWORD")) {
                        weapon = is;
                        break;
                    }
                }
            }

            PlayerInventory inv = player.getInventory();
            ItemStack[] armor = inv.getArmorContents();

            for (int i = 0; i < 2; i++) {
                Location spawnLoc = player.getLocation().clone();
                double angle = Math.toRadians(i * 180);
                spawnLoc.add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);

                Zombie zombie = (Zombie) player.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.setCustomName("Guerrero de " + player.getName());
                zombie.setCustomNameVisible(true);
                zombie.setPersistent(true);
                zombie.setRemoveWhenFarAway(false);

                try { zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth); } catch (Exception ignored) {}
                try { zombie.setHealth(Math.min(health, maxHealth)); } catch (Exception ignored) {}

                EntityEquipment eq = zombie.getEquipment();
                if (weapon != null && eq != null) eq.setItemInMainHand(weapon.clone());
                if (eq != null) eq.setArmorContents(armor);
                if (eq != null) {
                    eq.setItemInMainHandDropChance(0);
                    eq.setHelmetDropChance(0);
                    eq.setChestplateDropChance(0);
                    eq.setLeggingsDropChance(0);
                    eq.setBootsDropChance(0);
                }

                zombie.setTarget(closest);
                Bukkit.getPluginManager().callEvent(new org.bukkit.event.entity.EntityTargetLivingEntityEvent(zombie, closest, EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
            }

            hotbarMessage.sendHotbarMessage(player, "¡Has invocado a tus guerreros zombis!");
        }
    );
}
