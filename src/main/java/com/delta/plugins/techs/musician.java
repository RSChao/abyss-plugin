package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// techapi -> techniqueAPI
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;

import java.util.List;

public class musician {
    static final String ID = "musician";

    public static void register(){
        TechRegistry.registerTechnique(ID, buff);
        TechRegistry.registerTechnique(ID, debuff);
        TechRegistry.registerTechnique(ID, wolves);
        Plugin.registerAbyssID(ID);
    }

    static Technique buff = new Technique(
        "buff",
        "Musician's Motivation Melody",
        new TechniqueMeta(false, 300000, List.of("Buff nearby entities.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
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
        }
    );

    static Technique debuff = new Technique(
        "debuff",
        "Musician's Dissonance Melody",
        new TechniqueMeta(false, 300000, List.of("Debuff nearby entities.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
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
        }
    );

    static Technique wolves = new Technique(
        "wolves",
        "Musician's Wolf Call",
        new TechniqueMeta(false, 300000, List.of("Spawn wolves.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            for(int i = 0; i<7; i++){
                Wolf w = player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Wolf.class);
                w.setOwner(player);
                w.setTamed(true);
                w.setCustomName(player.getName() + "'s Wolf");
            }
        }
    );

}
