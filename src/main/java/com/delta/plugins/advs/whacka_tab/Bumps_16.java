package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.items.Items;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class Bumps_16 extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bumps_16");


  public Bumps_16(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Items.bronzeWhackaBump(), "Dos chichones rojos", AdvancementFrameType.GOAL, true, true, 3f, 0f , "Eres preocupantemente malvado"), parent, 1);
      registerEvent(PlayerPickupItemEvent.class, event -> {
          int bumpCount = 0;
          for(ItemStack item : event.getPlayer().getInventory().getContents()){
              if(item != null && item.isSimilar(Items.rare_whacka_bump)){
                  bumpCount += item.getAmount();
              }
          }

          if(bumpCount >1){

              incrementProgression(event.getPlayer());
          }
      });
  }
}