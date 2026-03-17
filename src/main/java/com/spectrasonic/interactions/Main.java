package com.spectrasonic.interactions;

import com.spectrasonic.interactions.managers.CommandManager;
import com.spectrasonic.interactions.managers.ConfigManager;
import com.spectrasonic.interactions.managers.EventManager;
import com.spectrasonic.interactions.managers.MessagesManager;
import com.spectrasonic.interactions.services.UseLimitService;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import java.util.UUID;
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

                Location location = new Location(world, x, y, z);
                Interaction interaction = null;

                // Primero buscar por UUID almacenado (método más eficiente)
                if (uuidStr != null) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Entity entity = getServer().getEntity(uuid);
                        if (entity instanceof Interaction && entity.getWorld().equals(world)) {
                            interaction = (Interaction) entity;
                        }
                    } catch (IllegalArgumentException e) {
                        // UUID inválido, continuar con búsqueda por ubicación
                    }
                }

                // Si no se encontró por UUID, buscar por ubicación cercana
                if (interaction == null) {
                    interaction = findInteractionAtLocation(world, location);
                }

                // Si aún no se encontró, crear nueva entidad
                if (interaction == null) {
                    interaction = (Interaction) world.spawnEntity(location, EntityType.INTERACTION);
                    // Actualizar UUID en config
                    configManager.getConfig().set("interactions." + name + ".uuid",
                            interaction.getUniqueId().toString());
                    configManager.saveConfig();
                    getLogger().info("Created new interaction '" + name + "' at " + location);
                } else {
                    getLogger()
                            .info("Loaded existing interaction '" + name + "' with UUID " + interaction.getUniqueId());
                }

                // Actualizar propiedades
                interaction.setInteractionWidth(width);
                interaction.setInteractionHeight(height);
                interaction.setResponsive(responsive);

            } catch (Exception e) {
                getLogger().severe("Failed to load interaction '" + name + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Busca una entidad Interaction en una ubicación específica con un radio
    // pequeño
    private Interaction findInteractionAtLocation(World world, Location location) {
        final double SEARCH_RADIUS = 0.5; // Radio de búsqueda en bloques

        return world.getEntitiesByClass(Interaction.class).stream()
                .filter(entity -> {
                    Location entityLoc = entity.getLocation();
                    return entityLoc.getWorld().equals(location.getWorld()) &&
                            Math.abs(entityLoc.getX() - location.getX()) <= SEARCH_RADIUS &&
                            Math.abs(entityLoc.getY() - location.getY()) <= SEARCH_RADIUS &&
                            Math.abs(entityLoc.getZ() - location.getZ()) <= SEARCH_RADIUS;
                })
                .findFirst()
                .orElse(null);
    }

}