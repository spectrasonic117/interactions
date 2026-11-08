package com.spectrasonic.interactions.managers;

import com.spectrasonic.interactions.Main;
import com.spectrasonic.interactions.commands.InteractionCommand;
import lombok.Getter;

@Getter
public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        new InteractionCommand(plugin).getCommand().register();
    }
}