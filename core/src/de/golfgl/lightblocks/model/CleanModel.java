package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Type B: Vorgegebene Garbage wegcleanen
 * <p>
 * Created by Benjamin Schulte on 22.04.2017.
 */

public class CleanModel extends MissionModel {
    @Override
    public int getRating() {
        addBonusScore((getMaxBlocksToUse() - getScore().getDrawnTetrominos()) * 100);
        return super.getRating();
    }

    @Override
    public String getMissionModelGoalDescription() {
        return "goalClean";
    }

    @Override
    public String[] getGoalParams() {
        return new String[]{String.valueOf(getMaxBlocksToUse())};
    }

    @Override
    protected void activeTetrominoDropped() {
        if (getScore().getClearedLines() > 0 && !getGameboard().hasGarbage())
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        // Der Wert darf nicht wieder gespeichert werden, um die Garbage nicht bei jedem Laden
        // wieder einzufügen
        int insertGarbage = jsonData.getInt("initialGarbage", 0);
        getGameboard().initGarbage(insertGarbage);
    }
}
