package de.golfgl.lightblocks.state;

import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.model.BackendBattleModel;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.RetroMarathonModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.model.TutorialModel;
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
    private GameMode gameMode;
    private AbstractMultiplayerRoom multiplayerRoom;
    private MatchEntity matchEntity;

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
        return GameMode.Multiplayer.equals(gameMode);
    }

    public int getBeginningLevel() {
        return beginningLevel;
    }

    public void setBeginningLevel(int beginningLevel) {
        this.beginningLevel = beginningLevel;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public AbstractMultiplayerRoom getMultiplayerRoom() {
        return multiplayerRoom;
    }

    public void setMultiplayerRoom(AbstractMultiplayerRoom multiplayerRoom) {
        this.multiplayerRoom = multiplayerRoom;
    }

    public MatchEntity getMatchEntity() {
        return matchEntity;
    }

    public void setMatchEntity(MatchEntity matchEntity) {
        this.matchEntity = matchEntity;
    }

    public GameModel newGameModelInstance() {
        switch (gameMode) {
            case Sprint:
                return new SprintModel();
            case Practice:
                return new PracticeModel();
            case Multiplayer:
                return new MultiplayerModel();
            case Marathon:
                return new MarathonModel();
            case MarathonRetro89:
                return new RetroMarathonModel();
            case Tutorial:
                return new TutorialModel();
            case TurnbasedBattle:
                return new BackendBattleModel();
        }
        throw new IllegalStateException("Unsupported game mode");
    }

    public enum GameMode {Multiplayer, Marathon, Tutorial, Sprint, MarathonRetro89, Practice, TurnbasedBattle}
}
