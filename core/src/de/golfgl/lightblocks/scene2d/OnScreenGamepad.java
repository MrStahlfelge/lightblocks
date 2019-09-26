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
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.OnScreenGamepadConfig;

public class OnScreenGamepad extends Group {
    private final Touchpad touchpad;
    private final LightBlocksGame app;
    private final Button rotateRightButton;
    private final Button rotateLeftButton;
    private final Button hardDropButton;
    private final TextButton holdButton;
    boolean isLandscapeConfig;
    private TextButton freezeButton;
    private OnScreenGamepadConfig config;

    public OnScreenGamepad(LightBlocksGame app, final PlayScreen playScreen,
                           EventListener touchPadListener, InputListener holdInputListener,
                           InputListener freezeButtonInputListener) {
        touchpad = new Touchpad(0, app.skin);
        this.app = app;
        touchpad.addListener(touchPadListener != null ? touchPadListener : new MoveDragListener() {
            @Override
            protected void savePosDelta(int deltaX, int deltaY) {
                config.touchpadX = config.touchpadX + deltaX;
                config.touchpadY = config.touchpadY + deltaY;
            }
        });
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
            rotateLeftButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.rlX += deltaX;
                    config.rlY += deltaY;
                }
            });
            rotateRightButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.rrX += deltaX;
                    config.rlY += deltaY;
                }
            });
            hardDropButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.dropX += deltaX;
                    config.dropY += deltaY;
                }
            });
            holdButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.holdX += deltaX;
                    config.holdY += deltaY;
                }
            });
            freezeButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.frzX += deltaX;
                    config.frzY += deltaY;
                }
            });
        }
        if (freezeButton != null)
            addActor(freezeButton);
    }

    public void resize(IOnScreenButtonsScreen screen) {
        isLandscapeConfig = screen.isLandscape();
        config = isLandscapeConfig ? app.localPrefs.getGamepadConfigLandscape() : app.localPrefs.getGamepadConfigPortrait();

        float size = MathUtils.clamp(screen.getCenterPosX(), LightBlocksGame.nativeGameWidth * .45f, screen.getStage().getHeight() * .5f);
        float fontScale = size * .002f;
        touchpad.setSize(size, size);
        touchpad.setPosition(0 + config.touchpadX, 0 + config.touchpadY);
        float buttonSize = size * .4f;
        rotateRightButton.setSize(buttonSize, buttonSize);
        rotateLeftButton.setSize(buttonSize, buttonSize);
        hardDropButton.setSize(buttonSize, buttonSize);
        holdButton.setSize(buttonSize, buttonSize);
        holdButton.getLabel().setFontScale(fontScale);

        float rrDefaultX = screen.getStage().getWidth() - size * .5f;
        float rrDefaultY = size - buttonSize;
        rotateRightButton.setPosition(rrDefaultX + config.rrX,
                rrDefaultY + config.rrY);
        rotateLeftButton.setPosition(rrDefaultX - size * .45f + config.rlX,
                (rrDefaultY - buttonSize) / 2 + config.rlY);
        hardDropButton.setPosition(rrDefaultX - size * .55f + config.dropX,
                rrDefaultY + config.dropY);
        holdButton.setPosition(rrDefaultX + config.holdX, rrDefaultY + .5f * size + config.holdY);

        if (freezeButton != null) {
            freezeButton.setSize(buttonSize, buttonSize);
            freezeButton.getLabel().setFontScale(fontScale);
            freezeButton.setPosition(rrDefaultX - size * .55f + config.frzX, rrDefaultY + .5f * size + config.frzY);
        }
    }

    protected void saveConfig() {
        if (isLandscapeConfig)
            app.localPrefs.saveGamepadConfigLandscape(config);
        else
            app.localPrefs.saveGamepadConfigPortrait(config);
    }

    public void resetConfig() {
        config = new OnScreenGamepadConfig();
        saveConfig();
    }

    public interface IOnScreenButtonsScreen {
        MyStage getStage();

        float getCenterPosX();

        float getGameboardTop();

        boolean isLandscape();
    }

    private abstract class MoveDragListener extends DragListener {
        float dragStartPosX;
        float dragStartPosY;
        boolean landscapeStart;

        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            dragStartPosX = event.getListenerActor().getX();
            dragStartPosY = event.getListenerActor().getY();
            landscapeStart = isLandscapeConfig;
        }

        @Override
        public void drag(InputEvent event, float x, float y, int pointer) {
            if (isLandscapeConfig != landscapeStart) {
                cancel();
                return;
            }

            float moveX = x - getDragStartX();
            float moveY = y - getDragStartY();

            event.getListenerActor().setX(event.getListenerActor().getX() + moveX);
            event.getListenerActor().setY(event.getListenerActor().getY() + moveY);
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            if (isLandscapeConfig == landscapeStart) {
                savePosDelta((int) (event.getListenerActor().getX() - dragStartPosX), (int) (event.getListenerActor().getY() - dragStartPosY));
                saveConfig();
            }
        }

        protected abstract void savePosDelta(int deltaX, int deltaY);
    }
}
