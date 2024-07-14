package com.github.smuddgge.test.database;

import com.github.smuddgge.squishydatabase.interfaces.TableAdapter;
import org.jetbrains.annotations.NotNull;

public class PlayerTable extends TableAdapter<PlayerRecord> {

    @Override
    public @NotNull String getName() {
        return "Player";
    }
}
