package com.delta.plugins.whacka;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.items.Items;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.tracker.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

import static com.delta.plugins.mobs.custom.Whacka_1_12_10.FRIEND_KEY;
import static com.delta.plugins.mobs.custom.Whacka_1_12_10.WHACKA_KEY;

public class WhackaManager {
    private static Silverfish whacka = null;
    private static Location currentSpawn = null;
    private static long nextSpawnTime = 0L; // system time in ms
    static boolean spawnedOnce = false;

    public static void init() {
    }

    public static void assignRandomSpawn() {
        List<Location> locs = WhackaLocationManager.getLocations();
        if (locs.isEmpty()) {
            currentSpawn = null;
            return;
        }
        currentSpawn = locs.get(new Random().nextInt(locs.size()));
    }

    public static Location getCurrentSpawn() {
        return currentSpawn;
    }

    public static void trySpawnWhackaIfPlayerNearby() {
        if (currentSpawn == null) return;
        if (System.currentTimeMillis() < nextSpawnTime) return; // cooldown check
        if (getWhacka() != null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {

            if (p.getWorld().equals(currentSpawn.getWorld()) && p.getLocation().distance(currentSpawn) <= 50) {
                if(PitEvents.getFloor(p) != 0) continue; // don't spawn if any player is in the Pit
                if(whacka != null && whacka.isValid()) return; // already spawned
                spawnWhacka(currentSpawn);
                try{
                    if(!spawnedOnce){
                        WhackaListener.whackaTask.runTaskTimer(Plugin.getPlugin(Plugin.class), 20, 2);
                        spawnedOnce = true;
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Failed to spawn Whacka: " + e.getMessage());
                }
                break;
            }
        }
    }

    public static void spawnWhacka(Location loc) {
        if (whacka != null && whacka.isValid()) whacka.remove();
        whacka = (Silverfish) spawnWhackaEntity(loc);
    }


    public static Entity spawnWhackaEntity(Location loc) {
        Silverfish whack = loc.getWorld().spawn(loc, Silverfish.class);
        whack.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(10.0);
        whack.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200.0);
        whack.setHealth(200.0);
        whack.setCustomName("Whacka");
        whack.getPersistentDataContainer().set(WHACKA_KEY, PersistentDataType.INTEGER, 0);
        //whack.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 0));
        whack.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, 0));
        whack.getEquipment().setHelmet(Items.whackaBump());
        whack.getEquipment().setHelmetDropChance(100);
        /*ModeledEntity whackModel = ModelEngineAPI.createModeledEntity(whack);
         ActiveModel model = ModelEngineAPI.createActiveModel("whacka");
         whackModel.addModel(model, true);*/
        whack.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 0));
        EntityTracker tracker = BetterModel.model("whacka")
                .map(r -> r.getOrCreate(whack)) //Gets or creates entity tracker by this renderer to some entity.
                .orElse(null);

        return whack;
    }


    public static Entity spawnWhackaFriendEntity(Location loc, Player p) {
        Wolf whack = loc.getWorld().spawn(loc, Wolf.class);
        whack.setOwner(p);
        whack.setTamed(true);
        whack.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(10.0);
        whack.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200.0);
        whack.setHealth(200.0);
        whack.setCustomName("Whacka");
        setBumpDrop(whack);
        whack.getEquipment().setHelmetDropChance(100);
        whack.getPersistentDataContainer().set(WHACKA_KEY, PersistentDataType.INTEGER, 0);
        whack.getPersistentDataContainer().set(FRIEND_KEY, PersistentDataType.BOOLEAN, true);
        //whack.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 0));
        whack.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, 0));
        whack.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(Integer.MAX_VALUE, 0));
        EntityTracker tracker = BetterModel.model("whacka")
                .map(r -> r.getOrCreate(whack)) //Gets or creates entity tracker by this renderer to some entity.
                .orElse(null);
        /*ModeledEntity whackModel = ModelEngineAPI.createModeledEntity(whack);
         ActiveModel model = ModelEngineAPI.createActiveModel("whacka");
         whackModel.addModel(model, true);*/

        return whack;
    }

    public static Silverfish getWhacka() {
        return (whacka != null && whacka.isValid() && !whacka.isDead()) ? whacka : null;
    }

    public static void removeWhacka() {
        if (whacka != null) whacka.remove();
        whacka = null;
    }
    public static void setBumpDrop(LivingEntity whack){
        ItemStack drop = Items.whackaBump();
        ItemMeta meta = drop.getItemMeta();
        if(!whack.getName().equals("Whacka")) meta.setDisplayName(whack.getCustomName() + "'s Bump");
        drop.setItemMeta(meta);
        whack.getEquipment().setHelmet(drop);
    }

    public static void setCooldown(long millis) {
        nextSpawnTime = System.currentTimeMillis() + millis;
    }
}
