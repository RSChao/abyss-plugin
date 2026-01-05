package com.delta.plugins.events.specialEvents;

import com.delta.plugins.Plugin;
import com.delta.plugins.events.events;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GravesManager {
    public static class GraveEntry {
        public final Location location;
        public final String activator;
        public GraveEntry(Location location, String activator) {
            this.location = location;
            this.activator = activator;
        }
    }

    private static final JavaPlugin plugin = Plugin.getPlugin(Plugin.class);
    private static final File gravesFile = new File(plugin.getDataFolder(), "graves.yml");
    private static YamlConfiguration cfg;

    private static void ensureLoaded() {
        if (cfg != null) return;
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!gravesFile.exists()) {
            try {
                gravesFile.createNewFile();
                cfg = YamlConfiguration.loadConfiguration(gravesFile);
                cfg.set("graves", Collections.emptyMap());
                cfg.save(gravesFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            cfg = YamlConfiguration.loadConfiguration(gravesFile);
        }
    }

    public static void saveGrave(String name, Location loc, String activator) {
        ensureLoaded();
        String base = "graves." + name + ".";
        cfg.set(base + "world", loc.getWorld().getName());
        cfg.set(base + "x", loc.getX());
        cfg.set(base + "y", loc.getY());
        cfg.set(base + "z", loc.getZ());
        cfg.set(base + "activator", activator);
        try {
            cfg.save(gravesFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String, GraveEntry> getGraves() {
        ensureLoaded();
        if (!cfg.contains("graves")) return Collections.emptyMap();
        Map<String, GraveEntry> out = new LinkedHashMap<>();
        Set<String> keys = cfg.getConfigurationSection("graves").getKeys(false);
        for (String k : keys) {
            String base = "graves." + k + ".";
            String world = cfg.getString(base + "world", null);
            if (world == null) continue;
            World w = Bukkit.getWorld(world);
            if (w == null) continue;
            double x = cfg.getDouble(base + "x", 0);
            double y = cfg.getDouble(base + "y", 0);
            double z = cfg.getDouble(base + "z", 0);
            String activator = cfg.getString(base + "activator", null);
            Location loc = new Location(w, x, y, z);
            out.put(k, new GraveEntry(loc, activator));
        }
        return out;
    }

    public static void ensureHeartsSpawned() {
    }

    // Nuevo: transferir las abysses del activador a graves.yml y reemplazarlas por "???"
    public static void transferAbysses(String activatorName, String graveName) {
        if (activatorName == null || graveName == null) return;
        OfflinePlayer off = Bukkit.getOfflinePlayer(activatorName);
        if (off == null) return;
        String uuid = off.getUniqueId().toString();

        // Intentar localizar listas de abysses en config principal bajo varias rutas comunes
        List<String> found = new ArrayList<>();
        org.bukkit.configuration.file.FileConfiguration mainCfg = plugin.getConfig();

        String[] candidatePaths = new String[] {
                off.getName() + ".groupids",
        };
        for (String path : candidatePaths) {
            if (mainCfg.isList(path)) {
                List<?> l = mainCfg.getList(path);
                for (Object o : l) if (o != null) found.add(o.toString());
                // eliminar la lista existente
                plugin.getConfig().set(path, List.of("?????"));
            }
        }

        // Guardar lista encontrada (incluso vacía) dentro de graves.yml bajo la tumba
        ensureLoaded();
        String base = "graves." + graveName + ".";
        if (!found.isEmpty()) cfg.set(base + "stolen_abysses", found);
        else cfg.set(base + "stolen_abysses", Collections.emptyList());

        // Guardar también el UUID del activador para localizar la tumba luego
        cfg.set(base + "activator_uuid", uuid);

        try {
            cfg.save(gravesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Intentar guardar y recargar la config principal
        plugin.saveConfig();
        plugin.reloadConfig();
        events.playerGroupIdIndex.put(off.getUniqueId(), 0);
    }
    public static void onMysteriousAbyssUse(Player player) {
        //ensureLoaded();


        // 2) matar instantáneamente todos los zombies en Dimension D alrededor de "dimension_d.loc"
        Location dimLoc = plugin.getConfig().getLocation("dimension_d.loc");
        if (dimLoc != null && dimLoc.getWorld() != null) {
            double killRadius = 50.0;
            for (org.bukkit.entity.Entity ent : dimLoc.getWorld().getEntities()) {
                if (ent instanceof Zombie) {
                    if (ent.getLocation().distance(player.getLocation()) <= (killRadius)) {
                        ent.remove();
                    }
                }
            }
        }
        // 1) localizar la tumba asociada al jugador (por activator_uuid o nombre)
        String targetGrave = null;
        if (cfg.contains("graves")) {
            for (String g : cfg.getConfigurationSection("graves").getKeys(false)) {
                String path = "graves." + g + ".";
                String actUuid = cfg.getString(path + "activator_uuid", null);
                String actName = cfg.getString(path + "activator", null);
                if ((actUuid != null && actUuid.equals(player.getUniqueId().toString()))
                        || (actName != null && actName.equalsIgnoreCase(player.getName()))) {
                    targetGrave = g;
                    break;
                }
            }
        }
        if (targetGrave == null) {
            player.sendMessage("No se encontró la tumba asociada a tu abyss.");
            return;
        }

        // Reconstruir la localización principal de la tumba
        String base = "graves." + targetGrave + ".";
        String worldName = cfg.getString(base + "world", null);
        if (worldName == null) return;
        World graveWorld = Bukkit.getWorld(worldName);
        if (graveWorld == null) return;
        double gx = cfg.getDouble(base + "x", 0);
        double gy = cfg.getDouble(base + "y", 0);
        double gz = cfg.getDouble(base + "z", 0);
        Location graveLoc = new Location(graveWorld, gx, gy, gz);
        player.sendMessage(gx + "," + gy + "," + gz);

        // Finalmente: eliminar toda la entrada de la tumba del graves.yml
        String finalTargetGrave = targetGrave;
        cfg.set("graves." + finalTargetGrave, null);
        try {
            cfg.save(gravesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        

        player.teleport(graveLoc);

        // 3) Teleportar a todos los jugadores que estuvieran en Dimension D (cerca de dimLoc) de vuelta a la tumba
        final Set<Player> teleported = new HashSet<>();
        if (dimLoc != null && dimLoc.getWorld() != null) {
            double tpRadius = 100.0;
            for (Player p : dimLoc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(dimLoc) <= (tpRadius * tpRadius)) {
                    p.teleport(graveLoc);
                    teleported.add(p);
                }
            }
        }

        // 4) eliminar el Chaos Heart cercano (3 bloques sobre la tumba o en un radio pequeño)
        try {
            ItemStack chaosRef = com.rschao.items.Items.ChaosHeart;
            double removeRadius = 5.0;
            for (org.bukkit.entity.Entity ent : graveWorld.getEntities()) {
                if (ent instanceof Item) {
                    Item it = (Item) ent;
                    if (it.getLocation().distanceSquared(graveLoc.clone().add(0, 3.0, 0)) <= (removeRadius * removeRadius)) {
                        try {
                            ItemStack s = it.getItemStack();
                            if (chaosRef != null && s != null && s.isSimilar(chaosRef)) {
                                it.remove();
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 5) Preparar diálogo y reproducirlo cada 20 ticks a los jugadores teleporteados (y al activador si no está en la lista)
        List<Player> recipients = new ArrayList<>(teleported);
        if (!recipients.contains(player)) recipients.add(player);

        List<String> dialogue = Arrays.asList(
                "§7Yo soy vos...",
                "§7Vos sos yo...",
                "",
                "§7Los vínculos que os unen a vuestro pasado...",
                "§7Habrán de liberar vuestras cadenas.",
                "",
                "§7He aquí, por el poder de los Corazones Puros...",
                "§7La cárcel que os aprisiona se desvanecerá.",
                "",
                "§7Id ahora, y enfrentad vuestro destino..."
        );

        final int[] index = {0};
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (index[0] >= dialogue.size()) return;
            String line = dialogue.get(index[0]++);
            for (Player r : recipients) {
                if (r != null && r.isOnline()) r.sendMessage(line);
            }
        }, 0L, 20L).getTaskId();
        for(Player p : recipients) {
            if(p != null && p.isOnline()) {
                if(!p.equals(player)) {
                    p.teleport(player.getLocation());
                }
            }
        }
        // Cuando el diálogo termine, ejecutar la siguiente secuencia: partículas, spawn item, borrar tumba y cancelar task
        int totalDelay = dialogue.size() * 20; // ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // cancelar el task de diálogo (seguro)
            try { Bukkit.getScheduler().cancelTask(taskId); } catch (Throwable ignored) {}

            // Particle flash 1 bloque arriba de la tumba
            Location flashLoc = graveLoc.clone().add(0, 1.0, 0);
            flashLoc.getWorld().spawnParticle(org.bukkit.Particle.FLASH, flashLoc, 30, 0.3, 0.3, 0.3, 0, Color.WHITE);

            // Spawn floating abyss container "Familiar_love" (indestructible / no-gravity / pickup delay)
            try {
                ItemStack abyss = com.delta.plugins.items.Items.abyssContainer("Familiar_love");
                Item spawned = flashLoc.getWorld().dropItem(flashLoc, abyss);
                spawned.setInvulnerable(true);
                spawned.setGravity(false);
                spawned.setVelocity(spawned.getVelocity().zero());
                spawned.setPersistent(true);
                try { spawned.setUnlimitedLifetime(true); } catch (Throwable ignored) {}
            } catch (Throwable t) {
                t.printStackTrace();
            }
            //set the activator's abyss list to the stolen abysses
            String basePath = "graves." + finalTargetGrave + ".";
            List<String> stolen = cfg.getStringList(base + "stolen_abysses");
            if (!stolen.isEmpty()) {
                org.bukkit.configuration.file.FileConfiguration mainCfg = plugin.getConfig();
                String uuid = cfg.getString(basePath + "activator_uuid", null);
                if (uuid != null) {
                    mainCfg.set(Bukkit.getPlayer(uuid).getName() + ".groupids", stolen);
                    // Intentar guardar y recargar la config principal
                    plugin.saveConfig();
                    plugin.reloadConfig();
                }
            }
        }, totalDelay + 1L);

    }
}
