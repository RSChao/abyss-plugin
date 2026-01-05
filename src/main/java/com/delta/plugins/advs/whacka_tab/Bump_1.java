package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.items.Items;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class Bump_1 extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_1");


  public Bump_1(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Items.whacka_bump, "Guaka-Auu!", AdvancementFrameType.TASK, true, true, 1f, 0f , "Obtén un chichón de Guaka. ¡Pobrecito!"), parent, 1);
      registerEvent(PlayerPickupItemEvent.class, event -> {
          for(ItemStack item : event.getPlayer().getInventory().getContents()){
              if(item != null && item.isSimilar(Items.whacka_bump)){
                  incrementProgression(event.getPlayer());
              }
          }
      });
  }
}