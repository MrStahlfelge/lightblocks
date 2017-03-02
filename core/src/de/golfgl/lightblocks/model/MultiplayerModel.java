package de.golfgl.lightblocks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Set;

import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Das Multiplayer-Modell
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MultiplayerModel extends GameModel {

    public static final String MODEL_ID = "multiplayer";

    //TODO!
    private byte numberOfPlayers = 2;
    private AbstractMultiplayerRoom playerRoom;

    private HashMap<String, MultiPlayerObjects.PlayerInGame> playerInGame;

    @Override
    public String getIdentifier() {
        return MODEL_ID + numberOfPlayers;
    }

    @Override
    public String saveGameModel() {
        // Saving multiplayer gamestate is unfortunately impossible
        return null;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        playerRoom = newGameParams.getMultiplayerRoom();

        Set<String> playersInRoom = playerRoom.getPlayers();
        playerInGame = new HashMap<String, MultiPlayerObjects.PlayerInGame>(playersInRoom.size());
        for (String player : playersInRoom) {
            playerInGame.put(player, new MultiPlayerObjects.PlayerInGame());
        }

        super.startNewGame(newGameParams);
    }

    @Override
    protected void activeTetrominoDropped() {
        // den Meister über den Stand der Dinge informieren
        MultiPlayerObjects.PlayerInGame meInGame = new MultiPlayerObjects.PlayerInGame();
        meInGame.playerId = playerRoom.getMyPlayerId();
        meInGame.filledBlocks = 0; // TODO AUswertung muss noch
        final GameScore myScore = getScore();
        meInGame.drawnBlocks = myScore.getDrawnTetrominos();
        meInGame.score = myScore.getScore();
        playerRoom.sendToReferee(meInGame);
    }

    @Override
    public InitGameParameters getInitParameters() {
        return null;
    }

    public void handleMessagesFromOthers(Object o) {
        // Player disconnected oder ähnliches => wie Game Over behandeln
        if (o instanceof MultiPlayerObjects.PlayerChanged) {
            if (((MultiPlayerObjects.PlayerChanged) o).changeType == MultiPlayerObjects.CHANGE_REMOVE)
                handlePlayerIsOver(((MultiPlayerObjects.PlayerChanged) o).changedPlayer.name);
        }

        // Meldung dass ein Spieler raus ist
        if (o instanceof MultiPlayerObjects.PlayerIsOver)
            handlePlayerIsOver(((MultiPlayerObjects.PlayerIsOver) o).playerId);

        if (o instanceof MultiPlayerObjects.BonusScore)
            handleBonusScore(((MultiPlayerObjects.BonusScore) o).score);

        // Meldung dass das Spiel zu Ende ist
        if (o instanceof MultiPlayerObjects.GameIsOver) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (!isGameOver())
                        setGameOverWon();
                }
            });
        }

        if (o instanceof MultiPlayerObjects.PlayerInGame) {
            handlePlayerInGameChanged((MultiPlayerObjects.PlayerInGame) o);
        }

    }

    private void handlePlayerInGameChanged(MultiPlayerObjects.PlayerInGame pig) {

        boolean changedIt = false;

        synchronized (playerInGame) {
            // wenn wirklich noch im rennen, dann updaten
            if (playerInGame.containsKey(pig.playerId)) {
                playerInGame.put(pig.playerId, pig);
                changedIt = true;
            }
        }

        if (changedIt) {
            if (playerRoom.isOwner())
                playerRoom.sendToAllPlayers(pig);

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.playersInGameChanged();
                }
            });

        }

    }

    private void handleBonusScore(final int score) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                getScore().addBonusScore(score);
                // Highscores updaten
                bestScore.setBestScores(getScore());
                totalScore.addScore(score);
                userInterface.updateScore(getScore(), score);
            }
        });
    }

    protected void handlePlayerIsOver(final String playerId) {
        MultiPlayerObjects.PlayerInGame deadPlayer = null;
        Set<String> playersLeft = null;

        synchronized (playerInGame) {
            deadPlayer = playerInGame.remove(playerId);
            playersLeft = playerInGame.keySet();
        }

        if (deadPlayer != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.playersInGameChanged();

                    if (!isGameOver())
                        userInterface.showMotivation(IGameModelListener.MotivationTypes.playerOver, playerId);
                }
            });

            if (playerRoom.isOwner()) {
                //der Meister teilt das Geld dann auf und informiert alle über den Abgang
                playerRoom.sendToAllPlayers(new MultiPlayerObjects.PlayerIsOver().withPlayerId(playerId));

                if (playersLeft.size() >= 1) {
                    MultiPlayerObjects.BonusScore bonus = new MultiPlayerObjects.BonusScore();
                    bonus.score = (int) ((deadPlayer.score * .3f) / playersLeft.size());
                    for (String leftPlayerId : playersLeft) {
                        if (leftPlayerId.equals(playerRoom.getMyPlayerId()))
                            handleMessagesFromOthers(bonus);
                        else
                            playerRoom.sendToPlayer(leftPlayerId, bonus);
                    }
                }

                if (playersLeft.size() <= 1) {
                    final MultiPlayerObjects.GameIsOver gameOverMsg = new MultiPlayerObjects.GameIsOver();
                    playerRoom.sendToAllPlayers(gameOverMsg);
                    // und auch selbst behandeln
                    handleMessagesFromOthers(gameOverMsg);
                }
            }
        }

    }

    @Override
    protected void setGameOverBoardFull() {
        final MultiPlayerObjects.PlayerIsOver playerOverMsg = new MultiPlayerObjects.PlayerIsOver();
        playerOverMsg.playerId = playerRoom.getMyPlayerId();
        playerRoom.sendToReferee(playerOverMsg);

        super.setGameOverBoardFull();
    }

    @Override
    public void write(Json json) {
        throw new UnsupportedOperationException("Not allowed in multiplayer");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        throw new UnsupportedOperationException("Not allowed in multiplayer");
    }
}
