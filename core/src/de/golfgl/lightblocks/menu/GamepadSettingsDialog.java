package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 04.11.2017.
 */

public class GamepadSettingsDialog extends ControllerMenuDialog {

    private final RoundedTextButton closeButton;
    private final LightBlocksGame app;
    private Button refreshButton;
    private RefreshListener controllerListener;
    private ControllerMappings mappings;
    private boolean runsOnChrome;

    public GamepadSettingsDialog(LightBlocksGame app) {
        super("", app.skin);

        this.app = app;
        this.mappings = app.controllerMappings;

        getButtonTable().defaults().pad(20, 40, 20, 40);
        closeButton = new RoundedTextButton("Close", app.skin);
        button(closeButton);

        refreshButton = new RoundedTextButton("Refresh", getSkin());
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshShownControllers();
            }
        });

        getButtonTable().add(refreshButton);
        addFocusableActor(refreshButton);

        controllerListener = new RefreshListener();
    }

    private void refreshShownControllers() {
        fillContentTable();
        invalidate();
        pack();
        setPosition(getStage().getWidth() / 2, getStage().getHeight() / 2, Align.center);
    }

    private void fillContentTable() {
        Table contentTable = getContentTable();
        contentTable.clear();

        Array<Controller> controllers = Controllers.getControllers();

        contentTable.pad(15);
        contentTable.row();
        contentTable.add(new ScaledLabel(controllers.size == 0 ? app.TEXTS.get("configGamepadNoneFound")
                : app.TEXTS.get("configGamepadInit"), getSkin(), LightBlocksGame.SKIN_FONT_TITLE)).padBottom(10);

        Table controllerList = new Table();
        controllerList.defaults().pad(5);
        for (int i = 0; i < controllers.size; i++) {
            controllerList.row();
            final Controller controller = controllers.get(i);
            String shownName = controller.getName();
            if (shownName.length() > 25)
                shownName = shownName.substring(0, 23) + "...";
            TextButton configureButton = new RoundedTextButton(shownName, getSkin());
            configureButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    startControllerConfiguration(controller);
                }
            });
            controllerList.add(configureButton);
            addFocusableActor(configureButton);
            if (getStage() != null)
                ((MyStage) getStage()).addFocusableActor(configureButton);
        }
        contentTable.row();
        contentTable.add(controllerList);

        contentTable.row().padTop(20);
        Label hint = new ScaledLabel(app.TEXTS.get("configGamepadHelp") +
                (Gdx.app.getType() == Application.ApplicationType.WebGL && runsOnChrome ?
                        "\nIf you face problems with controllers on Chrome, press a button, reload the game, try " +
                                "again" +
                                ".\n" +
                                "If that does not help, try Mozilla Firefox." : ""),
                getSkin(), LightBlocksGame.SKIN_FONT_BIG, .8f);
        hint.setFontScale(.8f);
        hint.setWrap(true);
        hint.setAlignment(Align.center);
        contentTable.add(hint).fill().minWidth(LightBlocksGame.nativeGameWidth * .7f);
    }

    private void startControllerConfiguration(Controller controller) {
        Dialog configurationDialog = new GamepadMappingDialog(app, controller);

        configurationDialog.show(getStage());
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        //getContentTable().setWidth(stage.getWidth() * .7f);
        fillContentTable();

        super.show(stage, action);

        if (stage instanceof MyStage)
            ((MyStage) stage).setEscapeActor(closeButton);

        Controllers.addListener(controllerListener);
        return this;
    }

    @Override
    public void hide(Action action) {
        // removeListener darf erst im nächsten Call passieren, da es eine Exception gibt wenn diese Aktion
        // aus einem Controller-Aufruf heraus passiert
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Controllers.removeListener(controllerListener);
            }
        });
        super.hide(action);
    }

    private class RefreshListener extends ControllerAdapter {
        @Override
        public void connected(Controller controller) {
            refreshShownControllers();
        }

        @Override
        public void disconnected(Controller controller) {
            refreshShownControllers();
        }
    }
}
