package com.delta.plugins.events;

import com.delta.plugins.Plugin;
import com.rschao.plugins.techapi.tech.PlayerTechniqueManager;
import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TechEvents {
    // ... clase utilitaria no-instanciable ...
    private TechEvents() {}

    public static String sanitizePlayerName(String name) {
        if (name != null && name.startsWith(".")) {
            return name.substring(1);
        }
        return name;
    }

    public static String getGroupId(Player p, int index){
        FileConfiguration config = Plugin.getPlugin(Plugin.class).getConfig();
        String playerName = sanitizePlayerName(p.getName());
        List<String> groupIds = config.getStringList(playerName + ".groupids");
        if (groupIds.isEmpty() || index < 0 || index >= groupIds.size()) {
            return "none";
        }
        return groupIds.get(index);
    }

    public static int getGroupIdCount(Player p) {
        FileConfiguration config = Plugin.getPlugin(Plugin.class).getConfig();
        String playerName = sanitizePlayerName(p.getName());
        List<String> groupIds = config.getStringList(playerName + ".groupids");
        return groupIds.size();
    }

    // Devuelve la técnica actualmente seleccionada para el jugador (según playerGroupIdIndex en events)
    public static Optional<Technique> getSelectedTechnique(Player p) {
        int groupIndex = events.playerGroupIdIndex.getOrDefault(p.getUniqueId(), 0);
        String groupId = getGroupId(p, groupIndex);
        if (groupId == null || groupId.equals("none")) return Optional.empty();
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
        List<Technique> techs = TechRegistry.getAllTechniques(groupId);
        if (techs == null || techs.isEmpty() || techIndex < 0 || techIndex >= techs.size()) return Optional.empty();
        return Optional.of(techs.get(techIndex));
    }

    // Llama al uso de la técnica actualmente seleccionada (si existe)
    public static void useCurrentTechnique(Player p, ItemStack item) {
        Optional<Technique> t = getSelectedTechnique(p);
        t.ifPresent(technique -> technique.use(p, item, Technique.nullValue()));
    }

    // Avanzar a la siguiente técnica (respeta corazón del caos)
    public static int nextTechniqueIndex(Player p, String groupId) {
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
        boolean chaos = events.hasChaosHeart(p);
        int size = chaos ? TechRegistry.getAllTechniques(groupId).size() : TechRegistry.getNormalTechniques(groupId).size();
        if (size <= 0) return techIndex;
        int newIndex = (techIndex + 1) % size;
        PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, newIndex);
        return newIndex;
    }

    // Retroceder a la técnica previa (respeta corazón del caos)
    public static int previousTechniqueIndex(Player p, String groupId) {
        int techIndex = PlayerTechniqueManager.getCurrentTechnique(p.getUniqueId(), groupId);
        boolean chaos = events.hasChaosHeart(p);
        int size = chaos ? TechRegistry.getAllTechniques(groupId).size() : TechRegistry.getNormalTechniques(groupId).size();
        if (size <= 0) return techIndex;
        int newIndex;
        if (techIndex == 0) {
            newIndex = size - 1;
        } else {
            newIndex = techIndex - 1;
        }
        PlayerTechniqueManager.setCurrentTechnique(p.getUniqueId(), groupId, newIndex);
        return newIndex;
    }
}

