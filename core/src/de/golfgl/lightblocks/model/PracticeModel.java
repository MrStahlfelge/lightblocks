package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Practice: Wird nicht schneller und der Bestscore wird ausschließlich über Line Clears definiert
 * <p>
 * Created by Benjamin Schulte on 24.05.2018.
 */

public class PracticeModel extends GameModel {
    public static final String MODEL_PRACTICE_ID = "practice";
    public static final int TYPE_CLASSIC = 0;
    public static final int TYPE_MODERN = 1;

    private int modeType;

    public static int getMaxBeginningLevel(LightBlocksGame app) {
        // gibt mindestens 14 zurück, oder das ansonsten per Spiel erreichte Level plus 1

        int maxClearedLines = app.savegame.getBestScore(MarathonModel.MODEL_MARATHON_NORMAL_ID)
                .getClearedLines();

        maxClearedLines = Math.max(maxClearedLines,
                app.savegame.getBestScore(RetroMarathonModel.MODEL_MARATHON_RETRO89).getClearedLines());

        maxClearedLines = Math.max(maxClearedLines,
                app.savegame.getBestScore(MarathonModel.MODEL_MARATHON_GRAVITY_ID).getClearedLines());

        return MathUtils.clamp(maxClearedLines / 10 + 1, 14, 19);
    }

    @Override
    public String getIdentifier() {
        return MODEL_PRACTICE_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelPractice";
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.Practice);
        retVal.setModeType(modeType);

        return retVal;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        modeType = newGameParams.getModeType();
        super.startNewGame(newGameParams);
    }

    @Override
    protected void submitGameEnded(boolean success) {
        // Practice endet immer mit vollem Board. Also definieren wir success hier so, ob 100 Lines geschafft wurden
        super.submitGameEnded(getScore().getClearedLines() >= 100);
    }

    @Override
    protected void initGameScore(int beginningLevel) {
        super.initGameScore(beginningLevel);
        // Scoring auf PRACTICE-Mode stellen (Level für Score unerheblich, und es wird nicht schneller)
        getScore().setScoringType(GameScore.TYPE_PRACTICE);
    }

    @Override
    public boolean showBlocksScore() {
        return true;
    }

    @Override
    public void setBestScore(BestScore bestScore) {
        super.setBestScore(bestScore);
        // bei Practice sind die meisten abgelegten Blöcke maßgeblich
        bestScore.setComparisonMethod(BestScore.ComparisonMethod.blocks);
    }

    @Override
    public String getScoreboardParameters() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("startLevel", new JsonValue(getScore().getStartingLevel()));
        root.addChild("type", new JsonValue(modeType));
        return root.toJson(JsonWriter.OutputType.json);
    }

    @Override
    public boolean isModernRotation() {
        return modeType == TYPE_MODERN;
    }

    @Override
    protected int getLockDelayMs() {
        return modeType == TYPE_MODERN ? ModernFreezeModel.LOCK_DELAY : super.getLockDelayMs();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        modeType = jsonData.getInt("modeType", TYPE_CLASSIC);
        super.read(json, jsonData);
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("modeType", modeType);
    }
}
