package com.spectrasonic.interactions.services;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestionar el límite de usos de cada interacción por jugador.
 * Usa un mapa en memoria para rendimiento y persiste en config.yml.
 */
@Getter
public class UseLimitService {

    private final JavaPlugin plugin;
    private final Map<String, Map<UUID, Integer>> usageCache;

    public UseLimitService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.usageCache = new HashMap<>();
        loadUsageData();
    }

    /**
     * Obtiene el límite de uso configurado para una interacción
     */
    public int getLimitForInteraction(String interactionName) {
        FileConfiguration config = plugin.getConfig();
        return config.getInt("interactions." + interactionName + ".limit_use", -1);
    }

    /**
     * Obtiene los usos restantes para un jugador en una interacción específica
     */
    public int getRemainingUses(String interactionName, UUID playerUUID) {
        FileConfiguration config = plugin.getConfig();
        String path = "usages." + interactionName + "." + playerUUID.toString();
        
        int limit = getLimitForInteraction(interactionName);
        if (limit == -1) {
            return -1; // Sin límite
        }
        
        int used = config.getInt(path, 0);
        return Math.max(0, limit - used);
    }

    /**
     * Verifica si un jugador puede usar una interacción
     * Retorna true si puede usar, false si alcanzó el límite
     */
    public boolean canUse(String interactionName, UUID playerUUID) {
        int limit = getLimitForInteraction(interactionName);
        
        if (limit == -1) {
            return true; // Sin límite
        }
        
        if (limit == 0) {
            return false; // Límite es 0, no puede usar nunca
        }
        
        return getRemainingUses(interactionName, playerUUID) > 0;
    }

    /**
     * Registra un uso de la interacción por parte del jugador
     * Retorna true si el uso fue registrado, false si alcanzó el límite
     */
    public boolean registerUse(String interactionName, UUID playerUUID) {
        if (!canUse(interactionName, playerUUID)) {
            return false;
        }
        
        FileConfiguration config = plugin.getConfig();
        String path = "usages." + interactionName + "." + playerUUID.toString();
        
        int currentUses = config.getInt(path, 0);
        config.set(path, currentUses + 1);
        plugin.saveConfig();
        
        return true;
    }

    /**
     * Reinicia los usos de una interacción específica para un jugador
     */
    public void resetUsage(String interactionName, UUID playerUUID) {
        FileConfiguration config = plugin.getConfig();
        String path = "usages." + interactionName + "." + playerUUID.toString();
        
        if (config.contains(path)) {
            config.set(path, null);
            plugin.saveConfig();
        }
    }

    /**
     * Reinicia todos los usos de una interacción específica (todos los jugadores)
     */
    public void resetAllUsagesForInteraction(String interactionName) {
        FileConfiguration config = plugin.getConfig();
        
        if (config.contains("usages." + interactionName)) {
            config.set("usages." + interactionName, null);
            plugin.saveConfig();
        }
    }

    /**
     * Reinicia todos los usos de todas las interacciones
     */
    public void resetAllUsages() {
        FileConfiguration config = plugin.getConfig();
        
        if (config.contains("usages")) {
            config.set("usages", null);
            plugin.saveConfig();
        }
    }

    /**
     * Carga los datos de uso desde la configuración (solo caché en memoria si es necesario)
     */
    private void loadUsageData() {
        // Los datos se leen directamente de la config para mantener consistencia
        // Este método puede расширяse para caché en memoria si el rendimiento lo requiere
    }

    /**
     * Obtiene el número de usos actuales de un jugador en una interacción
     */
    public int getCurrentUses(String interactionName, UUID playerUUID) {
        FileConfiguration config = plugin.getConfig();
        String path = "usages." + interactionName + "." + playerUUID.toString();
        return config.getInt(path, 0);
    }
}
