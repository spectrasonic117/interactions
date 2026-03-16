package com.spectrasonic.interactions;

import com.spectrasonic.interactions.managers.CommandManager;
import com.spectrasonic.interactions.managers.ConfigManager;
import com.spectrasonic.interactions.managers.EventManager;
import com.spectrasonic.interactions.managers.MessagesManager;
import com.spectrasonic.interactions.services.UseLimitService;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;

import com.spectrasonic.Utils.CommandUtils;
import com.spectrasonic.Utils.MessageUtils;

import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private UseLimitService useLimitService;
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);
        this.useLimitService = new UseLimitService(this);

        // Check for PlaceholderAPI
        this.placeholderAPIEnabled = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        CommandUtils.setPlugin(this);
        MessageUtils.sendStartupMessage(this);

        loadInteractions();

    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

    private void loadInteractions() {
        if (!configManager.getConfig().contains("interactions")) {
            return;
        }

        for (String name : configManager.getConfig().getConfigurationSection("interactions").getKeys(false)) {
            try {
                String worldName = configManager.getConfig().getString("interactions." + name + ".location.world");
                double x = configManager.getConfig().getDouble("interactions." + name + ".location.x");
                double y = configManager.getConfig().getDouble("interactions." + name + ".location.y");
                double z = configManager.getConfig().getDouble("interactions." + name + ".location.z");
                float width = (float) configManager.getConfig().getDouble("interactions." + name + ".width");
                float height = (float) configManager.getConfig().getDouble("interactions." + name + ".height");
                boolean responsive = configManager.getConfig().getBoolean("interactions." + name + ".responsive");
                String uuidStr = configManager.getConfig().getString("interactions." + name + ".uuid");

                org.bukkit.World world = getServer().getWorld(worldName);
                if (world == null) {
                    getLogger().warning("World '" + worldName + "' not found for interaction '" + name + "'");
                    continue;
                }

                Interaction interaction = null;

                // Check if entity already exists by UUID
                if (uuidStr != null) {
                    try {
                        java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                        interaction = (Interaction) world.getEntities().stream()
                                .filter(entity -> entity.getUniqueId().equals(uuid) && entity instanceof Interaction)
                                .findFirst()
                                .orElse(null);
                    } catch (IllegalArgumentException e) {
                        // Invalid UUID, will spawn new
                    }
                }

                if (interaction == null) {
                    // Spawn new interaction
                    Location location = new Location(world, x, y, z);
                    interaction = (Interaction) world.spawnEntity(location, EntityType.INTERACTION);
                    // Update UUID in config
                    configManager.getConfig().set("interactions." + name + ".uuid",
                            interaction.getUniqueId().toString());
                    configManager.saveConfig();
                }

                // Update properties
                interaction.setInteractionWidth(width);
                interaction.setInteractionHeight(height);
                interaction.setResponsive(responsive);

            } catch (Exception e) {
                getLogger().severe("Failed to load interaction '" + name + "': " + e.getMessage());
            }
        }
    }

}