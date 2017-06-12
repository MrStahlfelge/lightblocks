package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.GamepadConfigDialog;
import de.golfgl.lightblocks.scenes.MusicButton;

/**
 * Einstellungen
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class SettingsScreen extends AbstractMenuScreen {

    PlayGesturesInput pgi;
    Group touchPanel;
    private Slider touchPanelSizeSlider;

    public SettingsScreen(final LightBlocksGame app) {
        super(app);

        initializeUI();

    }

    @Override
    protected void fillMenuTable(Table settingsTable) {
        final Label musicButtonLabel = new Label("", app.skin, app.SKIN_FONT_BIG);
        final Button menuMusicButton = new MusicButton(app, musicButtonLabel);

        final Button touchPanelButton = new TextButton(PlayScreenInput.getInputFAIcon(1), app.skin, FontAwesome
                .SKIN_FONT_FA + "-checked");
        touchPanelButton.setChecked(app.getShowTouchPanel());
        touchPanelButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             app.setShowTouchPanel(touchPanelButton.isChecked());
                                         }
                                     }
        );
        touchPanelSizeSlider = new Slider(25, Gdx.graphics.getWidth() * .33f, 1, false, app.skin);
        touchPanelSizeSlider.setValue(app.getTouchPanelSize());
        touchPanelSizeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                touchPanelSizeChanged();
            }
        });
        final Button pauseSwipeButton = new TextButton(FontAwesome.UP_ARROW, app.skin, FontAwesome
                .SKIN_FONT_FA + "-checked");
        pauseSwipeButton.setChecked(app.getPauseSwipeEnabled());
        pauseSwipeButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             app.setPauseSwipeEnabled(pauseSwipeButton.isChecked());
                                         }
                                     }
        );

        Button gamePadButton = new TextButton(PlayScreenInput.getInputFAIcon(3), app.skin, FontAwesome.SKIN_FONT_FA);
        gamePadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GamepadConfigDialog gpc = new GamepadConfigDialog(app);

                gpc.show(stage);
            }
        });

        //Settings Table
        settingsTable.row();
        settingsTable.defaults().fill();
        settingsTable.add(menuMusicButton).uniform();
        settingsTable.add(musicButtonLabel).expandX();

        settingsTable.row().spaceTop(30);
        settingsTable.add(new Label(app.TEXTS.get("menuInputGestures"), app.skin, app.SKIN_FONT_BIG)).colspan(2);
        settingsTable.row();
        settingsTable.add(touchPanelButton).uniform();
        Label tg = new Label(app.TEXTS.get("menuShowTouchPanel"), app.skin);
        tg.setWrap(true);
        settingsTable.add(tg).prefWidth(LightBlocksGame.nativeGameWidth * 0.5f);
        settingsTable.row();
        settingsTable.add();
        settingsTable.add(new Label(app.TEXTS.get("menuSizeOfTouchPanel"), app.skin));
        settingsTable.row();
        settingsTable.add();
        settingsTable.add(touchPanelSizeSlider).minHeight(40);
        settingsTable.row();
        settingsTable.add(pauseSwipeButton).uniform();
        settingsTable.add(new Label(app.TEXTS.get("menuPauseSwipeEnabled"), app.skin));

        settingsTable.row().spaceTop(30);
        settingsTable.add(gamePadButton).uniform();
        settingsTable.add(new Label(app.TEXTS.get("menuInputGamepad"), app.skin, app.SKIN_FONT_BIG));

    }


    @Override
    protected String getTitleIcon() {
        return FontAwesome.SETTINGS_GEARS;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuSettings");
    }

    protected void touchPanelSizeChanged() {
        if (pgi == null)
            pgi = new PlayGesturesInput();

        if (touchPanel != null && touchPanel.hasParent())
            touchPanel.remove();
        touchPanel = pgi.initializeTouchPanel(SettingsScreen.this,
                (int) touchPanelSizeSlider.getValue());
        touchPanel.setVisible(true);
        touchPanel.setTouchable(Touchable.disabled);

        Vector2 position = new Vector2(0, 0);
        position = touchPanelSizeSlider.localToStageCoordinates(position);

        touchPanel.setPosition(stage.getWidth() / 2, position.y - 30);
        Color c = pgi.rotateRightColor;
        c.a = 1;
        pgi.setTouchPanelColor(c);
        touchPanel.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeOut(1f, Interpolation.fade),
                Actions.removeActor()));
    }

    @Override
    protected void goBackToMenu() {
        flushChanges();
        super.goBackToMenu();
    }

    /**
     * speichert die Einstellungen die nicht sofort gespeichert werden
     */
    private void flushChanges() {
        app.setTouchPanelSize((int) touchPanelSizeSlider.getValue());
    }

    @Override
    public void pause() {
        flushChanges();
        super.pause();
    }

}
