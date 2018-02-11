package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.BeginningLevelChooser;
import de.golfgl.lightblocks.scenes.FaButton;
import de.golfgl.lightblocks.scenes.GlowLabelButton;
import de.golfgl.lightblocks.scenes.InputButtonTable;
import de.golfgl.lightblocks.scenes.ScaledLabel;
import de.golfgl.lightblocks.scenes.ScoresGroup;
import de.golfgl.lightblocks.scenes.VetoDialog;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class MenuMarathonScreen extends AbstractMenuDialog {

    private BeginningLevelChooser beginningLevelSlider;
    private InputButtonTable inputButtons;
    private Button playButton;
    private ScoresGroup scoresGroup;

    public MenuMarathonScreen(final LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);
        Button highScoreButton = new FaButton(FontAwesome.COMMENT_STAR_TROPHY, app.skin);
        highScoreButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {

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
        inputButtons.setExternalChangeListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshScores();
            }
        });

        params.row().padTop(30);
        params.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel"), app.skin, LightBlocksGame.SKIN_FONT_BIG))
                .left();
        params.row();
        params.add(beginningLevelSlider);
        params.row().padTop(30);
        params.add(new ScaledLabel(app.TEXTS.get("menuInputControl"), app.skin, LightBlocksGame.SKIN_FONT_BIG)).left();
        params.row();
        params.add(inputButtons);
        params.row();
        params.add(inputButtons.getInputLabel()).center();

        addFocusableActor(inputButtons);
        addFocusableActor(beginningLevelSlider.getSlider());

        menuTable.row();
        menuTable.add(params);

        playButton = new GlowLabelButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        menuTable.row();
        menuTable.add(playButton).minHeight(playButton.getPrefHeight() * 2.5f).top();
        addFocusableActor(playButton);
        menuTable.row();
        scoresGroup = new ScoresGroup(app);
        menuTable.add(scoresGroup).expandY().fill();
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        // die Scores erstmals anzeigen
        if (getStage() == null && stage != null)
            refreshScores();

        return super.show(stage, action);
    }

    protected void refreshScores() {
        // es ist nötig dass alles schon korrekt angezeigt wird, damit die Animationen der ScoreGroup
        // korrekt laufen. Also sicherstellen, oder zeitlich nach hinten schieben
        if (getStage() != null && !hasActions())
            scoresGroup.show(MarathonModel.MODEL_MARATHON_ID + inputButtons.getSelectedInput());
        else {
            // delay this
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    refreshScores();
                }
            }, 1f);
        }
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
