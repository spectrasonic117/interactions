package com.spectrasonic.interactions.managers;

import com.spectrasonic.interactions.Main;
import com.spectrasonic.interactions.listeners.InteractionListener;
import lombok.Getter;

@Getter
public class EventManager {

    private final Main plugin;

    public EventManager(Main plugin) {
        this.plugin = plugin;
        registerEvents();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(new InteractionListener(plugin), plugin);
    }

}