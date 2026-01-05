package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class Bump_two extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_two");


  public Bump_two(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Items.rare_whacka_bump, "8 guakas bonkeados", AdvancementFrameType.TASK, true, true, 2f, 0f , "Derrota a 8 guakas inocentes y obtén un chichón raro. Pobres Guakas, ¿qué han hecho?"), parent, 1);
      registerEvent(PlayerPickupItemEvent.class, event -> {
          for(ItemStack item : event.getPlayer().getInventory().getContents()){
              if(item == null) continue;
              if(item.getType() != Material.COOKIE) continue;
              if(!item.hasItemMeta()) continue;
              if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "rare_whacka_bump"),
                      org.bukkit.persistence.PersistentDataType.BOOLEAN)){
                  incrementProgression(event.getPlayer());
              }
          }
      });
  }
}