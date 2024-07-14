package com.github.smuddgge.test.placeholder;

import com.github.cozyplugins.cozylibrary.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestPlaceholder implements Placeholder {

    @Override
    public @NotNull String getIdentifier() {
        return "player_name"; // %player_name%
    }

    @Override
    public @NotNull String getValue(@Nullable Player player, @NotNull String s) {
        return player.getName();
    }
}
