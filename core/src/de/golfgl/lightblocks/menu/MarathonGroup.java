package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 15.02.2018.
 */

public class MarathonGroup extends Table {
    private SinglePlayerScreen menuScreen;
    private BeginningLevelChooser beginningLevelSlider;
    private InputButtonTable inputButtons;
    private Button playButton;
    private ScoresGroup scoresGroup;
    private LightBlocksGame app;

    public MarathonGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
        this.menuScreen = myParentScreen;
        this.app = app;

        Table params = new Table();
        beginningLevelSlider = new BeginningLevelChooser(app, app.prefs.getInteger
                ("beginningLevel", 0), 9);

        // die möglichen Inputs aufzählen
        inputButtons = new InputButtonTable(app, app.prefs.getInteger("inputType", 0));
        inputButtons.setExternalChangeListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshScores(0);
                // nur wenn es der aktive ist
                menuScreen.onGameModelChange(getGameModelId());
            }
        });

        params.row().padTop(20);
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

        menuScreen.addFocusableActor(inputButtons);
        menuScreen.addFocusableActor(beginningLevelSlider.getSlider());

        row();
        add(params).expandY();

        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        row();
        add(playButton).minHeight(playButton.getPrefHeight() * 2f).top();
        menuScreen.addFocusableActor(playButton);
        row();
        scoresGroup = new ScoresGroup(app);
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();
        refreshScores(0);
    }

    protected String getGameModelId() {
        return MarathonModel.MODEL_MARATHON_ID + inputButtons.getSelectedInput();
    }

    protected void refreshScores(final int tryCount) {
        // es ist nötig dass alles schon korrekt angezeigt wird, damit die Animationen der ScoreGroup
        // korrekt laufen. Also sicherstellen, oder zeitlich nach hinten schieben
        // Um ein Memory Leak zu verhindern, gibt es maxTries. Ansonsten würde die Aktion ewig in der Warteschleife
        // hängen, wenn der Dialog einmal abgeräumt ist
        if (getStage() != null && !hasActions())
            scoresGroup.show(getGameModelId());
        else if (tryCount < 5) {
            // delay this
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    refreshScores(tryCount + 1);
                }
            }, 2f);
        }
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
            menuScreen.hideImmediately();
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, menuScreen.getAvailableContentWidth() * .75f).show(getStage());
        }
    }

    public Actor getConfiguredDefaultActor() {
        return playButton;
    }
}
