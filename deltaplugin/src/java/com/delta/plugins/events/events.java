package com.delta.plugins.events;

import com.delta.plugins.items.Items;
import com.rschao.plugins.techapi.tech.cooldown.cooldownHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.feedback.hotbarMessage;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class events implements Listener {
    public static final Map<UUID, Boolean> hasCritDamage = new HashMap<>();
    public static final Map<UUID, Boolean> hasGenoDamage = new HashMap<>();
    public static final Map<UUID, Boolean> hasFlashDamage = new HashMap<>();
    public static final Map<UUID, Boolean> hasOmniNegate = new HashMap<>();
    // Track which group id index the player is currently using (0-2)
    public static final Map<UUID, Integer> playerGroupIdIndex = new HashMap<>();

    // Utility method to sanitize player names (remove leading dot)
    private String sanitizePlayerName(String name) {
        if (name != null && name.startsWith(".")) {
            return name.substring(1);
        }
        return name;
    }
    @EventHandler
    void onEchestInteract(PlayerInteractEvent ev){
        Player p = ev.getPlayer();
        if(ev.getClickedBlock() == null) return;
        if(ev.getClickedBlock().getType().equals(Material.ENDER_CHEST) && ev.getAction().toString().contains("RIGHT")){
            ev.setCancelled(true);
            Bukkit.dispatchCommand(p, "vault");
        }
    }

    @EventHandler
    void onPlayerUseHoe(PlayerInteractEvent ev){
        ItemStack item = ev.getItem();
        if(item == null) return;
        if(item.getType().equals(Material.AIR)) return;
        if(!item.isSimilar(Items.hoe())) return;
        if(!ev.getPlayer().isSneaking()) return;
        Player p = ev.getPlayer();
        fly.use(p, item, Technique.nullValue());
    }
    @EventHandler
    void onPlayerUseTech(PlayerInteractEvent ev){
        Player p = ev.getPlayer();
        if(ev.getItem() == null) return;
        if(ev.getItem().getItemMeta() == null) return;
        if(ev.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"), PersistentDataType.STRING)){
            String abyssId = ev.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"), PersistentDataType.STRING);
            p.sendMessage("Abyss ID found: " + abyssId);
            if(abyssId == null || abyssId.equals("none")) {
                p.sendMessage("Abyss ID is not valid: " + abyssId);
            }
            List<String> groupIds = Plugin.getPlugin(Plugin.class).getConfig().getStringList(sanitizePlayerName(p.getName()) + ".groupids");
            if(groupIds.size() >= 3){
                p.sendMessage("You cannot carry more than 3 abyss.");
                return;
            }
            if(groupIds.contains(abyssId)){
                p.sendMessage("You already have this abyss.");
                return;
            }
            groupIds.add(abyssId);
            Plugin.getPlugin(Plugin.class).getConfig().set(sanitizePlayerName(p.getName()) + ".groupids", groupIds);
            Plugin.getPlugin(Plugin.class).saveConfig();
            Plugin.getPlugin(Plugin.class).reloadConfig();
            p.sendMessage("You have acquired the abyss: " + abyssId);
            ev.getItem().setAmount(0);

        }
        if(ev.getItem() == null) return;
        if(ev.getItem().getItemMeta() == null) return;

        if(!ev.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "channeler"), PersistentDataType.BOOLEAN)) return;
        // Get current group id index for player (default 0)
        int groupIndex = playerGroupIdIndex.getOrDefault(p.getUniqueId(), 0);
        String groupId = getGroupId(p, groupIndex);
        if (groupId == null || groupId.equals("none")) {
            p.sendMessage("You have not yet harnessed the abyss.");
            return;
        }

        // Get current technique index for this group id
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);

        if(ev.getAction().toString().contains("LEFT")){
            if(p.isSneaking()){
                // Cycle group id index (0-2)
                int maxGroups = getGroupIdCount(p);
                if (maxGroups == 0) {
                    p.sendMessage("You have no abyss to switch to! Ask an admin if you think this is an error");
                    return;
                }
                int newIndex = (groupIndex + 1) % maxGroups;
                playerGroupIdIndex.put(p.getUniqueId(), newIndex);
                String newGroupId = getGroupId(p, newIndex);
                p.sendMessage("Your abyss has switched to " + newGroupId);
            }
            else{
                Technique technique = TechRegistry.getAllTechniques(groupId).get(techIndex);
                if(technique == null) return;
                technique.use(p, ev.getItem(), Technique.nullValue());
            }
        }
        else if(ev.getAction().toString().contains("RIGHT")){
            if(p.isSneaking()){
                if (techIndex == 0) {
                    PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, (hasChaosHeart(p)? TechRegistry.getAllTechniques(groupId).size() - 1 : TechRegistry.getNormalTechniques(groupId).size() - 1));
                    techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
                } else {
                    PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, techIndex - 1);
                    techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
                }
            }
            else{
                PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, (techIndex + 1) % (hasChaosHeart(p)? TechRegistry.getAllTechniques(groupId).size() : TechRegistry.getNormalTechniques(groupId).size()));
                techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
            }
            p.sendMessage("You have switched to technique: " + TechRegistry.getAllTechniques(groupId).get(techIndex).getName());
        }
    }

    // Get the group id for a player at a given index (0-2)
    public String getGroupId(Player p, int index){
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        String playerName = sanitizePlayerName(p.getName());
        java.util.List<String> groupIds = config.getStringList(playerName + ".groupids");
        if (groupIds.isEmpty() || index < 0 || index >= groupIds.size()) {
            return "none";
        }
        return groupIds.get(index);
    }

    // Get the number of group ids a player has
    public int getGroupIdCount(Player p) {
        FileConfiguration config = com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getConfig();
        String playerName = sanitizePlayerName(p.getName());
        java.util.List<String> groupIds = config.getStringList(playerName + ".groupids");
        return groupIds.size();
    }

    @EventHandler
    public void mobOuchie(EntityDamageByEntityEvent event) {
        //check if the player has crit damage enabled
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID playerId = player.getUniqueId();
            Player victim = null;
            if(event.getEntity() instanceof Player) victim = (Player) event.getEntity();
            if (hasCritDamage.getOrDefault(playerId, false)) {
                // Apply crit damage logic here
                double damage = event.getDamage();
                event.setDamage(damage * 1.5);
                player.sendMessage("You dealt critical damage!");
            }
            else if(hasGenoDamage.getOrDefault(playerId, false)) {
                int dmg = 400;
                if(victim != null){
                    if(hasChaosHeart(player)) dmg *= 2;
                    if(hasPurityHeart(victim)) dmg /= 4;
                }
                event.setDamage(dmg); // Example: double the damage
                player.sendMessage(ChatColor.DARK_RED + "=}");
                event.getEntity().sendMessage(ChatColor.DARK_RED + "Enjoy =}");
                hasGenoDamage.put(playerId, false);
            }
            if (hasFlashDamage.getOrDefault(playerId, false)) {
                // Apply crit damage logic here
                int dmg = 300;
                if(victim != null){
                    if(hasChaosHeart(player)) dmg *= 2;
                    if(hasPurityHeart(victim)) dmg /= 4;
                }
                event.setDamage(dmg);
                hotbarMessage.sendHotbarMessage(player, "You have used a Black Flash!");
                hasFlashDamage.put(playerId, false);
            }
        }
        
    }

    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem == null || newItem.getType() == null || !newItem.hasItemMeta()) return;
        if (!newItem.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(Plugin.getPlugin(Plugin.class), "channeler"),
                PersistentDataType.BOOLEAN)) return;

        // Get current group id index for player (default 0)
        int groupIndex = playerGroupIdIndex.getOrDefault(player.getUniqueId(), 0);
        String groupId = getGroupId(player, groupIndex);
        if (groupId == null || groupId.equals("none")) {
            hotbarMessage.sendHotbarMessage(player, "No group id set.");
            return;
        }
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(player.getUniqueId(), groupId);
        java.util.List<Technique> techs = TechRegistry.getAllTechniques(groupId);
        if (techs == null || techs.isEmpty() || techIndex < 0 || techIndex >= techs.size()) {
            hotbarMessage.sendHotbarMessage(player, "No technique selected.");
            return;
        }
        String techName = techs.get(techIndex).getName();
        hotbarMessage.sendHotbarMessage(player, "Technique: " + techName + " (Abyss " + groupId + ")");
    }

    @EventHandler (priority = EventPriority.LOWEST)
    void onPlayerOmniNegate(EntityDamageByEntityEvent ev){
        if(!(ev.getEntity() instanceof Player)) return;
        Player p = (Player) ev.getEntity();
        if(!hasOmniNegate.getOrDefault(p.getUniqueId(), false)) return;
        ev.setCancelled(true);
        if(!(ev.getDamager() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) ev.getDamager();
        le.damage(ev.getDamage());
        hotbarMessage.sendHotbarMessage(p, "Your Omni Negate absorbed the damage!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.getName().equals(victim.getName())) return;

        // Abyss stealing logic

        FileConfiguration config = Plugin.getPlugin(Plugin.class).getConfig();
        String victimName = sanitizePlayerName(victim.getName());
        String killerName = sanitizePlayerName(killer.getName());
        String victimKey = victimName + ".groupids";
        String killerKey = killerName + ".groupids";
        java.util.List<String> victimGroups = config.getStringList(victimKey);
        java.util.List<String> killerGroups = config.getStringList(killerKey);

        // Lista de abismos protegidos (puedes obtenerla de config o definirla aquí)
        java.util.List<String> protectedAbysses = config.getStringList("protectedAbysses");

        if (victimGroups == null || victimGroups.isEmpty() || killerGroups == null || killerGroups.isEmpty()) {
            return;
        }

        // Si la víctima solo tiene un abismo, no se roba nada
        if (victimGroups.size() == 1) {
            return;
        }
        if(com.rschao.smp.Plugin.getPauseLives()) return;
        // Si el asesino tiene 3 o más abismos, elimina el primero NO protegido y guarda

        victimGroups = config.getStringList(victimKey); // Recargar por si se modificó antes
        killerGroups = config.getStringList(killerKey); // Recargar por si se modificó antes

        // Buscar el siguiente abismo robable (no protegido)
        String stolenGroup = null;
        int stolenIndex = -1;
        for (int i = 0; i < victimGroups.size(); i++) {
            String candidate = victimGroups.get(i);
            if (protectedAbysses.contains(candidate)) continue;
            stolenGroup = candidate;
            stolenIndex = i;
            break;
        }
        if (stolenGroup == null) {
            // Todos los abismos están protegidos
            return;
        }
        killerGroups.remove(stolenGroup);
        ItemStack stolenAbyssItem = Items.abyssContainer(stolenGroup);
        event.getDrops().add(stolenAbyssItem);

        victim.sendMessage(ChatColor.RED + "Perdiste tu abyss " + stolenGroup + " ante " + killerName + "!");
        killer.sendMessage(ChatColor.GREEN + "Robaste el abyss: " + stolenGroup + " de " + victimName + "!");
        //save changes to config
        config.set(victimKey, victimGroups);
        config.set(killerKey, killerGroups);
        Plugin.getPlugin(Plugin.class).saveConfig();
        Plugin.getPlugin(Plugin.class).reloadConfig();
    }

    public static boolean hasPurityHeart(Player p){
        return (com.rschao.events.soulEvents.GetSoulN(p) == 30 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 30);
    }
    public static boolean hasChaosHeart(Player p){
        return (com.rschao.events.soulEvents.GetSoulN(p) == 66 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 66);
    }

    static Technique fly = new Technique("fly", "Fly", false, cooldownHelper.secondsToMiliseconds(30), ((player, itemStack, objects) -> {
        Player p = player;
        p.setInvisible(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () ->{
            p.setInvisible(false);
            p.setFlying(false);
            p.setAllowFlight(false);
        }, 60);
    }));

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent ev){
        Player p = ev.getPlayer();
        if(p.getName().startsWith(".")) return;
        p.setResourcePack("https://www.dropbox.com/scl/fi/62qwfxhu9c93dns78l6ht/Server-Pack.zip?rlkey=p61zix8a3oc4mglrgz0hejube&st=dh92p5r5&dl=1");
    }
    @EventHandler
    void onPlayerUsePureHeart(PlayerInteractEvent ev){
        Player p = ev.getPlayer();
        ItemStack item = ev.getItem();
        if(item == null) return;
        if(item.getType().equals(Material.AIR)) return;
        if(item.getItemMeta().getPersistentDataContainer().isEmpty()) return;
        String color = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getPlugin(Plugin.class), "pureheart"), PersistentDataType.STRING);
        if(color == null) return;
        switch (color) {
            case "red":
                handleRed.use(p, item, Technique.nullValue());
            case "orange":
                handleOrange.use(p, item, Technique.nullValue());
                break;
            case "yellow":
                handleYellow.use(p, item, Technique.nullValue());
                break;
            case "green":
                handleGreen.use(p, item, Technique.nullValue());
                break;
            case "blue":
                handleBlue.use(p, item, Technique.nullValue());
                break;
            case "indigo":
                handleIndigo.use(p, item, Technique.nullValue());
                break;
            case "purple":
                handlePurple.use(p, item, Technique.nullValue());
                break;
            case "white":
                handleWhite.use(p, item, Technique.nullValue());
                break;
        }

    }

    static Technique handleRed = new Technique("handleRed", "Handle Red", false, cooldownHelper.minutesToMiliseconds(1), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            p.setHealth(Math.min(p.getHealth() + 6, p.getMaxHealth()));
            p.sendMessage(ChatColor.RED + "You have used the Red Pure Heart to heal 3 hearts!");
        }
        else{
            p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(30*20, 0));
            p.sendMessage(ChatColor.RED + "You have used the Red Pure Heart to gain Fire Resistance!");
        }
    }));

    static Technique handleOrange = new Technique("handleOrange", "Handle Orange", false, cooldownHelper.minutesToMiliseconds(4), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            for (ItemStack item : p.getInventory().getArmorContents()) {
                if (item != null && item.getItemMeta() instanceof Damageable && item.getDurability() < item.getType().getMaxDurability()) {
                    Damageable meta = (Damageable) item.getItemMeta();
                    meta.setDamage(0); // Set durability to maximum
                    item.setItemMeta(meta);
                }
            }
            p.sendMessage(ChatColor.RED + "You have used the Orange Pure Heart to repair your armor!");
        }
        else{
            p.addPotionEffect(PotionEffectType.STRENGTH.createEffect(60*20, 1));
            p.sendMessage(ChatColor.RED + "You have used the Orange Pure Heart to gain Strength II for 60 seconds!");
        }
    }));

    static Technique handleYellow = new Technique("handleYellow", "Handle Yellow", false, cooldownHelper.minutesToMiliseconds(2), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(60*5*20, 1));
            p.sendMessage(ChatColor.RED + "You have used the Yellow Pure Heart to gain Night Vision for 5 minutes!");
        }
        else{
            p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(30*20, 3));
            p.sendMessage(ChatColor.RED + "You have used the Yellow Pure Heart to gain Absorption IV for 30 seconds!");
        }
    }));

    static Technique handleGreen = new Technique("handleGreen", "Handle Green", false, cooldownHelper.minutesToMiliseconds(4), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            for (Player pl : p.getNearbyEntities(5, 5, 5).stream().filter(e -> e instanceof Player).map(e -> (Player) e).toList()) {
                pl.addPotionEffect(PotionEffectType.REGENERATION.createEffect(15*20, 2));

            }
            p.sendMessage(ChatColor.RED + "You have used the Green Pure Heart to give nearby players Regeneration III for 15 seconds!");
        }
        else{
            for(PotionEffect effect : p.getActivePotionEffects()){
                if(effect.getType().getCategory().name().contains("HARMFUL")){
                    p.removePotionEffect(effect.getType());
                    p.sendMessage(ChatColor.RED + "You have used the Green Pure Heart to remove all harmful effects!");
                }
            }
        }
    }));

    static Technique handleBlue = new Technique("handleBlue", "Handle Blue", false, cooldownHelper.minutesToMiliseconds(3), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            p.getWorld().setClearWeatherDuration(3600*20);
            p.sendMessage(ChatColor.RED + "You have used the Blue Pure Heart to clear the weather!");
        }
        else{
            p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(10*20, 1));
            p.sendMessage(ChatColor.RED + "You have used the Blue Pure Heart to gain Resistance II for 10 seconds!");
        }
    }));

    static Technique handleIndigo = new Technique("handleIndigo", "Handle Indigo", false, cooldownHelper.minutesToMiliseconds(2), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            Location loc = p.getBedLocation();
            if(loc == null) loc = p.getWorld().getSpawnLocation();
            p.teleport(loc);
            p.sendMessage(ChatColor.RED + "You have used the Indigo Pure Heart to teleport to your spawn point!");
        }
        else{
            p.addPotionEffect(PotionEffectType.SPEED.createEffect(30*20, 2));
            p.sendMessage(ChatColor.RED + "You have used the Indigo Pure Heart to gain Speed III for 30 seconds!");
        }
    }));

    static Technique handlePurple = new Technique("handlePurple", "Handle Purple", false, cooldownHelper.minutesToMiliseconds(5), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                if(e instanceof LivingEntity le && !le.equals(p)) {
                    le.addPotionEffect(PotionEffectType.GLOWING.createEffect(30*20, 2));
                }
            }
            p.sendMessage(ChatColor.RED + "You have used the Purple Pure Heart to give nearby entities Glowing III for 30 seconds!");
        }
        else{
            for (Player pl : p.getNearbyEntities(5, 5, 5).stream().filter(e -> e instanceof Player).filter(e -> !e.equals(p)).map(e -> (Player) e).toList()) {
                pl.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(5*20, 1));
            }
            p.sendMessage(ChatColor.RED + "You have used the Purple Pure Heart to give nearby players Weakness II for 5 seconds!");
        }
    }));

    static Technique handleWhite = new Technique("handleWhite", "Handle White", false, cooldownHelper.minutesToMiliseconds(10), ((player, itemStack, objects) -> {
        Player p = player;
        if(p.isSneaking()){
            p.setFoodLevel(20);
            p.setSaturation(20);
            p.sendMessage(ChatColor.RED + "You have used the White Pure Heart to fully heal and satiate yourself!");
        }
        else{
            for(PotionEffect effect : p.getActivePotionEffects()){
                p.removePotionEffect(effect.getType());
            }
            p.sendMessage(ChatColor.RED + "You have used the White Pure Heart to remove all effects!");
        }
    }));
}
