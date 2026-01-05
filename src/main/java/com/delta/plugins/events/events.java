package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.delta.plugins.techs.Necrozma;
import com.delta.plugins.techs.chaosWielder;
import com.delta.plugins.techs.offspring;
import com.delta.plugins.techs.poet;
import com.rschao.plugins.fightingpp.techs.fly;
import com.rschao.plugins.techniqueAPI.TechAPI;
import com.rschao.plugins.techniqueAPI.event.TechniqueReadDescriptionEvent;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.feedback.hotbarMessage;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
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
        Player p = ev.getPlayer();
        if(ev.getItem() == null) return;
        if(ev.getItem().getType().equals(Material.AIR)) return;
        if(!ev.getItem().isSimilar(Items.hoe())) return;
        if(!ev.getPlayer().isSneaking()) return;
        Player p2 = ev.getPlayer();
        fly.use(new TechniqueContext(p2, ev.getItem()));
    }

    @EventHandler
    void onPlayerUseTech(PlayerInteractEvent ev){
        Player p = ev.getPlayer();
        if(ev.getItem() == null) return;
        if(ev.getItem().getItemMeta() == null) return;
        if(ev.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"), PersistentDataType.STRING)){
            String abyssId = ev.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getPlugin(Plugin.class), "abyss_id"), PersistentDataType.STRING);
            if(abyssId == null || abyssId.equals("none")) {
                p.sendMessage("Abyss ID es inválido: " + abyssId);
            }
            List<String> groupIds = Plugin.getPlugin(Plugin.class).getConfig().getStringList(sanitizePlayerName(p.getName()) + ".groupids");
            int maxSlots = (p.hasPermission("delta.abyss.4") ? 4 : 3);
            if(groupIds.size() >= maxSlots){
                p.sendMessage("You cannot carry more abyss.");
                return;
            }
            // Si el abyss es especial necesita 2 ranuras libres
            boolean isSpecial = Plugin.getSpecialAbysses().contains(abyssId);
            if(isSpecial){
                int neededSlots = 2;
                if(groupIds.size() > maxSlots - neededSlots){
                    p.sendMessage("You cannot carry more abyss. Try removing another abyss first.");
                    return;
                }
            }
            if(groupIds.contains(abyssId)){
                p.sendMessage("You already have this abyss.");
                return;
            }
            // Añadir el abyss; si es especial, ocupa 2 ranuras (añadimos el id dos veces)
            groupIds.add(abyssId);
            if(isSpecial){
                groupIds.add(abyssId);
            }
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
                String newGroupId = getGroupId(p, newIndex).replace("_", " ");
                p.sendMessage("Your abyss has switched to " + newGroupId);
            }
            else{
                Technique technique = TechRegistry.getAllTechniques(groupId).get(techIndex);
                if(technique == null) return;
                technique.use(new TechniqueContext(p, ev.getItem()));
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
            p.sendMessage("You have switched to technique: " + TechRegistry.getAllTechniques(groupId).get(techIndex).getDisplayName());
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
        if (event.getDamager() instanceof Player player) {
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

        // NUEVO: Si la entidad golpeada tiene algún modificador, hay 3% de probabilidad de lanzar la técnica correspondiente
        try {
            Entity ent = event.getEntity();
            if (ent instanceof LivingEntity le && !(ent instanceof Player)) {
                Random rnd = new Random();

                // chaos
                Boolean isChaos = le.getPersistentDataContainer().get(new NamespacedKey("tower","chaos_wielder"), PersistentDataType.BOOLEAN);
                if (isChaos != null && isChaos && rnd.nextDouble() < 0.03) {
                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> chaosWielder.spawnWaveAtEntity(le), 20L);
                }

                // carnage
                Boolean isCarnage = le.getPersistentDataContainer().get(new NamespacedKey("tower","carnage_wielder"), PersistentDataType.BOOLEAN);
                if (isCarnage != null && isCarnage && rnd.nextDouble() < 0.06) {
                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> offspring.carnageAtEntity(le), 20L);
                }

                // flash blind
                Boolean isFlash = le.getPersistentDataContainer().get(new NamespacedKey("tower","flash_wielder"), PersistentDataType.BOOLEAN);
                if (isFlash != null && isFlash && rnd.nextDouble() < 0.05) {
                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> Necrozma.flashBlindAtEntity(le), 20L);
                }

                // runic hellfire
                Boolean isRunic = le.getPersistentDataContainer().get(new NamespacedKey("tower","runic_wielder"), PersistentDataType.BOOLEAN);
                if (isRunic != null && isRunic && rnd.nextDouble() < 0.04) {
                    Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> poet.runicHellfireAtEntity(le), 20L);
                }
            }
        } catch (Exception ignored) {}

    }

    @EventHandler
    public void onHotbarSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem == null || !newItem.hasItemMeta()) return;
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
        groupId = groupId.replace("_", " ");
        String techName = techs.get(techIndex).getDisplayName();
        hotbarMessage.sendHotbarMessage(player, "Technique: " + techName + " (Abyss " + groupId + ")");
    }
    /*@EventHandler
    void onPreCast(PreCastEvent event) {
        if(event.getSpell().getName().equals("Whacka Summoner")){
            Whacka_Summon.summonWhackaTech.use(event.getMage().getPlayer(), event.getMage().getActiveWand().getItem());
        }
        if(event.getSpell().getName().equals("Pure Heart laser")){
            TechRegistry.getById("pureheart_laser").use(event.getMage().getPlayer(), event.getMage().getActiveWand().getItem());
        }
    }*/

    @EventHandler (priority = EventPriority.LOWEST)
    void onPlayerOmniNegate(EntityDamageByEntityEvent ev){
        if(!(ev.getEntity() instanceof Player p)) return;
        if(!hasOmniNegate.getOrDefault(p.getUniqueId(), false)) return;
        ev.setCancelled(true);
        if(!(ev.getDamager() instanceof LivingEntity le)) return;
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

        if (victimGroups.isEmpty() || killerGroups.isEmpty()) {
            return;
        }

        // Si la víctima solo tiene un abismo, no se roba nada
        if (victimGroups.size() == 1) {
            return;
        }
        if(com.rschao.smp.Plugin.getPauseLives()) return;

        // Buscar el siguiente abismo robable (no protegido)
        String stolenGroup = victimGroups.stream().filter(candidate -> !protectedAbysses.contains(candidate)).findFirst().orElse(null);
        if (stolenGroup == null) {
            // Todos los abismos están protegidos
            return;
        }
        // Eliminar todas las apariciones del abyss robado (libera ambas ranuras si era especial)
        victimGroups.removeIf(s -> s.equals(stolenGroup));

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
        if(p.hasPermission("purehearts.reverse"))return (com.rschao.events.soulEvents.GetSoulN(p) == 66 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 66);

        return (com.rschao.events.soulEvents.GetSoulN(p) == 30 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 30);
    }
    public static boolean hasChaosHeart(Player p){

        if(p.hasPermission("purehearts.reverse"))return (com.rschao.events.soulEvents.GetSoulN(p) == 30 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 30);

        return (com.rschao.events.soulEvents.GetSoulN(p) == 66 || com.rschao.events.soulEvents.GetSecondSoulN(p) == 66);
    }



    static Technique fly = new Technique("fly", "Fly", new TechniqueMeta(false, cooldownHelper.secondsToMiliseconds(30), List.of("Grants temporary flight.")), TargetSelectors.self(), (ctx, token) -> {
        Player p = ctx.caster();
        p.setInvisible(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.sendMessage(ChatColor.AQUA + "You can now fly for 30 seconds!");
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
            p.setFlying(false);
            p.setInvisible(false);
            p.setAllowFlight(false);
            p.sendMessage(ChatColor.AQUA + "Your flight has ended.");
        }, 30 * 20L);
    });
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
                handleRed.use(new TechniqueContext(p, item));
            case "orange":
                handleOrange.use(new TechniqueContext(p, item));
                break;
            case "yellow":
                handleYellow.use(new TechniqueContext(p, item));
                break;
            case "green":
                handleGreen.use(new TechniqueContext(p, item));
                break;
            case "blue":
                handleBlue.use(new TechniqueContext(p, item));
                break;
            case "indigo":
                handleIndigo.use(new TechniqueContext(p, item));
                break;
            case "purple":
                handlePurple.use(new TechniqueContext(p, item));
                break;
            case "white":
                handleWhite.use(new TechniqueContext(p, item));
                break;
        };
    }

    static Technique handleRed = new Technique(
        "handleRed",
        "Handle Red",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(1), List.of("Heal or grant fire resistance.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if(p.isSneaking()){
                p.setHealth(Math.min(p.getHealth() + 6, p.getMaxHealth()));
                p.sendMessage(ChatColor.RED + "You have used the Red Pure Heart to heal 3 hearts!");
            }
            else{
                p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(30*20, 0));
                p.sendMessage(ChatColor.RED + "You have used the Red Pure Heart to gain Fire Resistance!");
            }
        }
    );

    static Technique handleOrange = new Technique(
        "handleOrange",
        "Handle Orange",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Repair armor or grant strength.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
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
        }
    );

    static Technique handleYellow = new Technique(
        "handleYellow",
        "Handle Yellow",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("Night vision or absorption.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if(p.isSneaking()){
                p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(60*5*20, 1));
                p.sendMessage(ChatColor.RED + "You have used the Yellow Pure Heart to gain Night Vision for 5 minutes!");
            }
            else{
                p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(30*20, 3));
                p.sendMessage(ChatColor.RED + "You have used the Yellow Pure Heart to gain Absorption IV for 30 seconds!");
            }
        }
    );

    static Technique handleGreen = new Technique(
        "handleGreen",
        "Handle Green",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(4), List.of("Regenerate allies or cleanse.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
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
        }
    );

    static Technique handleBlue = new Technique(
        "handleBlue",
        "Handle Blue",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(3), List.of("Clear weather or grant resistance.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if(p.isSneaking()){
                p.getWorld().setClearWeatherDuration(3600*20);
                p.sendMessage(ChatColor.RED + "You have used the Blue Pure Heart to clear the weather!");
            }
            else{
                p.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(10*20, 1));
                p.sendMessage(ChatColor.RED + "You have used the Blue Pure Heart to gain Resistance II for 10 seconds!");
            }
        }
    );

    static Technique handleIndigo = new Technique(
        "handleIndigo",
        "Handle Indigo",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(2), List.of("Teleport to spawn or grant speed.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if(p.isSneaking()){
                Location loc = (p.getBedLocation() != null) ? p.getBedLocation() : p.getWorld().getSpawnLocation();
                p.teleport(loc);
                p.sendMessage(ChatColor.RED + "You have used the Indigo Pure Heart to teleport to your spawn point!");
            }
            else{
                p.addPotionEffect(PotionEffectType.SPEED.createEffect(30*20, 2));
                p.sendMessage(ChatColor.RED + "You have used the Indigo Pure Heart to gain Speed III for 30 seconds!");
            }
        }
    );

    static Technique handlePurple = new Technique(
        "handlePurple",
        "Handle Purple",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(5), List.of("Give glowing to entities or weakness to players.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
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
        }
    );

    static Technique handleWhite = new Technique(
        "handleWhite",
        "Handle White",
        new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Satiate or cleanse all effects.")),
        TargetSelectors.self(),
        (ctx, token) -> {
            Player p = ctx.caster();
            if(p.isSneaking()){
                p.setFoodLevel(20);
                p.setSaturation(20);
                p.sendMessage(ChatColor.RED + "You have used the White Pure Heart to fully satiate yourself!");
            }
            else{
                for(PotionEffect effect : p.getActivePotionEffects()){
                    p.removePotionEffect(effect.getType());
                }
                p.sendMessage(ChatColor.RED + "You have used the White Pure Heart to remove all effects!");
            }
        }
    );

    @EventHandler
    void onFrameRotate(PlayerInteractEntityEvent e){
        if(PitEvents.getFloor(e.getPlayer()) <1 || PitEvents.getFloor(e.getPlayer()) >100) return;
        if(e.getRightClicked() instanceof org.bukkit.entity.ItemFrame frame){
            ItemStack[] bannedItems = {
                    Items.sapphireWhackaBump(),
                    Items.silverWhackaBump(),
                    Items.goldWhackaBump(),
                    Items.aquamarineWhackaBump(),
                    Items.emeraldWhackaBump(),
                    Items.amethystWhackaBump(),
                    Items.bronzeWhackaBump(),
                    Items.onyxWhackaBump(),
                    Items.rubyWhackaBump(),
            };
            ItemStack itemInFrame = frame.getItem();
            for(ItemStack bannedItem : bannedItems) {
                if (itemInFrame.isSimilar(bannedItem)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}
