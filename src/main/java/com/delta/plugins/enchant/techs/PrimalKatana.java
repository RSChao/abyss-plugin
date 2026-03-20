package com.delta.plugins.enchant.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.enchant.PrimalOblivion;
import com.delta.plugins.events.BossEvents;
import com.delta.plugins.events.definition.KatanaSheathEvent;
import com.delta.plugins.events.events;
import com.delta.plugins.techs.roaring_soul;
import com.rschao.enchants.OblivionEnchant;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrimalKatana {
    static final String TECH_ID = "divine_primal_katana";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, change_model);
        TechRegistry.registerTechnique(TECH_ID, oblivionSlash);
        TechRegistry.registerTechnique(TECH_ID, sheathOfOblivion);
        TechRegistry.registerTechnique(TECH_ID, witheringWorld);
        TechRegistry.registerTechnique(TECH_ID, EternalGlitch);
        TechRegistry.registerTechnique(TECH_ID, forgotten_magic);

    }

    static Technique change_model = new Technique("change_model", "Change Model", new TechniqueMeta(false, 0, List.of("Changes the model of the weapon")), TargetSelectors.self(), (ctx, token) ->{
        Player p = ctx.caster();
        ItemStack i = p.getInventory().getItemInMainHand();
        if(i.containsEnchantment(new PrimalOblivion().getCustomEnchantment().toBukkitEnchantment())) {
            String m = i.getItemMeta().getItemModel().getKey();
            ItemMeta meta = i.getItemMeta();
            if(m.equals("oblivion_katana_l")) {
                meta.setItemModel(NamespacedKey.minecraft("oblivion_katana_r"));
                i.setItemMeta(meta);
                p.sendMessage("Switched to right hand model!");
                if(p.getMainHand().equals(MainHand.RIGHT)){
                    KatanaSheathEvent sheathEvent = new KatanaSheathEvent(p);
                    Bukkit.getPluginManager().callEvent(sheathEvent);
                }
            }
            else if(m.equals("oblivion_katana_r")) {
                meta.setItemModel(NamespacedKey.minecraft("oblivion_katana_l"));
                i.setItemMeta(meta);
                p.sendMessage("Switched to left hand model!");
                if(p.getMainHand().equals(MainHand.LEFT)){
                    KatanaSheathEvent sheathEvent = new KatanaSheathEvent(p);
                    Bukkit.getPluginManager().callEvent(sheathEvent);
                }
            }
        } else {
            p.sendMessage("You must have the Primal Oblivion enchantment to use this technique.");
        }

     });

    static Technique oblivionSlash = new Technique(
        "oblivion_slash",
        "Oblivion Slash",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Unleash a slashing particle and damage players in your FOV (7 blocks).")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if (p == null) return;

            // Damage players inside the player's field of view (<=7 blocks, within angle)
            double maxDist = 8.0;
            double maxAngleDeg = 45.0; // 90-degree cone total

            Location eye = p.getEyeLocation();
            Vector dir = eye.getDirection().clone().normalize();
            spawnSlashEffect(p);
            for (Player t : p.getWorld().getPlayers()) {
                if (t == null || !t.isValid() || t.equals(p)) continue;
                if (t.getLocation().distance(p.getLocation()) > maxDist) continue;

                Vector to = t.getEyeLocation().toVector().subtract(eye.toVector());
                if (to.lengthSquared() < 0.0001) continue;
                to = to.normalize();
                double dot = dir.dot(to);
                if (Double.isNaN(dot)) continue;
                double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
                if (angle <= maxAngleDeg) {
                    try {
                        t.damage(300.0, p);
                        OblivionEnchant.oblivion(p, t);
                    } catch (Throwable ignored) {}
                }
            }
        }
    );

    public static void spawnSlashEffect(Player p) {
        World world = p.getWorld();
        Vector direction = p.getLocation().getDirection().normalize();
        Location center = p.getLocation();
        for (int i = -2; i <= 2; i++) {
            Vector spread = direction.clone().rotateAroundY(i * Math.PI / 16);
            Arrow arrow = world.spawnArrow(center.clone().add(0, 1.5, 0), spread, 2.0f, 0.1f);
            arrow.setDamage(0);
        }
    }

    static Technique sheathOfOblivion = new Technique(
        "sheath_of_death",
        "Sheath of Massacre",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Sheath your katana to kill nearby opponents.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            BossEvents.isSheathTechOn.put(p, true);
        }
    );

    static Technique witheringWorld = new Technique(
        "withering_world",
        "Withering World",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Create a withering world around you that damages and withers enemies.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            Location center = p.getLocation();
            double radius = 300.0;
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                        if (ticks++ >= 20 *40) { // Lasts for 10 seconds
                            this.cancel();
                            return;
                        }
                    for (Player t : p.getWorld().getPlayers()) {
                        if (t == null || !t.isValid() || t.equals(p)) continue;
                        if (t.getLocation().distance(center) > radius) continue;
                        try {
                            p.addPotionEffect(PotionEffectType.WITHER.createEffect(20 * 5, 1));
                        } catch (Throwable ignored) {}
                    }
                }
            }.runTaskTimer(plugin, 0, 1); // Run every second
        }
    );

    static Technique EternalGlitch = new Technique(
        "eternal_glitch",
        "Eternal Glitch",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Glitch your victims.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            BossEvents.isGlitchTechOn.put(p, true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> BossEvents.isGlitchTechOn.put(p, false), 20*20);
        }
    );

    static Technique forgotten_magic = new Technique(
        "forgotten_magic",
        "Forgotten Magic",
        new TechniqueMeta(true, cooldownHelper.minutesToMiliseconds(15), List.of("A forgotten magic that has yet to be discovered.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            Player target = roaring_soul.getClosestPlayer(p.getLocation());
            if (target == null) return;
            int abysses = events.getGroupIdCount(p);
            List<String> ids = new ArrayList<>();
            for(int i = 0; i < abysses; i++) {
                ids.add(events.getGroupId(p, i));
            }
            Random rand = new Random();
            int random = rand.nextInt(ids.size());
            List<String> ids2 = new ArrayList<>();
            for(int i = 0; i < ids.size(); i++) {
                ids2.add(ids.get(i));
            }
            ids2.remove(random);
            plugin.getConfig().set(target.getUniqueId() + ".groupids", ids2);
            plugin.saveConfig();
            plugin.reloadConfig();
            target.sendMessage("Your magic " + ids.get(random) + " has been forgotten.");
            Bukkit.getScheduler().runTaskLater(plugin, () ->{
                plugin.getConfig().set(target.getUniqueId() + ".groupids", ids);
                plugin.saveConfig();
                plugin.reloadConfig();
                target.sendMessage("Your magic has been restored.");
            }, 20*60*5);
        }
    );
}
