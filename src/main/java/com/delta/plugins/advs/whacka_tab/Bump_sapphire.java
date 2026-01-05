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

public class Bump_sapphire extends BaseAdvancement implements HiddenVisibility {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_tower_sapph");
    public Bump_sapphire(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.sapphireWhackaBump(), "Guaka de la Fuente Sagrada", AdvancementFrameType.CHALLENGE, true, true, 1f, 3f , "Encuentra un chichón en el agua", "", "§6Recompensa:", "§6- Chichón de Zafiro"), parent, 1);
        registerEvent(PlayerInteractAtEntityEvent.class, event -> {
            if(event.getRightClicked() instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
                ItemStack itemInFrame = itemFrame.getItem();
                if(itemInFrame.isSimilar(Items.sapphireWhackaBump())) {
                    event.setCancelled(true);
                    incrementProgression(event.getPlayer());
                }
            }
        });
    }

    @Override
    public void giveReward(Player player) {
        Item i = player.getWorld().dropItemNaturally(player.getLocation(), Items.sapphireWhackaBump());
        i.setPickupDelay(0);
    }
}
