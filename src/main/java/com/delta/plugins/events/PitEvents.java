package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.delta.plugins.items.Items;
import com.delta.plugins.items.PitItems;
import com.delta.plugins.mobs.MobManager;
import com.delta.plugins.mobs.custom.Whacka_1_12_10;
import com.delta.plugins.whacka.WhackaManager;
import com.rschao.api.audio.AudioSelector;
import com.rschao.events.definitions.PlayerPopHeartEvent;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.context.TechniqueContext;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import com.rschao.plugins.techniqueAPI.tech.util.PlayerTechniqueManager;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.profile.ModelProfile;
import kr.toxicity.model.api.tracker.DummyTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public class PitEvents implements Listener {
    static Map<String, Integer> floor = new HashMap<>();
    static Map<String, Boolean> secondkey = new HashMap<>();
    static Player host;
    static boolean runTimer = true;
    static List<Player> players = new ArrayList<>();

    // --- Nuevas estructuras para el timer por jugador ---
    // acumulado en milisegundos
    static Map<String, Long> timerAccum = new HashMap<>();
    // si está corriendo, timestamp de inicio en ms; absent o 0 => pausado
    static Map<String, Long> timerStart = new HashMap<>();
    static boolean checkAudioPlay(Player p){
        int f = getFloor(p);
        if(f==1 || f==99) return true;
        return false;
    }
    static void playAudio(Player p){
        int f = getFloor(p);
        if(f==99){
            Player[] ps = new Player[]{p};
            AudioSelector.PlayBossAudio("soft_light", ps);
            return;
        }

        Player[] ps = new Player[]{p};
        AudioSelector.PlayBossAudio("tower", ps);


    }
    // Comprueba si un piso debe pausar el timer
    static boolean shouldPauseOnFloor(int f){
        if(f % 10 != 0) return false;
        return f != 50 && f != 80 && f != 90;
    }

    static void startTimer(Player p){
        String n = p.getName();
        timerAccum.put(n, 0L);
        players.add(p);
        timerStart.put(n, System.currentTimeMillis());
        if(runTimer){
            runTimer = false;

        }
    }
    public static void registerTimer(){

    }
    static void pauseTimer(Player p){
        String n = p.getName();
        Long start = timerStart.get(n);
        if(start == null || start == 0) return;
        long now = System.currentTimeMillis();
        long acc = timerAccum.getOrDefault(n, 0L);
        acc += (now - start);
        timerAccum.put(n, acc);
        timerStart.remove(n);
    }
    static void resumeTimer(Player p){
        String n = p.getName();
        Long start = timerStart.get(n);
        if(start != null && start != 0) return; // ya corriendo
        timerStart.put(n, System.currentTimeMillis());
    }
    static long getElapsedMillis(Player p){
        String n = p.getName();
        long acc = timerAccum.getOrDefault(n, 0L);
        Long start = timerStart.get(n);
        if(start != null && start != 0){
            acc += (System.currentTimeMillis() - start);
        }
        return acc;
    }
    static String formatMillis(long ms){
        long totalSec = ms / 1000;
        long hh = totalSec / 3600;
        long mm = (totalSec % 3600) / 60;
        long ss = totalSec % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }
    // Guarda el tiempo final en timers.yml en tower.timer.<playername> (segundos) y manda mensaje al jugador
    static void finishTower(Player p){
        String n = p.getName();
        pauseTimer(p);
        long ms = getElapsedMillis(p);
        long seconds = ms / 1000;
        String human = formatMillis(ms);
        // mensaje en chat
        p.sendMessage("Has terminado la torre. Tiempo total: " + human);
        // guardar en timers.yml
        File file = new File(Plugin.getPlugin(Plugin.class).getDataFolder(), "timers.yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("tower.timer." + n, seconds);
        try{
            cfg.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
        // limpiar datos
        timerAccum.remove(n);
        timerStart.remove(n);
    }

    public static void startPit(Player p){
        floor.put(p.getName(), 1);
        p.sendMessage("Has comenzado las Cien Pruebas. Estás en el piso 1.");
        // iniciar timer al empezar la prueba
        startTimer(p);
        playAudio(p);
        // comprobar si se debe pausar en el piso inicial (no aplica para 1, pero mantenemos la lógica)
        if(shouldPauseOnFloor(getFloor(p))){
            pauseTimer(p);
            p.sendMessage("El timer está en pausa en este piso.");
        }
    }
    public static int getFloor(Player p){
        return floor.getOrDefault(p.getName(), 0);
    }
    public static void nextFloor(Player p){
        int currentFloor = getFloor(p);
        if(currentFloor == 0){
            p.sendMessage("No has comenzado las Cien Pruebas.");
            return;
        }

        // permitir avanzar más allá de 100; si >100 => finalizar
        currentFloor++;
        floor.put(p.getName(), currentFloor);
        if(checkAudioPlay(p)) playAudio(p);
        p.sendMessage("" + checkAudioPlay(p) + ", " + getFloor(p));
        if(currentFloor<93) PlayerTechniqueManager.setInmune(p.getUniqueId(), false, 0);
        if(currentFloor > 100){
            // finalizar torre
            p.sendMessage("Has completado las Cien Pruebas. Felicidades.");
            // log y guardar tiempo
            finishTower(p);
            floor.put(p.getName(), 0);
            return;
        }

        p.sendMessage("Has avanzado al piso " + currentFloor + " de las Cien Pruebas.");

        // controlar pause/resume: si el nuevo piso requiere pausa -> pause, en otro caso resume
        if(shouldPauseOnFloor(currentFloor)){
            pauseTimer(p);
            p.sendMessage("Timer pausado en piso " + currentFloor + ".");
        } else {
            resumeTimer(p);
            // opcional feedback mínimo
            // p.sendMessage("Timer reanudado.");
        }
    }
    public static void setFloor(Player p, int amount){
        int currentFloor = getFloor(p);
        if(currentFloor == 0){
            p.sendMessage("No has comenzado las Cien Pruebas.");
            return;
        }
        currentFloor+= amount;
        // chequear finalización según especificación (floor >100)
        if(currentFloor > 100){
            p.sendMessage("Has completado las Cien Pruebas. Felicidades.");
            // actualizar antes de finalizar
            floor.put(p.getName(), 0);
            finishTower(p);
            return;
        }
        floor.put(p.getName(), currentFloor);
        p.sendMessage("Has avanzado al piso " + currentFloor + " de las Cien Pruebas.");

        // control de pausa/resume en el nuevo piso
        if(shouldPauseOnFloor(currentFloor)){
            pauseTimer(p);
            p.sendMessage("Timer pausado en piso " + currentFloor + ".");
        } else {
            resumeTimer(p);
            // p.sendMessage("Timer reanudado.");
        }
    }
    // Nuevo: establecer el piso absoluto (no relativo). Maneja pause/resume y finalización.
    public static void setFloorAbsolute(Player p, int newFloor){
        if(p == null) return;
        if(getFloor(p) == 0){
            // si no ha empezado, iniciarlo
            p.sendMessage("Iniciando las Cien Pruebas...");
            startPit(p);
        }
        if(newFloor > 100){
            p.sendMessage("Has completado las Cien Pruebas. Felicidades.");
            finishTower(p);
            floor.put(p.getName(), 0);
            return;
        }
        floor.put(p.getName(), newFloor);
        p.sendMessage("Piso establecido a " + newFloor + " de las Cien Pruebas.");

        // control de pausa/resume según el piso
        if(shouldPauseOnFloor(newFloor)){
            pauseTimer(p);
            p.sendMessage("Timer pausado en piso " + newFloor + ".");
        } else {
            resumeTimer(p);
        }
    }
    public static void resetFloor(Player p){
        floor.put(p.getName(), 0);
        long ms = getElapsedMillis(p);
        String human = formatMillis(ms);
        // mensaje en chat
        p.sendMessage("Has terminado la torre. Tiempo total: " + human);
        // limpiar timer al resetear
        timerAccum.remove(p.getName());
        timerStart.remove(p.getName());
    }
    public static Player getHost(){
        return host;
    }
    public static void setHost(Player p){
        host = p;
    }
    @EventHandler
    void onPlayerChat(AsyncPlayerChatEvent ev){
        if(ev.getMessage().equals("!keyhole") && !Plugin.getMiawzVer()){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.key_hole);
            p.sendMessage("Has recibido un agujero para llave.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!key") && !Plugin.getMiawzVer()){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.floor_key);
            p.sendMessage("Has recibido una llave.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!coin") && !Plugin.getMiawzVer()){
            Player p = ev.getPlayer();
            p.getInventory().addItem(PitItems.coin);
            p.sendMessage("Has recibido una moneda.");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().equals("!kinektos") && !Plugin.getMiawzVer()){
            Player p = ev.getPlayer();
            p.getInventory().addItem(Items.hoe());
            p.sendMessage("Hi, kinektos");
            ev.setCancelled(true);
        }
        else if(ev.getMessage().startsWith("!coins") && !Plugin.getMiawzVer()){
            Player p = ev.getPlayer();
            if(p.getInventory().firstEmpty() == -1){
                p.sendMessage("No tienes espacio en el inventario.");
                ev.setCancelled(true);
                return;
            }
            ev.setCancelled(true);
            if(ev.getMessage().length() <7) return;
            String[] parts = ev.getMessage().split(" ");
            p.sendMessage(parts);
            p.sendMessage(parts.length + "");
            if(parts.length < 2) return;
            int amount;
            try{
                amount = Integer.parseInt(parts[1]);
            } catch (Exception e){
                return;
            }
            p.getInventory().addItem(PitItems.CoinPaper(amount));
        }
        else if(ev.getMessage().startsWith("!bumps")){
            if(ev.getMessage().length() < 7) {
                ev.setCancelled(true);
                return;
            }
            else {
                String[] parts = ev.getMessage().split(" ");
                if(parts.length < 2) return;
                String type = parts[1].toLowerCase();
                if(type.equals("spawn")){
                    Player p = ev.getPlayer();
                    Bukkit.getScheduler().runTask(Plugin.getPlugin(Plugin.class), () -> {
                        try{
                            WhackaManager.spawnWhackaEntity(p.getLocation());
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                    p.sendMessage("Whacka spawneado.");


                    ev.setCancelled(true);
                }
                if(type.equals("common")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("rare")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.rare_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("void")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.voidBump());
                    ev.setCancelled(true);
                }
                else if(type.equals("gold")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.gold_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("silver")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.silver_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("amethyst")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.amethyst_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("ruby")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.ruby_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("emerald")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.emerald_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("bronze")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.bronze_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("aqua") || type.equals("aquamarine")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.aquamarine_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("sapphire")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.sapphire_whacka_bump);
                    ev.setCancelled(true);
                }
                else if(type.equals("onyx")){
                    Player p = ev.getPlayer();
                    p.getInventory().addItem(Items.onyx_whacka_bump);
                    ev.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent ev){
        ItemStack item = ev.getItem();
        if(item == null) return;
        if(item.getItemMeta() == null) return;
        if(ev.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getPlugin(Plugin.class), "coin_value"), PersistentDataType.INTEGER)){
            Player p = ev.getPlayer();
            int value = ev.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getPlugin(Plugin.class), "coin_value"), PersistentDataType.INTEGER);
            if(value <= 0) return;
            item.setAmount(item.getAmount()-1);
            for (int i = 0; i < value; i++) {
                Item it = p.getWorld().dropItemNaturally(p.getLocation(), PitItems.coin);
                it.setPickupDelay(0);
            }
            p.sendMessage("Has canjeado una moneda de valor " + value + " por " + value + " moneda(s) normal(es).");

        }
        if(ev.getClickedBlock() == null || ev.getClickedBlock().getType().equals(Material.AIR)) return;
        if(!ev.getClickedBlock().getType().equals(Material.IRON_TRAPDOOR)) return;
        if(ev.getItem().isSimilar(PitItems.floor_key)){
            if(ev.getClickedBlock() == null) return;
            if(getFloor(ev.getPlayer()) <= 0 || getFloor(ev.getPlayer()) >= 100) return;
            nextFloorTech.use(new TechniqueContext(ev.getPlayer(), ev.getItem()));
        }
        else if (item.getType().equals(Material.TRIAL_KEY)){
            if(item.getItemMeta().getLore().isEmpty()) return;
            Block b = ev.getClickedBlock();
            if(!CheckKeyHole(b.getLocation(), item.getItemMeta().getLore().get(0))) return;

            // Guarda tipo y datos ANTES de cambiar a aire
            Material originalType = b.getType();
            org.bukkit.block.data.BlockData originalData = b.getBlockData();

            b.setType(Material.AIR);
            b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1, 1);
            ev.getPlayer().sendMessage("Has abierto la puerta con la llave.");
            Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
                b.setType(originalType);
                b.setBlockData(originalData);
                b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1, 1);
            }, 20*5);
        }
    }
    @EventHandler
    void onBlockPlace(BlockPlaceEvent ev){
        ItemStack item = ev.getItemInHand();
        if(item.getItemMeta() == null) return;
        if(item.getType().equals(Material.IRON_TRAPDOOR)){

            SaveKeyHole(ev.getBlockPlaced().getLocation(), item.getItemMeta().getDisplayName());


        }
    }

    @EventHandler
    void onMobDed(EntityDeathEvent ev){
        Player p = ev.getEntity().getKiller();
        if(ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("tower", "necrozma_summoned"))){
            ev.getDrops().clear();
            return;
        }
        if(!ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("tower", "floor"))) return;
        if(!ev.getEntity().getPersistentDataContainer().has(Whacka_1_12_10.WHACKA_KEY))ev.getDrops().clear();
        if(p==null) {
            for(Player p1 : Bukkit.getOnlinePlayers()){
                try{
                    if(getFloor(p1) >0){
                        p = p1;
                        break;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        int mobs = MobManager.buildSpawnFunctions(getFloor(p)).size();
        int coins = computeCoinDrop(getFloor(p), mobs);
        for (int i = 0; i < coins; i++) {
            ev.getDrops().add(PitItems.coin);
        }

        if(ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("tower", "key"))){
            ev.getDrops().clear();

            // PARA CADA JUGADOR EN EL MISMO PISO: intentar entregar llave según reglas:
            int floorInt = getFloor(p);
            for(Player candidate : p.getWorld().getPlayers()){
                if(getFloor(candidate) == floorInt){
                    // comprobar cantidad actual
                    int have = countKeys(candidate);
                    if(have <= 0){
                        giveKeySafe(candidate);
                        candidate.sendMessage("Has recibido una llave.");
                    } else if(have == 1 && hasExtraKeyEffect(candidate)){
                        // permite segunda llave
                        for(Player pl: p.getWorld().getPlayers()){
                            if(countKeys(pl) == 0 && getFloor(pl) == floorInt){
                                giveKeySafe(candidate);
                            }
                        }
                        giveKeySafe(candidate);
                        candidate.sendMessage("Has recibido una llave adicional (efecto activo).");
                    } else {
                        // no dar llave
                    }
                }
            }
        }

    }

    // Reemplaza la antigua checkHasKey por la nueva lógica arriba; eliminar o comentar la vieja
    // ...existing code...

    private void giveKeySafe(Player p){
        // Evitar dar más de 2 si el jugador ya tiene efecto
        int have = countKeys(p);
        if(hasExtraKeyEffect(p) && have >= 2) return;
        if(!hasExtraKeyEffect(p) && have >= 1) return;

        Item i = p.getWorld().dropItemNaturally(p.getLocation(), PitItems.floor_key);
        i.setPickupDelay(0);
    }

    @EventHandler
    void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event){
        Player p = event.getEntity();
        if(getFloor(p) > 0){
            p.sendMessage("Has muerto en el piso " + getFloor(p) + " de la torre.");
            players.remove(p);
            resetFloor(p);
        }
    }
    static void SavePitKeyHole(Location loc){
        String s = "pit_keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Location> list = (List<Location>) config.get("keyholes", new ArrayList<>());
        list.add(loc);
        config.set("keyholes", list);
        try{
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    static void SaveKeyHole(Location loc, String pass){
        String s = "keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        if(!file.exists()){
            try{
                file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        UUID id = UUID.randomUUID();
        config.set("keyholes." + id.toString() + ".loc", loc);
        config.set("keyholes." + id.toString() + ".pass", pass);
        try{
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    static boolean CheckPitKeyHole(Location loc){
        String s = "pit_keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<Location> list = (List<Location>) config.get("keyholes", new ArrayList<>());
        if(list.contains(loc)) return true;

        return false;
    }
    static boolean CheckKeyHole(Location loc, String pass){
        String s = "keyholes";
        File file = new File(com.delta.plugins.Plugin.getPlugin(com.delta.plugins.Plugin.class).getDataFolder(), s + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.isConfigurationSection("keyholes")) return false;

        for(String key : config.getConfigurationSection("keyholes").getKeys(false)){
            Object savedLoc = config.get("keyholes." + key + ".loc");
            String savedPass = config.getString("keyholes." + key + ".pass");
            if(savedLoc != null && savedLoc.equals(loc) && savedPass != null && savedPass.equals(pass)) return true;
        }
        return false;
    }
    @EventHandler
    void onSlimeSplit(org.bukkit.event.entity.SlimeSplitEvent ev){
        if(ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("tower", "floor"))){
            ev.setCancelled(true);
            try{
                ev.getEntity().remove();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @EventHandler
    void onEntityDamageByEntity(EntityDamageByEntityEvent ev){
        if(ev.getEntity().getPersistentDataContainer().has(new NamespacedKey("dario", "ddt"))){
            if(!ev.getDamageSource().getDamageType().equals(DamageType.PLAYER_ATTACK)){
                ev.setCancelled(true);
            }
        }
    }
    @EventHandler
    void onPopCheartEvent(PlayerPopHeartEvent ev){
        if(getFloor(ev.getPlayer()) > 0){
            ev.getPlayer().addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 0));
        }
    }
    private static final Random COIN_RNG = new Random();

    /**
     * Calcula cuántas monedas debe soltar un mob al morir.
     * - targetPerFloor: monedas por limpiar el piso (crece suave).
     * - p: probabilidad por mob de soltar 1 moneda = targetPerFloor / mobs.
     * - bonusProb: pequeña probabilidad de soltar monedas adicionales (escala con el piso).
     */
    static int computeCoinDrop(int floor, int mobs) {
        if (mobs <= 0) return 0;

        // Ajusta estos parámetros según conveniencia:
        double targetPerFloor = Math.min(3.0 + floor * 1.25, 20.0); // monedas esperadas por piso (clamped)
        double p = targetPerFloor / mobs; // probabilidad por mob de soltar la moneda base
        p = Math.max(0.02, Math.min(p, 0.5)); // evitar 0 y evitar >50%

        double bonusProb = Math.min(0.02 + floor * 0.003, 0.15); // pequeña probabilidad de bonus
        int coins = 0;

        if (COIN_RNG.nextDouble() < p) {
            coins = 3;
            if (COIN_RNG.nextDouble() < bonusProb) {
                // bonus adicional que escala muy lentamente con el piso
                coins += 1 + (int)(floor / 20.0);
            }
        }

        // tope por kill para evitar drops explosivos
        return Math.min(coins, 8);
    }
    static Technique nextFloorTech = new Technique("pit_next_floor", "Next Floor", new TechniqueMeta(false, 5000, List.of()), TargetSelectors.self(), (ctx, token) -> {
        Player p = ctx.caster();
        ItemStack item = ctx.sourceItem();
        p.sendMessage("Has usado una llave para abrir la puerta.");
        // consumir 1 del item en la mano
        item.setAmount(item.getAmount()-1);

        // tras consumir el stack en mano puede quedar 0; contar llaves en inventario
        Bukkit.getScheduler().runTaskLater(Plugin.getPlugin(Plugin.class), () -> {
            // chequeo y eliminación del efecto si ya no hay llaves
            removeExtraIfNoKeys(p);
        }, 1L);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nextfloor " + p.getName());
    });

    public static int countKeys(Player p){
        int count = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.isSimilar(PitItems.floor_key)) {
                count += is.getAmount();
            }
        }
        return count;
    }

    static boolean hasExtraKeyEffect(Player p){
        return secondkey.getOrDefault(p.getName(), false);
    }

    public static void setExtraKeyEffect(Player p, boolean b){
        secondkey.put(p.getName(), b);
    }

    static void removeExtraIfNoKeys(Player p){
        int have = countKeys(p);
        if(have == 0 && hasExtraKeyEffect(p)){
            setExtraKeyEffect(p, false);
            p.sendMessage("El efecto de llave adicional ha sido removido por falta de llaves.");
        }
    }

}
