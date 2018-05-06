package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import de.golfgl.lightblocks.model.GameModel;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {
    public static final int SWIPEUP_DONOTHING = 0;
    public static final int SWIPEUP_PAUSE = 1;
    public static final int SWIPEUP_HARDDROP = 2;

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

        if (playScreen.app.localPrefs.getShowTouchPanel())
            initializeTouchPanel(playScreen, dragThreshold);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        } else if (pointer != 0 || button != Input.Buttons.LEFT)
            return false;

        touchDownValid = screenY > Gdx.graphics.getHeight() * SCREEN_BORDER_PERCENTAGE * (isPaused() ? 2 : 1);

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

        if (!beganSoftDrop) {
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
        if (screenY - this.screenY < -4 * dragThreshold && swipeUpType != SWIPEUP_DONOTHING) {
            if (swipeUpType == SWIPEUP_PAUSE && !isPaused())
                playScreen.switchPause(false);
            else if (swipeUpType == SWIPEUP_HARDDROP && !didDrop) {
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
}
