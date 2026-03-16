package com.spectrasonic.interactions.commands;

import com.spectrasonic.Utils.MessageUtils;
import com.spectrasonic.interactions.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
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
                .withPermission(CommandPermission.OP)
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"))
                        .executesPlayer((player, args) -> {
                            String name = (String) args.get(0);
                            createInteraction(player, name);
                        }))
                .withSubcommand(new CommandAPICommand("delete")
                        .withArguments(new StringArgument("name").replaceSuggestions(
                                ArgumentSuggestions.stringCollection(
                                        info -> getInteractionNames())))
                        .executesPlayer((player, args) -> {
                            String name = (String) args.get(0);
                            deleteInteraction(player, name);
                        }))
                .withSubcommand(new CommandAPICommand("resize")
                        .withSubcommand(new CommandAPICommand("horizontal")
                                .withArguments(new IntegerArgument("size", 1),
                                        new StringArgument("name")
                                                .replaceSuggestions(
                                                        ArgumentSuggestions
                                                                .stringCollection(
                                                                        info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    int size = (Integer) args.get(0);
                                    String name = (String) args.get(1);
                                    resizeHorizontal(player, size, name);
                                }))
                        .withSubcommand(new CommandAPICommand("vertical")
                                .withArguments(new IntegerArgument("size", 1),
                                        new StringArgument("name")
                                                .replaceSuggestions(
                                                        ArgumentSuggestions
                                                                .stringCollection(
                                                                        info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    int size = (Integer) args.get(0);
                                    String name = (String) args.get(1);
                                    resizeVertical(player, size, name);
                                })))
                .withSubcommand(new CommandAPICommand("limit")
                        .withSubcommand(new CommandAPICommand("set")
                                .withArguments(new IntegerArgument("limit"),
                                        new StringArgument("name")
                                                .replaceSuggestions(
                                                        ArgumentSuggestions
                                                                .stringCollection(
                                                                        info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    int limit = (Integer) args.get(0);
                                    String name = (String) args.get(1);
                                    setLimit(player, limit, name);
                                }))
                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new StringArgument("name")
                                        .replaceSuggestions(
                                                ArgumentSuggestions
                                                        .stringCollection(
                                                                info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    String name = (String) args.get(0);
                                    removeLimit(player, name);
                                }))
                        .withSubcommand(new CommandAPICommand("check")
                                .withArguments(new StringArgument("name")
                                        .replaceSuggestions(
                                                ArgumentSuggestions
                                                        .stringCollection(
                                                                info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    String name = (String) args.get(0);
                                    checkLimit(player, name);
                                })))
                .withSubcommand(new CommandAPICommand("reset")
                        .withSubcommand(new CommandAPICommand("player")
                                .withArguments(
                                        new EntitySelectorArgument.OnePlayer(
                                                "player"),
                                        new StringArgument("name")
                                                .replaceSuggestions(
                                                        ArgumentSuggestions
                                                                .stringCollection(
                                                                        info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    Player target = (Player) args.get(0);
                                    String name = (String) args.get(1);
                                    resetPlayerUsage(player, target, name);
                                }))
                        .withSubcommand(new CommandAPICommand("all")
                                .withArguments(new StringArgument("name")
                                        .replaceSuggestions(
                                                ArgumentSuggestions
                                                        .stringCollection(
                                                                info -> getInteractionNames())))
                                .executesPlayer((player, args) -> {
                                    String name = (String) args.get(0);
                                    resetAllUsages(player, name);
                                })))
                .withSubcommand(new CommandAPICommand("reload")
                        .executes((sender, args) -> {
                            reloadConfig(sender);
                        }));
    }

    private void createInteraction(Player player, String name) {
        // Check if interaction already exists
        if (plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_already_exists",
                            name));
            return;
        }

        Location location = player.getLocation();
        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location,
                EntityType.INTERACTION);

        // Set default values
        double defaultWidth = plugin.getConfigManager().getConfig().getDouble("settings.default_width", 1.0);
        double defaultHeight = plugin.getConfigManager().getConfig().getDouble("settings.default_height", 1.0);
        boolean defaultResponsive = plugin.getConfigManager().getConfig().getBoolean(
                "settings.default_responsive",
                true);
        boolean defaultCommandAsConsole = plugin.getConfigManager().getConfig()
                .getBoolean("settings.default_command_as_console", false);

        interaction.setInteractionWidth((float) defaultWidth);
        interaction.setInteractionHeight((float) defaultHeight);
        interaction.setResponsive(defaultResponsive);

        // Get default limit use
        int defaultLimitUse = plugin.getConfigManager().getConfig().getInt("settings.default_limit_use", -1);

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
        plugin.getConfigManager().getConfig().set("interactions." + name + ".command_as_console",
                defaultCommandAsConsole);
        plugin.getConfigManager().getConfig().set("interactions." + name + ".command", ""); // Empty command by
                                                                                            // default
        plugin.getConfigManager().getConfig().set("interactions." + name + ".limit_use", defaultLimitUse); // Default
                                                                                                           // limit
                                                                                                           // use

        plugin.getConfigManager().saveConfig();

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.interaction_created", name));
    }

    private void deleteInteraction(Player player, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
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

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.interaction_deleted", name));
    }

    private void resizeHorizontal(Player player, int size, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        String uuidStr = plugin.getConfigManager().getConfig().getString("interactions." + name + ".uuid");
        if (uuidStr != null) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getUniqueId().equals(uuid)
                                && entity instanceof Interaction)
                        .findFirst()
                        .ifPresent(entity -> {
                            Interaction interaction = (Interaction) entity;
                            interaction.setInteractionWidth(size);
                        });
            } catch (IllegalArgumentException e) {
                MessageUtils.noPrefixMessage(player,
                        plugin.getMessagesManager().getMessage("message.interaction_not_found",
                                name));
                return;
            }
        }

        // Update config
        plugin.getConfigManager().getConfig().set("interactions." + name + ".width", size);
        plugin.getConfigManager().saveConfig();

        MessageUtils.noPrefixMessage(player, plugin.getMessagesManager()
                .getMessage("message.interaction_resized_horizontal", name, Integer.toString(size)));
    }

    private void resizeVertical(Player player, int size, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        String uuidStr = plugin.getConfigManager().getConfig().getString("interactions." + name + ".uuid");
        if (uuidStr != null) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getUniqueId().equals(uuid)
                                && entity instanceof Interaction)
                        .findFirst()
                        .ifPresent(entity -> {
                            Interaction interaction = (Interaction) entity;
                            interaction.setInteractionHeight(size);
                        });
            } catch (IllegalArgumentException e) {
                MessageUtils.noPrefixMessage(player,
                        plugin.getMessagesManager().getMessage("message.interaction_not_found",
                                name));
                return;
            }
        }

        // Update config
        plugin.getConfigManager().getConfig().set("interactions." + name + ".height", size);
        plugin.getConfigManager().saveConfig();

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.interaction_resized_vertical",
                        name, Integer.toString(size)));
    }

    private void reloadConfig(Object sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getMessagesManager().loadMessages();
        MessageUtils.sucessMessage((CommandSender) sender,
                plugin.getMessagesManager().getMessage("message.config_reloaded"));
    }

    // Métodos para gestionar límites de uso

    private void setLimit(Player player, int limit, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        plugin.getConfigManager().getConfig().set("interactions." + name + ".limit_use", limit);
        plugin.getConfigManager().saveConfig();

        String limitStr = limit == -1 ? "sin límite" : String.valueOf(limit);
        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.limit_set", name, limitStr));
    }

    private void removeLimit(Player player, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        plugin.getConfigManager().getConfig().set("interactions." + name + ".limit_use", -1);
        plugin.getConfigManager().saveConfig();

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.limit_removed", name));
    }

    private void checkLimit(Player player, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        int limit = plugin.getUseLimitService().getLimitForInteraction(name);
        int currentUses = plugin.getUseLimitService().getCurrentUses(name, player.getUniqueId());

        String limitStr;
        if (limit == -1) {
            limitStr = "∞";
        } else {
            limitStr = String.valueOf(limit);
        }

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.current_uses", name,
                        String.valueOf(currentUses), limitStr));
    }

    private void resetPlayerUsage(Player player, Player target, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        plugin.getUseLimitService().resetUsage(name, target.getUniqueId());

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.uses_reset", target.getName(), name));
    }

    private void resetAllUsages(Player player, String name) {
        if (!plugin.getConfigManager().getConfig().contains("interactions." + name)) {
            MessageUtils.noPrefixMessage(player,
                    plugin.getMessagesManager().getMessage("message.interaction_not_found", name));
            return;
        }

        plugin.getUseLimitService().resetAllUsagesForInteraction(name);

        MessageUtils.noPrefixMessage(player,
                plugin.getMessagesManager().getMessage("message.uses_reset_all", name));
    }

    private java.util.Collection<String> getInteractionNames() {
        if (!plugin.getConfigManager().getConfig().contains("interactions")) {
            return java.util.Collections.emptyList();
        }
        return plugin.getConfigManager().getConfig().getConfigurationSection("interactions").getKeys(false);
    }
}