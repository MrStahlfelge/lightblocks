package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.OnScreenGamepadConfig;

public class OnScreenGamepad extends Group {
    private static final float TOUCHPAD_MIN = LightBlocksGame.nativeGameWidth * .45f;
    private static final float HIDE_TRESHOLD = .45f;
    public static final int GRID_SIZE = 5;

    private final Touchpad touchpad;
    private final Group touchpadContainer;
    private final LightBlocksGame app;
    private final Button rotateRightButton;
    private final Button rotateLeftButton;
    private final Button hardDropButton;
    private final TextButton holdButton;
    boolean isLandscapeConfig;
    private TextButton freezeButton;
    private OnScreenGamepadConfig config;
    private Slider sizeSlider;
    private IOnScreenButtonsScreen sliderScreen;
    private MoveDragListener currentButtonListener;

    public OnScreenGamepad(LightBlocksGame app, final PlayScreen playScreen,
                           EventListener touchPadListener, InputListener holdInputListener,
                           InputListener freezeButtonInputListener) {
        this.app = app;

        touchpad = new Touchpad(0, app.skin);
        touchpadContainer = new Group();
        touchpadContainer.addActor(touchpad);

        if (touchPadListener != null) {
            touchpad.addListener(touchPadListener);
            touchpadContainer.setTouchable(Touchable.childrenOnly);
        } else
            touchpadContainer.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.touchpadX = config.touchpadX + deltaX;
                    config.touchpadY = config.touchpadY + deltaY;
                }

                @Override
                public float getScale() {
                    return config.touchpadScale;
                }

