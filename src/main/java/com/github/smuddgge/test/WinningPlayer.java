package com.github.smuddgge.test;

import com.github.smuddgge.squishyconfiguration.indicator.ConfigurationConvertable;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.github.smuddgge.squishyconfiguration.memory.MemoryConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.UUID;

public class WinningPlayer implements ConfigurationConvertable<WinningPlayer> {

    private UUID uuid;
    private String name;
    private String color;
    private int winNumber;

    public WinningPlayer(final UUID uuid, final String name, final String color, final int winNumber) {
        this.uuid = uuid;
        this.name = name;
        this.color = color;
        this.winNumber = winNumber;
    }

    @Override
    public @NotNull ConfigurationSection convert() {
        ConfigurationSection section = new MemoryConfigurationSection(new LinkedHashMap<>());

        section.set("uuid", uuid.toString());
        section.set("name", name);
        section.set("color", color);
        section.set("win_number", winNumber);

        return section;
    }

    @Override
    public @NotNull WinningPlayer convert(@NotNull ConfigurationSection section) {

        this.uuid = UUID.fromString(section.getString("uuid"));
        this.name = section.getString("name", "null");
        this.color = section.getString("color", "null");
        this.winNumber = section.getInteger("win_number", 0);

        return this;
    }

    public void incrementWins() {
        this.winNumber++;
    }
}
