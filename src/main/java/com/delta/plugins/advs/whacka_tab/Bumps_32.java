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

public class Bumps_32 extends BaseAdvancement {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bumps_32");


    public Bumps_32(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.silverWhackaBump(), "32 chichones", AdvancementFrameType.GOAL, true, true, 4f, 0f , "Me estas asustando"), parent, 1);
        registerEvent(PlayerPickupItemEvent.class, event -> {
            int bumpCount = 0;
            for(ItemStack item : event.getPlayer().getInventory().getContents()){
                if(item != null && item.isSimilar(Items.whacka_bump)){
                    bumpCount += item.getAmount();
                }
            }

            if(bumpCount >31){

                incrementProgression(event.getPlayer());
            }
        });
    }
}
