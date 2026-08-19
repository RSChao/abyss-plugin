package com.delta.plugins.advs.pure_hearts;

import com.delta.plugins.Plugin;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.events.events;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Heart_purity extends BaseAdvancement implements HiddenVisibility {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.pure_hearts_NAMESPACE, "heart_purity");

  static ItemStack icon = new ItemStack(Material.LEATHER);
  static {

    ItemMeta meta = icon.getItemMeta();
    meta.setEnchantmentGlintOverride(true);
    meta.setItemModel(NamespacedKey.minecraft("purity_heart"));
    icon.setItemMeta(meta);
  }

  public Heart_purity(Advancement parent, float x, float y) {
    super(KEY.getKey(), new AdvancementDisplay(icon, "Purity Heart", AdvancementFrameType.CHALLENGE, true, true, x, y , "Adquiere el corazón de la pureza,", "representación del amor y la bondad.", "Con él, resistirás el poder", "nacido del abismo"), parent, 1);
    registerEvent(PlayerPickupItemEvent.class,
            event -> {
              if(event.getItem() == null) return;
              if(!event.getItem().getItemStack().getItemMeta().hasItemModel()) return;

              if(event.getItem().getItemStack().getItemMeta().getItemModel().equals(icon.getItemMeta().getItemModel())){
                if(getProgression(event.getPlayer()) >0) return;
                incrementProgression(event.getPlayer());
              }

            });
  }

    @Override
    public void giveReward(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" parent add pure_hearts");
    }
}