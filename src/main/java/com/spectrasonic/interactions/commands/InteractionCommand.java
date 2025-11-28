package com.spectrasonic.interactions.commands;

import com.spectrasonic.Utils.MessageUtils;
import com.spectrasonic.interactions.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class InteractionCommand {

    private final Main plugin;

    public CommandAPICommand getCommand() {
        return new CommandAPICommand("interaction")
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            String name = (String) args.get(0);
                            createInteraction(player, name);
                        }))
                .withSubcommand(new CommandAPICommand("delete")
                        .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> getInteractionNames())))
                        .executesPlayer((player, args) -> {
                            String name = (String) args.get(0);
                            deleteInteraction(player, name);
                        }))
                .withSubcommand(new CommandAPICommand("resize")
                        .withSubcommand(new CommandAPICommand("horizontal")
                                .withArguments(new IntegerArgument("size", 1), new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    int size = (Integer) args.get(0);
                                    String name = (String) args.get(1);
                                    resizeHorizontal(player, size, name);
                                }))
                        .withSubcommand(new CommandAPICommand("vertical")
                                .withArguments(new IntegerArgument("size", 1), new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    int size = (Integer) args.get(0);
                                    String name = (String) args.get(1);
                                    resizeVertical(player, size, name);
                                })))
                .withSubcommand(new CommandAPICommand("reload")
                        .executes((sender, args) -> {
                            reloadConfig(sender);
                        }));
    }

    private void createInteraction(Player player, String name) {
        // Check if interaction already exists
        if (plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.sendMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_already_exists", name));
            return;
        }

        Location location = player.getLocation();
        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);

        // Set default values
        double defaultWidth = plugin.getConfigManager().getConfig().getDouble("settings.default_width", 1.0);
        double defaultHeight = plugin.getConfigManager().getConfig().getDouble("settings.default_height", 1.0);
        boolean defaultResponsive = plugin.getConfigManager().getConfig().getBoolean("settings.default_responsive",
                true);

        interaction.setInteractionWidth((float) defaultWidth);
        interaction.setInteractionHeight((float) defaultHeight);
        interaction.setResponsive(defaultResponsive);

        // Save to config
        plugin.getConfigManager().getConfig().set("interactions." + name + ".uuid",
                interaction.getUniqueId().toString());
        plugin.getConfigManager().getConfig().set("interactions." + name + ".location.world",
                location.getWorld().getName());
        plugin.getConfigManager().getConfig().set("interactions." + name + ".location.x", location.getX());
        plugin.getConfigManager().getConfig().set("interactions." + name + ".location.y", location.getY());
        plugin.getConfigManager().getConfig().set("interactions." + name + ".location.z", location.getZ());
        plugin.getConfigManager().getConfig().set("interactions." + name + ".width", defaultWidth);
        plugin.getConfigManager().getConfig().set("interactions." + name + ".height", defaultHeight);
        plugin.getConfigManager().getConfig().set("interactions." + name + ".responsive", defaultResponsive);
        plugin.getConfigManager().getConfig().set("interactions." + name + ".command", ""); // Empty command by default

        plugin.getConfigManager().saveConfig();

        MessageUtils.sendMessage(player, plugin.getMessagesManager().getMessage("message.interaction_created", name));
    }

    private void deleteInteraction(Player player, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.sendMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        String uuidStr = plugin.getConfigManager().getConfig().getString("interactions." + name + ".uuid");
        if (uuidStr != null) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getUniqueId().equals(uuid))
                        .findFirst()
                        .ifPresent(entity -> entity.remove());
            } catch (IllegalArgumentException e) {
                // Invalid UUID, continue with config removal
            }
        }

        // Remove from config
        plugin.getConfigManager().getConfig().set("interactions." + name, null);
        plugin.getConfigManager().saveConfig();

        MessageUtils.sendMessage(player, plugin.getMessagesManager().getMessage("message.interaction_deleted", name));
    }

    private void resizeHorizontal(Player player, int size, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.sendMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        String uuidStr = plugin.getConfigManager().getConfig().getString("interactions." + name + ".uuid");
        if (uuidStr != null) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getUniqueId().equals(uuid) && entity instanceof Interaction)
                        .findFirst()
                        .ifPresent(entity -> {
                            Interaction interaction = (Interaction) entity;
                            interaction.setInteractionWidth(size);
                        });
            } catch (IllegalArgumentException e) {
                MessageUtils.sendMessage(player,
                        plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
                return;
            }
        }

        // Update config
        plugin.getConfigManager().getConfig().set("interactions." + name + ".width", size);
        plugin.getConfigManager().saveConfig();

        MessageUtils.sendMessage(player, plugin.getMessagesManager().getMessage("message.interaction_resized_horizontal", name, Integer.toString(size)));
    }

    private void resizeVertical(Player player, int size, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.sendMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        String uuidStr = plugin.getConfigManager().getConfig().getString("interactions." + name + ".uuid");
        if (uuidStr != null) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getUniqueId().equals(uuid) && entity instanceof Interaction)
                        .findFirst()
                        .ifPresent(entity -> {
                            Interaction interaction = (Interaction) entity;
                            interaction.setInteractionHeight(size);
                        });
            } catch (IllegalArgumentException e) {
                MessageUtils.sendMessage(player,
                        plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
                return;
            }
        }

        // Update config
        plugin.getConfigManager().getConfig().set("interactions." + name + ".height", size);
        plugin.getConfigManager().saveConfig();

        MessageUtils.sendMessage(player, plugin.getMessagesManager().getMessage("message.interaction_resized_vertical", name, Integer.toString(size)));
    }

    private void reloadConfig(Object sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getMessagesManager().loadMessages();
        MessageUtils.sucessMessage((CommandSender) sender,
                plugin.getMessagesManager().getMessage("message.config_reloaded"));
    }

    private java.util.Collection<String> getInteractionNames() {
        if (!plugin.getConfigManager().getConfig().contains("interactions")) {
            return java.util.Collections.emptyList();
        }
        return plugin.getConfigManager().getConfig().getConfigurationSection("interactions").getKeys(false);
    }
}