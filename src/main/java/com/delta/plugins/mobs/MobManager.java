package com.delta.plugins.mobs;

import com.delta.plugins.whacka.WhackaManager;
import com.rschao.events.events;
import com.rschao.items.weapons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class MobManager {

    public static List<Entity> getEntitiesByFloor(Location location, int floor) {
        List<Location> single = new ArrayList<>();
        single.add(location);
        return getEntitiesByFloor(single, floor);
    }

    public static List<Entity> getEntitiesByFloor(List<Location> locations, int floor) {
        List<Entity> entities = new ArrayList<>();
        if (locations == null || locations.isEmpty()) {
            return entities; // Dejar que el llamador haga fallback si lo desea
        }

        // Construye la "plantilla" de spawns (funciones que crean una entidad dado un Location)
        List<Function<Location, Entity>> templates = buildSpawnFunctions(floor);

        // Si no hay plantillas (piso sin mobs definidos), devolver vacío para que el llamador haga fallback
        if (templates.isEmpty()) return entities;

        try {
            int playersOnFloor = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                try {
                    Integer pf = p.getPersistentDataContainer().get(new NamespacedKey("tower", "floor"), PersistentDataType.INTEGER);
                    if (pf != null && pf == floor) playersOnFloor++;
                } catch (Exception ignored) {}
            }
            Function<Location, Entity> elderFunc = MobManager::baseElderGuardian;

            if (playersOnFloor > 1 && templates.size() > 40) {
                int originalSize = templates.size();
                int targetSize = Math.max(1, (int) Math.ceil((double) originalSize / playersOnFloor));
                // mezclar y recortar
                Collections.shuffle(templates, new Random());
                templates = new ArrayList<>(templates.subList(0, Math.min(targetSize, templates.size())));

                // Asegurar que si el piso tenía un Elder Guardian (caso 25), quede al menos uno
                if (floor == 25) {
                    boolean hasElder = false;
                    for (Function<Location, Entity> f : templates) {
                        if (f == elderFunc) { hasElder = true; break; }
                    }
                    // Si tras la reducción no queda elder, añadir uno
                    if (!hasElder) {
                        templates.add(0, MobManager::baseElderGuardian);
                    }
                }
            }
        } catch (Exception ignored) {}

        Random rnd = new Random();
        // Asigna cada plantilla a una ubicación de la lista (aleatorio)
        for (Function<Location, Entity> func : templates) {
            Location chosen = locations.get(rnd.nextInt(locations.size()));
            try {
                Entity e = func.apply(chosen);
                if (e != null) entities.add(e);
            } catch (Exception ex) {
                // Si una creación falla, ignorar esa entidad pero continuar con las demás
            }
        }

        // Aplica metadata como antes
        for (Entity entity : entities) {
            entity.getPersistentDataContainer().set(new NamespacedKey("tower", "floor"), PersistentDataType.INTEGER, floor);
            entity.setPersistent(true);
        }

        if (!entities.isEmpty()) {
            for(Entity e : entities){
                if(e instanceof ElderGuardian){
                    e.getPersistentDataContainer().set(new NamespacedKey("tower", "key"), PersistentDataType.BOOLEAN, true);
                    return entities;

                }
            }

            int i = rnd.nextInt(entities.size());
            Entity entity = entities.get(i);
            entity.getPersistentDataContainer().set(new NamespacedKey("tower", "key"), PersistentDataType.BOOLEAN, true);
            entities.set(i, entity);
        }

        return entities;
    }

    // Construye la "plantilla" de spawns (funciones que crean una entidad dado un Location)
    public static List<Function<Location, Entity>> buildSpawnFunctions(int floor) {
        List<Function<Location, Entity>> funcs = new ArrayList<>();

        switch (floor) {
            case 1:
                funcs.add(MobManager::baseZombie);
                break;
            case 2:
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseSkeleton);
                break;
            case 3:
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseSkeleton);
                break;
            case 4:
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                break;
            case 5:
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseSpider);
                funcs.add(MobManager::baseSpider);
                break;
            case 6:
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                break;
            case 7:
                funcs.add(MobManager::baseZombie);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSpider);
                break;
            case 8:
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                funcs.add(MobManager::baseSkeleton);
                break;
            case 9:
                // Ejemplo: añadir la variante del ironZombie con vida máxima duplicada
                funcs.add(withDoubledHealth(MobManager::ironZombie));
                break;
            case 10, 18, 20, 30, 40, 50, 60, 70, 80, 90, 99, 100:
                break;
            case 11:
                for (int i = 0; i < 7; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 4; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 12:
                for (int i = 0; i < 11; i++) funcs.add(MobManager::baseHusk);
                break;
            case 13:
                for (int i = 0; i < 13; i++) funcs.add(MobManager::baseHusk);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 14:
                for (int i = 0; i < 18; i++) funcs.add(MobManager::baseHusk);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::slime);
                break;
            case 15:
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseHusk);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSpider);
                break;
            case 16:
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseHusk);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 2; i++) funcs.add(MobManager::slime);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSpider);
                break;
            case 17:
                for (int i = 0; i < 2; i++) funcs.add(withDoubledHealth(MobManager::diamondZombie));
                funcs.add(withDoubledHealth(MobManager::nethZombie));;
                funcs.add(MobManager::towerWhacka);
                break;
            case 19:
                funcs.add(MobManager::slime);
                break;
            case 21:
                for (int i = 0; i < 18; i++) funcs.add(MobManager::tridentDrowned);
                for (int i = 0; i < 2; i++) funcs.add(MobManager::baseDrowned);
                break;
            case 22:
                for (int i = 0; i < 10; i++) funcs.add(MobManager::tridentDrowned);
                for (int i = 0; i < 4; i++) funcs.add(MobManager::baseDrowned);
                for (int i = 0; i < 10; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 23:
                for (int i = 0; i < 3; i++) funcs.add(MobManager::tridentDrowned);
                for (int i = 0; i < 20; i++) funcs.add(MobManager::baseDrowned);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseGuardian);
                break;
            case 24:
                for (int i = 0; i < 2; i++) funcs.add(MobManager::tridentDrowned);
                for (int i = 0; i < 14; i++) funcs.add(MobManager::baseDrowned);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseGuardian);
                break;
            case 25:
                funcs.add(MobManager::baseElderGuardian);
                for (int i = 0; i < 20; i++) funcs.add(MobManager::baseGuardian);
                break;
            case 26:
                for (int i = 0; i < 30; i++) funcs.add(MobManager::baseDrowned);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 27, 28:
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseSpider);
                break;
            case 29:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSpider);
                break;
            case 31:
                for (int i = 0; i < 48; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 32:
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSpider);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::slime);
                break;
            case 33:
                for (int i = 0; i < 4; i++) funcs.add(MobManager::slime);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSpider);
                break;
            case 34:
                for (int i = 0; i < 48; i++) funcs.add(MobManager::slime);
                break;
            case 35:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::slime);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 36:
                for (int i = 0; i < 48; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 48; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 37:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseSpider);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 38:
                for (int i = 0; i < 64; i++) funcs.add(MobManager::Silverfish);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 39:
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 42; i++) funcs.add(MobManager::baseSkeleton);
                break;
            case 41:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombie);
                break;
            case 43:
                for (int i = 0; i < 24; i++) funcs.add(MobManager::baseBogged);
                for (int i = 0; i < 48; i++) funcs.add(MobManager::baseZombie);
                break;
            case 42:
                for (int i = 0; i < 15; i++) funcs.add(MobManager::baseBogged);
                for (int i = 0; i < 36; i++) funcs.add(MobManager::baseZombie);
                break;
            case 44:
                for (int i = 0; i < 35; i++) funcs.add(MobManager::baseBogged);
                break;
            case 45:
                for (int i = 0; i < 48; i++) funcs.add(MobManager::slime);
                break;
            case 46:
                for (int i = 0; i < 40; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 24; i++) funcs.add(MobManager::baseZombie);
                break;
            case 47:
                for (int i = 0; i < 46; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 26; i++) funcs.add(MobManager::baseZombie);
                break;
            case 48:
                for (int i = 0; i < 30; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 24; i++) funcs.add(MobManager::baseZombie);
                break;
            case 49:
                for (int i = 0; i < 46; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 26; i++) funcs.add(MobManager::baseZombie);
                for (int i = 0; i < 10; i++) funcs.add(MobManager::baseZombiePiglin);
                break;
            case 51:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombiePiglin);
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                break;
            case 52:
                for (int i = 0; i < 8; i++) funcs.add(MobManager::baseZoglin);
                for (int i = 0; i < 4; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                break;
            case 53:
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseZombiePiglin);
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                funcs.add(MobManager::baseBlaze);
                break;
            case 54:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombiePiglin);
                for (int i = 0; i < 6; i++) funcs.add(MobManager::baseHoglin);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::basePiglin);
                break;
            case 55:
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombiePiglin);
                for (int i = 0; i < 7; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                break;
            case 56:
                for (int i = 0; i < 64; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZombiePiglin);
                break;
            case 57:
                for (int i = 0; i < 64; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 4; i++) funcs.add(MobManager::baseZoglin);
                for (int i = 0; i < 12; i++) funcs.add(MobManager::baseZombiePiglin);
                break;
            case 58:
                for (int i = 0; i < 48; i++) funcs.add(MobManager::baseSkeleton);
                for (int i = 0; i < 32; i++) funcs.add(MobManager::baseZoglin);
                break;
            case 59:
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                for (int i = 0; i < 30; i++) funcs.add(MobManager::baseZoglin);
                for (int i = 0; i < 24; i++) funcs.add(MobManager::baseZombiePiglin);
                break;
            case 61:
                for (int i = 0; i < 8; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                for (int i = 0; i < 64; i++) funcs.add(MobManager::basePiglin);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseHoglin);
                break;
            case 62:
                for (int i = 0; i < 4; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                for (int i = 0; i < 5; i++) funcs.add(MobManager::basePiglinBrute);
                break;
            case 63:
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyBFB));
                for (int i = 0; i < 4; i++) funcs.add(MobManager::baseBlaze);
                Random r = new Random();
                int yes = r.nextInt(100);
                for (int i = 0; i < ((yes>=50)? 2 : 1); i++) funcs.add(WhackaManager::spawnWhackaEntity);
                funcs.add(MobManager::basePiglinBrute);
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::basePiglin, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                break;
            case 64:
                // Añadir 3 Piglin MOAB reforzados (MOAB -> Reinforced), luego los Blaze existentes
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::basePiglin, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 7; i++) funcs.add(MobManager::baseBlaze);
                break;
            case 65:
                for (int i = 0; i < 4; i++) funcs.add(withModifier(MobManager::basePiglin, MobManager::applyMOAB));
                for (int i = 0; i < 3; i++) funcs.add(MobManager::baseBlaze);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseHoglin);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::basePiglinBrute);
                break;
            case 66:
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::basePiglin, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 15; i++) funcs.add(MobManager::baseBlaze);
                for (int i = 0; i < 16; i++) funcs.add(MobManager::baseWitherSkeleton);
                break;
            case 67:
                funcs.add(withModifier(MobManager::basePiglin, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 2; i++) funcs.add(MobManager::baseBlaze);
                for (int i = 0; i < 3; i++) funcs.add(MobManager::baseWitherSkeleton);
                break;
            case 68:
                funcs.add(withModifier(MobManager::baseZombiePiglin, MobManager::applyBFB));
                for (int i = 0; i < 2; i++) funcs.add(MobManager::baseBlaze);
                for (int i = 0; i < 3; i++) funcs.add(MobManager::baseWitherSkeleton);
                break;
            case 69:
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::basePiglin, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseBlaze);
                break;
            case 71:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::ironZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::chaoZombie, MobManager::applyZong));
                break;
            case 72:
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::ironZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                break;
            case 73:
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::ironZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 15; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 10; i++) funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                break;
            case 74:
                funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 15; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 15; i++) funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                break;
            case 75:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 10; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 5; i++) funcs.add(MobManager::basePillager);
                break;
            case 76:
                for (int i = 0; i < 16; i++) funcs.add(MobManager::basePillager);
                for (int i = 0; i < 20; i++) funcs.add(MobManager::baseVindicator);
                break;
            case 77:
                for (int i = 0; i < 30; i++) funcs.add(MobManager::basePillager);
                for (int i = 0; i < 20; i++) funcs.add(MobManager::baseVindicator);
                break;
            case 78:
                funcs.add(WhackaManager::spawnWhackaEntity);
                funcs.add(MobManager::baseEvoker);
                for (int i = 0; i < 3; i++) funcs.add(MobManager::baseWitch);
                for (int i = 0; i < 3; i++) funcs.add(MobManager::baseRavager);
                for (int i = 0; i < 10; i++) funcs.add(MobManager::basePillager);
                for (int i = 0; i < 5; i++) funcs.add(MobManager::baseVindicator);
                break;
            case 79:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::basePiglinBrute, MobManager::applyBFB));
                for (int i = 0; i < 20; i++) funcs.add(MobManager::baseVindicator);
                for (int i = 0; i < 20; i++) funcs.add(MobManager::basePillager);
                break;
            case 81:
                funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyDDT, MobManager::applyNetherite, MobManager::applyReinforce)));
                for (int i = 0; i < 10; i++) funcs.add(withModifier(MobManager::baseSpider, chainModifiers(MobManager::applyDDT, MobManager::applyReinforce)));
                for (int i = 0; i < 7; i++) funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                break;
            case 82:
                for (int i = 0; i < 7; i++) funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce)));
                for (int i = 0; i < 10; i++) funcs.add(withModifier(MobManager::baseVindicator, chainModifiers(MobManager::applyDDT)));
                break;
            case 83:
                for (int i = 0; i < 15; i++) funcs.add(withModifier(MobManager::baseWitherSkeleton, chainModifiers(MobManager::applyDDT, MobManager::applyChao)));
                break;
            case 84:
                for(int i = 0; i<12;i++) funcs.add(withModifier(MobManager::nethZombie, MobManager::applyZong));
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyZong, MobManager::applyReinforce)));
                break;
            case 85:
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::chaoZombie, MobManager::applyZong));
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyZong, MobManager::applyReinforce)));
                break;
            case 86:
                for (int i = 0; i < 10; i++) funcs.add(withModifier(MobManager::chaoZombie, MobManager::applyZong));
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyZong, MobManager::applyReinforce)));
                break;
            case 87:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::towerWhacka, MobManager::applyMOAB));
                funcs.add(withModifier(MobManager::baseZombie, MobManager::applyNightmare));
                break;
            case 88:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyNetherite, MobManager::applyChaos, MobManager::applyBFB)));
                funcs.add(withModifier(MobManager::baseZombie, MobManager::applyNightmare));
                break;
            case 89:
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers( MobManager::applyChaos, MobManager::applyBFB)));
                funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyNetherite, MobManager::applyChaos, MobManager::applyBFB)));
                funcs.add(withModifier(MobManager::baseZombie, MobManager::applyNightmare));
                break;
            case 91:
                for (int i = 0; i < 5; i++) funcs.add(withModifier(MobManager::diamondZombie, chainModifiers(MobManager::applyNetherite, MobManager::applyChao, MobManager::applyReinforce)));
                funcs.add(withModifier(MobManager::baseHusk, chainModifiers(MobManager::applyMOAB, MobManager::applyNetherite, MobManager::applyReinforce, MobManager::applyTechFlashBlindWielder)));
                break;
            case 92:
                for (int i = 0; i < 3; i++) funcs.add(withModifier(MobManager::baseZombie, chainModifiers(MobManager::applyNetherite, MobManager::applyChao, MobManager::applyReinforce)));
                for (int i = 0; i < 2; i++) funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyChao, MobManager::applyTechRunicHellfireWielder, MobManager::applyReinforce)));
                break;
            case 93:
                funcs.add(MobManager::baseSpider);
                break;
            case 94:
                funcs.add(withModifier(MobManager::baseBreeze, MobManager::applyDDT));
                funcs.add(withModifier(MobManager::basePiglinBrute, MobManager::applyZong));
                funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce, MobManager::applyTechFlashBlindWielder, MobManager::applyNetherite)));
                break;
            case 95:
                funcs.add(withModifier(MobManager::baseHusk, chainModifiers(MobManager::applyMOAB, MobManager::applyNetherite, MobManager::applyReinforce, MobManager::applyTechCarnageWielder)));
                funcs.add(withModifier(MobManager::baseHusk, chainModifiers(MobManager::applyMOAB, MobManager::applyNetherite, MobManager::applyReinforce, MobManager::applyTechFlashBlindWielder)));
                break;
            case 96:
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyNightmare, MobManager::applyZong, MobManager::applyReinforce, MobManager::applyTechChaosWielder)));
                break;
            case 97:
                funcs.add(withModifier(MobManager::baseBreeze, chainModifiers(MobManager::applyDDT, MobManager::applyReinforce)));
                funcs.add(withModifier(MobManager::basePiglinBrute, chainModifiers(MobManager::applyZong, MobManager::applyReinforce)));
                funcs.add(withModifier(MobManager::baseSkeleton, chainModifiers(MobManager::applyMOAB, MobManager::applyReinforce, MobManager::applyReinforce, MobManager::applyTechFlashBlindWielder, MobManager::applyNetherite)));
                break;
            case 98:
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyNightmare, MobManager::applyZong, MobManager::applyTechChaosWielder)));
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyNightmare, MobManager::applyZong, MobManager::applyTechCarnageWielder)));
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyNightmare, MobManager::applyZong, MobManager::applyTechFlashBlindWielder)));
                funcs.add(withModifier(MobManager::nethZombie, chainModifiers(MobManager::applyNightmare, MobManager::applyZong, MobManager::applyTechRunicHellfireWielder)));
                break;
        }

        return funcs;
    }

    static void repeatTimes(int times, Runnable action) {
        for (int i = 0; i < times; i++) {
            action.run();
        }
    }

    static Entity baseZombie(Location loc) {
        Zombie z = loc.getWorld().spawn(loc, Zombie.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(null);
        z.getEquipment().clear();

        return z;
    }

    static Entity baseDrowned(Location loc) {
        Drowned z = loc.getWorld().spawn(loc, Drowned.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(null);
        z.getEquipment().clear();

        return z;
    }

    static Entity towerWhacka(Location loc) {
        Silverfish z = (Silverfish) WhackaManager.spawnWhackaEntity(loc);
        z.getActivePotionEffects().clear();
        z.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(Integer.MAX_VALUE, 0));
        z.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(Integer.MAX_VALUE, 0));

        return z;
    }

    static Entity basePillager(Location loc) {

        return loc.getWorld().spawn(loc, Pillager.class);
    }

    static Entity baseVindicator(Location loc) {

        return loc.getWorld().spawn(loc, Vindicator.class);
    }

    static Entity baseRavager(Location loc) {

        return loc.getWorld().spawn(loc, Ravager.class);
    }

    static Entity baseWitch(Location loc) {

        return loc.getWorld().spawn(loc, Witch.class);
    }

    static Entity baseEvoker(Location loc) {

        return loc.getWorld().spawn(loc, Evoker.class);
    }

    static Entity baseBreeze(Location loc) {

        return loc.getWorld().spawn(loc, Breeze.class);
    }

    static Entity tridentDrowned(Location loc) {
        Drowned z = loc.getWorld().spawn(loc, Drowned.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
        z.getEquipment().clear();

        return z;
    }


    static Entity baseHusk(Location loc) {
        Husk z = loc.getWorld().spawn(loc, Husk.class);

        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(null);
        z.getEquipment().clear();

        return z;
    }

    static Entity ironZombie(Location loc) {
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
        z.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

        return z;
    }

    static Entity diamondZombie(Location loc) {
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
        z.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

        return z;
    }

    static Entity chaoZombie(Location loc) {
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
        z.getEquipment().setItemInMainHand(weapons.ChaoSword);

        return z;
    }

    static Entity nethZombie(Location loc) {
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
        z.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));

        return z;
    }

    static Entity baseSkeleton(Location loc) {
        Skeleton z = loc.getWorld().spawn(loc, Skeleton.class);

        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));

        return z;
    }

    static Entity baseWitherSkeleton(Location loc) {
        WitherSkeleton z = loc.getWorld().spawn(loc, WitherSkeleton.class);

        z.setCanPickupItems(false);

        return z;
    }

    static Entity baseZombiePiglin(Location loc) {
        PigZombie z = loc.getWorld().spawn(loc, PigZombie.class);

        z.setCanPickupItems(false);
        z.setAdult();

        return z;
    }

    static Entity baseZoglin(Location loc) {
        Zoglin z = loc.getWorld().spawn(loc, Zoglin.class);
        z.setAdult();

        return z;
    }

    static Entity baseHoglin(Location loc) {
        Hoglin z = loc.getWorld().spawn(loc, Hoglin.class);
        z.setImmuneToZombification(true);
        z.setIsAbleToBeHunted(false);
        return z;
    }

    static Entity basePiglin(Location loc) {
        Piglin z = loc.getWorld().spawn(loc, Piglin.class);

        z.setCanPickupItems(false);
        z.setImmuneToZombification(true);
        int randomArmor = new Random().nextInt(2);
        if(randomArmor == 0){
            z.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        } else {
            z.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        }
        z.setBaby(false);
        z.setIsAbleToHunt(false);

        return z;
    }

    static Entity basePiglinBrute(Location loc) {
        PiglinBrute z = loc.getWorld().spawn(loc, PiglinBrute.class);

        z.setCanPickupItems(false);
        z.setImmuneToZombification(true);
        z.setAdult();

        return z;
    }

    static Entity baseBlaze(Location loc) {
        Blaze z = loc.getWorld().spawn(loc, Blaze.class);

        z.setCanPickupItems(false);

        return z;
    }

    static Entity baseBogged(Location loc) {
        Bogged z = loc.getWorld().spawn(loc, Bogged.class);

        z.setCanPickupItems(false);
        z.getEquipment().setArmorContents(null);
        z.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));

        return z;
    }

    static Entity baseSpider(Location loc) {
        Spider z = loc.getWorld().spawn(loc, Spider.class);
        z.getActivePotionEffects().clear();

        return z;
    }

    static Entity slime(Location loc) {
        Slime z = loc.getWorld().spawn(loc, Slime.class);
        z.setSize(2);

        return z;
    }

    static Entity baseGuardian(Location loc) {
        Guardian g = loc.getWorld().spawn(loc, Guardian.class);
        return g;
    }

    static Entity Silverfish(Location loc) {
        Silverfish g = loc.getWorld().spawn(loc, Silverfish.class);
        return g;
    }

    static Entity baseElderGuardian(Location loc) {
        ElderGuardian g = loc.getWorld().spawn(loc, ElderGuardian.class);

        return g;
    }

    /**
     * Envuelve una función de spawn para aplicar un Consumer<Entity> inmediatamente después de crear la entidad.
     * Esto permite crear variantes (por ejemplo duplicar la vida máxima) sin duplicar la lógica de spawn.
     */
    static Function<Location, Entity> withModifier(Function<Location, Entity> spawn, Consumer<Entity> modifier) {
        return loc -> {
            Entity e = spawn.apply(loc);
            if (e == null) return null;
            try {
                modifier.accept(e);
            } catch (Exception ex) {
                // ignorar errores de modificador individuales
            }
            return e;
        };
    }

    /**
     * Helper para encadenar múltiples modificadores en orden.
     */
    static Consumer<Entity> chainModifiers(Consumer<Entity>... modifiers) {
        return entity -> {
            if (entity == null) return;
            for (Consumer<Entity> m : modifiers) {
                if (m == null) continue;
                try {
                    m.accept(entity);
                } catch (Exception ignored) {}
            }
        };
    }

    /**
     * Conveniencia para crear una función de spawn que duplica la vida máxima de la entidad creada.
     */
    static Function<Location, Entity> withDoubledHealth(Function<Location, Entity> spawn) {
        return withModifier(spawn, MobManager::doubleEntityMaxHealth);
    }

    /**
     * Multiplica la vida máxima de una entidad viva por el factor dado y ajusta la vida actual al nuevo máximo.
     */
    static void multiplyMaxHealth(Entity entity, double factor) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        AttributeInstance attr = le.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double base = attr.getBaseValue();
        double newMax = base * factor;
        try {
            attr.setBaseValue(newMax);
            // Ajustar la salud actual al nuevo máximo (mantener al menos 1)
            double newHealth = Math.max(1.0, Math.min(newMax, le.getHealth() * factor));
            // si la salud actual era 0 o inválida, setear al máximo seguro
            if (newHealth <= 0) newHealth = Math.min(newMax, 1.0);
            le.setHealth(Math.min(newMax, newHealth));
        } catch (Exception ex) {
            // Silenciar excepciones (p. ej. si entidad ya está muerta)
        }
    }

    /**
     * Versión existente que duplica la vida (compatibilidad): delega en multiplyMaxHealth.
     */
    static void doubleEntityMaxHealth(Entity entity) {
        multiplyMaxHealth(entity, 2.0);
    }

    /**
     * MOAB: duplica la vida máxima y renombra a "MOAB".
     */
    static void applyMOAB(Entity entity) {
        multiplyMaxHealth(entity, 2.0);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setCustomName("MOAB");
            ((LivingEntity) entity).setCustomNameVisible(true);
        } else {
            entity.setCustomName("MOAB");
        }
    }

    /**
     * BFB: multiplica salud por 5 y renombra a "BFB".
     */
    static void applyBFB(Entity entity) {
        multiplyMaxHealth(entity, 5.0);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setCustomName("BFB");
            ((LivingEntity) entity).setCustomNameVisible(true);
        } else {
            entity.setCustomName("BFB");
        }
    }

    /**
     * DDT: x2 salud, tag persistente "dario:ddt", velocidad permanente 2, renombra a "DDT".
     * Efectos: Speed level 2 -> amplifier 1 (recordar que los amplifiers empiezan en 0).
     */
    static void applyDDT(Entity entity) {
        multiplyMaxHealth(entity, 2.0);

        if (entity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) entity;
            // Tag persistente
            try {
                le.getPersistentDataContainer().set(new NamespacedKey("dario", "ddt"), PersistentDataType.BOOLEAN, true);
            } catch (Exception ignored) {}
            // Speed 2 => amplifier 1
            le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
            le.setCustomName("DDT");
            le.setCustomNameVisible(true);
        } else {
            try {
                entity.getPersistentDataContainer().set(new NamespacedKey("dario", "ddt"), PersistentDataType.BOOLEAN, true);
            } catch (Exception ignored) {}
            entity.setCustomName("DDT");
        }
    }

    /**
     * Zong: x3 salud, Resistance 2 (amplifier 1), Strength 2 (amplifier 1), Speed 1 (amplifier 0).
     * Todos los efectos se aplican con duración muy larga (prácticamente permanente).
     */
    static void applyZong(Entity entity) {
        multiplyMaxHealth(entity, 3.0);

        if (!(entity instanceof LivingEntity)) {
            if (entity != null) entity.setCustomName("Zong");
            return;
        }
        LivingEntity le = (LivingEntity) entity;
        // Resistance 2 -> amplifier 1
        le.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false));
        // Strength 2 -> amplifier 1
        le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false));
        // Speed 1 -> amplifier 0
        le.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));

        le.setCustomName("Zong");
        le.setCustomNameVisible(true);
    }

    /**
     * Reinforce: duplica de nuevo la vida máxima de la entidad (x2).
     * Además marca la entidad como reforzada en el PDC y combina el nombre existente para no perder etiquetas como "MOAB".
     */
    static void applyReinforce(Entity entity) {
        multiplyMaxHealth(entity, 2.0);
        try {
            entity.getPersistentDataContainer().set(new NamespacedKey("tower", "reinforced"), PersistentDataType.BOOLEAN, true);
        } catch (Exception ignored) {}

        if (entity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) entity;
            String prev = le.getCustomName();
            if (prev == null || prev.isEmpty()) {
                le.setCustomName("Reinforced");
            } else {
                le.setCustomName("Reinforced " + prev);
            }
            le.setCustomNameVisible(true);
        } else {
            String prev = entity.getCustomName();
            if (prev == null || prev.isEmpty()) {
                entity.setCustomName("Reinforced");
            } else {
                entity.setCustomName("Reinforced " + prev);
            }
        }
    }
    /**
     * Netherite: equipa a la entidad con armadura y espada de netherita.
     */
    static void applyNetherite(Entity entity) {

        if (entity instanceof LivingEntity le) {
            EntityEquipment e = le.getEquipment();
            e.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            e.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            e.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            e.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
            if(e instanceof Skeleton) return;
            e.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        }
    }
    /**
     * Chao: equipa a la entidad con la Chao Sword.
     */
    static void applyChao(Entity entity) {
        if (entity instanceof LivingEntity le) {
            le.getEquipment().setItemInMainHand(new ItemStack(weapons.ChaoSword));
        }
    }

    static void applyNightmare(Entity entity) {
        multiplyMaxHealth(entity, 4.0);

        if (entity instanceof LivingEntity le) {
            ItemStack helm = new ItemStack(Material.NETHERITE_HELMET);
            ItemStack chest = new ItemStack(Material.NETHERITE_CHESTPLATE);
            ItemStack legs = new ItemStack(Material.NETHERITE_LEGGINGS);
            ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
            ItemStack[] armor = {boots, legs, chest, helm};
            for(int i =0; i< armor.length; i++){
                ItemMeta meta = armor[i].getItemMeta();
                if(meta != null){
                    meta.addEnchant(Enchantment.PROTECTION, 5, true);
                    meta.setUnbreakable(true);
                    meta.addEnchant(Enchantment.THORNS, 3, true);
                    armor[i].setItemMeta(meta);
                }
            }
            le.getEquipment().setArmorContents(armor);
            ItemStack sword = new ItemStack(events.buffsword);
            le.getEquipment().setItemInMainHand(sword);
            le.setCustomName("Nightmare");
            le.setCustomNameVisible(true);
        }
    }

    static void applyChaos(Entity entity) {
        if (entity instanceof LivingEntity le) {
            le.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(Integer.MAX_VALUE, 0));
            le.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, 1));
            le.addPotionEffect(PotionEffectType.STRENGTH.createEffect(Integer.MAX_VALUE, 1));
            le.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(Integer.MAX_VALUE, 1));
        }
    }

    // Nuevo modificador: marca la entidad para que ocasionalmente pueda disparar la técnica Chaos Heartbeat al ser golpeada.
    static void applyTechChaosWielder(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        try {
            le.getPersistentDataContainer().set(new NamespacedKey("tower", "chaos_wielder"), PersistentDataType.BOOLEAN, true);
        } catch (Exception ignored) {}
        // marcar visualmente (opcional)
        if (le.getCustomName() == null || le.getCustomName().isEmpty()) {
            le.setCustomName("Chaos Wielder");
            le.setCustomNameVisible(true);
        }
    }

    // Nuevo: aplicar Carnage tech a una entidad
    static void applyTechCarnageWielder(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        try {
            le.getPersistentDataContainer().set(new NamespacedKey("tower", "carnage_wielder"), PersistentDataType.BOOLEAN, true);
        } catch (Exception ignored) {}
        if (le.getCustomName() == null || le.getCustomName().isEmpty()) {
            le.setCustomName("Carnage Wielder");
            le.setCustomNameVisible(true);
        }
    }

    // Nuevo: aplicar Flash Blind tech a una entidad
    static void applyTechFlashBlindWielder(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        try {
            le.getPersistentDataContainer().set(new NamespacedKey("tower", "flash_wielder"), PersistentDataType.BOOLEAN, true);
        } catch (Exception ignored) {}
        if (le.getCustomName() == null || le.getCustomName().isEmpty()) {
            le.setCustomName("Flash Wielder");
            le.setCustomNameVisible(true);
        }
    }

    // Nuevo: aplicar Runic Hellfire tech a una entidad
    static void applyTechRunicHellfireWielder(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) entity;
        try {
            le.getPersistentDataContainer().set(new NamespacedKey("tower", "runic_wielder"), PersistentDataType.BOOLEAN, true);
        } catch (Exception ignored) {}
        if (le.getCustomName() == null || le.getCustomName().isEmpty()) {
            le.setCustomName("Runic Wielder");
            le.setCustomNameVisible(true);
        }
    }
}
