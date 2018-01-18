package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 24.02.2017.
 */
public abstract class AbstractMenuScreen extends AbstractScreen {
    protected static final Color COLOR_TABLE_DEACTIVATED = new Color(.2f, .2f, .2f, 1);
    protected static final Color COLOR_TABLE_NORMAL = LightBlocksGame.LIGHT_HIGHLIGHT_COLOR;
    protected static final Color COLOR_TABLE_HIGHLIGHTED = new Color(1, 1, 1, 1);
    private Button leaveButton;
    private ScrollPane menuScrollPane;

    public AbstractMenuScreen(LightBlocksGame app) {
        super(app);
    }

    protected ScrollPane getMenuScrollPane() {
        return menuScrollPane;
    }

    public Button getLeaveButton() {
        return leaveButton;
    }

    public void initializeUI() {

        Table menuTable = new Table();
        fillMenuTable(menuTable);
        // setFillParent verursacht Probleme mit ScrollPane
        menuTable.setFillParent(false);
        menuScrollPane = new ScrollPane(menuTable, app.skin);
        menuScrollPane.setSize(LightBlocksGame.nativeGameWidth, 150);
        menuScrollPane.setScrollingDisabled(true, false);
        // für Slider nötig, bei großen Buttons oder Auswahlen aber sehr störend. dann wieder true setzen
        menuScrollPane.setCancelTouchFocus(false);

        //Titel
        // Der Titel wird nach der Menütabelle gefüllt, eventuell wird dort etwas gesetzt (=> Scores)
        Label title = new Label(getTitle().toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        final String subtitle = getSubtitle();

        // Buttons
        Table buttons = new Table();

        // Back button
        leaveButton = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        setBackButton(leaveButton);
        buttons.add(leaveButton).uniform();
        fillButtonTable(buttons);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.row().padTop(15);
        mainTable.add(new Label(getTitleIcon(), app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(title);
        mainTable.row();
        if (subtitle != null)
            mainTable.add(new Label(subtitle, app.skin, LightBlocksGame.SKIN_FONT_BIG));
        mainTable.row().spaceTop(30);
        mainTable.add(menuScrollPane);
        mainTable.row();
        mainTable.add(buttons).spaceTop(30);

        stage.addActor(mainTable);

    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);

        swoshIn();

    }

    protected abstract String getTitleIcon();

    protected void fillButtonTable(Table buttons) {

    }

    protected abstract String getSubtitle();

    protected abstract String getTitle();

    protected abstract void fillMenuTable(Table menuTable);

    protected Slider constructBeginningLevelSlider(final Label beginningLevelLabel, int defaultValue, int maxValue) {
        // Startlevel
        final Slider beginningLevelSlider = new Slider(0, maxValue, 1, false, app.skin);
        beginningLevelSlider.setValue(defaultValue);

        if (beginningLevelLabel != null) {
            final ChangeListener changeListener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    beginningLevelLabel.setText(app.TEXTS.get("labelLevel") + " " + Integer.toString((int)
                            beginningLevelSlider.getValue()));
                }
            };
            beginningLevelSlider.addListener(changeListener);
            changeListener.changed(null, null);
        }

        return beginningLevelSlider;
    }


}
