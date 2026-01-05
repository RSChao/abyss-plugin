package com.delta.plugins.techs;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

import java.util.List;

public class berserker {
    static final String GROUP_ID = "berserker";
    public static void registerTechniques() {
        TechRegistry.registerTechnique(GROUP_ID, rage);
        TechRegistry.registerTechnique(GROUP_ID, fury);
        TechRegistry.registerTechnique(GROUP_ID, berserk);
        Plugin.registerAbyssID(GROUP_ID);
    }
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);

    static Technique rage = new Technique(
        "rage",
        "Berserker's Rage",
        new TechniqueMeta(false, 240000, List.of("Temporary high strength.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120 * 20, 4, false, false));
            hotbarMessage.sendHotbarMessage(player, "You have used the Berserker's Rage technique!");
        }
    );

    static Technique fury = new Technique(
        "fury",
        "Berserker's Fury",
        new TechniqueMeta(false, 600000, List.of("Massive attack speed boost temporarily.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(999);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4);
                }, 90 * 20);
            }, 2);
            hotbarMessage.sendHotbarMessage(player, "You have used the Berserker's Fury technique!");
        }
    );

    static Technique berserk = new Technique(
        "berserk",
        "Full Berserk",
        new TechniqueMeta(false, 300000, List.of("Cleanse negative effects and gain strength.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            org.bukkit.entity.Player player = ctx.caster();
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 1, false, false));
            //remove negative effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().getCategory() == PotionEffectTypeCategory.HARMFUL) {
                    player.removePotionEffect(effect.getType());
                }
            }
            hotbarMessage.sendHotbarMessage(player, "You have used the Berserker's Berserk technique!");
        }
    );
}
