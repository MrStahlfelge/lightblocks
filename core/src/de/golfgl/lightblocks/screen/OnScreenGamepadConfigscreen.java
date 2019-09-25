package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.OnScreenGamepad;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

public class OnScreenGamepadConfigscreen extends AbstractScreen implements OnScreenGamepad.IOnScreenButtonsScreen {

    private final Table gameboardTable;
    private final OnScreenGamepad gamepad;

    public OnScreenGamepadConfigscreen(LightBlocksGame app) {
        super(app);

        gameboardTable = new Table();
        gameboardTable.setBackground(app.skin.getDrawable("whitesmoke"));
        gameboardTable.setSize(Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth, Gameboard.GAMEBOARD_ALLROWS * BlockActor.blockWidth);
        gamepad = new OnScreenGamepad(app, null, null, null, null);
        stage.addActor(gameboardTable);
        stage.addActor(gamepad);

        ScaledLabel label = new ScaledLabel(app.TEXTS.get("configureOSGHelp"), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Button okButton = new RoundedTextButton(app.TEXTS.get("donationButtonClose"), app.skin);
        setBackButton(okButton);

        gameboardTable.add(label).fillX().expandX().pad(20);
        gameboardTable.row();
        gameboardTable.add(okButton);
        gameboardTable.row();
        gameboardTable.add().expand();
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
        app.controllerMappings.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        gameboardTable.setPosition((stage.getWidth() - gameboardTable.getWidth()) / 2,
                (LightBlocksGame.nativeGameHeight - gameboardTable.getHeight()) / 2 + (stage.getHeight() - LightBlocksGame.nativeGameHeight));

        gamepad.resize(this);
    }

    @Override
    public MyStage getStage() {
        return stage;
    }

    @Override
    public float getCenterPosX() {
        return (stage.getWidth() - LightBlocksGame.nativeGameWidth) / 2;
    }

    @Override
    public float getGameboardTop() {
        return gameboardTable.getY() + gameboardTable.getHeight()
                - (Gameboard.GAMEBOARD_ALLROWS - Gameboard.GAMEBOARD_NORMALROWS) * BlockActor.blockWidth;
    }
}
