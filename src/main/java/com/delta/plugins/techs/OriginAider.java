package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.core.CoreScripts;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.events.events;
import com.delta.plugins.events.techEvents.DestinyBondEvents;
import com.delta.plugins.items.Items;
import com.delta.plugins.items.PitItems;
import com.rschao.events.soulEvents;
import com.rschao.plugins.showdowncore.showdownCore.api.enchantment.CustomEnchantment;
import com.rschao.plugins.showdowncore.showdownCore.api.enchantment.registry.EnchantmentRegistry;
import com.rschao.plugins.showdowncore.showdownCore.api.items.registry.ItemRegistry;
import com.rschao.plugins.showdowncore.showdownCore.api.runnables.ShowdownScript;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class OriginAider {

    /*
     *
     * tech 1:
     * if void: buff 1
     *
     * tech 2:
     * if void: 5s de omninegate
     *
     * tech 3:
     * if void: 3 holy moly
     *
     * tech 4:
     * ptisimo destiny bond de los huevos (9 pops o c muere 1)
     *
     *  */

    static final String TECH_ID = "oblivion_aider";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, OblivionBuff);
        TechRegistry.registerTechnique(TECH_ID, oblivionNegate);
        TechRegistry.registerTechnique(TECH_ID, oblivionHolyMoly);
        TechRegistry.registerTechnique(TECH_ID, oblivionDestinyBond);

    }

    static Technique OblivionBuff = new Technique("oblivion_aider_buff", "Oblivion's Aid", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("")), TargetSelectors.self(), (techUser, target) -> {
        for(Player p: Bukkit.getOnlinePlayers()){
            if(!soulEvents.hasSoul(p, 19)) continue;
            ShowdownScript<Void> buffScript = CoreScripts.buff;
            buffScript.setArgs(p, 1);
            buffScript.run();
        }
    });

    static Technique oblivionNegate = new Technique("oblivion_aider_negate", "Oblivion's Negate", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(30), List.of("")), TargetSelectors.self(), (ctx, target) -> {
        for(Player p: Bukkit.getOnlinePlayers()){
            if(!soulEvents.hasSoul(p, 19)) continue;

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
    });

    static Technique oblivionHolyMoly = new Technique("oblivion_aider_holy_moly", "Oblivion's Holy Moly", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(7), List.of("")), TargetSelectors.self(), (ctx, target) -> {
        for(Player p: Bukkit.getOnlinePlayers()){
            if(!soulEvents.hasSoul(p, 19)) continue;
            ItemStack moly = Items.HolyMoly().clone();
            moly.setAmount(3);
            p.getInventory().addItem(moly);

        }
    });

    static Technique oblivionDestinyBond = new Technique("oblivion_aider_destiny_bond", "Oblivion's Destiny Bond", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("")), TargetSelectors.self(), (ctx, target) -> {

            Player p = getClosestPlayer(ctx.caster().getLocation());
            if(p == null) return;
            DestinyBondEvents.addDestinyBond(ctx.caster(), p);
            hotbarMessage.sendHotbarMessage(ctx.caster(), "Destiny Bonded to " + p.getName() + "!");

    });


    public static Player getClosestPlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(soulEvents.hasSoul(player, 19)) continue;
            boolean hasEmblem = false;
            for(ItemStack item : player.getInventory().getContents()) {
                if(item == null) continue;
                if(item.getItemMeta().hasEnchant(EnchantmentRegistry.getCustomEnchantment("minecraft", "emblem_god_touch"))) {
                    hasEmblem = true;
                    break;
                }
            }
            if (hasEmblem && (new Random()).nextInt(100) > 50) continue;
            if(player.getWorld() != location.getWorld()) continue; // Skip players in different worlds
            if(PlayerTechniqueManager.isInmune(player.getUniqueId())) continue;
            double distance = player.getLocation().distance(location);
            if (distance > 1 && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;

            }
        }

        return closestPlayer;
    }
}
