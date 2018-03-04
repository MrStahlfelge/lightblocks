package rs.pedjaapps.smc.view;

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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import rs.pedjaapps.smc.MaryoGame;
import rs.pedjaapps.smc.assets.Assets;

/**
 * Created by Benjamin Schulte on 04.11.2017.
 */

public class GamepadSettingsDialog extends ControllerMenuDialog {

    private final ColorableTextButton closeButton;
    private Button refreshButton;
    private RefreshListener controllerListener;
    private ControllerMappings mappings;
    private boolean runsOnChrome;

    public GamepadSettingsDialog(Skin skin, ControllerMappings mappings, String isRunningOn) {
        super("", skin, Assets.WINDOW_SMALL);

        this.mappings = mappings;

        getButtonTable().defaults().pad(20, 40, 0, 40);
        closeButton = new ColorableTextButton("Close", skin, Assets.BUTTON_SMALL);
        button(closeButton);

        refreshButton = new ColorableTextButton("Refresh", getSkin(), Assets.BUTTON_SMALL);
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshShownControllers();
            }
        });

        getButtonTable().add(refreshButton);
        buttonsToAdd.add(refreshButton);

        controllerListener = new RefreshListener();

        runsOnChrome = (isRunningOn != null && isRunningOn.toLowerCase().contains("chrome/"));
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

        contentTable.add(new Label(controllers.size == 0 ? "No controllers found." : "Controllers found:",
                getSkin(), Assets.LABEL_SIMPLE25)).padBottom(30);

        Table controllerList = new Table();
        controllerList.defaults().pad(10);
        for (int i = 0; i < controllers.size; i++) {
            controllerList.row();
            final Controller controller = controllers.get(i);
            String shownName = controller.getName();
            if (shownName.length() > 40)
                shownName = shownName.substring(0, 38) + "...";
            controllerList.add(new Label(shownName, getSkin(), Assets.LABEL_SIMPLE25)).left().expandX();
            ColorableTextButton configureButton = new ColorableTextButton("Configure", getSkin(), Assets
                    .BUTTON_SMALL_FRAMELESS);
            configureButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    startControllerConfiguration(controller);
                }
            });
            controllerList.add(configureButton);
            buttonsToAdd.add(configureButton);
            if (getStage() != null)
                ((MenuStage) getStage()).addFocussableActor(configureButton);
        }
        contentTable.row();
        contentTable.add(controllerList);

        contentTable.row().padTop(30);
        Label hint = new Label("If a connected controller does not show up,\ntry pressing a button.\n" +
                (Gdx.app.getType() == Application.ApplicationType.WebGL && runsOnChrome ?
                        "If you face problems with controllers on Chrome, press a button, reload the game, try again.\n" +
                                "If that does not help, try Mozilla Firefox." : ""),
                getSkin(), Assets.LABEL_SIMPLE25);
        hint.setFontScale(.8f);
        hint.setWrap(true);
        hint.setAlignment(Align.center);
        contentTable.add(hint).fill().minWidth(MaryoGame.NATIVE_WIDTH * .7f);
    }

    private void startControllerConfiguration(Controller controller) {
        Dialog configurationDialog = new GamepadMappingDialog(getSkin(), controller, mappings);

        configurationDialog.show(getStage());
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        //getContentTable().setWidth(stage.getWidth() * .7f);
        fillContentTable();

        super.show(stage, action);

        if (stage instanceof MenuStage)
            ((MenuStage) stage).setEscapeActor(closeButton);

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
