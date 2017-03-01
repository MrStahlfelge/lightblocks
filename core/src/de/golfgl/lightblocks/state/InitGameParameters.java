package de.golfgl.lightblocks.state;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.MultiplayerModel;

/**
 * This class defines new games.
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class InitGameParameters {

    private int inputKey;
    private int beginningLevel;
    private Class<? extends GameModel> gameModelClass;

    public int getInputKey() {
        return inputKey;
    }

    public void setInputKey(int inputKey) {
        this.inputKey = inputKey;
    }

    public boolean isMultiplayer() {
        return MultiplayerModel.class.isAssignableFrom(gameModelClass);
    }

    public int getBeginningLevel() {
        return beginningLevel;
    }

    public void setBeginningLevel(int beginningLevel) {
        this.beginningLevel = beginningLevel;
    }

    public Class<? extends GameModel> getGameModelClass() {
        return gameModelClass;
    }

    public void setGameModelClass(Class<? extends GameModel> gameModelClass) {
        this.gameModelClass = gameModelClass;
    }
}
