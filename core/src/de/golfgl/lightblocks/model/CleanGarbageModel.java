package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.state.InitGameParameters;

public class CleanGarbageModel extends GameModel {
  public static final String MODEL_CLEAN_GARBAGE_ID = "cleanGarbage";

  private int modeType;

  @Override
  public String getIdentifier() {
    return MODEL_CLEAN_GARBAGE_ID;
  }

  @Override
  public String getGoalDescription()
  {
    return "goalClean";
  }

  @Override
  public InitGameParameters getInitParameters() {
    InitGameParameters retVal = new InitGameParameters();

    retVal.setBeginningLevel(getScore().getStartingLevel());
    retVal.setInputKey(inputTypeKey);
    retVal.setGameMode(InitGameParameters.GameMode.Clean);
    retVal.setModeType(modeType);

    return retVal;
  }

  @Override
  protected void activeTetrominoDropped() {
    if (getScore().getClearedLines() > 0 && !getGameboard().hasGarbage())
      setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
  }


  @Override
  public void startNewGame(InitGameParameters newGameParams) {
    modeType = newGameParams.getModeType();
    super.startNewGame(newGameParams);
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
