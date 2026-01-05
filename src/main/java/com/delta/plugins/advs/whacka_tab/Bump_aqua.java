package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.items.Items;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Bump_aqua extends BaseAdvancement implements HiddenVisibility {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_tower_aqua");


    public Bump_aqua(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.aquamarineWhackaBump(), "Guaka del Cielo Azul", AdvancementFrameType.CHALLENGE, true, true, 1f, 2f , "Obtén un chichón donde no brilla el sol", "", "§6Recompensa:", "§6- Chichón de Aguamarina"), parent, 1);
        registerEvent(PlayerInteractAtEntityEvent.class, event -> {
            if(event.getRightClicked() instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
                ItemStack itemInFrame = itemFrame.getItem();
                if(itemInFrame.isSimilar(Items.aquamarineWhackaBump())) {
                    event.setCancelled(true);
                    incrementProgression(event.getPlayer());
                }
            }
        });
    }


    @Override
    public void giveReward(Player player) {
        Item i = player.getWorld().dropItemNaturally(player.getLocation(), Items.aquamarineWhackaBump());
        i.setPickupDelay(0);
    }
}
