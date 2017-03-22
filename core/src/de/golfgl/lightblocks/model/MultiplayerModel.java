package de.golfgl.lightblocks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.minlog.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.MultiplayerMatch;

import static de.golfgl.lightblocks.model.IGameModelListener.MotivationTypes.watchOutGarbage;

/**
 * Das Multiplayer-Modell
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MultiplayerModel extends GameModel {

    public static final String MODEL_ID = "multiplayer";
    public static final float SCORE_BONUS_PLAYEROVER = .33f;
    public static final int GARBAGEGAP_CHANGECOUNT = 9;

    // Referee - aber auch die anderen pflegen die Werte, falls Übergabe erfolgt
    private static final byte DRAWYER_PACKAGESIZE = 3;
    private AbstractMultiplayerRoom playerRoom;
    private HashMap<String, MultiPlayerObjects.PlayerInGame> playerInGame;
    private int tetrominosSent = 0;
    private int maxTetrosAPlayerDrawn;
    private Integer waitingGarbageLines = new Integer(0);

    // siehe drawGarbageLines
    private int[] garbageHolePosition;
    private int currentGarbageHolePosIndex = 0;
    private int currentGarbageHolePosUsed = 0;

    private MultiplayerMatch matchStats;

    private boolean isInitialized = false;
    private boolean isCompletelyOver = false;

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMultiplayer";
    }

    @Override
    public String saveGameModel() {
        // Saving multiplayer gamestate is unfortunately impossible
        return null;
    }

    @Override
    protected int[] drawGarbageLines() {
        // Das Loch in der Garbage wird über das Array definiert. Es ist mit Zufallszahlen gefüllt.
        // Immer 9 Reihen werden mit dem definierten Loch gefüllt. Dann wird currentGarbageHolePosIndex
        // weiter gesetzt. currentGarbageHolePosUsed ist der Merker wieviele Reihen schon für den
        // aktiven Index gefüllt wurden.

        int numOfLines = 0;
        synchronized (waitingGarbageLines) {
            numOfLines = waitingGarbageLines;
            waitingGarbageLines = 0;
        }

        int[] retVal = new int[numOfLines];

        for (int garbageLine = 0; garbageLine < numOfLines; garbageLine++) {
            System.out.println(currentGarbageHolePosIndex + ";" + currentGarbageHolePosUsed);
            retVal[garbageLine] = garbageHolePosition[currentGarbageHolePosIndex];
            currentGarbageHolePosUsed++;

            if (currentGarbageHolePosUsed >= GARBAGEGAP_CHANGECOUNT) {
                currentGarbageHolePosUsed = 0;
                currentGarbageHolePosIndex++;

                if (currentGarbageHolePosIndex >= garbageHolePosition.length)
                    currentGarbageHolePosIndex = 0;
            }
        }

        return retVal;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        playerRoom = newGameParams.getMultiplayerRoom();

        Set<String> playersInRoom = playerRoom.getPlayers();
        playerInGame = new HashMap<String, MultiPlayerObjects.PlayerInGame>(playersInRoom.size());
        for (String player : playersInRoom) {
            final MultiPlayerObjects.PlayerInGame pig = new MultiPlayerObjects.PlayerInGame();
            pig.playerId = player;
            playerInGame.put(player, pig);
        }

        garbageHolePosition = new int[10];
        for (byte i = 0; i < garbageHolePosition.length; i++)
            garbageHolePosition[i] = MathUtils.random(0, Gameboard.GAMEBOARD_COLUMNS - 1);

        super.startNewGame(newGameParams);
    }

    @Override
    protected void initializeActiveAndNextTetromino() {

        // der Meister würfelt die Tetrominos aus
        if (playerRoom.isOwner()) {

            // 21 steine bestimmen
            for (int i = 0; i < DRAWYER_PACKAGESIZE; i++)
                drawyer.determineNextTetrominos();

            // und dann an alle anderen senden
            MultiPlayerObjects.InitGame initGame = new MultiPlayerObjects.InitGame();
            initGame.firstTetrominos = drawyer.drawyer.toArray();
            initGame.garbageHolePosition = this.garbageHolePosition;
            playerRoom.sendToAllPlayers(initGame);
            tetrominosSent = initGame.firstTetrominos.length;

            // das erst nach der Kopie in initGame... sonst sind zwei Steine bereits entnommen
            super.initializeActiveAndNextTetromino();
            isInitialized = true;
        }
    }

    @Override
    protected void fireUserInterfaceTetrominoSwap() {
        //darf noch nicht, wenn noch nicht initialisiert ist
        if (isInitialized)
            super.fireUserInterfaceTetrominoSwap();
    }

    @Override
    protected void activeTetrominoDropped() {
        // den Meister über den Stand der Dinge informieren
        MultiPlayerObjects.PlayerInGame meInGame = new MultiPlayerObjects.PlayerInGame();
        meInGame.playerId = playerRoom.getMyPlayerId();
        meInGame.filledBlocks = getGameboard().calcGameboardFill();
        final GameScore myScore = getScore();
        meInGame.drawnBlocks = myScore.getDrawnTetrominos();
        meInGame.score = myScore.getScore();
        playerRoom.sendToReferee(meInGame);
    }

    @Override
    protected void linesRemoved(int linesRemoved, boolean isSpecial, boolean isDouble) {

        if (linesRemoved > 1) {
            MultiPlayerObjects.LinesRemoved lr = new MultiPlayerObjects.LinesRemoved();
            lr.playerId = playerRoom.getMyPlayerId();
            lr.isDouble = isDouble;
            lr.isSpecial = isSpecial;
            lr.linesRemoved = linesRemoved;

            playerRoom.sendToReferee(lr);
        }
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
            // isCompletelyOver erst nach kurzer Zeit setzen, damit Spieler kurz zu sich finden können
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    isCompletelyOver = true;
                }
            }, 2);

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (!isGameOver()) {
                        totalScore.incMultiPlayerMatchesWon();
                        totalScore.incMultiPlayerMatchesStarted();
                        setGameOverWon();
                    }
                }
            });
        }

        if (o instanceof MultiPlayerObjects.PlayerInGame)
            handlePlayerInGameChanged((MultiPlayerObjects.PlayerInGame) o);


        if (o instanceof MultiPlayerObjects.NextTetrosDrawn) {
            if (playerRoom.isOwner())
                Log.error("Multiplayer", "Drawn tetros received but I am the referee.");
            else {
                final int[] receivedTetros = ((MultiPlayerObjects.NextTetrosDrawn) o).nextTetrominos;
                drawyer.queueNextTetrominos(receivedTetros);
                tetrominosSent += receivedTetros.length;
            }
        }

        if (o instanceof MultiPlayerObjects.InitGame)
            handleInitGame((MultiPlayerObjects.InitGame) o);

        if (o instanceof MultiPlayerObjects.LinesRemoved)
            handleLinesRemoved((MultiPlayerObjects.LinesRemoved) o);

        if (o instanceof MultiPlayerObjects.GarbageForYou)
            handleGarbageForYou((MultiPlayerObjects.GarbageForYou) o);

    }

    protected void handleGarbageForYou(MultiPlayerObjects.GarbageForYou o) {
        boolean warningTreshold = false;
        synchronized (waitingGarbageLines) {
            int oldWaitingLines = waitingGarbageLines;
            waitingGarbageLines = waitingGarbageLines + o.garbageLines;

            if (waitingGarbageLines >= 4 && oldWaitingLines < 4)
                warningTreshold = true;
        }

        if (warningTreshold)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.showMotivation(watchOutGarbage, null);
                }
            });
    }

    protected void handleInitGame(MultiPlayerObjects.InitGame o) {
        if (isInitialized)
            Log.error("Multiplayer", "InitGame message received but already initialized");
        else {
            drawyer.queueNextTetrominos(o.firstTetrominos);
            this.garbageHolePosition = o.garbageHolePosition;

            // ok, das war das init...
            isInitialized = true;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MultiplayerModel.super.initializeActiveAndNextTetromino();

                }
            });

            //TODO zurückmelden dass dieser Spieler nun soweit ist - mit PlayerInGame
        }
    }

    private void handleLinesRemoved(MultiPlayerObjects.LinesRemoved lr) {
        if (!playerRoom.isOwner()) {
            Log.error("Multiplayer", "Got LinesRemoved message, but I am not the referee.");
            return;
        }

        int garbageToSend = lr.isSpecial ? 4 : lr.linesRemoved - 1;

        if (garbageToSend >= 1) {
            String playerWithLowestFill = null;

            synchronized (playerInGame) {
                int lowestFill = Integer.MAX_VALUE;
                for (Map.Entry<String, MultiPlayerObjects.PlayerInGame> player : playerInGame.entrySet()) {
                    String playerId = player.getKey();
                    int playerFill = player.getValue().filledBlocks;
                    if (!playerId.equals(lr.playerId) && playerFill < lowestFill) {
                        playerWithLowestFill = playerId;
                        lowestFill = playerFill;
                    }
                }

                // die Garbage sofort intern vermerken, denn der Spieler selbst sendet erst beim nächsten Drop
                if ((playerWithLowestFill != null))
                    playerInGame.get(playerWithLowestFill).filledBlocks += garbageToSend * Gameboard.GAMEBOARD_COLUMNS;
            }

            if (playerWithLowestFill != null) {
                MultiPlayerObjects.GarbageForYou gfu = new MultiPlayerObjects.GarbageForYou();
                gfu.garbageLines = garbageToSend;

                if (playerWithLowestFill.equals(playerRoom.getMyPlayerId()))
                    handleMessagesFromOthers(gfu);
                else
                    playerRoom.sendToPlayer(playerWithLowestFill, gfu);
            }
        }

    }

    private void handlePlayerInGameChanged(final MultiPlayerObjects.PlayerInGame pig) {

        synchronized (playerInGame) {
            // wenn wirklich noch im rennen, dann updaten
            if (playerInGame.containsKey(pig.playerId)) {
                playerInGame.put(pig.playerId, pig);

                maxTetrosAPlayerDrawn = Math.max(maxTetrosAPlayerDrawn, pig.drawnBlocks);

                if (playerRoom.isOwner()) {
                    playerRoom.sendToAllPlayers(pig);

                    if (tetrominosSent - maxTetrosAPlayerDrawn <= 10) {
                        // es wird zeit die nächsten Tetrominos zu ziehen
                        int offset = drawyer.drawyer.size;
                        for (int i = 0; i < DRAWYER_PACKAGESIZE; i++)
                            drawyer.determineNextTetrominos();
                        int drawnTetros = drawyer.drawyer.size - offset;

                        // auf synchronized drawyer wird verzichtet. es kann nur noch der eigene Render-Thread
                        // zugreifen,
                        // und dieser wird nur lesen da ja genug Tetros da sind
                        MultiPlayerObjects.NextTetrosDrawn nt = new MultiPlayerObjects.NextTetrosDrawn();

                        nt.nextTetrominos = new int[drawnTetros];

                        for (int i = 0; i < drawnTetros; i++) {
                            nt.nextTetrominos[i] = drawyer.drawyer.get(i + offset);
                        }
                        playerRoom.sendToAllPlayers(nt);

                        tetrominosSent += drawnTetros;

                    }
                }

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        userInterface.playersInGameChanged(pig);
                    }
                });

            }
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
            final MultiPlayerObjects.PlayerInGame pig = deadPlayer;
            // um anzuzeigen dass er Game Over ist Gameboard voll
            pig.filledBlocks = Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_ALLROWS;

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.playersInGameChanged(pig);

                    if (!isGameOver())
                        userInterface.showMotivation(IGameModelListener.MotivationTypes.playerOver, playerId);
                }
            });

            if (playerRoom.isOwner()) {
                //der Meister teilt das Geld dann auf und informiert alle über den Abgang
                playerRoom.sendToAllPlayers(new MultiPlayerObjects.PlayerIsOver().withPlayerId(playerId));

                if (playersLeft.size() >= 1) {
                    MultiPlayerObjects.BonusScore bonus = new MultiPlayerObjects.BonusScore();
                    bonus.score = (int) ((deadPlayer.score * SCORE_BONUS_PLAYEROVER) / playersLeft.size());
                    for (String leftPlayerId : playersLeft) {
                        if (leftPlayerId.equals(playerRoom.getMyPlayerId()))
                            handleMessagesFromOthers(bonus);
                        else
                            playerRoom.sendToPlayer(leftPlayerId, bonus);

                        // beim letzten übrigen kommt kein playerisOver mehr, daher Score + Bonus in
                        // dieser Ausnahme schon hier auf die Match-Stats rechnen
                        if (playersLeft.size() == 1)
                            matchStats.getPlayerStat(leftPlayerId).addTotalScore(playerInGame.get(leftPlayerId).score
                                    + bonus.score);
                    }
                }

                // Stats aktualisieren & an die Clients senden
                for (String playerLeftId : playersLeft) {
                    MultiplayerMatch.PlayerStat playerLeftStat = matchStats.getPlayerStat(playerLeftId);
                    playerLeftStat.incNumberOutplays();
                    playerRoom.sendToAllPlayers(playerLeftStat.toPlayerInMatch());
                }
                matchStats.getPlayerStat(deadPlayer.playerId).addTotalScore(deadPlayer.score);
                playerRoom.sendToAllPlayers(matchStats.getPlayerStat(deadPlayer.playerId).toPlayerInMatch());

                // und nun Spiel beenden wenn niemand mehr da ist
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
        totalScore.incMultiPlayerMatchesStarted();

        super.setGameOverBoardFull();
    }

    @Override
    public void setUserInterface(IGameModelListener userInterface) {
        super.setUserInterface(userInterface);

        synchronized (playerInGame) {
            for (MultiPlayerObjects.PlayerInGame pig : playerInGame.values())
                userInterface.playersInGameChanged(pig);
        }

    }

    public void setMatchStats(MultiplayerMatch matchStats) {
        this.matchStats = matchStats;
    }

    public boolean isCompletelyOver() {
        return isCompletelyOver;
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
