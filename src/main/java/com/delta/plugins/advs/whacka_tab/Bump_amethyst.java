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

public class Bump_amethyst extends BaseAdvancement implements HiddenVisibility {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_tower_amethyst");
    public Bump_amethyst(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.amethystWhackaBump(), "Guaka secreto", AdvancementFrameType.CHALLENGE, true, true, 1f, 5f , "Encuentra un chichón entre pisos", "", "§6Recompensa:", "§6- Chichón de Amatista"), parent, 1);
        registerEvent(PlayerInteractAtEntityEvent.class, event -> {
            if(event.getRightClicked() instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
                ItemStack itemInFrame = itemFrame.getItem();
                if(itemInFrame.isSimilar(Items.amethystWhackaBump())) {
                    event.setCancelled(true);
                    incrementProgression(event.getPlayer());
                }
            }
        });
    }

    @Override
    public void giveReward(Player player) {
        Item i = player.getWorld().dropItemNaturally(player.getLocation(), Items.amethyst_whacka_bump);
        i.setPickupDelay(0);
    }
}
