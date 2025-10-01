package com.delta.plugins.mobs;

import com.delta.plugins.whacka.WhackaManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobManager {

    public static List<Entity> getEntitiesByFloor(Location location, int floor) {
        List<Entity> entities = new ArrayList<>();

        switch (floor) {
            case 1:
                entities.add(baseZombie(location));
                break;
            case 2:
                entities.add(baseZombie(location));
                entities.add(baseSkeleton(location));
                break;
            case 3:
                entities.add(baseZombie(location));
                entities.add(baseZombie(location));
                entities.add(baseZombie(location));
                entities.add(baseSkeleton(location));
                break;
            case 4:
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
            case 5:
                entities.add(baseZombie(location));
                entities.add(baseZombie(location));
                entities.add(baseSpider(location));
                entities.add(baseSpider(location));
                break;
            case 6:
                entities.add(baseZombie(location));
                entities.add(baseZombie(location));
                entities.add(baseZombie(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                break;
            case 7:
                entities.add(baseZombie(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSpider(location));
                break;
            case 8:
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                entities.add(baseSkeleton(location));
                break;
            case 9:
                entities.add(ironZombie(location));
                break;
            case 10:
                break;
            case 11:
                for(int i = 0; i < 7; i++){
                    entities.add(baseZombie(location));
                }

                for(int i = 0; i < 4; i++){
                    entities.add(baseSkeleton(location));
                }
                break;
            case 12:
                repeatTimes(11, () -> entities.add(baseHusk(location)));
                break;
            case 13:
                repeatTimes(13, () -> entities.add(baseHusk(location)));
                repeatTimes(5, () -> entities.add(baseSkeleton(location)));
                break;
            case 14:
                repeatTimes(18, () -> entities.add(baseHusk(location)));
                repeatTimes(8, () -> entities.add(slime(location)));
                break;
            case 15:
                repeatTimes(8, () -> entities.add(baseHusk(location)));
                repeatTimes(8, () -> entities.add(baseSkeleton(location)));
                repeatTimes(8, () -> entities.add(baseSpider(location)));
                break;
            case 16:
                repeatTimes(5, () -> entities.add(baseHusk(location)));
                repeatTimes(5, () -> entities.add(baseSkeleton(location)));
                repeatTimes(2, () -> entities.add(slime(location)));
                repeatTimes(8, () -> entities.add(baseSpider(location)));
                break;
            case 17:
                repeatTimes(2, () -> entities.add(diamondZombie(location)));
                repeatTimes(1, () -> entities.add(nethZombie(location)));
                entities.add(WhackaManager.spawnWhackaEntity(location));
                break;
            case 18:
                break;
        }

        int i = (new Random()).nextInt(0, entities.size());
        Entity entity = entities.get(i);
        entity.getPersistentDataContainer().set(new NamespacedKey("tower", "key"), PersistentDataType.BOOLEAN, true);
        entities.set(i, entity);


        return entities;
    }

    static void repeatTimes(int times, Runnable action) {
        for (int i = 0; i < times; i++) {
            action.run();
        }
    }

    static Entity baseZombie(Location loc){
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(null);
        z.getEquipment().clear();

        return z;
    }


    static Entity baseHusk(Location loc){
        Husk z = loc.getWorld().spawn(loc, Husk.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(null);
        z.getEquipment().clear();

        return z;
    }

    static Entity ironZombie(Location loc){
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().clear();
        ItemStack helm = new ItemStack(Material.IRON_HELMET);
        ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
        ItemStack legs = new ItemStack(Material.IRON_LEGGINGS);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        ItemStack[] armor = {boots, legs, chest, helm};
        z.getEquipment().setArmorContents(armor);
        z.getEquipment().setItemInMainHand(null);

        return z;
    }

    static Entity diamondZombie(Location loc){
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().clear();
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemStack[] armor = {boots, legs, chest, helm};
        z.getEquipment().setArmorContents(armor);
        z.getEquipment().setItemInMainHand(null);

        return z;
    }

    static Entity nethZombie(Location loc){
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().clear();
        ItemStack helm = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chest = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack legs = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        ItemStack[] armor = {boots, legs, chest, helm};
        z.getEquipment().setArmorContents(armor);
        z.getEquipment().setItemInMainHand(null);

        return z;
    }

    static Entity baseSkeleton(Location loc){
        Skeleton z = loc.getWorld().spawn(loc, Skeleton.class);

        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));

        return z;
    }

    static Entity baseSpider(Location loc){
        Spider z = loc.getWorld().spawn(loc, Spider.class);
        z.getActivePotionEffects().clear();

        return z;
    }

    static Entity slime(Location loc){
        Slime z = loc.getWorld().spawn(loc, Slime.class);
        z.setSize(2);

        return z;
    }
}
