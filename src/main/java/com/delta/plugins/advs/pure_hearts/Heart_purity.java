package com.delta.plugins.advs.pure_hearts;

import com.delta.plugins.Plugin;
import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.events.events;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.visibilities.HiddenVisibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Heart_purity extends BaseAdvancement implements HiddenVisibility {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.pure_hearts_NAMESPACE, "heart_purity");

  static ItemStack icon = new ItemStack(Material.LEATHER);
  static {

    ItemMeta meta = icon.getItemMeta();
    meta.setEnchantmentGlintOverride(true);
    meta.setItemModel(NamespacedKey.minecraft("purity_heart"));
    icon.setItemMeta(meta);
  }

  public Heart_purity(Advancement parent, float x, float y) {
    super(KEY.getKey(), new AdvancementDisplay(icon, "Corazón de la Pureza", AdvancementFrameType.CHALLENGE, true, true, x, y , "Adquiere el corazón de la pureza,", "representación del amor y la bondad.", "Con él, resistirás el poder", "nacido del abismo"), parent, 1);
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
        FileConfiguration config = Plugin.getPlugin(Plugin.class).getConfig();
        List<String> abyss = config.getStringList(player.getName() + ".groupids");
        if(abyss.contains("devourer")){
            for(int i = 0; i<abyss.size();i++){
                if(abyss.get(i).equals("devourer")){
                    abyss.set(i, "redeemed");
                }
            }
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user "+player.getName()+" parent add pure_hearts");
        if(events.hasChaosHeart(player)) return;
        player.sendMessage(ChatColor.GOLD + "Sabía que harías lo correcto. Me alegro de que así sea, héroe de todos los mundos.");
    }
}