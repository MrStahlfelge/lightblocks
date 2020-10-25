package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Mission mit Garbage
 * <p>
 * Created by Benjamin Schulte on 23.04.2017.
 */

public class GarbageModel extends MissionModel {
    private float garbageChance;

    @Override
    public String getMissionModelGoalDescription() {
        return "goalGarbage";
    }

    @Override
    public String[] getGoalParams() {
        return new String[]{String.valueOf(getMaxBlocksToUse())};
    }

    @Override
    protected void activeTetrominoDropped() {
        if (getMaxBlocksToUse() <= getScore().getDrawnTetrominos())
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    protected int[] drawGarbageLines(int removedLines) {
        int[] retVal = null;

        if (MathUtils.randomBoolean(garbageChance)) {
            retVal = new int[1];
            retVal[0] = 0;
        }

        return retVal;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.garbageChance = jsonData.getFloat("garbageChance", 0.5f);
        
        setReplayValidOnFirstStart();
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("garbageChance", garbageChance);
    }

}
