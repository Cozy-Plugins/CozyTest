package com.github.smuddgge.test;

import org.bukkit.plugin.java.JavaPlugin;

public final class TestLoader extends JavaPlugin {

    private TestPlugin plugin;

    @Override
    public void onEnable() {
        this.plugin = new TestPlugin(this);
        this.plugin.enable();

        // Set up events.
        this.getServer().getPluginManager().registerEvents(this.plugin, this);
    }

    @Override
    public void onDisable() {
        this.plugin.disable();
    }
}
