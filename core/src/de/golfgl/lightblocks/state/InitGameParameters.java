package de.golfgl.lightblocks.state;

import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.model.BackendBattleModel;
import de.golfgl.lightblocks.model.CleanGarbageModel;
import de.golfgl.lightblocks.model.DeviceMultiplayerModel;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.ModernFreezeModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.RetroMarathonModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.ServerMultiplayerManager;
import de.golfgl.lightblocks.multiplayer.ServerMultiplayerModel;

/**
 * This class defines new games parameters.
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class InitGameParameters {
    public static final int TYPE_CLASSIC = 0;
    public static final int TYPE_MODERN = 1;

    private int inputKey;
    private int beginningLevel;
    private String missionId;
    private GameMode gameMode;
    private int initialGarbage;
    private int modeType;

    // for Multiplayer
    private AbstractMultiplayerRoom multiplayerRoom;
    private InputIdentifier firstPlayerInputId;
    private InputIdentifier secondPlayerInputId;
    private ServerMultiplayerManager serverMultiplayerManager;

    // for Turn Based Battle
    private MatchEntity matchEntity;
    private String playKey;
    private boolean startPaused;

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

    public int getInitialGarbage()
    {
        return initialGarbage;
    }

    public void setInitialGarbage(int initialGarbage)
    {
        this.initialGarbage = initialGarbage;
    }

    public AbstractMultiplayerRoom getMultiplayerRoom() {
        return multiplayerRoom;
    }

    public void setMultiplayerRoom(AbstractMultiplayerRoom multiplayerRoom) {
        this.multiplayerRoom = multiplayerRoom;
    }

    public ServerMultiplayerManager getServerMultiplayerManager() {
        return serverMultiplayerManager;
    }

    public void setServerMultiplayerManager(ServerMultiplayerManager serverMultiplayerManager) {
        this.serverMultiplayerManager = serverMultiplayerManager;
    }

    public InputIdentifier getFirstPlayerInputId() {
        return firstPlayerInputId;
    }

    public InputIdentifier getSecondPlayerInputId() {
        return secondPlayerInputId;
    }

    public void setPlayerInputIds(InputIdentifier firstPlayerInputId, InputIdentifier secondPlayerInputId) {
        this.firstPlayerInputId = firstPlayerInputId;
        this.secondPlayerInputId = secondPlayerInputId;
    }

    public MatchEntity getMatchEntity() {
        return matchEntity;
    }

    public void setMatchEntity(MatchEntity matchEntity) {
        this.matchEntity = matchEntity;
    }

    public String getPlayKey() {
        return playKey;
    }

    public void setPlayKey(String playKey) {
        this.playKey = playKey;
    }

    public boolean isStartPaused() {
        return startPaused;
    }

    public void setStartPaused(boolean startPaused) {
        this.startPaused = startPaused;
    }

    public int getModeType() {
        return modeType;
    }

    public void setModeType(int modeType) {
        this.modeType = modeType;
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
            case ModernFreeze:
                return new ModernFreezeModel();
            case Clean:
                return new CleanGarbageModel();
            case DeviceMultiplayer:
                return new DeviceMultiplayerModel();
            case ServerMultiplayer:
                return new ServerMultiplayerModel();
        }
        throw new IllegalStateException("Unsupported game mode");
    }

    public enum GameMode {Multiplayer, Marathon, Tutorial, Sprint, MarathonRetro89, Practice, TurnbasedBattle, ModernFreeze, ServerMultiplayer, DeviceMultiplayer, Clean}
}
