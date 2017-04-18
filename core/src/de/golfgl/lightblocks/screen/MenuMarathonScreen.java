package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.scenes.InputButtonTable;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class MenuMarathonScreen extends AbstractMenuScreen {

    private Slider beginningLevelSlider;
    private InputButtonTable inputButtons;

    public MenuMarathonScreen(final LightBlocksGame app) {
        super(app);

        initializeUI();
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        TextButton playButton = new FATextButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        TextButton highScoreButton = new FATextButton(FontAwesome.COMMENT_STAR_TROPHY, app.TEXTS.get("labelScores"),
                app.skin);
        highScoreButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            showHighscores();
                                        }
                                    }
        );

        buttons.defaults().fill();
        buttons.add(highScoreButton).uniform();
        buttons.add(playButton).prefWidth(playButton.getPrefWidth() * 1.2f);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {

        final Label beginningLevelLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        beginningLevelSlider = constructBeginningLevelSlider(beginningLevelLabel, app.prefs.getInteger
                ("beginningLevel", 0), 9);


        Table beginningLevelTable = new Table();
        beginningLevelTable.add(beginningLevelSlider).minHeight(30).minWidth(200).right().fill();
        beginningLevelTable.add(beginningLevelLabel).left().spaceLeft(10);

        // die möglichen Inputs aufzählen
        inputButtons = new InputButtonTable(app, app.prefs.getInteger("inputType", 0));

        menuTable.row();
        menuTable.add(new Label(app.TEXTS.get("labelBeginningLevel"), app.skin)).left();
        menuTable.row();
        menuTable.add(beginningLevelTable);
        menuTable.row().padTop(20);
        menuTable.add(new Label(app.TEXTS.get("menuInputControl"), app.skin)).left();
        menuTable.row();
        menuTable.add(inputButtons);
        menuTable.row();
        menuTable.add(inputButtons.getInputLabel()).center();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PERSON;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("labelMarathon");
    }

    protected void showHighscores() {
        if (!app.savegame.canSaveState()) {
            showDialog("Sorry, highscores are only saved in the native Android " +
                    "version of Lightblocks. Download it to" +
                    " your mobile!");
            return;
        }

        ScoreScreen scoreScreen = new ScoreScreen(app);
        scoreScreen.setGameModelId(MarathonModel.MODEL_MARATHON_ID + inputButtons.getSelectedInput());
        scoreScreen.addScoreToShow(app.savegame.getBestScore("marathon" +
                        inputButtons.getSelectedInput()),
                app.TEXTS.get("labelBestScores"));
        scoreScreen.setBackScreen(MenuMarathonScreen.this);
        scoreScreen.setMaxCountingTime(1);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);
    }

    protected void beginNewGame() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameModelClass(MarathonModel.class);
        initGameParametersParams.setBeginningLevel((int) beginningLevelSlider.getValue());
        initGameParametersParams.setInputKey(inputButtons.getSelectedInput());

        // Einstellungen speichern
        app.prefs.putInteger("inputType", inputButtons.getSelectedInput());
        app.prefs.putInteger("beginningLevel", initGameParametersParams
                .getBeginningLevel());
        app.prefs.flush();

        try {
            PlayScreen.gotoPlayScreen(MenuMarathonScreen.this, initGameParametersParams);
            dispose();
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

}