                @Override
                public void saveScale(float value) {
                    config.touchpadScale = value;
                }
            });
        addActor(touchpadContainer);

        rotateRightButton = new Button(app.skin, "rotateright");
        addActor(rotateRightButton);

        rotateLeftButton = new Button(app.skin, "rotateleft");
        addActor(rotateLeftButton);

        hardDropButton = new Button(app.skin, "harddrop");
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

                @Override
                public float getScale() {
                    return config.rlScale;
                }

                @Override
                public void saveScale(float value) {
                    config.rlScale = value;
                }
            });
            rotateRightButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.rrX += deltaX;
                    config.rrY += deltaY;
                }

                @Override
                public float getScale() {
                    return config.rrScale;
                }

                @Override
                public void saveScale(float value) {
                    config.rrScale = value;
                }
            });
            hardDropButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.dropX += deltaX;
                    config.dropY += deltaY;
                }

                @Override
                public float getScale() {
                    return config.dropScale;
                }

                @Override
                public void saveScale(float value) {
                    config.dropScale = value;
                }
            });
            holdButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.holdX += deltaX;
                    config.holdY += deltaY;
                }

                @Override
                public float getScale() {
                    return config.holdScale;
                }

                @Override
                public void saveScale(float value) {
                    config.holdScale = value;
                }
            });
            freezeButton.addListener(new MoveDragListener() {
                @Override
                protected void savePosDelta(int deltaX, int deltaY) {
                    config.frzX += deltaX;
                    config.frzY += deltaY;
                }

                @Override
                public float getScale() {
                    return config.frzScale;
                }

                @Override
                public void saveScale(float value) {
                    config.frzScale = value;
                }
            });
        }
        if (freezeButton != null)
            addActor(freezeButton);
    }

    public void resize(IOnScreenButtonsScreen screen) {
        if (isLandscapeConfig != screen.isLandscape() && sizeSlider != null)
            resetSizeSlider();

        isLandscapeConfig = screen.isLandscape();
        config = isLandscapeConfig ? app.localPrefs.getGamepadConfigLandscape() : app.localPrefs.getGamepadConfigPortrait();

        float size = MathUtils.clamp(screen.getCenterPosX(), TOUCHPAD_MIN, screen.getStage().getHeight() * .5f);
        float fontScale = size * .002f;
        float touchpadSize = Math.max(size * config.touchpadScale, TOUCHPAD_MIN);
        float touchpadScaling = (size * config.touchpadScale) / TOUCHPAD_MIN;
        touchpad.setSize(touchpadSize, touchpadSize);
        if (touchpadScaling < 1) {
            touchpadContainer.setScale(touchpadScaling);
            touchpadContainer.setTransform(true);
        } else {
            touchpadContainer.setScale(1);
            touchpadContainer.setTransform(false);
        }
        touchpadContainer.setPosition(0 + config.touchpadX, 0 + config.touchpadY);
        float buttonSize = size * .4f;
        rotateRightButton.setSize(buttonSize * config.rrScale, buttonSize * config.rrScale);
        rotateLeftButton.setSize(buttonSize * config.rlScale, buttonSize * config.rlScale);
        hardDropButton.setSize(buttonSize * config.dropScale, buttonSize * config.dropScale);
        holdButton.setSize(buttonSize * config.holdScale, buttonSize * config.holdScale);
        holdButton.getLabel().setFontScale(fontScale * config.holdScale);

        float rrDefaultX = screen.getStage().getWidth() - size * .5f;
        float rrDefaultY = size - buttonSize;
        rotateRightButton.setPosition(snapToGrid(rrDefaultX + config.rrX),
                snapToGrid(rrDefaultY + config.rrY));
        rotateLeftButton.setPosition(snapToGrid(rrDefaultX - size * .45f + config.rlX),
                snapToGrid((rrDefaultY - buttonSize) / 2 + config.rlY));
        hardDropButton.setPosition(snapToGrid(rrDefaultX - size * .55f + config.dropX),
                snapToGrid(rrDefaultY + config.dropY));
        holdButton.setPosition(snapToGrid(rrDefaultX + config.holdX), snapToGrid(rrDefaultY + .5f * size + config.holdY));

        if (freezeButton != null) {
            freezeButton.setSize(buttonSize * config.frzScale, buttonSize * config.frzScale);
            freezeButton.getLabel().setFontScale(fontScale * config.frzScale);
            freezeButton.setPosition(snapToGrid(rrDefaultX - size * .55f + config.frzX),
                    snapToGrid(rrDefaultY + .5f * size + config.frzY));
        }

        // Manche dürfen versteckt werden, aber nicht im config screen
        if (sizeSlider != null) {
            holdButton.getColor().a = config.holdScale < HIDE_TRESHOLD ? .5f : 1;
            rotateRightButton.getColor().a = config.rrScale < HIDE_TRESHOLD ? .5f : 1;
            rotateLeftButton.getColor().a = config.rlScale < HIDE_TRESHOLD ? .5f : 1;
            hardDropButton.getColor().a = config.dropScale < HIDE_TRESHOLD ? .5f : 1;
        } else {
            if (config.holdScale < HIDE_TRESHOLD)
                holdButton.setVisible(false);

            if (config.rrScale < HIDE_TRESHOLD)
                rotateRightButton.setVisible(false);

            if (config.rlScale < HIDE_TRESHOLD)
                rotateLeftButton.setVisible(false);

            if (config.dropScale < HIDE_TRESHOLD)
                hardDropButton.setVisible(false);
        }
    }

    private int snapToGrid(float pos) {
        int intPos = (int) pos;
        return intPos - (intPos % GRID_SIZE);
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

    public void setSizeConfigSlider(final Slider sizeSlider, final IOnScreenButtonsScreen sliderScreen) {
        this.sizeSlider = sizeSlider;
        this.sliderScreen = sliderScreen;
        sizeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentButtonListener != null) {
                    currentButtonListener.saveScale(sizeSlider.getValue());
                    saveConfig();
                    resize(sliderScreen);
                }
            }
        });
    }

    protected void resetSizeSlider() {
        sizeSlider.setVisible(false);
        currentButtonListener = null;
    }

    private void setSizeSliderForListener(final MoveDragListener moveDragListener) {
        this.currentButtonListener = moveDragListener;
        sizeSlider.setVisible(true);
        sizeSlider.setValue(moveDragListener.getScale());
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
        // abbrechen, wenn die Orientierung während des Drag gewechselt wird
        boolean landscapeStart;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            setSizeSliderForListener(this);
            return super.touchDown(event, x, y, pointer, button);
        }

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

            Actor moveActor = event.getListenerActor();
            moveActor.setX(moveActor.getX() + moveX * moveActor.getScaleX());
            moveActor.setY(moveActor.getY() + moveY * moveActor.getScaleY());
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            if (isLandscapeConfig == landscapeStart) {
                savePosDelta((int) (event.getListenerActor().getX() - dragStartPosX), (int) (event.getListenerActor().getY() - dragStartPosY));
                saveConfig();
                if (sliderScreen != null)
                    resize(sliderScreen);
            }
        }

        protected abstract void savePosDelta(int deltaX, int deltaY);

        public abstract float getScale();

        public abstract void saveScale(float value);
    }
}
