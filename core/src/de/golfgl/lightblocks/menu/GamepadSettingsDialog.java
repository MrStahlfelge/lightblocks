package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
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
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 04.11.2017.
 */

public class GamepadSettingsDialog extends ControllerMenuDialog {

    private final Button closeButton;
    private final LightBlocksGame app;
    private Button refreshButton;
    private boolean runsOnChrome;
    private Actor defaultActor;

    private int connectedControllersOnLastCheck;
    private float timeSinceLastControllerCheck;

    public GamepadSettingsDialog(LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        getButtonTable().defaults().pad(20, 40, 20, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        refreshButton = new FaButton(FontAwesome.ROTATE_RELOAD, getSkin());
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshShownControllers();
            }
        });

        getButtonTable().add(refreshButton);
        addFocusableActor(refreshButton);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceLastControllerCheck = timeSinceLastControllerCheck + delta;

        if (timeSinceLastControllerCheck > .2f) {
            if (connectedControllersOnLastCheck != Controllers.getControllers().size)
                refreshShownControllers();
            timeSinceLastControllerCheck = 0;
        }
    }

    private void refreshShownControllers() {
        fillContentTable();
        invalidate();
        pack();
        setPosition(getStage().getWidth() / 2, getStage().getHeight() / 2, Align.center);
    }

    /**
     * füllt die Liste der Controller und das Umfeld. Wird bei Anschluss eines Controllers neu ausgeführt
     */
    private void fillContentTable() {
        Table contentTable = getContentTable();
        contentTable.clear();
        defaultActor = null;

        Array<Controller> controllers = Controllers.getControllers();
        connectedControllersOnLastCheck = controllers.size;

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
            if (defaultActor == null)
                defaultActor = configureButton;
            if (getStage() != null)
                ((MyStage) getStage()).addFocusableActor(configureButton);
        }

        if (controllers.size == 0) {
            controllerList.row();
            TextButton recommendedControllers = new RoundedTextButton(
                    app.TEXTS.get("configGamepadShowRecommendations"), getSkin());
            recommendedControllers.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.openOrShowUri(LightBlocksGame.CONTROLLER_RECOMMENDATION_URL);
                }
            });
            controllerList.add(recommendedControllers);
            addFocusableActor(recommendedControllers);
            if (getStage() != null)
                ((MyStage) getStage()).addFocusableActor(recommendedControllers);
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
                getSkin(), LightBlocksGame.SKIN_FONT_BIG);
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

        return this;
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return defaultActor != null ? defaultActor : super.getConfiguredDefaultActor();
    }
}
