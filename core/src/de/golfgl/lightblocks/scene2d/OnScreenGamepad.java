package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayScreen;

public class OnScreenGamepad extends Group {
    private final Touchpad touchpad;
    private final Button rotateRightButton;
    private final Button rotateLeftButton;
    private final Button hardDropButton;
    private final TextButton holdButton;
    private TextButton freezeButton;

    public OnScreenGamepad(LightBlocksGame app, final PlayScreen playScreen,
                           EventListener touchPadListener, InputListener holdInputListener,
                           InputListener freezeButtonInputListener) {
        touchpad = new Touchpad(0, app.skin);
        touchpad.addListener(touchPadListener != null ? touchPadListener : new MoveDragListener());
        addActor(touchpad);

        rotateRightButton = new ImageButton(app.skin, "rotateright");
        addActor(rotateRightButton);

        rotateLeftButton = new ImageButton(app.skin, "rotateleft");
        addActor(rotateLeftButton);

        hardDropButton = new ImageButton(app.skin, "harddrop");
        addActor(hardDropButton);

        holdButton = new TextButton("HOLD", app.skin, LightBlocksGame.SKIN_BUTTON_GAMEPAD);
        addActor(holdButton);

        if (playScreen != null) {
            holdButton.addListener(holdInputListener);
            setVisible(playScreen.gameModel.isHoldMoveAllowedByModel());

            rotateRightButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.setRotate(true);
                    return true;
                }
            });
            rotateLeftButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.setRotate(false);
                    return true;
                }
            });
            hardDropButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
                }
            });

            String freezeButtonLabel = playScreen.gameModel.getShownTimeButtonDescription();
            if (freezeButtonLabel != null) {
                freezeButton = new TextButton(freezeButtonLabel, app.skin, LightBlocksGame.SKIN_BUTTON_GAMEPAD);
                freezeButton.addListener(freezeButtonInputListener);
            }

        } else {
            freezeButton = new TextButton("FREEZE", app.skin, LightBlocksGame.SKIN_BUTTON_GAMEPAD);
            rotateLeftButton.addListener(new MoveDragListener());
            rotateRightButton.addListener(new MoveDragListener());
            hardDropButton.addListener(new MoveDragListener());
            holdButton.addListener(new MoveDragListener());
            freezeButton.addListener(new MoveDragListener());
        }
        if (freezeButton != null)
            addActor(freezeButton);
    }

    public void resize(IOnScreenButtonsScreen screen) {
        float size = MathUtils.clamp(screen.getCenterPosX(), LightBlocksGame.nativeGameWidth * .45f, screen.getStage().getHeight() * .5f);
        float fontScale = size * .002f;
        touchpad.setSize(size, size);
        touchpad.setPosition(0, 0);
        rotateRightButton.setSize(size * .4f, size * .4f);
        rotateLeftButton.setSize(size * .4f, size * .4f);
        hardDropButton.setSize(size * .4f, size * .4f);
        holdButton.setSize(size * .4f, size * .4f);
        holdButton.getLabel().setFontScale(fontScale);
        rotateRightButton.setPosition(screen.getStage().getWidth() - size * .5f, size - rotateRightButton
                .getHeight());
        rotateLeftButton.setPosition(rotateRightButton.getX() - size * .45f, (rotateRightButton.getY() -
                rotateRightButton.getHeight()) / 2);
        hardDropButton.setPosition(rotateRightButton.getX() - size * .55f, rotateRightButton.getY());
        holdButton.setPosition(rotateRightButton.getX(), rotateRightButton.getY() + .5f * size);

        if (freezeButton != null) {
            freezeButton.setSize(size * .4f, size * .4f);
            freezeButton.getLabel().setFontScale(fontScale);
            freezeButton.setPosition(hardDropButton.getX(), holdButton.getY());
        }
    }

    public interface IOnScreenButtonsScreen {
        MyStage getStage();

        float getCenterPosX();

        float getGameboardTop();
    }


    private class MoveDragListener extends DragListener {

        private float dragStartX;
        private float dragStartY;

        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            dragStartX = x;
            dragStartY = y;
        }

        @Override
        public void drag(InputEvent event, float x, float y, int pointer) {
            float moveX = x - dragStartX;
            float moveY = y - dragStartY;

            event.getListenerActor().setX(event.getListenerActor().getX() + moveX);
            event.getListenerActor().setY(event.getListenerActor().getY() + moveY);
        }
    }
}
