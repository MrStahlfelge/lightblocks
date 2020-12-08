package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;

import java.util.HashSet;
import java.util.Iterator;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendScore;
import de.golfgl.lightblocks.gpgs.GaHelper;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.menu.PauseDialog;
import de.golfgl.lightblocks.menu.RoundOverScoreScreen;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.MissionModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.MyExtendViewport;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.OnScreenGamepad;
import de.golfgl.lightblocks.scene2d.OverlayMessage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * The main playing screen
 * <p>
 * Serves as adapter between GameModel and input/gui
 * <p>
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends AbstractScreen implements OnScreenGamepad.IOnScreenButtonsScreen {

    private static final float GAMEOVER_TOUCHFREEZE = 1.5f;
    protected final TextButton pauseButton;
    protected final PlayerArea playerArea;
    private final PlayMusic music;
    private final Image backgroundImage;
    public GameModel gameModel;
    PlayScreenInput inputAdapter;
    private PlayerArea secondPlayer;
    private PauseDialog pauseDialog;
    private Dialog pauseMsgDialog;
    private boolean isPaused = true;
    private HashSet<GameBlocker> gameBlockers = new HashSet<>();
    private OverlayMessage overlayWindow;
    private boolean showScoresWhenGameOver = true;
    private float timeSinceGameOver = 0;
    private GameBlocker.UsePortraitGameBlocker usePortraitGameBlocker = new GameBlocker.UsePortraitGameBlocker();
    private boolean isGameOver = false;

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
            gameModel.checkPrerequisites(app);
            gameModel.startNewGame(initGameParametersParams);
        }

        playerArea.gameModel = gameModel;
        gameModel.setUserInterface(app, this, playerArea);

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
        //TODO would be better in game model, but game model can't access the saved states
        gameModel.setBestScore(app.savegame.getBestScore(gameModel.getIdentifier()));

        if (gameModel.hasSecondGameboard()) {
            secondPlayer = new PlayerArea(app, this);
            stage.getRoot().addActorAfter(playerArea, secondPlayer);
            secondPlayer.gameModel = gameModel.getSecondGameModel();
            secondPlayer.gameModel.setUserInterface(app, this, secondPlayer);
            secondPlayer.gameModelInitialized();

            if (gameModel.isSecondGameboardOptional()) {
                secondPlayer.setSoundVolumeFactor(.5f);
            }
        }

        ((MyExtendViewport) stage.getViewport()).setMinWorldWidth((gameModel.hasSecondGameboard()
                && !gameModel.isSecondGameboardOptional() ? 2 : 1) * LightBlocksGame.nativeGameWidth);

        playerArea.gameModelInitialized();
    }

    @Override
    public void render(float delta) {

        // Controller und Schwerkraft müssen gepollt werden
        inputAdapter.doPoll(delta);

        music.act(delta);
        app.theme.updateAnimations(delta, gameModel.getScore().getCurrentLevel());

        delta = Math.min(delta, 1 / 30f);

        if (!isPaused || !gameModel.canPause())
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
        gameModel.dispose();
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

            if (!gameModel.hasSecondGameboard()) {
                playerArea.switchedPause(immediately, isPaused);
            } else if (!isPaused) {
                // for second gameboard, we fade in immediately (no freeze interval supported)
                // and we don't fade out
                playerArea.switchedPause(true, isPaused);
                secondPlayer.switchedPause(true, isPaused);
            }

            if (!isPaused) {
                music.setPlayingMusic(app.localPrefs.isPlayMusic());
                music.play();
                pauseDialog.hide(null);
                if (pauseMsgDialog != null && pauseMsgDialog.hasParent())
                    pauseMsgDialog.hide();

                if (inputAdapter.getRequestedScreenOrientation() == null)
                    app.lockOrientation(null);

                //inform the game model that there was a pause
                gameModel.fromPause();
            } else {
                music.pause();

                // Spielstand speichern
                saveGameState();
                pauseDialog.show(stage);

                if (inputAdapter.getRequestedScreenOrientation() == null)
                    app.unlockOrientation();
            }
        }
    }

    public void setGameboardCriticalFill(boolean critical) {
        music.setFastPlay(critical);
    }

    public void startFreezeMode() {
        music.pause();
        if (app.localPrefs.isPlaySounds() && app.theme.freezeBeginSound != null)
            app.theme.freezeBeginSound.play();
    }

    public void endFreezeMode() {
        music.play();
    }

    private void goToHighscores() {

        RoundOverScoreScreen scoreScreen = new RoundOverScoreScreen(app);
        scoreScreen.setGameModelId(gameModel.getIdentifier());
        if (gameModel.hasSecondGameboard()) {
            scoreScreen.addScoreToShow(gameModel.getScore(), "Player 1");
            scoreScreen.addScoreToShow(gameModel.getSecondGameModel().getScore(), "Player 2");
            scoreScreen.setSecondReplay(gameModel.getSecondGameModel().getReplay());
        } else {
            scoreScreen.addScoreToShow(gameModel.getScore(), app.TEXTS.get("labelRoundScore"));
            scoreScreen.setBest(gameModel.getBestScore());
            scoreScreen.addScoreToShow(gameModel.getBestScore(), app.TEXTS.get("labelBestScore"));
        }
        scoreScreen.setNewGameParams(gameModel.getInitParameters());
        scoreScreen.setBackScreen(this.backScreen);
        scoreScreen.setReplay(gameModel.getReplay());
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);

        this.dispose();
    }

    public void setGameOver() {
        // might get called twice, avoid unnecessary operations
        if (isGameOver) {
            return;
        }

        isGameOver = true;

        music.stop();
        if (app.localPrefs.isPlaySounds() && app.theme.gameOverSound != null)
            app.theme.gameOverSound.play();
        inputAdapter.setGameOver();
        saveGameState();
        app.savegame.gpgsSaveGameState(null);
        pauseButton.setVisible(false);

        app.backendManager.enqueueAndSendScore(new BackendScore(gameModel.getScore(), gameModel.getIdentifier(),
                app.backendManager.getPlatformString(), inputAdapter.getScoreboardKey(),
                gameModel.getScoreboardParameters(), gameModel.getReplay()));
    }

    public void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig) {
        // only used on multiplayer
    }

    public void playersGameboardChanged(MultiPlayerObjects.ChatMessage gameboardInfo) {
        // only used on multiplayer
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

    public void addGameBlocker(GameBlocker e) {
        gameBlockers.add(e);
        refreshResumeFromPauseText();

        // Pause auslösen
        if (!isPaused && !gameBlockers.isEmpty())
            switchPause(false);
    }

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

    public void showFreeTextMessage(final String message) {
        if (overlayWindow == null)
            overlayWindow = new OverlayMessage(app, playerArea.labelGroup.getWidth());

        if (message == null)
            overlayWindow.hide();
        else {
            overlayWindow.showOrChangeText(stage, message);
        }
    }

    /**
     * shows an overlay message. if changed, window disappears and reappears again
     * @param message message i18n key, or null to hide the window
     * @param params
     */
    public void showOverlayMessage(final String message, final String... params) {
        if (overlayWindow == null)
            overlayWindow = new OverlayMessage(app, playerArea.labelGroup.getWidth());

        if (message == null)
            overlayWindow.hide();
        else {
            String localizedMessage = app.TEXTS.format(message, (Object[]) params);
            if (localizedMessage.contains("_CONTINUE_")) {
                if (inputAdapter != null)
                    localizedMessage = localizedMessage.replace("_CONTINUE_", inputAdapter.getTutorialContinueText());
                else {
                    // => not yet initialized completely, postpone running the code for later
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            showOverlayMessage(message, params);
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

        if (overlayWindow != null) {
            overlayWindow.setY(OverlayMessage.POS_Y);
        }

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

        float maxWidthPerGameboard = Math.max(gameModel.hasSecondGameboard() ? stage.getWidth() / 2 : stage.getWidth(),
                LightBlocksGame.nativeGameWidth);

        playerArea.setPosition((maxWidthPerGameboard - LightBlocksGame.nativeGameWidth) / 2,
                (stage.getHeight() - LightBlocksGame.nativeGameHeight) / 2);

        int gameboardAlignment = inputAdapter != null ? inputAdapter.getRequestedGameboardAlignment() : Align.center;

        playerArea.setScoreTablePosition(gameboardAlignment, maxWidthPerGameboard);

        if (gameboardAlignment == Align.top) {
            float deltaY = (stage.getHeight() - LightBlocksGame.nativeGameHeight) / 2;
            playerArea.setY(playerArea.getY() + deltaY);
            backgroundImage.setY(deltaY);
        }

        if (gameModel.hasSecondGameboard()) {
            secondPlayer.setPosition(maxWidthPerGameboard + playerArea.getX(), playerArea.getY());
            secondPlayer.setScoreTablePosition(gameboardAlignment, maxWidthPerGameboard);
        }

        pauseButton.getLabel().setFontScale(MathUtils.clamp((float) width / height, 1f, 2f));
        pauseButton.pack();
        pauseButton.setSize(pauseButton.getWidth() * 1.2f, pauseButton.getHeight() * 1.2f);

        PlayScoreTable scoreTable = playerArea.scoreTable;
        if ((scoreTable.getX() + playerArea.getX() - scoreTable.getPrefWidth() / 2) > pauseButton.getWidth())
            pauseButton.setPosition(scoreTable.getX() - scoreTable.getPrefWidth() / 2 - pauseButton.getWidth(),
                    (scoreTable.getHeight() - pauseButton.getHeight()) / 2 + scoreTable.getY());
        else
            pauseButton.setPosition(scoreTable.getX() - scoreTable.getPrefWidth() / 2, scoreTable.getY() - 2f * pauseButton.getHeight());

        if (inputAdapter != null) {
            inputAdapter.setPlayScreen(this, app);

            // this is named PortraitGameBlocker, because there is only portrait modus forced for gravity input
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
