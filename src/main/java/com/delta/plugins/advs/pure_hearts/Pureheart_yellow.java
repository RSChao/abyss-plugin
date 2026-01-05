package com.delta.plugins.advs.pure_hearts;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pureheart_yellow extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.pure_hearts_NAMESPACE, "pureheart_yellow");
  static ItemStack icon = new ItemStack(Material.BLAZE_POWDER);
  static {

    ItemMeta meta = icon.getItemMeta();
    meta.setEnchantmentGlintOverride(true);
    meta.setItemModel(NamespacedKey.minecraft("pure_heart_yellow"));
    icon.setItemMeta(meta);
  }

  public Pureheart_yellow(Advancement parent, float x, float y) {
    super(KEY.getKey(), new AdvancementDisplay(icon, "Corazón Puro: Amarillo", AdvancementFrameType.TASK, true, true, x, y , "Adquiere el corazón puro amarillo,", "hallado en un Showdown de", "tiempos remotos"), parent, 1);
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
}