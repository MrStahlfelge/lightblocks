package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.TutorialModel;

/**
 * Jegliche Touchscreen-Kontrollen verarbeiten
 * <p>
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {
    public static final int SWIPEUP_DONOTHING = 0;
    public static final int SWIPEUP_PAUSE = 1;
    public static final int SWIPEUP_HARDDROP = 2;
    private static final float TOUCHPAD_DEAD_RADIUS = .33f;

    private static final float SCREEN_BORDER_PERCENTAGE = 0.1f;
    // Flipped Mode: Tap löst horizontale Bewegung aus, Swipe rotiert
    private static final boolean flippedMode = false;

    public Color rotateRightColor = new Color(.1f, 1, .3f, .8f);
    public Color rotateLeftColor = new Color(.2f, .8f, 1, .8f);
    int screenX;
    int screenY;
    boolean touchDownValid = false;
    boolean beganHorizontalMove;
    boolean beganSoftDrop;
    boolean didSomething;
    Group touchPanel;
    Vector2 touchCoordinates;
    private int dragThreshold;
    private Label toTheRight;
    private Label toTheLeft;
    private Label toDrop;
    private Label rotationLabel;
    private boolean didDrop;

    private Group onScreenControls;
    private Touchpad touchpad;
    private Button rotateRightButton;
    private Button rotateLeftButton;
    private Button hardDropButton;
    private boolean tutorialMode;

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get("inputGesturesHelp");
    }

    @Override
    public String getTutorialContinueText() {
        return playScreen.app.TEXTS.get("tutorialContinueGestures");
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);

        dragThreshold = playScreen.app.localPrefs.getTouchPanelSize();
        tutorialMode = playScreen.gameModel instanceof TutorialModel;

        if (playScreen.app.localPrefs.getShowTouchPanel() || tutorialMode)
            initializeTouchPanel(playScreen, dragThreshold);

        if (playScreen.app.localPrefs.useOnScreenControlsInLandscape() && !tutorialMode)
            initializeOnScreenControls(playScreen);

    }

    @Override
    public void doPoll(float delta) {
        if (onScreenControls != null)
            onScreenControls.setVisible(playScreen.isLandscape() && !isPaused());
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        } else if (pointer != 0 || button != Input.Buttons.LEFT)
            return false;

        touchDownValid = screenY > Gdx.graphics.getHeight() * SCREEN_BORDER_PERCENTAGE * (isPaused() ? 2 : 1)
                && !(onScreenControls != null && onScreenControls.isVisible());

        if (!touchDownValid)
            return false;

        this.screenX = screenX;
        this.screenY = screenY;
        didDrop = false;

        if (isPaused()) {
            playScreen.switchPause(false);
            didSomething = true;
        } else {
            if (!flippedMode)
                playScreen.gameModel.setInputFreezeInterval(.1f);

            setTouchPanel(screenX, screenY);
            didSomething = false;
        }

        return true;
    }

    public Group initializeTouchPanel(AbstractScreen playScreen, int dragTrashold) {
        if (touchPanel != null)
            touchPanel.remove();

        touchPanel = new Group();
        touchPanel.setTransform(false);
        Vector2 touchCoordinates1;

        touchCoordinates1 = new Vector2(0, 0);
        touchCoordinates = new Vector2(dragTrashold, 0);

        playScreen.stage.getViewport().unproject(touchCoordinates);
        playScreen.stage.getViewport().unproject(touchCoordinates1);

        float screenDragTreshold = Math.abs(touchCoordinates.x - touchCoordinates1.x);

        touchPanel.setVisible(false);
        rotationLabel = new Label(FontAwesome.ROTATE_RIGHT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);

        float scale = Math.min(1, screenDragTreshold * 1.9f / rotationLabel.getPrefWidth());
        rotationLabel.setFontScale(scale);

        rotationLabel.setPosition(-rotationLabel.getPrefWidth() / 2, -rotationLabel.getPrefHeight() / 2);

        toTheRight = new Label(FontAwesome.CIRCLE_RIGHT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toTheRight.setFontScale(scale);
        toTheRight.setPosition(screenDragTreshold, -toTheRight.getPrefHeight() / 2);
        toTheLeft = new Label(FontAwesome.CIRCLE_LEFT, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toTheLeft.setFontScale(scale);
        toTheLeft.setPosition(-screenDragTreshold - toTheRight.getPrefWidth(), -toTheRight.getPrefHeight() / 2);

        toDrop = new Label(FontAwesome.CIRCLE_DOWN, playScreen.app.skin, FontAwesome.SKIN_FONT_FA);
        toDrop.setFontScale(scale);
        toDrop.setPosition(-toDrop.getPrefWidth() / 2, -screenDragTreshold - toDrop.getPrefHeight());

        touchPanel.addActor(toTheLeft);
        touchPanel.addActor(toTheRight);
        touchPanel.addActor(toDrop);
        touchPanel.addActor(rotationLabel);

        playScreen.stage.addActor(touchPanel);

        return touchPanel;
    }

    private void initializeOnScreenControls(final PlayScreen playScreen) {
        if (onScreenControls == null) {
            onScreenControls = new Group();

            touchpad = new Touchpad(0, playScreen.app.skin);
            touchpad.addListener(new TouchpadChangeListener());
            onScreenControls.addActor(touchpad);

            rotateRightButton = new ImageButton(playScreen.app.skin, "rotateright");
            rotateRightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setRotate(true);
                }
            });
            onScreenControls.addActor(rotateRightButton);

            rotateLeftButton = new ImageButton(playScreen.app.skin, "rotateleft");
            rotateLeftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setRotate(false);
                }
            });
            onScreenControls.addActor(rotateLeftButton);

            hardDropButton = new ImageButton(playScreen.app.skin, "harddrop");
            hardDropButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                }
            });
            onScreenControls.addActor(hardDropButton);

            onScreenControls.setVisible(false);
            playScreen.stage.addActor(onScreenControls);
        }
        float size = Math.min(playScreen.stage.getHeight() * .5f, playScreen.centerGroup.getX());
        touchpad.setSize(size, size);
        touchpad.setPosition(0, 0);
        rotateRightButton.setSize(size * .4f, size * .4f);
        rotateLeftButton.setSize(size * .4f, size * .4f);
        hardDropButton.setSize(size * .4f, size * .4f);
        rotateRightButton.setPosition(playScreen.stage.getWidth() - size * .5f, size - rotateRightButton.getHeight());
        rotateLeftButton.setPosition(rotateRightButton.getX() - size * .45f, (rotateRightButton.getY() -
                rotateRightButton.getHeight()) / 2);
        hardDropButton.setPosition(rotateRightButton.getX() - size * .55f, rotateRightButton.getY());
    }

    public void setTouchPanel(int screenX, int screenY) {

        if (touchPanel == null || flippedMode)
            return;

        touchCoordinates.set(screenX, screenY);
        playScreen.stage.getViewport().unproject(touchCoordinates);
        touchPanel.setPosition(touchCoordinates.x, touchCoordinates.y);

        if (this.screenX >= Gdx.graphics.getWidth() / 2) {
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

        // Horizontale Bewegung  bemerken - aber nur wenn kein Soft Drop/Hard Drop eingeleitet!
        if (!beganSoftDrop && !didDrop) {
            if (!flippedMode) {
                if ((!beganHorizontalMove) && (Math.abs(screenX - this.screenX) > dragThreshold)) {
                    beganHorizontalMove = true;
                    playScreen.gameModel.startMoveHorizontal(screenX - this.screenX < 0);
                }
                if ((beganHorizontalMove) && (Math.abs(screenX - this.screenX) < dragThreshold)) {
                    playScreen.gameModel.endMoveHorizontal(true);
                    playScreen.gameModel.endMoveHorizontal(false);
                    beganHorizontalMove = false;
                }
            }
            if (flippedMode) {
                if (Math.abs(screenX - this.screenX) > dragThreshold) {
                    playScreen.gameModel.setRotate(screenX - this.screenX > 0);
                    touchDownValid = false;
                }
            }
        }

        if (!beganHorizontalMove) {
            if (screenY - this.screenY > dragThreshold && !beganSoftDrop) {
                beganSoftDrop = true;
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
            }
            if (screenY - this.screenY < dragThreshold && beganSoftDrop) {
                beganSoftDrop = false;
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
            }
        }

        int swipeUpType = playScreen.app.localPrefs.getSwipeUpType();
        int swipeUpTresholdFactor = swipeUpType == SWIPEUP_HARDDROP ? 3 : 4;
        if (screenY - this.screenY < -swipeUpTresholdFactor * dragThreshold && swipeUpType != SWIPEUP_DONOTHING) {
            if (swipeUpType == SWIPEUP_PAUSE && !isPaused())
                playScreen.switchPause(false);
            else if (swipeUpType == SWIPEUP_HARDDROP && !didDrop && !beganHorizontalMove) {
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                didDrop = true;
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
            if (!flippedMode)
                playScreen.gameModel.setRotate(screenX >= Gdx.graphics.getWidth() / 2);
            else
                playScreen.gameModel.doOneHorizontalMove(screenX <= Gdx.graphics.getWidth() / 2);

        } else
            playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);

        if (beganHorizontalMove) {
            playScreen.gameModel.endMoveHorizontal(true);
            playScreen.gameModel.endMoveHorizontal(false);
            beganHorizontalMove = false;
        }

        return true;
    }

    @Override
    public String getAnalyticsKey() {
        return "gestures";
    }

    private class TouchpadChangeListener extends ChangeListener {
        boolean upPressed;
        boolean downPressed;
        boolean rightPressed;
        boolean leftPressed;

        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
            boolean upNowPressed = touchpad.getKnobPercentY() > TOUCHPAD_DEAD_RADIUS;
            boolean downNowPressed = touchpad.getKnobPercentY() < -TOUCHPAD_DEAD_RADIUS;
            boolean rightNowPressed = touchpad.getKnobPercentX() > TOUCHPAD_DEAD_RADIUS;
            boolean leftNowPressed = touchpad.getKnobPercentX() < -TOUCHPAD_DEAD_RADIUS;

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

                // nix zu tun
            }

            if (downPressed != downNowPressed) {
                downPressed = downNowPressed;
                if (downPressed)
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
                else
                    playScreen.gameModel.setSoftDropFactor(0);
            }

            if (rightPressed != rightNowPressed) {
                rightPressed = rightNowPressed;
                if (rightPressed)
                    playScreen.gameModel.startMoveHorizontal(false);
                else
                    playScreen.gameModel.endMoveHorizontal(false);
            }

            if (leftPressed != leftNowPressed) {
                leftPressed = leftNowPressed;
                if (leftPressed)
                    playScreen.gameModel.startMoveHorizontal(true);
                else
                    playScreen.gameModel.endMoveHorizontal(true);
            }

        }
    }
}
