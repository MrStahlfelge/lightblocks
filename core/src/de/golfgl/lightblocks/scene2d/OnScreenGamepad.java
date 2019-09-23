package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayScreen;

public class OnScreenGamepad extends Group {
    private final Touchpad touchpad;
    private final Button rotateRightButton;
    private final Button rotateLeftButton;
    private final Button hardDropButton;
    private final PlayGesturesInput.HoldButton holdButton;
    private PlayGesturesInput.FreezeButton freezeButton;

    public OnScreenGamepad(LightBlocksGame app, final PlayScreen playScreen,
                           ChangeListener touchPadListener, InputListener holdInputListener,
                           InputListener freezeButtonInputListener) {
        touchpad = new Touchpad(0, app.skin);
        touchpad.addListener(touchPadListener);
        addActor(touchpad);

        rotateRightButton = new ImageButton(app.skin, "rotateright");
        addActor(rotateRightButton);

        rotateLeftButton = new ImageButton(app.skin, "rotateleft");
        addActor(rotateLeftButton);

        hardDropButton = new ImageButton(app.skin, "harddrop");
        addActor(hardDropButton);

        holdButton = new PlayGesturesInput.HoldButton(app, playScreen, holdInputListener);
        addActor(holdButton);

        if (playScreen != null) {
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
                freezeButton = new PlayGesturesInput.FreezeButton(app, freezeButtonLabel,
                        freezeButtonInputListener);
            }

        } else {
            freezeButton = new PlayGesturesInput.FreezeButton(app, "FREEZE", freezeButtonInputListener);
        }
        if (freezeButton != null)
            addActor(freezeButton);
    }

    public void resize(IOnScreenButtonsScreen screen) {
        float size = MathUtils.clamp(screen.getCenterPosX(), LightBlocksGame.nativeGameWidth * .45f, screen.getStage().getHeight() * .5f);
        touchpad.setSize(size, size);
        touchpad.setPosition(0, 0);
        rotateRightButton.setSize(size * .4f, size * .4f);
        rotateLeftButton.setSize(size * .4f, size * .4f);
        hardDropButton.setSize(size * .4f, size * .4f);
        rotateRightButton.setPosition(screen.getStage().getWidth() - size * .5f, size - rotateRightButton
                .getHeight());
        rotateLeftButton.setPosition(rotateRightButton.getX() - size * .45f, (rotateRightButton.getY() -
                rotateRightButton.getHeight()) / 2);
        hardDropButton.setPosition(rotateRightButton.getX() - size * .55f, rotateRightButton.getY());

        holdButton.resize(screen,20);
        if (freezeButton != null)
            freezeButton.resize(screen, 20);
    }

    public interface IOnScreenButtonsScreen {
        MyStage getStage();
        float getCenterPosX();
        float getGameboardTop();
    }
}
