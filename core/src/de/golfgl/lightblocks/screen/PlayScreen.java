package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;

import java.util.HashSet;
import java.util.Iterator;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendScore;
import de.golfgl.lightblocks.gpgs.GaHelper;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.input.VibrationType;
import de.golfgl.lightblocks.menu.PauseDialog;
import de.golfgl.lightblocks.menu.RoundOverScoreScreen;
import de.golfgl.lightblocks.model.BackendBattleModel;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.MissionModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.OnScreenGamepad;
import de.golfgl.lightblocks.scene2d.OverlayMessage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Theme;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * The main playing screen
 * <p>
 * Serves as adapter between GameModel and input/gui
 * <p>
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends AbstractScreen implements IGameModelListener, OnScreenGamepad.IOnScreenButtonsScreen {

    public static final float DURATION_TETRO_MOVE = 1 / 30f;
    public static final float DURATION_REMOVE_DELAY = .15f;
    public static final float DURATION_REMOVE_FADEOUT = .2f;
    private static final float GAMEOVER_TOUCHFREEZE = 1.5f;
    protected final TextButton pauseButton;
    protected final PlayerArea playerArea;
    private final PlayMusic music;
    private final Image backgroundImage;
    public GameModel gameModel;
    PlayScreenInput inputAdapter;
    boolean noLineClearAnimation;
    private PauseDialog pauseDialog;
    private Dialog pauseMsgDialog;
    private boolean isPaused = true;
    private HashSet<GameBlocker> gameBlockers = new HashSet<GameBlocker>();
    private OverlayMessage overlayWindow;
    private boolean showScoresWhenGameOver = true;
    private float timeSinceGameOver = 0;
    private GameBlocker.UsePortraitGameBlocker usePortraitGameBlocker = new GameBlocker.UsePortraitGameBlocker();

    public PlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app);

        bgColor = app.theme.bgColor;

        music = new PlayMusic(app);

        backgroundImage = new Image();
        stage.addActor(backgroundImage);

        playerArea = new PlayerArea(app, this);
        stage.addActor(playerArea);

        pauseButton = new TextButton(FontAwesome.CIRCLE_PAUSE, app.skin, FontAwesome.SKIN_FONT_FA);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switchPause(false);
            }
        });
        pauseButton.getLabel().setColor(app.theme.buttonColor);
        pauseButton.setVisible(LightBlocksGame.isWebAppOnMobileDevice() ||
                Gdx.app.getType() == Application.ApplicationType.iOS || LightBlocksGame.GAME_DEVMODE);

        pauseDialog = new PauseDialog(app, this);

        // this will add tutorial messages to the screen - everything added to the Stage later than
        // this, will overlay the OverlayWindow!
        initializeGameModel(initGameParametersParams);

        // TODO this shouldn't be added to playerarea
        playerArea.addActor(pauseButton);

        Mission mission = app.getMissionFromUid(gameModel.getIdentifier());
        String modelIdLabel = (mission != null ? app.TEXTS.format("labelMission", mission.getDisplayIndex())
                : app.TEXTS.get(Mission.getLabelUid(gameModel.getIdentifier())));

        playerArea.gameType.setText(modelIdLabel);
        pauseDialog.setTitle(modelIdLabel);
        pauseDialog.addRetryButton(gameModel.getInitParameters());
        final String goalDescription = gameModel.getGoalDescription();
        if (goalDescription != null && !goalDescription.isEmpty()) {
            String[] goalParams = gameModel.getGoalParams();
            pauseDialog.setText(goalParams == null ? app.TEXTS.get(goalDescription)
                    : app.TEXTS.format(goalDescription, goalParams));
        }
        refreshResumeFromPauseText();

        if (!gameModel.beginPaused() && gameBlockers.isEmpty()) {
            switchPause(true);
            pauseDialog.setResumeLabel();
        } else
            pauseDialog.show(stage);

        // if the tutorial is not available and this is the very first game, but not multiplayer,
        // we show a short help how to control the game
        if (!TutorialModel.tutorialAvailable() && gameModel.totalScore.getClearedLines() < 1
                && (gameModel.beginPaused() || gameModel instanceof MissionModel)) {
            // postRunnable needed, because mission overly window uses it and would overlay the help
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    showInputHelp();
                }
            });
        }


    }

    /**
     * Constructs a new game and sets the screen to it.
     *
     * @param newGameParams null if game should be resumed.
     */
    public static PlayScreen gotoPlayScreen(LightBlocksGame app, InitGameParameters newGameParams) throws
            VetoException {

        boolean resumeGame = (newGameParams == null);

        if (!resumeGame && app.savegame.hasSavedGame())
            app.savegame.resetGame();

        try {
            final PlayScreen currentGame;
            if (!resumeGame && newGameParams.isMultiplayer())
                currentGame = new MultiplayerPlayScreen(app, newGameParams);
            else
                currentGame = new PlayScreen(app, newGameParams);

            Gdx.input.setInputProcessor(null);
            app.controllerMappings.setInputProcessor(null);
            app.setScreen(currentGame);

            // Game Analysis
            GaHelper.startGameEvent(app, currentGame.gameModel, currentGame.inputAdapter);

            // GPGS Event
            if (app.gpgsClient != null) {
                // Unterschied machen wenn Multiplayer
                String modelId = currentGame.gameModel.getIdentifier();
                String eventId = null;
                if (!modelId.equals(MultiplayerModel.MODEL_ID))
                    eventId = GpgsHelper.getNewGameEventByModelId(modelId);
                else if (app.multiRoom != null && app.multiRoom.isLocalGame())
                    eventId = GpgsHelper.EVENT_LOCAL_MULTIPLAYER_MATCH_STARTED;
                else if (app.multiRoom != null && !app.multiRoom.isLocalGame())
                    eventId = GpgsHelper.EVENT_INET_MULTIPLAYER_MATCH_STARTED;

                if (eventId != null) {
                    Gdx.app.log("GPGS", "Submitting newly started game " + modelId);
                    app.gpgsClient.submitEvent(eventId, 1);
                }
            }

            return currentGame;

        } catch (InputNotAvailableException inp) {
            throw new VetoException(app.TEXTS.format("errorInputNotAvail",
                    app.TEXTS.get(PlayScreenInput.getInputTypeName(inp.getInputKey()))));
        }
    }

    public void touchTimeLabelWithWarning() {
        boolean somethingDone = gameModel.onTimeLabelTouchedByPlayer();

        if (!somethingDone && !playerArea.timeLabel.hasActions()) {
            Color oldColor = new Color(playerArea.timeLabel.getColor());
            playerArea.timeLabel.setColor(app.theme.emphasizeColor);
            playerArea.timeLabel.addAction(Actions.color(oldColor, 1f));
        }

    }

    protected void populateScoreTable(Table scoreTable) {
        // for overriding purpose
    }

    /**
     * returns if the game state is currently paused
     *
     * @return true if is paused, false if not
     */
    public boolean isPaused() {
        return isPaused;
    }

    protected void initializeGameModel(InitGameParameters initGameParametersParams) throws InputNotAvailableException,
            VetoException {
        // Game Model erst hinzufügen, wenn die blockgroup schon steht
        if (initGameParametersParams == null && !app.savegame.hasSavedGame()) {
            throw new VetoException("No savegame available!");
        } else if (initGameParametersParams == null) {
            Json json = new Json();
            gameModel = json.fromJson(GameModel.class, app.savegame.loadGame());
        } else if (initGameParametersParams.getMissionId() != null) {
            Json json = new Json();
            try {
                gameModel = json.fromJson(GameModel.class,
                        app.savegame.loadMission(initGameParametersParams.getMissionId()));
            } catch (Throwable t) {
                Gdx.app.error("Gamestate", "Error loading mission", t);
                throw new IllegalStateException("Mission corrupted.", t);
            }

            // sicher ist sicher
            if (!gameModel.getIdentifier().equals(initGameParametersParams.getMissionId()))
                throw new IllegalStateException("Mission corrupted: " + initGameParametersParams.getMissionId());
        } else {
            try {
                gameModel = initGameParametersParams.newGameModelInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Given game model class is not appropriate.", e);
            }
            gameModel.startNewGame(initGameParametersParams);
        }

        gameModel.app = app;
        gameModel.setUserInterface(this);

        // input initialisieren
        inputAdapter = PlayScreenInput.getPlayInput(gameModel.inputTypeKey, app);
        inputAdapter.setPlayScreen(this, app);
        if (inputAdapter.getRequestedScreenOrientation() != null) {
            boolean rotated = app.lockOrientation(inputAdapter.getRequestedScreenOrientation());
            if (!rotated)
                addGameBlocker(usePortraitGameBlocker);
        }

        // Highscores
        gameModel.totalScore = app.savegame.getTotalScore();
        //TODO das sollte ins GameModel
        gameModel.setBestScore(app.savegame.getBestScore(gameModel.getIdentifier()));

        playerArea.gameModelInitialized(gameModel);
    }

    @Override
    public void render(float delta) {

        // Controller und Schwerkraft müssen gepollt werden
        inputAdapter.doPoll(delta);

        music.act(delta);
        app.theme.updateAnimations(delta, gameModel.getScore().getCurrentLevel());

        delta = Math.min(delta, 1 / 30f);

        if (!isPaused)
            gameModel.update(delta);

        if (gameModel.isGameOver() && timeSinceGameOver < GAMEOVER_TOUCHFREEZE)
            timeSinceGameOver = timeSinceGameOver + delta;

        playerArea.updateTimeLabel();

        super.render(delta);

    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);

        InputMultiplexer keyboardmultiplexer = new InputMultiplexer();
        keyboardmultiplexer.addProcessor(stage);
        keyboardmultiplexer.addProcessor(inputAdapter);
        Gdx.input.setInputProcessor(keyboardmultiplexer);

        InputMultiplexer controllerMultiplexer = new InputMultiplexer();
        controllerMultiplexer.addProcessor(stage);
        controllerMultiplexer.addProcessor(inputAdapter.getControllerInputProcessor());

        app.controllerMappings.setInputProcessor(controllerMultiplexer);

        swoshIn();
    }

    @Override
    public void goBackToMenu() {

        if (gameModel.isGameOver() && getShowScoresWhenGameOver())
            goToHighscores();

        else if (isPaused() || gameModel.isGameOver()) {
            //TODO im Fall von isPaused und !isGameOver bei BattleModel eine Warnung einblenden
            saveGameState();
            super.goBackToMenu();
        } else
            switchPause(true);
    }

    /**
     * for overriding. Defines if scores should be shown after a round.
     */
    protected boolean getShowScoresWhenGameOver() {
        return showScoresWhenGameOver;
    }

    public void setShowScoresWhenGameOver(boolean showScoresWhenGameOver) {
        this.showScoresWhenGameOver = showScoresWhenGameOver;
    }

    @Override
    public void dispose() {
        if (inputAdapter != null)
            inputAdapter.dispose();
        music.dispose();
        app.unlockOrientation();
        playerArea.dispose();
        super.dispose();
    }

    private void saveGameState() {
        app.savegame.saveTotalScore();
        app.savegame.saveBestScores();
        app.savegame.saveGame(gameModel.saveGameModel());
    }

    @Override
    public void pause() {
        super.pause();

        if (!isPaused && !gameModel.isGameOver())
            switchPause(true);
    }

    public void switchPause(boolean immediately) {

        if (gameModel.isGameOver()) {
            // bei Game Over auf beliebige Taste/Touch den Spielbildschirm verlassen, aber nicht ganz sofort
            if (timeSinceGameOver >= GAMEOVER_TOUCHFREEZE)
                goBackToMenu();
        } else if (!isPaused || gameBlockers.isEmpty()) {
            isPaused = !isPaused;

            final float fadingInterval = immediately ? 0 : .2f;

            BlockGroup blockGroup = playerArea.blockGroup;
            blockGroup.clearActions();

            if (!isPaused) {

                music.setPlayingMusic(app.localPrefs.isPlayMusic());
                music.play();
                if (blockGroup.getColor().a < 1) {
                    blockGroup.addAction(Actions.fadeIn(fadingInterval));
                    gameModel.setFreezeInterval(fadingInterval);
                }

                pauseDialog.hide(null);
                if (pauseMsgDialog != null && pauseMsgDialog.hasParent())
                    pauseMsgDialog.hide();

                if (inputAdapter.getRequestedScreenOrientation() == null)
                    app.lockOrientation(null);

                //inform the game model that there was a pause
                gameModel.fromPause();
            } else {
                blockGroup.addAction(Actions.fadeOut(fadingInterval));
                music.pause();

                // Spielstand speichern
                saveGameState();
                pauseDialog.show(stage);

                if (inputAdapter.getRequestedScreenOrientation() == null)
                    app.unlockOrientation();
            }
        }
    }

    @Override
    public void setGameboardCriticalFill(boolean critical) {
        music.setFastPlay(critical);
    }

    @Override
    public void startFreezeMode() {
        music.pause();
        if (app.localPrefs.isPlaySounds() && app.theme.freezeBeginSound != null)
            app.theme.freezeBeginSound.play();
    }

    @Override
    public void endFreezeMode(IntArray removedLines) {
        int removedLineNum = removedLines.size;
        if (removedLineNum > 0) {
            playerArea.motivatorLabel.addMotivationText((removedLineNum + " " + app.TEXTS.get("labelLines")).toUpperCase(), 1.5f);
            clearAndInsertLines(removedLines, removedLineNum >= 8, null);
        }
        music.play();
    }

    private void goToHighscores() {

        RoundOverScoreScreen scoreScreen = new RoundOverScoreScreen(app);
        scoreScreen.setGameModelId(gameModel.getIdentifier());
        scoreScreen.addScoreToShow(gameModel.getScore(), app.TEXTS.get("labelRoundScore"));
        scoreScreen.setBest(gameModel.getBestScore());
        scoreScreen.addScoreToShow(gameModel.getBestScore(), app.TEXTS.get("labelBestScore"));
        scoreScreen.setNewGameParams(gameModel.getInitParameters());
        scoreScreen.setBackScreen(this.backScreen);
        scoreScreen.setReplay(gameModel.getReplay());
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);

        this.dispose();
    }

    @Override
    public void insertNewBlock(int x, int y, int blockType) {
        BlockActor block = new BlockActor(app, blockType, true);
        playerArea.insertBlock(x, y, block);
    }

    @Override
    public void moveTetro(Integer[][] v, int dx, int dy, int ghostPieceDistance) {
        if (dx != 0 && app.localPrefs.isPlaySounds() && app.theme.horizontalMoveSound != null)
            app.theme.horizontalMoveSound.play();

        if (dx != 0 || dy != 0) {
            // erst alle vom Spielbrett einsammeln...
            Array<BlockActor> blocks = removeBlockActorsFromMatrix(v);

            //... und dann neu ablegen
            for (int i = 0; i < v.length; i++) {
                BlockActor block = blocks.get(i);
                int x = v[i][0];
                int y = v[i][1];
                block.setMoveAction(Actions.moveTo((x + dx) * BlockActor.blockWidth, (y + dy) * BlockActor
                        .blockWidth, DURATION_TETRO_MOVE));
                playerArea.blockMatrix[x + dx][y + dy] = block;
                playerArea.blockGroup.setGhostPiecePosition(i, x + dx, y - ghostPieceDistance, ghostPieceDistance);
            }
        }
    }

    private Array<BlockActor> removeBlockActorsFromMatrix(Integer[][] v) {
        Array<BlockActor> blocks = new Array<BlockActor>(v.length);

        BlockActor[][] blockMatrix = playerArea.blockMatrix;
        for (Integer[] xy : v) {
            if (blockMatrix[xy[0]][xy[1]] == null)
                Gdx.app.error("BLOCKS", "Block null at " + xy[0].toString() + " " + xy[1].toString());

            blocks.add(blockMatrix[xy[0]][xy[1]]);
            blockMatrix[xy[0]][xy[1]] = null;
        }
        return blocks;
    }

    @Override
    public void rotateTetro(Integer[][] vOld, Integer[][] vNew, int ghostPieceDistance) {
        if (app.localPrefs.isPlaySounds() && app.theme.rotateSound != null)
            app.theme.rotateSound.play();

        // erst alle vom Spielbrett einsammeln...
        Array<BlockActor> blocks = removeBlockActorsFromMatrix(vOld);

        //... und dann neu ablegen
        for (int i = 0; i < vOld.length; i++) {
            BlockActor block = blocks.get(i);
            int newx = vNew[i][0];
            int newy = vNew[i][1];
            block.setMoveAction(Actions.moveTo((newx) * BlockActor.blockWidth, (newy) * BlockActor.blockWidth, 1 /
                    20f));
            playerArea.blockMatrix[newx][newy] = block;
            playerArea.blockGroup.setGhostPiecePosition(i, newx, newy - ghostPieceDistance, ghostPieceDistance);
        }


    }

    @Override
    public void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {
        BlockActor[][] blockMatrix = playerArea.blockMatrix;;

        final float removeDelayTime = DURATION_REMOVE_DELAY;
        final float removeFadeOutTime = DURATION_REMOVE_FADEOUT;
        final float moveActorsTime = .1f;

        int linesToInsert = (garbageHolePosition == null ? 0 : garbageHolePosition.length);

        if (linesToRemove.size <= 0 && linesToInsert <= 0)
            return;

        if (noLineClearAnimation) {
            // nur volle Reihen beleuchten und fertig
            for (int i = linesToRemove.size - 1; i >= 0; i--) {
                int y = linesToRemove.get(i);

                // enlighten and remove all blocks from the line to be removed
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][y];
                    blockMatrix[x][y] = null;
                    block.setEnlightened(true);

                    block.addAction(sequence(Actions.delay(5), Actions.fadeOut(2),
                            Actions.removeActor()));
                    //block.addAction(Actions.moveBy((BlockActor.blockWidth / 2) * (x - 5), 0, 2 + 5));
                }
            }

            return;
        }

        gameModel.setFreezeInterval(removeDelayTime);

        // Vorbereitung zum Heraussuchen der Zeilen, die welche ersetzen
        IntArray lineMove = new IntArray(Gameboard.GAMEBOARD_ALLROWS);
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++)
            lineMove.add(i);


        if (linesToRemove.size > 0) {
            if (app.localPrefs.isPlaySounds() && app.theme.removeSound != null) {
                if (!special || app.theme.cleanSpecialSound == null)
                    app.theme.removeSound.play(.4f + linesToRemove.size * .2f);
                else
                    app.theme.cleanSpecialSound.play(.8f);
            }
            inputAdapter.vibrate(special ? VibrationType.SPECIAL_CLEAR : VibrationType.CLEAR);

            for (int i = linesToRemove.size - 1; i >= 0; i--) {
                int y = linesToRemove.get(i);

                // enlighten and remove all blocks from the line to be removed
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][y];
                    blockMatrix[x][y] = null;
                    block.setEnlightened(true);


                    if (special)
                        // Spezialeffekt: Verdichtung auf einen Block
                        block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(0) - .5f +
                                linesToRemove.size / 2) * BlockActor.blockWidth, removeDelayTime, Interpolation.fade));
                    else if (linesToRemove.size >= 3)
                        // ab 3 Zeilen alle zeilenweise zusammen schieben
                        block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(i)) *
                                BlockActor.blockWidth, removeDelayTime, Interpolation.fade));
                    // else if (y == i && linesToInsert == 0) - entfernt wegen anhaltendem Ärger!
                    // die untersten zusammenhängenden Zeilen rausschieben
                    //block.setMoveAction(Actions.moveBy(0, -2 * BlockActor.blockWidth, moveActorsTime),
                    //        removeDelayTime);

                    block.addAction(sequence(Actions.delay(removeDelayTime), Actions.fadeOut(removeFadeOutTime),
                            Actions.removeActor()));
                }

                // heraussuchen durch welche Zeile diese hier ersetzt wird (linesToInsert hier noch nicht beachtet)
                for (int higherY = y; higherY < Gameboard.GAMEBOARD_ALLROWS; higherY++)
                    if (higherY < Gameboard.GAMEBOARD_ALLROWS - 1)
                        lineMove.set(higherY, lineMove.get(higherY + 1));
                    else
                        lineMove.set(higherY, -1);
            }

            // den Explosions-Effekt einfügen
            if (app.theme.usesParticleEffect && (special && app.theme.particleEffectTrigger == Theme.EffectTrigger.specialClear
                    || app.theme.particleEffectTrigger == Theme.EffectTrigger.lineClear)) {

                if (app.theme.particleEffectPosition == Theme.EffectSpawnPosition.clear)
                    playerArea.weldEffect.setPosition(playerArea.blockGroup.getX() + 5f * BlockActor.blockWidth - app.theme.particleEffectWidth / 2,
                            playerArea.blockGroup.getY() + (linesToRemove.size / 2 + linesToRemove.get(0)) * BlockActor.blockWidth - app.theme.particleEffectHeight / 2);

                playerArea.weldEffect.start();
            }
        }

        // bis hier gilt: i = Zeile; lineMove.get(i): Zeile durch die i ersetzt wird ohne Garbage (oder -1 für keine)
        for (int i = 0; i < lineMove.size; i++) {
            int replaceLineIwith = lineMove.get(i);
            int destinationY = i + linesToInsert;
            if (replaceLineIwith >= 0) {
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    // hier die Garbage ebenfalls noch nicht einrechnen... in der Matrix erst im nächsten Schritt
                    // hochziehen
                    BlockActor block = blockMatrix[x][replaceLineIwith];
                    blockMatrix[x][replaceLineIwith] = null;
                    blockMatrix[x][i] = block;

                    // verschieben des Actors... hier wird aber die Garbage schon beachtet!
                    // falls gar keine Bewegung da, dann auch nix machen
                    if (block != null && destinationY != replaceLineIwith) {
                        float delay = 0;

                        // jetzt eine grundsätzliche Unterscheidung: wird die Zeile hochgeschoben oder runterbewegt?
                        // wenn hoch, dann kein delay
                        final SequenceAction moveSequence = Actions.action(SequenceAction.class);

                        if (destinationY <= replaceLineIwith)
                            delay = removeDelayTime;

                        moveSequence.addAction(Actions.moveTo((x) * BlockActor.blockWidth, (destinationY) *
                                BlockActor.blockWidth, moveActorsTime));

                        // wenn Block durch insert rausgeschoben wird, dann weg
                        // in der moveAction eigentlich nicht ganz korrekt, aber der Fall tritt sehr selten auf
                        // und der Block wird sowieso nicht nochmal angefasst werden
                        if (destinationY >= Gameboard.GAMEBOARD_ALLROWS) {
                            moveSequence.addAction(Actions.fadeOut(removeFadeOutTime));
                            moveSequence.addAction(removeActor());
                        }

                        if (delay > 0) {
                            final BlockActor timedBlock = block;

                            Timer.schedule(new Timer.Task() {
                                @Override
                                public void run() {
                                    timedBlock.setMoveAction(moveSequence);
                                }
                            }, delay);

                        } else
                            block.setMoveAction(moveSequence);
                    }

                }

            }
        }

        if (linesToInsert > 0) {
            if (app.localPrefs.isPlaySounds() && app.theme.garbageSound != null)
                app.theme.garbageSound.play(.4f + linesToInsert * .2f);
            inputAdapter.vibrate(VibrationType.GARBAGE);
            // nun die Referenz hochziehen
            for (int i = Gameboard.GAMEBOARD_ALLROWS - 1; i >= linesToInsert; i--)
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][i - linesToInsert];
                    blockMatrix[x][i - linesToInsert] = null;
                    blockMatrix[x][i] = block;
                }

            // und zu guter letzt die neuen Blöcke einfügen
            for (int y = linesToInsert - 1; y >= 0; y--) {
                int holePos = garbageHolePosition[linesToInsert - 1 - y];

                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    if (x != holePos) {
                        BlockActor block = new BlockActor(app, Gameboard.SQUARE_GARBAGE, true);
                        block.setX(x * BlockActor.blockWidth);
                        block.setY((y - linesToInsert) * BlockActor.blockWidth);
                        block.setEnlightened(true, true);
                        block.setMoveAction(Actions.sequence(Actions.moveTo((x) * BlockActor.blockWidth, y *
                                BlockActor.blockWidth, moveActorsTime), Actions.run(block.getDislightenAction())));
                        playerArea.blockGroup.addActor(block);
                        blockMatrix[x][y] = block;
                    }
                }
            }

        }
    }

    @Override
    public void markAndMoveFreezedLines(boolean playSoundAndMove, IntArray movedLines, IntArray fullLines) {
        BlockActor[][] blockMatrix = playerArea.blockMatrix;

        if (movedLines.size == 0 && fullLines.size == 0)
            return;

        if (playSoundAndMove && app.localPrefs.isPlaySounds() && app.theme.cleanFreezedSound != null)
            app.theme.cleanFreezedSound.play();

        // volle Reihen erleuchten
        for (int i = 0; i < fullLines.size; i++) {
            int y = fullLines.get(i);
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                BlockActor block = blockMatrix[x][y];
                block.setEnlightened(true);
            }

        }
        // zu verschiebende Reihen verschieben
        BlockActor[] lineBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS];
        for (int movedLineNum = 0; movedLineNum < movedLines.size; movedLineNum++) {

            int y = movedLines.get(movedLineNum);

            // die Reihe aufnehmen
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                lineBlocks[x] = blockMatrix[x][y];
                lineBlocks[x].setEnlightened(true);
            }

            // nun die Referenzen weiter unten schieben
            for (int i = y; i > movedLineNum; i--)
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][i - 1];
                    blockMatrix[x][i - 1] = null;
                    blockMatrix[x][i] = block;
                }

            // und die eigentliche Reihe dann ablegen
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++)
                blockMatrix[x][movedLineNum] = lineBlocks[x];
        }

        // die Blockmatrix zeigt jetzt den korrekten Stand der Dinge an, also jetzt auch in der
        // GUI verschieben
        if (movedLines.size > 0 && playSoundAndMove)
            for (int y = 0; y <= movedLines.get(movedLines.size - 1); y++) {
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    final BlockActor block = blockMatrix[x][y];
                    final int xf = x;
                    final int yf = y;

                    if (block != null) {
                        // der kleine Delay ist nötig, da bei Hard Drop der aktive nicht immer schon
                        // da ist
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                block.setMoveAction(Actions.moveTo(xf * BlockActor.blockWidth, yf *
                                        BlockActor.blockWidth, .3f));
                            }
                        }, .05f);
                    }
                }
            }

    }

    @Override
    public void setGameOver() {
        music.stop();
        if (app.localPrefs.isPlaySounds() && app.theme.gameOverSound != null)
            app.theme.gameOverSound.play();
        inputAdapter.setGameOver();
        playerArea.setGameOver();
        saveGameState();
        app.savegame.gpgsSaveGameState(null);
        pauseButton.setVisible(false);

        app.backendManager.enqueueAndSendScore(new BackendScore(gameModel.getScore(), gameModel.getIdentifier(),
                app.backendManager.getPlatformString(), inputAdapter.getScoreboardKey(),
                gameModel.getScoreboardParameters(), gameModel.getReplay()));
    }

    @Override
    public void showNextTetro(Integer[][] relativeBlockPositions, int blockType) {
        playerArea.showNextTetro(relativeBlockPositions, blockType);
    }

    @Override
    public void activateNextTetro(Integer[][] boardBlockPositions, int blockType, int ghostPieceDistance) {
        playerArea.activateNextTetro(boardBlockPositions, blockType, ghostPieceDistance);
    }

    @Override
    public void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions,
                                       Integer[][] newActivePiecePositions, int ghostPieceDistance, int holdBlockType) {
        playerArea.swapHoldAndActivePiece(newHoldPiecePositions, oldActivePiecePositions,
                newActivePiecePositions, ghostPieceDistance, holdBlockType);
    }

    @Override
    public void pinTetromino(Integer[][] currentBlockPositions) {
        if (app.localPrefs.isPlaySounds() && app.theme.dropSound != null)
            app.theme.dropSound.play();

        inputAdapter.vibrate(VibrationType.DROP);

        for (Integer[] vAfterMove : currentBlockPositions) {
            BlockActor activePieceBlock = playerArea.blockMatrix[vAfterMove[0]][vAfterMove[1]];
            activePieceBlock.setEnlightened(false);
        }
    }

    @Override
    public void markConflict(int x, int y) {
        BlockActor block = playerArea.blockMatrix[x][y];
        block.showConflictTouch();
    }

    @Override
    public void showMotivation(MotivationTypes achievement, @Nullable String extraMsg) {

        boolean playSound = true;
        boolean vibrate = true;
        String text = "";
        float duration = 2;

        switch (achievement) {
            case newLevel:
                text = app.TEXTS.get("labelLevel") + " " + extraMsg;
                break;
            case tSpin:
                text = app.TEXTS.get("motivationTSpin");
                break;
            case doubleSpecial:
                text = app.TEXTS.get("motivationDoubleSpecial");
                vibrate = false;
                break;
            case tenLinesCleared:
                text = extraMsg + " " + app.TEXTS.get("labelLines");
                playSound = false;
                vibrate = false;
                break;
            case boardCleared:
                text = app.TEXTS.get("motivationCleanComplete");
                vibrate = false;
                break;
            case newHighscore:
                text = app.TEXTS.get("motivationNewHighscore");
                break;
            case hundredBlocksDropped:
                text = app.TEXTS.format("motivationHundredBlocks", extraMsg);
                vibrate = false;
                playSound = false;
                break;
            case playerOver:
                if (extraMsg == null)
                    extraMsg = "Other player";
                else if (extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationPlayerOver", extraMsg);
                break;
            case gameOver:
                text = app.TEXTS.format("motivationGameOver");
                duration = 10;
                playSound = false;
                break;
            case gameWon:
                text = app.TEXTS.format("motivationGameWon");
                duration = 10;
                playSound = false;
                break;
            case gameSuccess:
                text = app.TEXTS.format("motivationGameSuccess");
                duration = 10;
                playSound = false;

                // keine weiteren Animationen zu abgebauten Reihen
                noLineClearAnimation = true;
                break;
            case bonusScore:
                text = app.TEXTS.format("motivationBonusScore", extraMsg);
                duration = 3;
                vibrate = false;
                break;
            case comboCount:
                text = app.TEXTS.format("motivationComboCount", extraMsg);
                duration = .5f;
                vibrate = false;
                break;
            case turnGarbage:
                playSound = true;
                if (extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationTurnGarbage", extraMsg);
                break;
            case turnOver:
                text = app.TEXTS.format("motivationTurnOver");
                break;
            case turnSurvive:
                if (extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationSurvive", extraMsg);
                duration = .75f;
                break;
            case prepare:
                text = app.TEXTS.format("motivationPrepare");
                duration = BackendBattleModel.PREPARE_TIME_SECONDS;
                break;
        }

        if (playSound && app.localPrefs.isPlaySounds() && app.theme.unlockedSound != null)
            app.theme.unlockedSound.play();

        if (vibrate && inputAdapter != null)
            inputAdapter.vibrate(VibrationType.MOTIVATION);

        if (!text.isEmpty())
            playerArea.addMotivationText(text.toUpperCase(), duration);
    }

    @Override
    public void showGarbageAmount(int lines) {
        playerArea.showGarbageAmount(lines);
    }

    @Override
    public void showComboHeight(int comboHeight) {
        playerArea.showComboHeight(comboHeight);
    }

    @Override
    public void updateScore(GameScore score, int gainedScore) {
        playerArea.updateScore(score);
    }

    @Override
    public void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig) {
        // Das passiert nur beim Multiplayer daher hier nichts
    }

    @Override
    public void playersGameboardChanged(MultiPlayerObjects.ChatMessage gameboardInfo) {
        // Das passiert nur beim Multiplayer daher hier nichts
    }

    public void showInputHelp() {
        pauseMsgDialog = new VetoDialog(inputAdapter.getInputHelpText(), app.skin,
                LightBlocksGame.nativeGameWidth * .9f);
        pauseMsgDialog.show(stage);
    }

    @Override
    public Dialog showConfirmationDialog(String text, Runnable doWhenYes) {
        Dialog dialog = super.showConfirmationDialog(text, doWhenYes);

        if (isPaused)
            pauseMsgDialog = dialog;

        return dialog;
    }

    @Override
    public void addGameBlocker(GameBlocker e) {
        gameBlockers.add(e);
        refreshResumeFromPauseText();

        // Pause auslösen
        if (!isPaused && !gameBlockers.isEmpty())
            switchPause(false);
    }

    @Override
    public void removeGameBlocker(GameBlocker e) {
        gameBlockers.remove(e);
        refreshResumeFromPauseText();
    }

    public boolean isGameBlockersEmpty() {
        return gameBlockers.isEmpty();
    }

    private void refreshResumeFromPauseText() {
        String blockText = "";
        if (gameBlockers.isEmpty())
            blockText = "";
        else {
            Iterator<GameBlocker> gbi = gameBlockers.iterator();
            while (gbi.hasNext()) {
                GameBlocker gb = gbi.next();
                blockText += "\n" + gb.getDescription(app.TEXTS);
            }
            blockText = blockText.substring(1);
        }
        pauseDialog.getInputMsgLabel().setText(blockText);
        pauseDialog.setEmphasizeInputMsg(!gameBlockers.isEmpty());
    }

    @Override
    public void showOverlayMessage(final String message, final float autoHide, final String... params) {
        if (overlayWindow == null)
            overlayWindow = new OverlayMessage(app, playerArea.labelGroup.getWidth());

        if (message == null)
            overlayWindow.hide();
        else {
            String localizedMessage = app.TEXTS.format(message, (Object) params);
            if (localizedMessage.contains("_CONTINUE_")) {
                if (inputAdapter != null)
                    localizedMessage = localizedMessage.replace("_CONTINUE_", inputAdapter.getTutorialContinueText());
                else {
                    // => eine Schleife nach hinten schieben, da noch nicht initialisiert ist
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            showOverlayMessage(message, autoHide, params);
                        }
                    });
                    return;
                }
            }
            overlayWindow.showText(stage, localizedMessage);
        }
    }

    public boolean showsOverlayMessage() {
        return overlayWindow != null && overlayWindow.hasParent();
    }

    public boolean showsPauseButton() {
        return pauseButton.isVisible();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        backgroundImage.setSize(stage.getWidth(), stage.getHeight());
        backgroundImage.setPosition(0, 0);
        Drawable backgroundPic;
        if (isLandscape() && app.theme.backgroundLandscapePic != null)
            backgroundPic = app.theme.backgroundLandscapePic;
        else
            backgroundPic = app.theme.backgroundPic;

        if (backgroundPic != backgroundImage.getDrawable()) {
            backgroundImage.setDrawable(backgroundPic);

            if (backgroundPic instanceof NinePatchDrawable)
                backgroundImage.setScaling(Scaling.stretch);
            else
                backgroundImage.setScaling(Scaling.none);
        }

        playerArea.setPosition((stage.getWidth() - LightBlocksGame.nativeGameWidth) / 2,
                (stage.getHeight() - LightBlocksGame.nativeGameHeight) / 2);

        int gameboardAlignment = inputAdapter != null ? inputAdapter.getRequestedGameboardAlignment() : Align.center;

        // sobald Platz neben Spielfeld breiter ist als Scoretable, diese dann mittig positionieren
        // und dann auch langsam herunterschieben aber maximal zwei Zeilen
        PlayScoreTable scoreTable = playerArea.scoreTable;
        scoreTable.validate();
        scoreTable.setX(Math.max(10 - playerArea.getX() + scoreTable.getPrefWidth() / 2, -playerArea.getX() / 2));
        scoreTable.setY((gameboardAlignment == Align.top ? LightBlocksGame.nativeGameHeight : stage.getHeight() - playerArea.getY() * 1.2f)
                - MathUtils.clamp(playerArea.getX() / 2 - scoreTable.getPrefWidth() / 2,
                scoreTable.getPrefHeight() / 2 + 5, scoreTable.getLinePrefHeight() * 2 + scoreTable.getPrefHeight() /
                        2));

        if (gameboardAlignment == Align.top) {
            float deltaY = (stage.getHeight() - LightBlocksGame.nativeGameHeight) / 2;
            playerArea.setY(playerArea.getY() + deltaY);
            backgroundImage.setY(deltaY);
        }

        pauseButton.getLabel().setFontScale(MathUtils.clamp((float) width / height, 1f, 2f));
        pauseButton.pack();
        pauseButton.setSize(pauseButton.getWidth() * 1.2f, pauseButton.getHeight() * 1.2f);

        if ((scoreTable.getX() + playerArea.getX() - scoreTable.getPrefWidth() / 2) > pauseButton.getWidth())
            pauseButton.setPosition(scoreTable.getX() - scoreTable.getPrefWidth() / 2 - pauseButton.getWidth(),
                    (scoreTable.getHeight() - pauseButton.getHeight()) / 2 + scoreTable.getY());
        else
            pauseButton.setPosition(scoreTable.getX() - scoreTable.getPrefWidth() / 2, scoreTable.getY() - 2f * pauseButton.getHeight());

        // GestureInput neues TouchPanel und Resize On Screen Controls
        if (inputAdapter != null) {
            inputAdapter.setPlayScreen(this, app);

            // es gibt momentan nur Portrait Zwang, eigentlich usePortraitGameBlocker natürlich für Landscape falsch
            Input.Orientation requestedScreenOrientation = inputAdapter.getRequestedScreenOrientation();
            if (requestedScreenOrientation != null && (requestedScreenOrientation.equals(Input.Orientation.Portrait)
                    && !isLandscape() || isLandscape() && requestedScreenOrientation.equals(Input.Orientation.Landscape)))
                removeGameBlocker(usePortraitGameBlocker);
        }
    }

    @Override
    public MyStage getStage() {
        return stage;
    }

    @Override
    public float getCenterPosX() {
        return playerArea.getX();
    }

    public float getCenterPosY() {
        return playerArea.getY();
    }

    @Override
    public float getGameboardTop() {
        return playerArea.getY() + playerArea.blockGroup.getY() + Gameboard.GAMEBOARD_NORMALROWS * BlockActor.blockWidth;
    }

    public static class PlayScoreTable extends Table {
        private final LightBlocksGame app;
        private ScoreLabel scoreNum;
        private ScoreLabel levelNum;
        private ScoreLabel linesNum;

        public PlayScoreTable(LightBlocksGame app) {
            this.app = app;
            defaults().height(BlockActor.blockWidth * .8f);
            row();
            Label levelLabel = new ScaledLabel(app.TEXTS.get("labelLevel").toUpperCase(), app.skin);
            app.theme.setScoreColor(levelLabel);
            add(levelLabel).right().bottom().padBottom(-2).spaceRight(3);
            levelNum = new ScoreLabel(2, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            app.theme.setScoreColor(levelNum);
            add(levelNum).left();
            Label linesLabel = new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin);
            app.theme.setScoreColor(linesLabel);
            add(linesLabel).right().bottom().padBottom(-2).spaceLeft(10).spaceRight(3);
            linesNum = new ScoreLabel(3, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            app.theme.setScoreColor(linesNum);
            linesNum.setCountingSpeed(100);
            add(linesNum).left();
            row();
            Label scoreLabel = new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin);
            app.theme.setScoreColor(scoreLabel);
            add(scoreLabel).right().bottom().padBottom(-2).spaceRight(3);
            scoreNum = new ScoreLabel(8, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            app.theme.setScoreColor(scoreNum);
            scoreNum.setCountingSpeed(2000);
            scoreNum.setMaxCountingTime(1);
            add(scoreNum).left().colspan(3);
        }

        public void setEmphasizeTresholds() {
            levelNum.setEmphasizeTreshold(1, app.theme.emphasizeColor);
            scoreNum.setEmphasizeTreshold(1000, app.theme.emphasizeColor);
        }

        public float getLinePrefHeight() {
            return levelNum.getPrefHeight();
        }

        public void setClearedLines(int clearedLines) {
            linesNum.setScore(clearedLines);
        }

        public void setCurrentLevel(int currentLevel) {
            levelNum.setScore(currentLevel);
        }

        public void setScore(int score) {
            scoreNum.setScore(score);
        }

        public void setLinesToClear(int linesToClear) {
            if (linesToClear == 0)
                return;

            String txtLinesToClear = String.valueOf(linesToClear);
            Label linesToClearLbl = new ScaledLabel(txtLinesToClear, app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f);
            app.theme.setScoreColor(linesToClearLbl);
            Label linesToClearSepLbl = new ScaledLabel("/", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .4f);
            app.theme.setScoreColor(linesToClearSepLbl);

            Cell cellToAdd = getCell(linesNum);

            Table linesTable = new Table();
            linesTable.add(linesNum);
            linesTable.add(linesToClearSepLbl).bottom().padBottom(5);
            linesTable.add(linesToClearLbl).bottom().padBottom(1);

            linesNum.setDigits(txtLinesToClear.length());

            cellToAdd.setActor(linesTable);
        }
    }
}
