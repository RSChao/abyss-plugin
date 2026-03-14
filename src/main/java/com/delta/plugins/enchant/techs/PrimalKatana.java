package com.delta.plugins.enchant.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.enchant.PrimalOblivion;
import com.rschao.enchants.OblivionEnchant;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
// ...existing code...
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.delta.plugins.particles.SlashEffect;

import java.util.List;

public class PrimalKatana {
    static final String TECH_ID = "divine_primal_katana";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        TechRegistry.registerTechnique(TECH_ID, oblivionSlash);
        TechRegistry.registerTechnique(TECH_ID, change_model);
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
            }
            else if(m.equals("oblivion_katana_r")) {
                meta.setItemModel(NamespacedKey.minecraft("oblivion_katana_l"));
                i.setItemMeta(meta);
                p.sendMessage("Switched to left hand model!");
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

            // Play the slash animation in front of the player
            try {
                SlashEffect eff = new SlashEffect(Plugin.getEffectManager());
                eff.setTargetPlayer(p);
                eff.start();
            } catch (Throwable ignored) {
                Bukkit.getLogger().severe("Effect bugged out");
            }

            // Damage players inside the player's field of view (<=7 blocks, within angle)
            double maxDist = 8.0;
            double maxAngleDeg = 45.0; // 90-degree cone total

            Location eye = p.getEyeLocation();
            Vector dir = eye.getDirection().clone().normalize();

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
}
