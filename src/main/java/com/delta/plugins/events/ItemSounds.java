package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.rschao.items.Items;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ItemSounds {
    private static final List<ItemStack> items = new ArrayList<>();
    public static void init(Player p){
        setList();

        new BukkitRunnable(){
            @Override
            public void run(){
                if(!p.isOnline()){
                    this.cancel();
                    return;
                }
                for(Entity entity : p.getNearbyEntities(20, 20, 20)){
                    if(entity instanceof Item i){
                        if(items.contains(i.getItemStack())){
                            Color color = null;

                        }
                    }
                }
            }
        }.runTaskTimer(Plugin.getPlugin(Plugin.class), 0, 2);


    }
    static void setList(){
        // Añadir todos los "pure hearts" disponibles
        items.add(Items.PurityHeart); // clase externa com.rschao.items.Items

        // pure_heart_* (con item model pure_heart_<color>)
        items.add(com.delta.plugins.items.Items.pure_heart_red);
        items.add(com.delta.plugins.items.Items.pure_heart_brown);
        items.add(com.delta.plugins.items.Items.pure_heart_blue);
        items.add(com.delta.plugins.items.Items.pure_heart_cyan);
        items.add(com.delta.plugins.items.Items.pure_heart_purple);
        items.add(com.delta.plugins.items.Items.pure_heart_pink);
        items.add(com.delta.plugins.items.Items.pure_heart_yellow);
        items.add(com.delta.plugins.items.Items.pure_heart_grey);

        // pureheart_* (originales con persistent data "pureheart" = color)
        items.add(com.delta.plugins.items.Items.pureheart_red);
        items.add(com.delta.plugins.items.Items.pureheart_orange);
        items.add(com.delta.plugins.items.Items.pureheart_yellow);
        items.add(com.delta.plugins.items.Items.pureheart_green);
        items.add(com.delta.plugins.items.Items.pureheart_blue);
        items.add(com.delta.plugins.items.Items.pureheart_indigo);
        items.add(com.delta.plugins.items.Items.pureheart_purple);
        items.add(com.delta.plugins.items.Items.pureheart_white);
        items.add(Items.PurityHeart);
        items.add(Items.ChaosHeart);
    }
}
