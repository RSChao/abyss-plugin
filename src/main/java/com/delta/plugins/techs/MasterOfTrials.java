package com.delta.plugins.techs;

import com.delta.plugins.mobs.MobManager;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MasterOfTrials {

    static final String TECH_ID = "master_of_trials";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);
    public static void register(){
        TechRegistry.registerTechnique(TECH_ID, floorSummoner);
    }

    static Technique floorSummoner = new Technique(
        "floor_summon",
        "Floor Summoner",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(10), List.of("Summon a mob floor according to player level.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            int xp = player.getLevel();
            List<Function<Location, Entity>> floor = new ArrayList<>();
            do{
                floor = MobManager.buildSpawnFunctions(xp);
                if(floor.isEmpty()){
                    xp--;
                }
            } while (floor.isEmpty());

            if(floor.isEmpty()){
                player.sendMessage("§c[DeltaTech] §7No mobs available for your level.");
                return;
            }
            player.sendMessage("Summoning floor " + xp + "!");

            Location location = player.getTargetBlockExact(100).getLocation().add(0.5, 0.1, 0.5);
            if(location.distance(player.getLocation()) > 100){
                player.sendMessage("§c[DeltaTech] §7Defaulting to player location.");
                location = player.getLocation();
            }

            for(Function<Location, Entity> func : floor){
                Entity e = func.apply(location);
                e.getPersistentDataContainer().set(new NamespacedKey("tower", "necrozma_summoned"), PersistentDataType.BOOLEAN, true);
                if(e instanceof LivingEntity le){
                    le.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(Integer.MAX_VALUE, 0));
                }
            }
            player.setLevel(xp-1);
        }
    );
}
