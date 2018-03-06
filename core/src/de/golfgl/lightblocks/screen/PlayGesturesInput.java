package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {

    public Color rotateRightColor = new Color(.1f, 1, .3f, .8f);
    public Color rotateLeftColor = new Color(.2f, .8f, 1, .8f);
    int screenX;
    int screenY;
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

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get("inputGesturesHelp");
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);

        dragThreshold = playScreen.app.getTouchPanelSize();

        if (playScreen.app.getShowTouchPanel())
            initializeTouchPanel(playScreen, dragThreshold);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        }

        this.screenX = screenX;
        this.screenY = screenY;

        if (isPaused) {
            playScreen.switchPause(false);
            didSomething = true;
        } else {
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

        if (touchPanel == null)
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

        if (!beganSoftDrop) {
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

        if (!beganHorizontalMove) {
            if (screenY - this.screenY > dragThreshold && !beganSoftDrop) {
                beganSoftDrop = true;
                playScreen.gameModel.setSoftDropFactor(1);
            }
            if (screenY - this.screenY < dragThreshold && beganSoftDrop) {
                beganSoftDrop = false;
                playScreen.gameModel.setSoftDropFactor(0);
            }
        }

        if (screenY - this.screenY < -4 * dragThreshold & !isPaused && playScreen.app.getPauseSwipeEnabled()) {
            playScreen.switchPause(false);
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

        playScreen.gameModel.setInputFreezeInterval(0);

        if (touchPanel != null)
            touchPanel.setVisible(false);

        if (!didSomething)
            playScreen.gameModel.setRotate(screenX >= Gdx.graphics.getWidth() / 2);

        else
            playScreen.gameModel.setSoftDropFactor(0);

        if (beganHorizontalMove) {
            playScreen.gameModel.endMoveHorizontal(true);
            playScreen.gameModel.endMoveHorizontal(false);
            beganHorizontalMove = false;
        }

        return true;
    }
}
