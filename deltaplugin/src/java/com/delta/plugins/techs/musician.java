package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

public class musician {
    static final String ID = "musician";

    public static void register(){
        TechRegistry.registerTechnique(ID, buff);
        TechRegistry.registerTechnique(ID, debuff);
        TechRegistry.registerTechnique(ID, wolves);
        Plugin.registerAbyssID(ID);
    }

    static Technique buff = new Technique("buff", "Musician's Motivation Melody", false, 300000, (player, item, args) ->{
        //buff all entities within 5 blocks
        player.getWorld().getEntities().stream()
            .filter(entity -> entity.getLocation().distance(player.getLocation()) <= 5)
            .forEach(entity -> {
                if ((entity instanceof LivingEntity)){
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 90*20, 1));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 90*20, 1));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 90*20, 1));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 90*20, 1));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 90*20, 1));
                    if(entity instanceof Wolf){
                        Wolf wolf = (Wolf) entity;
                        if(wolf.isTamed() && wolf.getOwner() != null && wolf.getOwner().getUniqueId().equals(player.getUniqueId())){
                            player.sendMessage("You buffed one of your wolves!");
                        }
                    }
                }
            });
    });

    static Technique debuff = new Technique("debuff", "Musician's Dissonance Melody", false, 300000, (player, item, args) ->{
        //debuff all entities within 5 blocks
        player.getWorld().getEntities().stream()
            .filter(entity -> entity.getLocation().distance(player.getLocation()) <= 3)
            .forEach(entity -> {
                if ((entity instanceof LivingEntity) && entity != player){
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.setHealth(livingEntity.getHealth() - 2);
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 5*20, 1));
                }
            });
    });

    static Technique wolves = new Technique("wolves", "Musician's Wolf Call", false, 300000, (player, item, args) -> {
        for(int i = 0; i<7; i++){
            Wolf w = (Wolf) player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Wolf.class);
            w.setOwner(player);
            w.setTamed(true);
            w.setCustomName(player.getName() + "'s Wolf");
        }
    });

}
