package de.golfgl.lightblocks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Timer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.screen.MultiplayerPlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.MultiplayerMatch;

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
    private final Object waitingGarbageLinesLock = new Object();
    private AbstractMultiplayerRoom playerRoom;
    private HashMap<String, MultiPlayerObjects.PlayerInGame> playerInGame;
    private HashSet<String> uninitializedPlayers;
    private int tetrominosSent = 0;
    private int maxTetrosAPlayerDrawn;
    private Integer waitingGarbageLinesNum = new Integer(0);
    private String waitingGarbageLinesFrom;

    // siehe drawGarbageLines
    private int[] garbageHolePosition;
    private int currentGarbageHolePosIndex = 0;
    private int currentGarbageHolePosUsed = 0;

    private MultiplayerMatch matchStats;

    private boolean isInitialized = false;
    private boolean isCompletelyOver = false;

    // Merker für Achievements
    private boolean filledOver85Perc = false;
    private boolean achTurnaroundSent = false;

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
        synchronized (waitingGarbageLinesLock) {
            numOfLines = waitingGarbageLinesNum;
            waitingGarbageLinesNum = 0;

            //TODO hier dann einblenden von wem die Zeilen kamen
            waitingGarbageLinesFrom = null;

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.showGarbageAmount(waitingGarbageLinesNum);
                }
            });
        }

        int[] retVal = new int[numOfLines];

        for (int garbageLine = 0; garbageLine < numOfLines; garbageLine++) {
            //System.out.println(currentGarbageHolePosIndex + ";" + currentGarbageHolePosUsed);
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

            // und dann an alle anderen senden und merken, wer geantwortet hat
            uninitializedPlayers = new HashSet<String>(playerRoom.getPlayers());
            uninitializedPlayers.remove(playerRoom.getMyPlayerId());

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
        sendPlayerInGameStats();
    }

    private void sendPlayerInGameStats() {
        MultiPlayerObjects.PlayerInGame meInGame = new MultiPlayerObjects.PlayerInGame();
        meInGame.playerId = playerRoom.getMyPlayerId();
        meInGame.filledBlocks = getGameboard().calcGameboardFill();
        final GameScore myScore = getScore();
        meInGame.drawnBlocks = myScore.getDrawnTetrominos();
        meInGame.score = myScore.getScore();

        // an dieser Stelle kann der Achievement-Test ohne Overhead durchgeführt werden
        // da die Füllung bereits berechnet wurde
        if (!achTurnaroundSent) {
            if (!filledOver85Perc && meInGame.filledBlocks *
                    100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_NORMALROWS) >= 80)
                filledOver85Perc = true;

            if (filledOver85Perc && meInGame.filledBlocks *
                    100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_NORMALROWS) <= 15) {
                gpgsUpdateAchievement(GpgsHelper.ACH_COMPLETE_TURNAROUND);
                achTurnaroundSent = true;
            }
        }


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
    public boolean isGameOver() {
        return super.isGameOver() || (playerRoom.getRoomState() != MultiPlayerObjects.RoomState.inGame);
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

                        submitEvent(GpgsHelper.EVENT_MULTIPLAYER_MATCH_WON, 1);
                        gpgsUpdateAchievement(GpgsHelper.ACH_MATCHMAKER, 1,
                                (float) totalScore.getMultiPlayerMatchesWon() / 3);
                    }
                }
            });
        }

        if (o instanceof MultiPlayerObjects.PlayerInGame)
            handlePlayerInGameChanged((MultiPlayerObjects.PlayerInGame) o);


        if (o instanceof MultiPlayerObjects.NextTetrosDrawn) {
            if (playerRoom.isOwner())
                Gdx.app.error("Multiplayer", "Drawn tetros received but I am the referee.");
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
        synchronized (waitingGarbageLinesLock) {
            int oldWaitingLines = waitingGarbageLinesNum;
            waitingGarbageLinesNum = waitingGarbageLinesNum + o.garbageLines;

            // falls Garbage von verschiedenen Spielern kam dann from auf null
            if (oldWaitingLines <= 0)
                waitingGarbageLinesFrom = o.fromPlayerId;
            else if (waitingGarbageLinesFrom != null && !waitingGarbageLinesFrom.equalsIgnoreCase(o.fromPlayerId))
                waitingGarbageLinesFrom = null;

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    userInterface.showGarbageAmount(waitingGarbageLinesNum);
                }
            });
        }
    }

    protected void handleInitGame(MultiPlayerObjects.InitGame o) {
        if (isInitialized)
            Gdx.app.error("Multiplayer", "InitGame message received but already initialized");
        else {
            drawyer.queueNextTetrominos(o.firstTetrominos);
            this.garbageHolePosition = o.garbageHolePosition;

            // ok, das war das init...
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    isInitialized = true;
                    MultiplayerModel.super.initializeActiveAndNextTetromino();
                    // zurückmelden dass fertig initialisiert ist
                    sendPlayerInGameStats();
                }
            });

        }
    }

    private void handleLinesRemoved(MultiPlayerObjects.LinesRemoved lr) {
        if (!playerRoom.isOwner()) {
            Gdx.app.error("Multiplayer", "Got LinesRemoved message, but I am not the referee.");
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
                gfu.fromPlayerId = lr.playerId;

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
                    // pig wird auch für initialize benutzt
                    if (!uninitializedPlayers.isEmpty()) {
                        uninitializedPlayers.remove(pig.playerId);
                        if (uninitializedPlayers.isEmpty()) {
                            // alle sind fertig, dann Nachricht schicken
                            // gibt keine extra Init nachricht, es wird einfach ein SwitchPause mit PlayerId null
                            // geschickt
                            MultiPlayerObjects.SwitchedPause sp = new MultiPlayerObjects.SwitchedPause();
                            sp.playerId = null;
                            sp.nowPaused = false;
                            playerRoom.sendToAllPlayers(sp);
                            ((MultiplayerPlayScreen) userInterface).multiPlayerGotModelMessage(sp);
                        }
                    }

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

    @Override
    public boolean beginPaused() {
        return false;
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

    @Override
    public void update(float delta) {
        if (isInitialized)
            super.update(delta);
    }
}
