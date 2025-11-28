package com.spectrasonic.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CommandUtils {

    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void ConsoleCommand(String command) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin instance not set. Call setPlugin() first.");
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        /* Use
         * ExecuteCommandUtils.ConsoleCommand("say Hola consola");
         */
    }

    public static void PlayerCommand(Player player, String command) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        player.performCommand(command);
        
        /* Use
         * ExecuteCommandUtils.PlayerCommand(player, "me dice hola");
         */
    }
}
