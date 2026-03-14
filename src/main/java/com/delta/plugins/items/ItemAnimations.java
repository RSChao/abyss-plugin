package com.delta.plugins.items;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemAnimations {
    private static final Map<String, ItemStack> heartMap = new LinkedHashMap<>();
    static {
        heartMap.put("red", Items.pure_heart_red);
        heartMap.put("brown", Items.pure_heart_brown);
        heartMap.put("blue", Items.pure_heart_blue);
        heartMap.put("cyan", Items.pure_heart_cyan);
        heartMap.put("purple", Items.pure_heart_purple);
        heartMap.put("pink", Items.pure_heart_pink);
        heartMap.put("yellow", Items.pure_heart_yellow);
        heartMap.put("grey", Items.pure_heart_grey);

    }

    private static List<Item> spawnedItems;

    public static void pureHeartOwnedAnimation(Player player, int radius, Location baseLoc) {
        List<Location> heartLocs = Arrays.asList(
                baseLoc.clone().add(-radius, 0, radius),
                baseLoc.clone().add(-radius, 0, 0),
                baseLoc.clone().add(-radius, 0, -radius),
                baseLoc.clone().add(0, 0, radius),
                baseLoc.clone().add(0, 0, -radius),
                baseLoc.clone().add(radius, 0, radius),
                baseLoc.clone().add(radius, 0, 0),
                baseLoc.clone().add(radius, 0, -radius)
        );

        for(int i = 0; i < heartLocs.size(); i++) {
            ItemStack heart = heartMap.values().stream().skip(i).findFirst().orElse(null);
            if (heart != null) {
                boolean hasHeart = false;
                for(ItemStack invHeart : player.getInventory().getContents()) {
                    if (invHeart != null && invHeart.isSimilar(heart)) {
                        hasHeart = true;
                        break;
                    }
                }
                if(!hasHeart) continue; // Si el jugador no tiene este corazón, no lo spawneamos
                Item spawned = player.getWorld().dropItem(heartLocs.get(i), heart);
                spawned.setPickupDelay(Integer.MAX_VALUE); // Evita que los items puedan ser recogidos
                spawned.setGravity(false);
                spawnedItems.add(spawned);
            }
        }

    }

    public static void disablePureHeartAnimation() {
        for(Item item : spawnedItems) {
            item.remove();
        }
        spawnedItems.clear();
    }
}
