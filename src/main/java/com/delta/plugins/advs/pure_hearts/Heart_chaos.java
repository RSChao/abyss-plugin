package com.delta.plugins.advs.pure_hearts;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.rschao.items.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Heart_chaos extends BaseAdvancement implements HiddenVisibility {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.pure_hearts_NAMESPACE, "heart_chaos");

  static ItemStack icon = new ItemStack(Material.LEATHER);
  static {

    ItemMeta meta = icon.getItemMeta();
    meta.setEnchantmentGlintOverride(true);
    meta.setItemModel(Items.ChaosHeart.getItemMeta().getItemModel());
    icon.setItemMeta(meta);
  }

  public Heart_chaos(Advancement parent, float x, float y) {
    super(KEY.getKey(), new AdvancementDisplay(icon, "Corazón del Caos", AdvancementFrameType.CHALLENGE, true, true, x, y , "Adquiere el corazón del Caos,", "representación del odio y el vacío.", "Con él, el poder nacido del abismo se", "verá potenciado"), parent, 1);
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

    @Override
    public void giveReward(Player player) {
        if(events.hasPurityHeart(player)) return;
        player.sendMessage("Has salvado todos los mundos, sólo para convertirte en la nueva amenaza. Mereció la pena?");
    }
}