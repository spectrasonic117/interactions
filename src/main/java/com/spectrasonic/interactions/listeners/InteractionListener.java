package com.spectrasonic.interactions.listeners;

import com.spectrasonic.Utils.CommandUtils;
import com.spectrasonic.interactions.Main;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

@RequiredArgsConstructor
public class InteractionListener implements Listener {

    private final Main plugin;

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction)) {
            return;
        }

        Interaction interaction = (Interaction) event.getRightClicked();
        Player player = event.getPlayer();

        // Find the interaction name by UUID
        String interactionName = findInteractionNameByUUID(interaction.getUniqueId().toString());
        if (interactionName == null) {
            return; // Not a managed interaction
        }

        // Get the command from config
        String command = plugin.getConfigManager().getConfig().getString("interactions." + interactionName + ".command");
        if (command == null || command.trim().isEmpty()) {
            return; // No command set
        }

        // Parse placeholders if PlaceholderAPI is enabled
        if (plugin.isPlaceholderAPIEnabled()) {
            command = PlaceholderAPI.setPlaceholders(player, command);
        }

        // Check if command should be executed as console
        boolean commandAsConsole = plugin.getConfigManager().getConfig().getBoolean("interactions." + interactionName + ".command_as_console");

        // Execute the command as console or player
        if (commandAsConsole) {
            CommandUtils.ConsoleCommand(command);
        } else {
            CommandUtils.PlayerCommand(player, command);
        }

        // Cancel the event to prevent default interaction
        event.setCancelled(true);
    }

    private String findInteractionNameByUUID(String uuid) {
        for (String key : plugin.getConfigManager().getConfig().getConfigurationSection("interactions").getKeys(false)) {
            String configUUID = plugin.getConfigManager().getConfig().getString("interactions." + key + ".uuid");
            if (uuid.equals(configUUID)) {
                return key;
            }
        }
        return null;
    }
}