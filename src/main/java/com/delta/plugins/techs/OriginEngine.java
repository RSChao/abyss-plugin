package com.delta.plugins.techs;

import com.delta.plugins.events.events;
import com.rschao.events.soulEvents;
import com.rschao.plugins.fightingpp.Plugin;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.register.TechniqueNameManager;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OriginEngine {

    static final String TECH_ID = "oblivion_engine";
    static final com.delta.plugins.Plugin plugin = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class);
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, will);
        TechRegistry.registerTechnique(TECH_ID, deathRavenEye);
        TechRegistry.registerTechnique(TECH_ID, oblivionReloader);
        TechRegistry.registerTechnique(TECH_ID, oblivionRestrainer);
    }

    static Technique will = new Technique("oblivion_will", "Oblivion's Will", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(6), List.of("Conqueror and Armor boost for 10 seconds.")), TargetSelectors.self(), (techUser, target) -> {
        TechRegistry.getById("armor").getAction().execute(techUser, target);
        TechRegistry.getById("conquerors_haki").getAction().execute(techUser, target);
    });

    static Technique deathRavenEye = new Technique("death_ravens_eye", "Death Raven's Eye", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(8), List.of("Gains the ability to use ultimate techniques for 15 seconds.")), TargetSelectors.self(), (techUser, target) -> {
        List<Technique> ultis = new ArrayList<>();
        for(String fruit: Plugin.getAllFruitIDs()){
            for(Technique tech : TechRegistry.getUltimateTechniques(fruit)) {
                ultis.add(tech);
            }
        }
        String techName = "";
        for(String fruit: com.delta.plugins.Plugin.getAllAbyssIDs()){
            for(Technique tech : TechRegistry.getUltimateTechniques(fruit)) {
                ultis.add(tech);
                techName = TechniqueNameManager.getDisplayName(techUser.caster(), tech);
            }
        }
        List<String> dialogue = List.of(
                "The shadows of oblivion grant you power...",
                "Embrace the darkness within...",
                "Let the void guide your hand...",
                "And true chaos shall reign...",
                "",
                ChatColor.WHITE + "Unleashing " + techName + "!"
        );
        for(int i = 0; i < dialogue.size()+1; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                techUser.caster().sendMessage(ChatColor.RED + dialogue.get(finalI));
            }, 2*i);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Random rand = new Random();
            Technique randomUlti = ultis.get(rand.nextInt(ultis.size()));
            randomUlti.getAction().execute(techUser, target);
        }, 2+(2* dialogue.size()));
    });

    static Technique oblivionReloader = new Technique("oblivion_reloader", "Oblivion Reloader", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Removes all technique and hit cooldowns.")), TargetSelectors.self(), (techUser, target) -> {
        Player p = techUser.caster();
        CooldownManager.removeAllCooldowns(p);
        double temp = p.getAttribute(Attribute.ATTACK_SPEED).getBaseValue();
        p.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(temp*20);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(temp);
        }, 30*20);

    });

    static Technique oblivionRestrainer = new Technique("oblivion_restrainer", "Oblivion Restrainer", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(12), List.of("If the target is not in Void Mode, deals massive damage.")), TargetSelectors.self(), (techUser, target) -> {
        Player player = techUser.caster();
        for(Player p: techUser.caster().getWorld().getPlayers()) {
            if(p != techUser.caster() && p.getLocation().distance(techUser.caster().getLocation()) < 50) {
                if(soulEvents.hasSoul(p, 19)) continue;
                if(p.equals(player)) return;
                if(p.getLocation().distance(player.getLocation())>30) return;
                int cooldown = ((new Random()).nextInt(60, 81))*1000;
                for(String id :com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                    for(Technique t : TechRegistry.getNormalTechniques(id)){
                        int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                        CooldownManager.setCooldown(p, t.getId(), events.hasPurityHeart(p)? cooldownFinal/2 : cooldownFinal);
                    }
                }
                for(String id : com.delta.plugins.Plugin.getAllAbyssIDs()){
                    for(Technique t : TechRegistry.getNormalTechniques(id)){
                        int cooldownFinal = Math.max(cooldown, (int) CooldownManager.getRemaining(player, t.getId()));
                        CooldownManager.setCooldown(p, t.getId(), events.hasPurityHeart(p)? cooldownFinal/2 : cooldownFinal);
                    }
                }
            }
        }

    });
}
