package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
            case Input.Keys.BACKSPACE:
                playScreen.goBackToMenu();
                return true;

            case Input.Keys.ENTER:
                playScreen.switchPause(false);
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.setSoftDropFactor(1);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.startMoveHorizontal(true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.startMoveHorizontal(false);
                return true;

            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.ALT_LEFT:
                playScreen.gameModel.setRotate(false);
                return true;

            case Input.Keys.SPACE:
                playScreen.gameModel.setRotate(true);
                return true;

            default:
                return super.keyDown(keycode);
        }

    }

    @Override
    public Actor showHelp(Group drawGroup, boolean isBegin) {

        Table table = (Table) super.showHelp(drawGroup, isBegin);

        table.row();
        Label hintBegin = new Label(playScreen.app.TEXTS.get(isBegin ? "labelPressEnterToStart" :
                "labelPressEnterToResume"), playScreen.app.skin, isBegin ? "big" : "default");

        table.add(hintBegin).spaceTop(30);

        table.row();

        Label keyHelp = new Label(playScreen.app.TEXTS.get("inputKeyboardHelp"), playScreen.app.skin);
        keyHelp.setWrap(true);
        keyHelp.setAlignment(Align.center);
        table.add(keyHelp).spaceTop(30).prefWidth(drawGroup.getWidth());

        return table;

    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            playScreen.gameModel.setSoftDropFactor(0);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.gameModel.endMoveHorizontal(true);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.gameModel.endMoveHorizontal(false);
            return true;
        }

        return super.keyUp(keycode);
    }


}
