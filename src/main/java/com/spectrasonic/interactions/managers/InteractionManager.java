package com.spectrasonic.interactions.managers;

import com.spectrasonic.interactions.Main;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;

import java.util.UUID;

@Getter
public class InteractionManager {

    private final Main plugin;
    private final ConfigManager configManager;

    // Radio de búsqueda para encontrar interacciones por ubicación
    private static final double SEARCH_RADIUS = 0.5;

    public InteractionManager(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void loadInteractions() {
        if (!configManager.getConfig().contains("interactions")) {
            return;
        }

        for (String name : configManager.getConfig().getConfigurationSection("interactions").getKeys(false)) {
            try {
                loadInteraction(name);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load interaction '" + name + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadInteraction(String name) {
        // Extraer datos de configuración
        InteractionData data = extractInteractionData(name);
        if (data == null) {
            return;
        }

        // Buscar o crear la interacción
        Interaction interaction = findOrCreateInteraction(name, data);
        if (interaction == null) {
            return;
        }

        // Actualizar propiedades de la interacción
        updateInteractionProperties(interaction, data);

        // Registrar la interacción cargada
        logInteractionLoad(name, interaction);
    }

    private InteractionData extractInteractionData(String name) {
        try {
            String worldName = configManager.getConfig().getString("interactions." + name + ".location.world");
            double x = configManager.getConfig().getDouble("interactions." + name + ".location.x");
            double y = configManager.getConfig().getDouble("interactions." + name + ".location.y");
            double z = configManager.getConfig().getDouble("interactions." + name + ".location.z");
            float width = (float) configManager.getConfig().getDouble("interactions." + name + ".width");
            float height = (float) configManager.getConfig().getDouble("interactions." + name + ".height");
            boolean responsive = configManager.getConfig().getBoolean("interactions." + name + ".responsive");
            String uuidStr = configManager.getConfig().getString("interactions." + name + ".uuid");

            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for interaction '" + name + "'");
                return null;
            }

            Location location = new Location(world, x, y, z);

            return new InteractionData(name, location, width, height, responsive, uuidStr);

        } catch (Exception e) {
            plugin.getLogger().severe("Error extracting data for interaction '" + name + "': " + e.getMessage());
            return null;
        }
    }

    private Interaction findOrCreateInteraction(String name, InteractionData data) {
        // Primero intentar encontrar por UUID (método más eficiente)
        Interaction interaction = findInteractionByUUID(data.getUuid(), data.getLocation().getWorld());

        // Si no se encontró por UUID, buscar por ubicación
        if (interaction == null) {
            interaction = findInteractionAtLocation(data.getLocation().getWorld(), data.getLocation());
        }

        // Si aún no se encontró, crear nueva entidad
        if (interaction == null) {
            interaction = createNewInteraction(name, data);
        }

        return interaction;
    }

    private Interaction findInteractionByUUID(String uuidStr, World world) {
        if (uuidStr == null) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = plugin.getServer().getEntity(uuid);
            return (entity instanceof Interaction && entity.getWorld().equals(world)) ? (Interaction) entity : null;
        } catch (IllegalArgumentException e) {
            // UUID inválido, continuar con búsqueda por ubicación
            return null;
        }
    }

    private Interaction findInteractionAtLocation(World world, Location location) {
        return world.getEntitiesByClass(Interaction.class).stream()
                .filter(entity -> isLocationMatch(entity.getLocation(), location))
                .findFirst()
                .orElse(null);
    }

    private boolean isLocationMatch(Location entityLoc, Location targetLoc) {
        return entityLoc.getWorld().equals(targetLoc.getWorld()) &&
                Math.abs(entityLoc.getX() - targetLoc.getX()) <= SEARCH_RADIUS &&
                Math.abs(entityLoc.getY() - targetLoc.getY()) <= SEARCH_RADIUS &&
                Math.abs(entityLoc.getZ() - targetLoc.getZ()) <= SEARCH_RADIUS;
    }

    private Interaction createNewInteraction(String name, InteractionData data) {
        Interaction interaction = (Interaction) data.getLocation().getWorld()
                .spawnEntity(data.getLocation(), EntityType.INTERACTION);

        // Actualizar UUID en configuración
        configManager.getConfig().set("interactions." + name + ".uuid", interaction.getUniqueId().toString());
        configManager.saveConfig();

        plugin.getLogger().info("Created new interaction '" + name + "' at " + data.getLocation());
        return interaction;
    }

    private void updateInteractionProperties(Interaction interaction, InteractionData data) {
        interaction.setInteractionWidth(data.getWidth());
        interaction.setInteractionHeight(data.getHeight());
        interaction.setResponsive(data.isResponsive());
    }

    private void logInteractionLoad(String name, Interaction interaction) {
        plugin.getLogger().info("Loaded existing interaction '" + name + "' with UUID " + interaction.getUniqueId());
    }

    /**
     * Record para almacenar datos de interacción
     * Utiliza el patrón Builder para una construcción limpia
     */
    public static class InteractionData {
        private final String name;
        private final Location location;
        private final float width;
        private final float height;
        private final boolean responsive;
        private final String uuid;

        public InteractionData(String name, Location location, float width, float height, boolean responsive,
                String uuid) {
            this.name = name;
            this.location = location;
            this.width = width;
            this.height = height;
            this.responsive = responsive;
            this.uuid = uuid;
        }

        // Getters
        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public boolean isResponsive() {
            return responsive;
        }

        public String getUuid() {
            return uuid;
        }
    }
}