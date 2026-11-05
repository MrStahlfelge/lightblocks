package de.golfgl.lightblocks.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.TimeUtils;

import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.OnScreenGamepad;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * This class controls touch input for gestures, swipe, on screen gamepad and on screen buttons
 * <p>
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {
    public static final String INPUT_KEY_GESTURES = "gestures";
    public static final int SWIPEUP_DONOTHING = 0;
    public static final int SWIPEUP_PAUSE = 1;
    public static final int SWIPEUP_HARDDROP = 2;
    public static final int SWIPEUP_HOLD = 3;
    private static final float TOUCHPAD_STICK_DEAD_RADIUS = .5f;
    private static final float TOUCHPAD_DPAD_DEAD_RADIUS = .35f;
    private static final float MAX_SOFTDROPBEGINNING_INTERVAL = .3f;
    private static final float SWIPE_AXIS_SLOP_CELLS = .35f;
    private static final float SWIPE_AXIS_BIAS = 1.1f;
    private static final float SWIPE_SOFT_DROP_CELLS = 1f;
    private static final float SWIPE_UP_THRESHOLD_CELLS = 3f;
    private static final long FLICK_SAMPLE_WINDOW_NS = 75000000L;
    private static final float FLICK_HARD_DROP_CELLS_PER_SECOND = 20f;
    private static final float FLICK_HARD_DROP_TRAVEL_CELLS = 1.5f;

    private static final float SCREEN_BORDER_PERCENTAGE = 0.1f;
    // Flipped Mode: Tap löst horizontale Bewegung aus, Swipe rotiert
    private static final boolean flippedMode = false;

    private final InputIdentifier inputId = new InputIdentifier.TouchscreenInput();

    public Color rotateRightColor = new Color(.1f, 1, .3f, .8f);
    public Color rotateLeftColor = new Color(.2f, .8f, 1, .8f);
    boolean touchDownValid = false;
    boolean beganHorizontalMove;
    boolean beganSoftDrop;
    boolean didSomething;
    Group touchPanel;
    Vector2 touchCoordinates;
    private int screenX;
    private int screenY;
    private float timeSinceTouchDown;
    private int dragThreshold;
    private Label toTheRight;
    private Label toTheLeft;
    private Label toDrop;
    private Label rotationLabel;
    private boolean didHardDrop;
    private boolean hadButtonEvent;

    private OnScreenGamepad gamepadOnScreenControls;
    private PortraitOnScreenButtons buttonOnScreenControls;
    private boolean tutorialMode;
    private boolean invertRotation;
    private GestureOnScreenButtons gestureOnScreenControls;
    private boolean buttonsHidden;
    private boolean swipeMode;
    private final IntArray swipeMoves = new IntArray();
    private final Vector2 swipeCoordinates = new Vector2();
    private final LongArray swipeSampleTimes = new LongArray();
    private final FloatArray swipeSampleYs = new FloatArray();
    private boolean swipeFlickDownward;
    private GameModel swipeGameModel;
    private Tetromino swipeNextTetromino;
    private int swipeDrawnTetrominos;
    private SwipeAxis swipeAxis = SwipeAxis.undecided;
    private float swipeStartY;
    private float swipeLastX;
    private float swipeRemainder;
    private boolean swipeMoveIssued;
    private int swipeMoveTimeMs;

    @Override
    public String getInputHelpText() {
        return app.TEXTS.get(!isUsingOnScreenButtons() ? "inputGesturesHelp" :
                gamepadOnScreenControls != null ? "inputOnScreenGamepadHelp" : "inputOnScreenButtonHelp");
    }

    @Override
    public String getTutorialContinueText() {
        return app.TEXTS.get("tutorialContinueGestures");
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen, LightBlocksGame app) {
        if (swipeMode)
            clearSwipeInput();
        super.setPlayScreen(playScreen, app);

        dragThreshold = app.localPrefs.getTouchPanelSize(app.getDisplayDensityRatio());
        tutorialMode = playScreen.gameModel instanceof TutorialModel;
        swipeMode = !tutorialMode && app.localPrefs.getUsedTouchControls() == LocalPrefs.TouchControlType.swipe;
        invertRotation = !tutorialMode && app.localPrefs.isInvertGesturesRotation();

        if (!swipeMode && (app.localPrefs.getShowTouchPanel() || tutorialMode))
            initializeTouchPanel(app, dragThreshold, playScreen.getStage());

        if (!isUsingOnScreenButtons()) {
            initGestureOnScreenControls();
        } else if (app.localPrefs.getUsedTouchControls() == LocalPrefs.TouchControlType.onScreenButtonsPortrait) {
            initPortraitOnScreenControls();
        } else {
            initGamepadOnScreenControls();
        }

    }

    @Override
    public boolean doPoll(float delta) {
        if (!isUsingOnScreenButtons()) {
            gestureOnScreenControls.setVisible(!isPaused() && !buttonsHidden);
        } else if (gamepadOnScreenControls != null) {
            gamepadOnScreenControls.setVisible(!isPaused() && !buttonsHidden);
        } else if (buttonOnScreenControls != null) {
            buttonOnScreenControls.setVisible(!isPaused() && !buttonsHidden);
        }

        if (touchDownValid)
            timeSinceTouchDown += delta;

        if (toDrop != null)
            toDrop.setVisible(beganSoftDrop || timeSinceTouchDown <= MAX_SOFTDROPBEGINNING_INTERVAL);

        if (swipeMode && checkSwipeInput()) {
            // Wait for frozen one-shot input to be consumed.
            if (swipeMoveIssued && swipeGameModel.getScore().getTimeMs() != swipeMoveTimeMs)
                swipeMoveIssued = false;
            if (!swipeMoveIssued && swipeMoves.size > 0) {
                playScreen.gameModel.inputDoOneHorizontalMove(inputId, swipeMoves.removeIndex(0) < 0);
                swipeMoveTimeMs = swipeGameModel.getScore().getTimeMs();
                swipeMoveIssued = true;
                giveHapticFeedback();
            }
        }

        // return hadButtonEvent to PlayKeyOrTouchInput so it can count all touch events
        if (hadButtonEvent) {
            hadButtonEvent = false;
            return true;
        }
        return false;
    }

    private GameModel getSwipeGameModel() {
        // Touch may control the second board.
        GameModel model = playScreen.gameModel;
        if (model.getFixedInputId() == null || model.getFixedInputId().isSameInput(inputId))
            return model;
        GameModel secondModel = model.getSecondGameModel();
        return secondModel != null && (secondModel.getFixedInputId() == null
                || secondModel.getFixedInputId().isSameInput(inputId)) ? secondModel : null;
    }

    private boolean checkSwipeInput() {
        if (swipeGameModel == null)
            return false;
        if (isPaused() || swipeGameModel.isGameOver()) {
            clearSwipeInput();
            return false;
        }
        if (swipeNextTetromino != swipeGameModel.getNextTetromino()
                || swipeDrawnTetrominos != swipeGameModel.getScore().getDrawnTetrominos()) {
            if (swipeAxis == SwipeAxis.vertical) {
                clearSwipeInput();
                return false;
            }
            clearSwipeMoves();
            swipeLastX = swipeCoordinates.x;
            didHardDrop = false;
            swipeNextTetromino = swipeGameModel.getNextTetromino();
            swipeDrawnTetrominos = swipeGameModel.getScore().getDrawnTetrominos();
        }
        return true;
    }

    private void clearSwipeMoves() {
        if (swipeMoveIssued && swipeGameModel != null && swipeGameModel.getScore().getTimeMs() == swipeMoveTimeMs) {
            // Cancel unconsumed one-shot input.
            playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
            playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
        }
        swipeMoves.clear();
        swipeMoveIssued = false;
        swipeRemainder = 0;
    }

    private void clearSwipeInput() {
        clearSwipeMoves();
        if (swipeGameModel != null) {
            if ((beganSoftDrop || didHardDrop)
                    && swipeNextTetromino == swipeGameModel.getNextTetromino()
                    && swipeDrawnTetrominos == swipeGameModel.getScore().getDrawnTetrominos())
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
        }
        swipeGameModel = null;
        swipeNextTetromino = null;
        swipeAxis = SwipeAxis.undecided;
        swipeSampleTimes.clear();
        swipeSampleYs.clear();
        swipeFlickDownward = false;
        didHardDrop = false;
        touchDownValid = false;
        beganHorizontalMove = false;
        beganSoftDrop = false;
        didSomething = true;
    }

    private void sampleSwipeVerticalTravel(long time) {
        if (swipeSampleTimes.size > 0 && time < swipeSampleTimes.peek()) {
            swipeSampleTimes.clear();
            swipeSampleYs.clear();
            swipeFlickDownward = false;
        }
        if (swipeSampleYs.size > 0 && swipeCoordinates.y != swipeSampleYs.peek())
            swipeFlickDownward = swipeCoordinates.y < swipeSampleYs.peek();
        swipeSampleTimes.add(time);
        swipeSampleYs.add(swipeCoordinates.y);
        // Keep one sample before the window for interpolation.
        while (swipeSampleTimes.size > 2 && swipeSampleTimes.get(1) <= time - FLICK_SAMPLE_WINDOW_NS) {
            swipeSampleTimes.removeIndex(0);
            swipeSampleYs.removeIndex(0);
        }
    }

    private boolean isSwipeDownFlick() {
        float cellSize = getSwipeCellSize();
        if (!app.localPrefs.isSwipeDownHardDrop() || swipeAxis != SwipeAxis.vertical
                || didHardDrop || !swipeFlickDownward || swipeSampleTimes.size < 2
                || swipeStartY - swipeCoordinates.y < FLICK_HARD_DROP_TRAVEL_CELLS * cellSize)
            return false;
        long startTime = swipeSampleTimes.first();
        float startY = swipeSampleYs.first();
        long windowStart = swipeSampleTimes.peek() - FLICK_SAMPLE_WINDOW_NS;
        if (startTime < windowStart) {
            float fraction = (float) (windowStart - startTime) / (swipeSampleTimes.get(1) - startTime);
            startY += (swipeSampleYs.get(1) - startY) * fraction;
            startTime = windowStart;
        }
        float seconds = (swipeSampleTimes.peek() - startTime) / 1000000000f;
        return seconds > 0 && (startY - swipeCoordinates.y) / cellSize / seconds > FLICK_HARD_DROP_CELLS_PER_SECOND;
    }

    private float getSwipeCellSize() {
        return BlockActor.blockWidth / app.localPrefs.getSwipeSensitivity();
    }

    @Override
    public void setGameOver() {
        if (swipeMode)
            clearSwipeInput();
        super.setGameOver();
    }

    @Override
    public void vibrate(VibrationType vibrationType, InputIdentifier fixedInput) {
        if (vibrationEnabled && !app.localPrefs.getVibrationOnlyController() && (fixedInput == null || fixedInput.isSameInput(inputId))) {
            try {
                Gdx.input.vibrate(vibrationType.getVibrationLength());
            } catch (Throwable throwable) {
                // We catch here, just in case. There were some problems reported
                app.gameAnalytics.submitErrorEvent(GameAnalytics.ErrorType.warning,
                        throwable.getMessage());
            }
        }
    }

    private void giveHapticFeedback() {
        if (vibrationEnabled && app.localPrefs.getVibrationHaptic()) {
            vibrate(VibrationType.HAPTIC_FEEDBACK, inputId);
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        } else if (pointer != 0 || button != Input.Buttons.LEFT)
            return false;

        if (swipeMode)
            checkSwipeInput();
        touchDownValid = screenY > Gdx.graphics.getHeight() * SCREEN_BORDER_PERCENTAGE * (isPaused() ? 2 : 1)
                && (!isUsingOnScreenButtons() || isPaused());

        if (!touchDownValid && isUsingOnScreenButtons() && playScreen.showsOverlayMessage())
            playScreen.gameModel.inputRotate(inputId, true);

        if (!touchDownValid)
            return buttonsHidden;

        this.screenX = screenX;
        this.screenY = screenY;
        this.timeSinceTouchDown = 0;
        if (!swipeMode)
            didHardDrop = false;

        if (!isPaused()) {
            if (!flippedMode && !swipeMode)
                playScreen.gameModel.setInputFreezeInterval(.1f);

            if (swipeMode) {
                swipeGameModel = getSwipeGameModel();
                if (swipeGameModel == null) {
                    touchDownValid = false;
                    return false;
                }
                swipeAxis = SwipeAxis.undecided;
                swipeRemainder = 0;
                swipeSampleTimes.clear();
                swipeSampleYs.clear();
                swipeFlickDownward = false;
                beganHorizontalMove = false;
                beganSoftDrop = false;
                swipeNextTetromino = swipeGameModel.getNextTetromino();
                swipeDrawnTetrominos = swipeGameModel.getScore().getDrawnTetrominos();
                swipeLastX = playScreen.getStage().getViewport().unproject(swipeCoordinates.set(screenX, screenY)).x;
                swipeStartY = swipeCoordinates.y;
                if (app.localPrefs.isSwipeDownHardDrop())
                    sampleSwipeVerticalTravel(TimeUtils.nanoTime());
            } else
                setTouchPanel(screenX, screenY);
            didSomething = false;
        } else if (playScreen.gameModel.isGameOver()) {
            playScreen.switchPause(false);
            didSomething = true;
        }

        return true;
    }

    protected boolean isUsingOnScreenButtons() {
        return app.localPrefs.getUsedTouchControls().isOnScreenButtons() && !tutorialMode;
    }

    public Group initializeTouchPanel(LightBlocksGame app, int dragTrashold, Stage stage) {
        if (touchPanel != null)
            touchPanel.remove();

        touchPanel = new Group();
        touchPanel.setTransform(false);
        Vector2 touchCoordinates1;

        touchCoordinates1 = new Vector2(0, 0);
        touchCoordinates = new Vector2(dragTrashold, 0);

        stage.getViewport().unproject(touchCoordinates);
        stage.getViewport().unproject(touchCoordinates1);

        float screenDragTreshold = Math.abs(touchCoordinates.x - touchCoordinates1.x);

        touchPanel.setVisible(false);
        rotationLabel = new Label(FontAwesome.ROTATE_RIGHT, app.skin, FontAwesome.SKIN_FONT_FA);

        float scale = Math.min(1, screenDragTreshold * 1.9f / rotationLabel.getPrefWidth());
        rotationLabel.setFontScale(scale);

        rotationLabel.setPosition(-rotationLabel.getPrefWidth() / 2, -rotationLabel.getPrefHeight() / 2);

        toTheRight = new Label(FontAwesome.CIRCLE_RIGHT, app.skin, FontAwesome.SKIN_FONT_FA);
        toTheRight.setFontScale(scale);
        toTheRight.setPosition(screenDragTreshold, -toTheRight.getPrefHeight() / 2);
        toTheLeft = new Label(FontAwesome.CIRCLE_LEFT, app.skin, FontAwesome.SKIN_FONT_FA);
        toTheLeft.setFontScale(scale);
        toTheLeft.setPosition(-screenDragTreshold - toTheRight.getPrefWidth(), -toTheRight.getPrefHeight() / 2);

        toDrop = new Label(FontAwesome.CIRCLE_DOWN, app.skin, FontAwesome.SKIN_FONT_FA);
        toDrop.setFontScale(scale);
        toDrop.setPosition(-toDrop.getPrefWidth() / 2, -screenDragTreshold - toDrop.getPrefHeight());

        touchPanel.addActor(toTheLeft);
        touchPanel.addActor(toTheRight);
        touchPanel.addActor(toDrop);
        touchPanel.addActor(rotationLabel);

        stage.addActor(touchPanel);

        return touchPanel;
    }


    private void initGestureOnScreenControls() {
        if (gestureOnScreenControls == null) {
            gestureOnScreenControls = new GestureOnScreenButtons();
            gestureOnScreenControls.setVisible(false);
            playScreen.getStage().addActor(gestureOnScreenControls);
        }
        gestureOnScreenControls.resize();
    }

    private void initPortraitOnScreenControls() {
        if (buttonOnScreenControls == null) {
            buttonOnScreenControls = new PortraitOnScreenButtons();
            buttonOnScreenControls.setVisible(false);
            playScreen.getStage().addActor(buttonOnScreenControls);
        }
        buttonOnScreenControls.resize();
    }

    private void initGamepadOnScreenControls() {
        if (gamepadOnScreenControls == null) {
            gamepadOnScreenControls = new OnScreenGamepad(app, this,
                    new TouchpadChangeListener(), new HoldButtonInputListener(),
                    new FreezeButtonInputListener());
            gamepadOnScreenControls.setVisible(false);
            playScreen.getStage().addActor(gamepadOnScreenControls);
        }
        gamepadOnScreenControls.resize(playScreen);
    }

    public PlayScreen getPlayScreen() {
        return playScreen;
    }

    public void hadButtonEvent() {
        hadButtonEvent = true;
        giveHapticFeedback();
    }

    public void setTouchPanel(int screenX, int screenY) {

        if (touchPanel == null || flippedMode)
            return;

        touchCoordinates.set(screenX, screenY);
        playScreen.getStage().getViewport().unproject(touchCoordinates);
        touchPanel.setPosition(touchCoordinates.x, touchCoordinates.y);

        boolean tappedRightScreenHalf = this.screenX >= Gdx.graphics.getWidth() / 2;
        if (tappedRightScreenHalf && !invertRotation || !tappedRightScreenHalf && invertRotation) {
            setTouchPanelColor(rotateRightColor);
            rotationLabel.setText(FontAwesome.ROTATE_RIGHT);
        } else {
            setTouchPanelColor(rotateLeftColor);
            rotationLabel.setText(FontAwesome.ROTATE_LEFT);
        }

        touchPanel.setZIndex(Integer.MAX_VALUE);
        touchPanel.setVisible(true);
    }

    public void setTouchPanelColor(Color c) {
        if (touchPanel == null)
            return;

        toTheRight.setColor(c);
        toTheLeft.setColor(c);
        toDrop.setColor(c);
        rotationLabel.setColor(c);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Bei mehr als dragThreshold Pixeln erkennen wir eine Bewegung an...
        if (pointer != 0 || !touchDownValid)
            return false;
        if (swipeMode && !checkSwipeInput())
            return false;

        if (swipeMode) {
            playScreen.getStage().getViewport().unproject(swipeCoordinates.set(screenX, screenY));
            float cellSize = getSwipeCellSize();
            if (app.localPrefs.isSwipeDownHardDrop())
                sampleSwipeVerticalTravel(TimeUtils.nanoTime());
            if (swipeAxis == SwipeAxis.undecided) {
                float dx = swipeCoordinates.x - swipeLastX;
                float dy = swipeCoordinates.y - swipeStartY;
                float slop = SWIPE_AXIS_SLOP_CELLS * cellSize;
                if (dx * dx + dy * dy <= slop * slop)
                    return true;
                swipeAxis = Math.abs(dy) > Math.abs(dx) * SWIPE_AXIS_BIAS
                        ? SwipeAxis.vertical : SwipeAxis.horizontal;
                didSomething = true;
            }
            if (swipeAxis == SwipeAxis.horizontal) {
                float travel = swipeCoordinates.x - swipeLastX;
                swipeLastX = swipeCoordinates.x;
                if (travel != 0) {
                    int direction = travel < 0 ? -1 : 1;
                    if (swipeRemainder * direction < 0)
                        swipeRemainder = 0;
                    swipeRemainder += travel;
                    while (Math.abs(swipeRemainder) >= cellSize) {
                        swipeMoves.add(direction);
                        swipeRemainder -= direction * cellSize;
                        beganHorizontalMove = true;
                    }
                }
                return true;
            }
            if (didHardDrop)
                return true;
        }

        // Horizontale Bewegung  bemerken - aber nur wenn kein Hard Drop eingeleitet!
        if (!swipeMode && !didHardDrop) {
            if (!flippedMode) {
                int currentHorizontalTreshold = (beganSoftDrop ? 2 * dragThreshold : dragThreshold);
                if ((!beganHorizontalMove) && (Math.abs(screenX - this.screenX) > currentHorizontalTreshold)) {
                    beganHorizontalMove = true;
                    playScreen.gameModel.inputStartMoveHorizontal(inputId, screenX - this.screenX < 0);
                    giveHapticFeedback();
                }
                if ((beganHorizontalMove) && (Math.abs(screenX - this.screenX) < currentHorizontalTreshold)) {
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
                    beganHorizontalMove = false;
                }
            }
            if (flippedMode) {
                if (Math.abs(screenX - this.screenX) > dragThreshold) {
                    playScreen.gameModel.inputRotate(inputId, screenX - this.screenX > 0);
                    touchDownValid = false;
                }
            }
        }

        float downTravel = swipeMode ? swipeStartY - swipeCoordinates.y : screenY - this.screenY;
        float softDropThreshold = swipeMode ? SWIPE_SOFT_DROP_CELLS * getSwipeCellSize() : dragThreshold;
        if (!beganHorizontalMove && downTravel > softDropThreshold && !beganSoftDrop
                && timeSinceTouchDown <= MAX_SOFTDROPBEGINNING_INTERVAL) {
            beganSoftDrop = true;
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_SOFT_DROP);
            giveHapticFeedback();
        }
        // Soft Drop sofort beenden, wenn horizontal bewegt oder wieder hochgezogen
        if ((beganHorizontalMove || downTravel < softDropThreshold) && beganSoftDrop) {
            beganSoftDrop = false;
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
        }

        int swipeUpType = app.localPrefs.getSwipeUpType();
        int swipeUpTresholdFactor = swipeUpType == SWIPEUP_PAUSE ? 4 : 3;
        float swipeUpThreshold = swipeMode ? SWIPE_UP_THRESHOLD_CELLS * getSwipeCellSize()
                : swipeUpTresholdFactor * dragThreshold;
        if (downTravel < -swipeUpThreshold && swipeUpType != SWIPEUP_DONOTHING) {
            if (swipeUpType == SWIPEUP_PAUSE && !isPaused())
                playScreen.switchPause(false);
            else if (swipeUpType == SWIPEUP_HARDDROP && !didHardDrop && !beganHorizontalMove) {
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                giveHapticFeedback();
                didHardDrop = true;
            } else if (swipeUpType == SWIPEUP_HOLD && !beganHorizontalMove) {
                boolean held = playScreen.gameModel.inputHoldActiveTetromino(inputId);
                if (swipeMode && held)
                    clearSwipeInput();
                giveHapticFeedback();
            }
        }

        // rotate vermeiden
        if (!didSomething && (Math.abs(screenX - this.screenX) > dragThreshold
                || Math.abs(screenY - this.screenY) > dragThreshold)) {
            playScreen.gameModel.setInputFreezeInterval(0);
            didSomething = true;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || !touchDownValid || button != Input.Buttons.LEFT)
            return false;
        if (swipeMode && !checkSwipeInput())
            return false;

        if (swipeMode) {
            touchDragged(screenX, screenY, pointer);
            if (!touchDownValid || !checkSwipeInput())
                return true;
            if (isSwipeDownFlick()) {
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                didHardDrop = true;
                giveHapticFeedback();
            }
        }

        playScreen.gameModel.setInputFreezeInterval(0);

        if (touchPanel != null)
            touchPanel.setVisible(false);

        if (!didSomething) {
            if (!flippedMode) {
                boolean tappedRightScreenHalf = screenX >= Gdx.graphics.getWidth() / 2;
                playScreen.gameModel.inputRotate(inputId, tappedRightScreenHalf && !invertRotation
                        || !tappedRightScreenHalf && invertRotation);
                giveHapticFeedback();
            } else
                playScreen.gameModel.inputDoOneHorizontalMove(inputId, screenX <= Gdx.graphics.getWidth() / 2);

        } else if (!swipeMode || !didHardDrop)
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);

        if (beganHorizontalMove) {
            if (!swipeMode) {
                playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
                playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
            }
            beganHorizontalMove = false;
        }

        if (swipeMode) {
            touchDownValid = false;
            beganSoftDrop = false;
            swipeAxis = SwipeAxis.undecided;
        }
        return true;
    }

    @Override
    public String getAnalyticsKey() {
        return INPUT_KEY_GESTURES;
    }

    @Override
    public int getRequestedGameboardAlignment() {
        if (isUsingOnScreenButtons() && gamepadOnScreenControls != null)
            return Align.top;
        else if (isUsingOnScreenButtons())
            return Align.center;

        return super.getRequestedGameboardAlignment();
    }

    void setButtonsHidden(boolean buttonsHidden) {
        this.buttonsHidden = buttonsHidden;
    }

    private enum SwipeAxis {
        undecided, horizontal, vertical
    }

    public static class FreezeButton extends Button {
        public FreezeButton(LightBlocksGame app, String label, InputListener inputListener) {
            super(app.skin, LightBlocksGame.SKIN_BUTTON_SMOKE);
            Label buttonLabel = new Label(label, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            buttonLabel.setAlignment(Align.top);
            buttonLabel.setFontScale(.8f);
            add(buttonLabel).fill().expand();
            buttonLabel.setColor(app.theme.buttonColor);

            setRotation(270);
            setTransform(true);
            addListener(inputListener);
        }

        public void resize(OnScreenGamepad.IOnScreenButtonsScreen screen, float padding) {
            float gameboardWidth = BlockActor.blockWidth * Gameboard.GAMEBOARD_COLUMNS;

            setWidth(BlockActor.blockWidth * Tetromino.TETROMINO_BLOCKCOUNT);
            setX(screen.getStage().getWidth() / 2 + gameboardWidth / 2 + 15);
            setY(screen.getGameboardTop() - getWidth() - padding);
            setHeight(screen.getStage().getWidth() - padding - getX());

        }
    }

    public static class HoldButton extends Button {
        public HoldButton(LightBlocksGame app, PlayScreen playScreen, InputListener inputListener) {
            super(app.skin, LightBlocksGame.SKIN_BUTTON_SMOKE);
            Label buttonLabel = new Label(app.TEXTS.get("labelHold").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            buttonLabel.setAlignment(Align.top);
            buttonLabel.setFontScale(.8f);
            add(buttonLabel).fill().expand();
            buttonLabel.setColor(app.theme.buttonColor);

            setRotation(270);
            setTransform(true);

            if (playScreen != null) {
                setVisible(playScreen.gameModel.isHoldMoveAllowedByModel());
            }
            addListener(inputListener);
        }

        public void resize(OnScreenGamepad.IOnScreenButtonsScreen screen, float padding) {
            float gameboardWidth = BlockActor.blockWidth * Gameboard.GAMEBOARD_COLUMNS;

            setX(screen.getStage().getWidth() / 2 + gameboardWidth / 2 + 15);
            setY(screen.getGameboardTop());
            setHeight(screen.getStage().getWidth() - padding - getX());
            setWidth(BlockActor.blockWidth * Tetromino.TETROMINO_BLOCKCOUNT);

        }

    }

    private class TouchpadChangeListener extends ChangeListener {
        boolean upPressed;
        boolean downPressed;
        boolean rightPressed;
        boolean leftPressed;

        final float deadRadius;

        public TouchpadChangeListener() {
            deadRadius = app.localPrefs.isShowDpadOnScreenGamepad() ? TOUCHPAD_DPAD_DEAD_RADIUS : TOUCHPAD_STICK_DEAD_RADIUS;
        }

        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
            if (!(actor instanceof Touchpad))
                return;

            Touchpad touchpad = (Touchpad) actor;

            boolean upNowPressed = touchpad.getKnobPercentY() > deadRadius * 1.8f;
            boolean downNowPressed = touchpad.getKnobPercentY() < -deadRadius;
            boolean rightNowPressed = touchpad.getKnobPercentX() > deadRadius;
            boolean leftNowPressed = touchpad.getKnobPercentX() < -deadRadius;

            // zwei Richtungen gleichzeitig: entscheiden welcher wichtiger ist
            if ((upNowPressed || downNowPressed) && (leftNowPressed || rightNowPressed)) {
                if (Math.abs(touchpad.getKnobPercentY()) >= Math.abs(touchpad.getKnobPercentX())) {
                    rightNowPressed = false;
                    leftNowPressed = false;
                } else {
                    upNowPressed = false;
                    downNowPressed = false;
                }

            }

            if (upPressed != upNowPressed) {
                upPressed = upNowPressed;

                // no dedicated hard drop button makes the up button work as hard drop
                if (!app.localPrefs.isShowHardDropButtonOnScreenGamepad()) {
                    if (upPressed) {
                        hadButtonEvent();
                        playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                    } else
                        playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
                }

            }

            if (downPressed != downNowPressed) {
                downPressed = downNowPressed;
                if (downPressed) {
                    hadButtonEvent();
                    playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_SOFT_DROP);
                } else
                    playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
            }

            if (rightPressed != rightNowPressed) {
                rightPressed = rightNowPressed;
                if (rightPressed) {
                    hadButtonEvent();
                    playScreen.gameModel.inputStartMoveHorizontal(inputId, false);
                } else
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
            }

            if (leftPressed != leftNowPressed) {
                leftPressed = leftNowPressed;
                if (leftPressed) {
                    hadButtonEvent();
                    playScreen.gameModel.inputStartMoveHorizontal(inputId, true);
                } else
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
            }

        }
    }

    private class PortraitOnScreenButtons extends Group {
        private static final int PADDING = 8;
        private final PortraitButton rotateRight;
        private final PortraitButton rotateLeft;
        private final PortraitButton moveRight;
        private final PortraitButton moveLeft;
        private final HoldButton holdButton;
        private final FreezeButton hardDropButton;
        private final Actor rotateRightArea;
        private final Actor rotateLeftArea;
        private final Actor moveRightArea;
        private final Actor moveLeftArea;
        private boolean rightPressed = false;
        private boolean leftPressed = false;
        private boolean didSoftDrop = false;

        public PortraitOnScreenButtons() {
            rotateRight = new PortraitButton(FontAwesome.ROTATE_RIGHT, Align.top);
            InputListener rotateRightListener = new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    rightPressed = true;
                    rotateRight.isPressed = true;
                    checkSoftDrop();
                    hadButtonEvent = true;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    rightPressed = false;
                    rotateRight.isPressed = false;
                    playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
                    if (!didSoftDrop) {
                        playScreen.gameModel.inputRotate(inputId, true);
                        giveHapticFeedback();
                    }
                }
            };
            rotateRight.addListener(rotateRightListener);
            rotateRightArea = new Actor();
            rotateRightArea.addListener(rotateRightListener);
            addActor(rotateRightArea);
            addActor(rotateRight);

            rotateLeft = new PortraitButton(FontAwesome.ROTATE_LEFT, Align.top);
            InputListener rotateLeftListener = new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    leftPressed = true;
                    rotateLeft.isPressed = true;
                    checkSoftDrop();
                    hadButtonEvent = true;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    leftPressed = false;
                    rotateLeft.isPressed = false;
                    playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
                    if (!didSoftDrop) {
                        playScreen.gameModel.inputRotate(inputId, false);
                        giveHapticFeedback();
                    }
                }
            };
            rotateLeft.addListener(rotateLeftListener);
            addActor(rotateLeft);
            rotateLeftArea = new Actor();
            rotateLeftArea.addListener(rotateLeftListener);
            addActor(rotateLeftArea);

            moveRight = new PortraitButton(FontAwesome.RIGHT_CHEVRON, Align.bottom);
            InputListener moveRightListener = new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    moveRight.isPressed = true;
                    playScreen.gameModel.inputStartMoveHorizontal(inputId, false);
                    hadButtonEvent();
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    moveRight.isPressed = false;
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
                }
            };
            moveRight.addListener(moveRightListener);
            addActor(moveRight);
            moveRightArea = new Actor();
            moveRightArea.addListener(moveRightListener);
            addActor(moveRightArea);

            moveLeft = new PortraitButton(FontAwesome.LEFT_CHEVRON, Align.bottom);
            InputListener moveLeftListener = new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    moveLeft.isPressed = true;
                    playScreen.gameModel.inputStartMoveHorizontal(inputId, true);
                    hadButtonEvent();
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    moveLeft.isPressed = false;
                    playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
                }
            };
            moveLeft.addListener(moveLeftListener);
            addActor(moveLeft);
            moveLeftArea = new Actor();
            moveLeftArea.addListener(moveLeftListener);
            addActor(moveLeftArea);

            holdButton = new HoldButton(app, playScreen, new HoldButtonInputListener());
            addActor(holdButton);

            // FreezeButton is an accurate fit for the Hard Drop Button
            hardDropButton = new FreezeButton(app, "DROP", new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                    return true;
                }
            });
            hardDropButton.setRotation(90);

            addActor(hardDropButton);
        }

        private void checkSoftDrop() {
            didSoftDrop = rightPressed && leftPressed;
            if (didSoftDrop) {
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_SOFT_DROP);
                giveHapticFeedback();
            }
        }


        public void resize() {
            float gameboardWidth = BlockActor.blockWidth * Gameboard.GAMEBOARD_COLUMNS;
            holdButton.resize(playScreen, PADDING);

            rotateRight.setPosition(playScreen.getStage().getWidth() / 2 + gameboardWidth / 2 + 2 * PADDING,
                    PADDING * 3 + playScreen.getCenterPosY());
            rotateRight.setSize(playScreen.getStage().getWidth() - rotateRight.getX() - PADDING,
                    playScreen.getStage().getHeight() * .4f - rotateRight.getY() - PADDING);
            rotateRightArea.setPosition(playScreen.getStage().getWidth() / 2, rotateRight.getY());
            rotateRightArea.setSize(playScreen.getStage().getWidth() / 2, rotateRight.getHeight());

            rotateLeft.setPosition(PADDING, rotateRight.getY());
            rotateLeft.setSize(rotateRight.getWidth(), rotateRight.getHeight());
            rotateLeftArea.setPosition(0, rotateLeft.getY());
            rotateLeftArea.setSize(rotateRightArea.getX(), rotateLeft.getHeight());

            moveRight.setPosition(rotateRight.getX(), rotateRight.getY() + rotateRight.getHeight() + PADDING);
            moveRight.setSize(rotateRight.getWidth(),
                    holdButton.getY() - holdButton.getWidth() - moveRight.getY() - PADDING);
            moveRightArea.setPosition(rotateRightArea.getX(), moveRight.getY());
            moveRightArea.setSize(rotateRightArea.getWidth(), moveRight.getHeight());

            moveLeft.setPosition(rotateLeft.getX(), moveRight.getY());
            moveLeft.setSize(moveRight.getWidth(), moveRight.getHeight());
            moveLeftArea.setPosition(rotateLeftArea.getX(), moveLeft.getY());
            moveLeftArea.setSize(rotateLeftArea.getWidth(), moveLeft.getHeight());

            hardDropButton.setVisible(!playScreen.showsPauseButton());
            hardDropButton.resize(playScreen, PADDING);
            // resize() sets the position for a freeze button, this needs to be changed
            hardDropButton.setPosition(PADDING + hardDropButton.getHeight(), holdButton.getY() - hardDropButton.getWidth());
        }

        private class PortraitButton extends Button {
            boolean isPressed;

            public PortraitButton(String label, int alignment) {
                super(app.skin, LightBlocksGame.SKIN_BUTTON_SMOKE);
                Label buttonLabel = new Label(label, app.skin, FontAwesome.SKIN_FONT_FA);
                buttonLabel.setAlignment(alignment);
                buttonLabel.setFontScale(.8f);
                buttonLabel.setColor(app.theme.buttonColor);
                add(buttonLabel).fill().expand();
            }

            @Override
            public boolean isPressed() {
                return isPressed || super.isPressed();
            }
        }
    }

    private class GestureOnScreenButtons extends Group {
        private static final int PADDING = 8;
        private final HoldButton holdButton;
        private FreezeButton freezeButton;

        public GestureOnScreenButtons() {
            holdButton = new HoldButton(app, playScreen, new HoldButtonInputListener());
            if (app.localPrefs.isShowTouchHoldButton())
                addActor(holdButton);

            String freezeButtonLabel = playScreen.gameModel.getShownTimeButtonDescription(app.TEXTS);
            if (freezeButtonLabel != null) {
                freezeButton = new FreezeButton(app, freezeButtonLabel,
                        new FreezeButtonInputListener());
                addActor(freezeButton);
            }
        }

        public void resize() {
            holdButton.resize(playScreen, PADDING);
            if (freezeButton != null)
                freezeButton.resize(playScreen, PADDING);
        }

    }

    private class FreezeButtonInputListener extends InputListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!isPaused()) {
                if (swipeMode)
                    clearSwipeInput();
                playScreen.gameModel.inputTimelabelTouched(inputId);
                hadButtonEvent();
                return true;
            }
            return false;
        }
    }

    private class HoldButtonInputListener extends InputListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!isPaused()) {
                if (swipeMode)
                    clearSwipeInput();
                playScreen.gameModel.inputHoldActiveTetromino(inputId);
                hadButtonEvent();
                return true;
            }
            return false;
        }
    }

}
