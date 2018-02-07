package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.BeginningLevelChooser;
import de.golfgl.lightblocks.scenes.FaButton;
import de.golfgl.lightblocks.scenes.GlowLabelButton;
import de.golfgl.lightblocks.scenes.InputButtonTable;
import de.golfgl.lightblocks.scenes.VetoDialog;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class MenuMarathonScreen extends AbstractMenuDialog {

    private BeginningLevelChooser beginningLevelSlider;
    private InputButtonTable inputButtons;
    private Button playButton;

    public MenuMarathonScreen(final LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);
        Button highScoreButton = new FaButton(FontAwesome.COMMENT_STAR_TROPHY, app.skin);
        highScoreButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            showHighscores();
                                        }
                                    }
        );

        buttons.add(highScoreButton);
        addFocusableActor(highScoreButton);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {

        Table params = new Table();
        beginningLevelSlider = new BeginningLevelChooser(app, app.prefs.getInteger
                ("beginningLevel", 0), 9);

        // die möglichen Inputs aufzählen
        inputButtons = new InputButtonTable(app, app.prefs.getInteger("inputType", 0));

        params.row().padTop(40);
        params.add(new Label(app.TEXTS.get("labelBeginningLevel"), app.skin)).left();
        params.row();
        params.add(beginningLevelSlider);
        params.row().padTop(40);
        params.add(new Label(app.TEXTS.get("menuInputControl"), app.skin)).left();
        params.row();
        params.add(inputButtons);
        params.row().padBottom(40);
        params.add(inputButtons.getInputLabel()).center();

        addFocusableActor(inputButtons);
        addFocusableActor(beginningLevelSlider);

        menuTable.row();
        menuTable.add(params).expandY();

        playButton = new GlowLabelButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        menuTable.row();
        menuTable.add(playButton).minHeight(playButton.getPrefHeight() * 2f).top();
        addFocusableActor(playButton);

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
            new VetoDialog("Sorry, highscores are only saved in the native Android " +
                    "version of Lightblocks. Download it to your mobile!",
                    app.skin, getAvailableContentWidth() * .75f).show(getStage());
            return;
        }

        ScoreScreen scoreScreen = new ScoreScreen(app);
        scoreScreen.setGameModelId(MarathonModel.MODEL_MARATHON_ID + inputButtons.getSelectedInput());
        scoreScreen.addScoreToShow(app.savegame.getBestScore("marathon" +
                        inputButtons.getSelectedInput()),
                app.TEXTS.get("labelBestScores"));
        scoreScreen.setMaxCountingTime(1);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);
    }

    protected void beginNewGame() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameModelClass(MarathonModel.class);
        initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
        initGameParametersParams.setInputKey(inputButtons.getSelectedInput());

        // Einstellungen speichern
        app.prefs.putInteger("inputType", inputButtons.getSelectedInput());
        app.prefs.putInteger("beginningLevel", initGameParametersParams
                .getBeginningLevel());
        app.prefs.flush();

        try {
            PlayScreen.gotoPlayScreen((AbstractScreen) app.getScreen(), initGameParametersParams);
            hideImmediately();
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, getAvailableContentWidth() * .75f);
        }
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return playButton;
    }
}
