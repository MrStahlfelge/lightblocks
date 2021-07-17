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
 * This class controls touch input for gestures, on screen gamepad and on screen buttons
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
        super.setPlayScreen(playScreen, app);

        dragThreshold = app.localPrefs.getTouchPanelSize(app.getDisplayDensityRatio());
        tutorialMode = playScreen.gameModel instanceof TutorialModel;
        invertRotation = !tutorialMode && app.localPrefs.isInvertGesturesRotation();

        if (app.localPrefs.getShowTouchPanel() || tutorialMode)
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

        // return hadButtonEvent to PlayKeyOrTouchInput so it can count all touch events
        if (hadButtonEvent) {
            hadButtonEvent = false;
            return true;
        }
        return false;
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

        touchDownValid = screenY > Gdx.graphics.getHeight() * SCREEN_BORDER_PERCENTAGE * (isPaused() ? 2 : 1)
                && (!isUsingOnScreenButtons() || isPaused());

        if (!touchDownValid && isUsingOnScreenButtons() && playScreen.showsOverlayMessage())
            playScreen.gameModel.inputRotate(inputId, true);

        if (!touchDownValid)
            return buttonsHidden;

        this.screenX = screenX;
        this.screenY = screenY;
        this.timeSinceTouchDown = 0;
        didHardDrop = false;

        if (!isPaused()) {
            if (!flippedMode)
                playScreen.gameModel.setInputFreezeInterval(.1f);

            setTouchPanel(screenX, screenY);
            didSomething = false;
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

        // Horizontale Bewegung  bemerken - aber nur wenn kein Hard Drop eingeleitet!
        if (!didHardDrop) {
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

        if (!beganHorizontalMove && screenY - this.screenY > dragThreshold && !beganSoftDrop
                && timeSinceTouchDown <= MAX_SOFTDROPBEGINNING_INTERVAL) {
            beganSoftDrop = true;
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_SOFT_DROP);
            giveHapticFeedback();
        }
        // Soft Drop sofort beenden, wenn horizontal bewegt oder wieder hochgezogen
        if ((beganHorizontalMove || screenY - this.screenY < dragThreshold) && beganSoftDrop) {
            beganSoftDrop = false;
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
        }

        int swipeUpType = app.localPrefs.getSwipeUpType();
        int swipeUpTresholdFactor = swipeUpType == SWIPEUP_PAUSE ? 4 : 3;
        if (screenY - this.screenY < -swipeUpTresholdFactor * dragThreshold && swipeUpType != SWIPEUP_DONOTHING) {
            if (swipeUpType == SWIPEUP_PAUSE && !isPaused())
                playScreen.switchPause(false);
            else if (swipeUpType == SWIPEUP_HARDDROP && !didHardDrop && !beganHorizontalMove) {
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                giveHapticFeedback();
                didHardDrop = true;
            } else if (swipeUpType == SWIPEUP_HOLD && !beganHorizontalMove) {
                playScreen.gameModel.inputHoldActiveTetromino(inputId);
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

        } else
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);

        if (beganHorizontalMove) {
            playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
            playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
            beganHorizontalMove = false;
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
                playScreen.gameModel.inputHoldActiveTetromino(inputId);
                hadButtonEvent();
                return true;
            }
            return false;
        }
    }

}
