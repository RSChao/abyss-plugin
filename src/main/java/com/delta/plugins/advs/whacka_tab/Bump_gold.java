package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.items.Items;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class Bump_gold extends BaseAdvancement implements HiddenVisibility {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_tower_gold");
    public Bump_gold(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.goldWhackaBump(), "Guaka de oro puro", AdvancementFrameType.CHALLENGE, true, true, 2f, 7f , "Completa la Torre del Suicidio", "", "§6Recompensa:", "§6- ???"), parent, 1);
        registerEvent(PlayerPickupItemEvent.class, event -> {
            if(event.getItem().getItemStack().isSimilar(Items.goldWhackaBump())) {
                incrementProgression(event.getPlayer());
            }
        });
    }
}
