package com.spectrasonic.interactions;

import com.spectrasonic.interactions.managers.CommandManager;
import com.spectrasonic.interactions.managers.ConfigManager;
import com.spectrasonic.interactions.managers.EventManager;
import com.spectrasonic.interactions.managers.InteractionManager;
import com.spectrasonic.interactions.managers.MessagesManager;
import com.spectrasonic.interactions.services.UseLimitService;

import lombok.Getter;

import com.spectrasonic.Utils.CommandUtils;
import com.spectrasonic.Utils.MessageUtils;

import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private CommandManager commandManager;
    private EventManager eventManager;
    private InteractionManager interactionManager;
    private UseLimitService useLimitService;
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        this.commandManager = new CommandManager(this);
        this.eventManager = new EventManager(this);
        this.interactionManager = new InteractionManager(this);
        this.useLimitService = new UseLimitService(this);

        // Check for PlaceholderAPI
        this.placeholderAPIEnabled = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        CommandUtils.setPlugin(this);
        MessageUtils.sendStartupMessage(this);

        interactionManager.loadInteractions();

    }

    @Override
    public void onDisable() {
        MessageUtils.sendShutdownMessage(this);
    }

}