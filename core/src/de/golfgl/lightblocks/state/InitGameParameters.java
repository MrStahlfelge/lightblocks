package de.golfgl.lightblocks.state;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

/**
 * This class defines new games.
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class InitGameParameters {

    private int inputKey;
    private int beginningLevel;
    private String missionId;
    private Class<? extends GameModel> gameModelClass;
    private AbstractMultiplayerRoom multiplayerRoom;

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public int getInputKey() {
        return inputKey;
    }

    public void setInputKey(int inputKey) {
        this.inputKey = inputKey;
    }

    public boolean isMultiplayer() {
        return gameModelClass != null && MultiplayerModel.class.isAssignableFrom(gameModelClass);
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

    public AbstractMultiplayerRoom getMultiplayerRoom() {
        return multiplayerRoom;
    }

    public void setMultiplayerRoom(AbstractMultiplayerRoom multiplayerRoom) {
        this.multiplayerRoom = multiplayerRoom;
    }
}
