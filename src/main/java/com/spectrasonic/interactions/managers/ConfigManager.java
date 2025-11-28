package com.spectrasonic.interactions.managers;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    /**
     * Obtiene un mensaje de la configuración usando una clave
     * @param key La clave del mensaje en la configuración
     * @return El mensaje como String
     */

    public String getMessage(String key) {
        return config.getString(key);
    }

    // /**
    //  * Obtiene un valor de la configuración usando una clave con un valor por defecto
    //  * @param key La clave del valor en la configuración
    //  * @param def El valor por defecto si la clave no existe
    //  * @return El valor como String
    //  */
    // public String getString(String key, String def) {
    //     return config.getString(key, def);
    // }
    
}