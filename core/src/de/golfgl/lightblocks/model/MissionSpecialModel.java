package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Spezialmission
 * <p>
 * Created by Benjamin Schulte on 22.04.2017.
 */

public class MissionSpecialModel extends MissionModel {

    private int numFourLines;
    private int numTSpins;
    private int numDoubleSpecials;

    @Override
    public String getGoalDescription() {
        return "goalSpecialMove";
    }

    @Override
    public String[] getGoalParams() {
        return new String[]{String.valueOf(numFourLines), String.valueOf(numTSpins),
                String.valueOf(getMaxBlocksToUse()), String.valueOf(numDoubleSpecials)};
    }

    @Override
    protected void achievementFourLines() {
        numFourLines--;
        super.achievementFourLines();
    }

    @Override
    protected void achievementTSpin() {
        numTSpins--;
        super.achievementTSpin();
    }

    @Override
    protected void achievementDoubleSpecial(boolean doubleSpecial) {
        if (doubleSpecial)
            numDoubleSpecials--;

        super.achievementDoubleSpecial(doubleSpecial);
    }

    @Override
    protected void activeTetrominoDropped() {
        if (numFourLines <= 0 && numTSpins <= 0 && numDoubleSpecials <= 0)
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    public int getRating() {
        addBonusScore((getMaxBlocksToUse() - getScore().getDrawnTetrominos()) * 100);

        return super.getRating();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.numFourLines = jsonData.getInt("numFourLines", 0);
        this.numTSpins = jsonData.getInt("numTSpins", 0);
        this.numDoubleSpecials = jsonData.getInt("numDoubleSpecials", 0);
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("numTSpins", numTSpins);
        json.writeValue("numFourLines", numFourLines);
        json.writeValue("numDoubleSpecials", numDoubleSpecials);
    }
}
