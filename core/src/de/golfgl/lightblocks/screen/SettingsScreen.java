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

/**
 * Einstellungen
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class SettingsScreen extends AbstractScreen {

    private final Slider touchPanelSizeSlider;
    PlayGesturesInput pgi;
    Group touchPanel;

    public SettingsScreen(final LightBlocksGame app) {
        super(app);

        final Button menuMusicButton = new TextButton(FontAwesome.SETTINGS_MUSIC, app.skin, FontAwesome.SKIN_FONT_FA
                + "-checked");
        menuMusicButton.setChecked(app.isPlayMusic());
        menuMusicButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            app.setPlayMusic(menuMusicButton.isChecked());
                                        }
                                    }
        );

        final Button touchPanelButton = new TextButton(FontAwesome.DEVICE_GESTURE2, app.skin, FontAwesome.SKIN_FONT_FA
                + "-checked");
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


        // Back button
        Button leave = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        setBackButton(leave);

        //Settings Table
        Table settingsTable = new Table();
        settingsTable.row();
        settingsTable.defaults().fill();
        settingsTable.add(menuMusicButton);
        settingsTable.add(new Label(app.TEXTS.get("menuMusicButton"), app.skin, app.SKIN_FONT_BIG));
        settingsTable.row().spaceTop(30);
        settingsTable.add(new Label(app.TEXTS.get("menuInputGestures"), app.skin, app.SKIN_FONT_BIG)).colspan(2).fill
                (false);
        settingsTable.row();
        settingsTable.add(new Label(app.TEXTS.get("menuSizeOfTouchPanel"), app.skin)).colspan(2).fill(false);
        settingsTable.row();
        settingsTable.add(touchPanelSizeSlider).colspan(2).minHeight(50);
        settingsTable.row();
        settingsTable.add(touchPanelButton);
        Label tg = new Label(app.TEXTS.get("menuShowTouchPanel"), app.skin);
        tg.setWrap(true);
        settingsTable.add(tg).prefWidth(LightBlocksGame.nativeGameWidth * 0.5f);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.row();
        mainTable.add(new Label(FontAwesome.SETTINGS_GEARS, app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("menuSettings").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).spaceBottom(50);
        mainTable.row();
        mainTable.add(settingsTable);
        mainTable.row();
        mainTable.add(leave).spaceTop(50);

        stage.addActor(mainTable);

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
        app.setTouchPanelSize((int) touchPanelSizeSlider.getValue());
        super.goBackToMenu();
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);

        swoshIn();
    }
}
