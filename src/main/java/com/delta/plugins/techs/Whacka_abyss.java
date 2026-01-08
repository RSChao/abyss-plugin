package com.delta.plugins.techs;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.delta.plugins.whacka.WhackaManager;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.CooldownManager;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Whacka_abyss {

    static final String TECH_ID = "whacka_abyss";
    static final Plugin plugin = Plugin.getPlugin(Plugin.class);
    public static void register() {
        Plugin.registerAbyssID(TECH_ID);
        TechRegistry.registerTechnique(TECH_ID, giveBump);
        TechRegistry.registerTechnique(TECH_ID, summonWhackaTech);
        TechRegistry.registerTechnique(TECH_ID, incredibleviolence);
        TechRegistry.registerTechnique(TECH_ID, resetCooldown);
    }


    static Technique giveBump = new Technique(
        "chichonesmuchosxd",
        "Sacrificio de Guaka",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(20), List.of("Sacrifice to get Whacka bumps.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Random rand = new Random();
            int rng = rand.nextInt(100);
            final int treshold = 100/8;
            ItemStack bump = (rng <= treshold) ? Items.rareWhackaBump() : Items.whackaBump();
            if(treshold >= rng) bump.setAmount(4);
            else bump.setAmount(32);

            Item i = player.getWorld().dropItemNaturally(player.getLocation(), bump);
            i.setPickupDelay(0);
            subtractHealthWhacka(player);
            hotbarMessage.sendHotbarMessage(player, "¡Has recibido chichones" + ((rng<=treshold)?" raros":"") +" de Guaka!" );
        }
    );

    static List<String> guakaNames = List.of("Guakórax", "Tómbaguak", "Guaktilio", "Chakaguá", "Guakumán", "Guakánimo", "Zambaguak", "Guaklopo", "Makaguá", "Guakarún", "Tlakaguak", "Guakencio", "Boroguak", "Guakachu", "Aguakán", "Guakito", "Guakardo", "Guaklitos", "Guakaster", "Guakín", "Guakardo", "Guakito", "Guakucho", "Ignaka", "Guakarío", "Guakabén");

    public static Technique summonWhackaTech = new Technique(
        "whacka_summon_toomany",
        "Ejército de Guakas",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Summon many Whackas to fight for you.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Random random = new Random();
            int amnt = random.nextInt(5) + 1;
            for(int i = 0; i < amnt; i++){
                String name = guakaNames.get(random.nextInt(guakaNames.size()));
                Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                    Wolf w =(Wolf) WhackaManager.spawnWhackaFriendEntity(player.getLocation(), player);
                    w.setCustomName(name);
                    if(name.equalsIgnoreCase("Ignaka") || name.equalsIgnoreCase("Guakarío") || name.equalsIgnoreCase("Guakabén")){
                        w.getAttribute(Attribute.MAX_HEALTH).setBaseValue(300.0);
                        w.setHealth(300.0);
                        player.sendMessage("§6¡Has invocado a " + name + ", un Guaka legendario! §c(300 HP)");
                    }
                    else {
                        player.sendMessage("Has invocado a " + name + " el Guaka");
                    }
                    WhackaManager.setBumpDrop(w);
                }, i); // small delay between spawns
            }
            hotbarMessage.sendHotbarMessage(player, "¡Has invocado un ejército de " + amnt + " Guaka(s)!");
        }
    );

    static Technique resetCooldown = new Technique(
        "reset_cooldown_whacka",
        "Reinicio de Guaka",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(50), List.of("Reset many abyss & fruit cooldowns.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            CooldownManager.setCooldown(player, "reset_cooldown", cooldownHelper.minutesToMiliseconds(50));
            List<String> excludedTechs = List.of("ultimate_cataclysm", "reset_cooldown_chaos", "reset_cooldown_whacka", "reset_cooldown");
            for(String id: Plugin.getAllAbyssIDs()){
                for(Technique t: TechRegistry.getAllTechniques(id)){
                    if(!excludedTechs.contains(t.getId())){
                        CooldownManager.removeCooldown(player, t.getId());
                    }
                }
            }
            for(String id: com.rschao.plugins.fightingpp.Plugin.getAllFruitIDs()){
                for(Technique t: TechRegistry.getAllTechniques(id)){
                    if(!excludedTechs.contains(t.getId())){
                        CooldownManager.removeCooldown(player, t.getId());
                    }
                }
            }
            subtractHealthWhacka(player);
            hotbarMessage.sendHotbarMessage(player, "¡Has reiniciado tus cooldowns!");
        }
    );

    // Nueva técnica: Guakataque (incredibleviolence)
    public static Technique incredibleviolence = new Technique(
        "incredibleviolence",
        "Guakataque",
        new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(40), List.of("Launch multiple bump projectiles consuming bumps.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player player = ctx.caster();
            Random rand = new Random();
            int wantNormal = rand.nextInt(8) + 1; // 1..8
            int wantRare = 8 - wantNormal;

            NamespacedKey normalKey = new NamespacedKey(Plugin.getPlugin(Plugin.class), "whacka_bump");
            NamespacedKey rareKey = new NamespacedKey(Plugin.getPlugin(Plugin.class), "rare_whacka_bump");

            int haveNormal = countItemsByKey(player, normalKey);
            int haveRare = countItemsByKey(player, rareKey);
            int totalHave = haveNormal + haveRare;

            // Si no hay ninguno -> fallback
            if(totalHave == 0){
                for(int i = 0; i < 8; i++){
                    subtractHealthWhacka(player);
                    boolean isRare = (rand.nextInt(8) == 0); // 1/8 probabilidad
                    LivingEntity target = findNearestLiving(player, 40);
                    if(target != null){
                        spawnVisualProjectile(player, target, isRare ? 25.0 : 10.0, isRare);
                    }
                }
                hotbarMessage.sendHotbarMessage(player, "¡No tenías chichones! Pagaste el precio y escupiste 8 proyectiles.");
                return;
            }

            if(totalHave < 8){
                hotbarMessage.sendHotbarMessage(player, "No tienes suficientes chichones (se requieren 8).");
                return;
            }

            int useNormal = wantNormal;
            int useRare = wantRare;

            if(haveRare < useRare){
                int shortage = useRare - haveRare;
                if(haveNormal >= useNormal + shortage){
                    useNormal += shortage;
                    useRare = haveRare;
                } else {
                    useNormal = Math.min(haveNormal, useNormal + shortage);
                    useRare = Math.min(haveRare, useRare);
                }
            }

            if(haveNormal < useNormal){
                int shortage = useNormal - haveNormal;
                if(haveRare >= useRare + shortage){
                    useRare += shortage;
                    useNormal = haveNormal;
                } else {
                    useNormal = Math.min(haveNormal, useNormal);
                    useRare = Math.min(haveRare, useRare + shortage);
                }
            }

            removeItemsByKey(player, normalKey, useNormal);
            removeItemsByKey(player, rareKey, useRare);

            for(int i = 0; i < useNormal; i++){
                LivingEntity target = findNearestLiving(player, 40);
                if(target != null) spawnVisualProjectile(player, target, 10.0, false);
            }
            for(int i = 0; i < useRare; i++){
                LivingEntity target = findNearestLiving(player, 40);
                if(target != null) spawnVisualProjectile(player, target, 25.0, true);
            }

            hotbarMessage.sendHotbarMessage(player, "Has lanzado " + (useNormal + useRare) + " chichones (" + useRare + " raros).");
        }
    );

    // Helper: cuenta items por NamespacedKey booleano
    private static int countItemsByKey(Player p, NamespacedKey key){
        int cnt = 0;
        for(ItemStack it : p.getInventory().getContents()){
            if(it == null) continue;
            if(it.hasItemMeta() && it.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)){
                cnt += it.getAmount();
            }
        }
        return cnt;
    }

    // Helper: remueve hasta count items por key del inventario (ajusta stacks)
    private static void removeItemsByKey(Player p, NamespacedKey key, int count){
        if(count <= 0) return;
        ItemStack[] contents = p.getInventory().getContents();
        for(int i = 0; i < contents.length && count > 0; i++){
            ItemStack it = contents[i];
            if(it == null) continue;
            if(it.hasItemMeta() && it.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)){
                int amt = it.getAmount();
                if(amt <= count){
                    count -= amt;
                    p.getInventory().setItem(i, null);
                } else {
                    it.setAmount(amt - count);
                    p.getInventory().setItem(i, it);
                    count = 0;
                }
            }
        }
    }

    // Encuentra la entidad viva más cercana distinta del jugador dentro de range, o null
    private static LivingEntity findNearestLiving(Player p, double range){
        List<Entity> nearby = new ArrayList<>(p.getWorld().getNearbyEntities(p.getLocation(), range, range, range));
        return nearby.stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .map(e -> (LivingEntity)e)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(p.getLocation())))
                .orElse(null);
    }

    // Spawnea un Snowball visual hacia target y aplica daño de inmediato usando subtractHealth
    private static void spawnVisualProjectile(Player shooter, LivingEntity target, double damage, boolean rare){
        // Spawn visual snowball from eye location towards target
        Snowball sb = shooter.getWorld().spawn(shooter.getEyeLocation().add(shooter.getLocation().getDirection()), Snowball.class);
        sb.setShooter(shooter);
        Vector dir = target.getEyeLocation().toVector().subtract(shooter.getEyeLocation().toVector()).normalize().multiply(1.5);
        sb.setVelocity(dir);
        // Aplicar daño inmediatamente (garantiza que "no falle")
        subtractHealth(target, damage);
        // Opcional: puedes personalizar efectos visuales según rare
    }

    protected static void subtractHealthWhacka(Player p){
        double health = p.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        subtractHealth(p, health/8);
    }
    public static void subtractHealth(LivingEntity entity, double amount) {
        if(entity.getAbsorptionAmount() >= 8 && entity.getHealth() < 4) return;
        double newHealth = Math.max(entity.getHealth() - amount, 0);
        if(newHealth <= 0) {
            entity.setHealth(1); // Prevent death
            entity.damage(99); // Trigger death event
            return;
        }
        entity.setHealth(newHealth);
    }
}
