package com.delta.plugins.advs.whacka_tab;

import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.items.Items;
import com.delta.plugins.items.PitItems;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class Bump_onyx extends BaseAdvancement implements HiddenVisibility {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.whacka_tab_NAMESPACE, "bump_tower_onyx");
    public Bump_onyx(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Items.onyxWhackaBump(), "Guaka oscuro", AdvancementFrameType.CHALLENGE, true, true, 2f, 1f , "Compra un chichón de Onix"), parent, 1);
        registerEvent(InventoryClickEvent.class, event -> {
            if(event.getInventory().getType() == InventoryType.MERCHANT) {
                ItemStack item = event.getInventory().getItem(2);
                if(item == null) return;
                if(item.isSimilar(Items.onyxWhackaBump())) {
                    Player player = (Player) event.getWhoClicked();
                    incrementProgression(player);
                }
            }
        });
    }

    @Override
    public void giveReward(@Nonnull Player player) {
        ItemStack i = PitItems.CoinPaper(256);
        Item it = player.getWorld().dropItemNaturally(player.getLocation(), i);
        it.setPickupDelay(0);
    }
}
