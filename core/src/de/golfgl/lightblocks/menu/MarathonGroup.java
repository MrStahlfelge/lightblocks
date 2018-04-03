package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 15.02.2018.
 */

public class MarathonGroup extends Table implements SinglePlayerScreen.IGameModeGroup {
    private static final String PREF_KEY_INPUT = "inputType";
    private static final String PREF_KEY_LEVEL = "beginningLevel";
    private final Cell choseInputCell;
    private final ScaledLabel choseInputLabel;
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
                (PREF_KEY_LEVEL, 0), 9) {
            @Override
            protected void onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(playButton);
            }
        };

        // die möglichen Inputs aufzählen
        inputButtons = new InputButtonTable(app, app.prefs.getInteger(PREF_KEY_INPUT, 0)) {
            @Override
            public boolean onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(playButton);
                return super.onControllerDefaultKeyDown();
            }
        };
        inputButtons.setExternalChangeListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshScores(0);
                menuScreen.onGameModelIdChanged();

                setInputButtonTableVisibility();
            }
        });

        params.row().padTop(15);
        params.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel"), app.skin, LightBlocksGame.SKIN_FONT_BIG))
                .left();
        params.row();
        params.add(beginningLevelSlider);
        params.row().padTop(15);
        choseInputLabel = new ScaledLabel(app.TEXTS.get("menuInputControl"), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        params.add(choseInputLabel).left();
        params.row();
        choseInputCell = params.add();
        params.row();
        params.add(inputButtons.getInputLabel()).center();
        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        params.row();
        params.add(playButton).minHeight(playButton.getPrefHeight() * 2f).top().fillX();
        menuScreen.addFocusableActor(playButton);

        menuScreen.addFocusableActor(inputButtons);
        menuScreen.addFocusableActor(beginningLevelSlider.getSlider());
        setInputButtonTableVisibility();

        row();
        add(new ScaledLabel(app.TEXTS.get("labelMarathon"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        row();
        add(params).expandY();

        row();
        scoresGroup = new ScoresGroup(app);
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();

        // TODO erst auslösen wenn Seite erstmals angezeigt wird
        refreshScores(0);
    }

    protected void setInputButtonTableVisibility() {
        boolean inputChoserVisible = inputButtons.getEnabledInputCount() != 1;
        choseInputCell.setActor(inputChoserVisible ? inputButtons : null);
        choseInputLabel.setVisible(inputChoserVisible);
        inputButtons.getInputLabel().setVisible(inputChoserVisible);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // die InputButtons trotzdem ihren Kram machen lassen - sonst erscheinen sie nicht :-)
        if (!inputButtons.hasParent())
            inputButtons.act(delta);
    }

    public String getGameModelId() {
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
        initGameParametersParams.setGameMode(InitGameParameters.GameMode.Marathon);
        initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
        initGameParametersParams.setInputKey(inputButtons.getSelectedInput());

        // Einstellungen speichern
        app.prefs.putInteger(PREF_KEY_INPUT, inputButtons.getSelectedInput());
        app.prefs.putInteger(PREF_KEY_LEVEL, initGameParametersParams
                .getBeginningLevel());
        app.prefs.flush();

        try {
            PlayScreen.gotoPlayScreen((AbstractScreen) app.getScreen(), initGameParametersParams);
            menuScreen.gameStarted(true);
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, menuScreen.getAvailableContentWidth() * .75f).show(getStage());
        }
    }

    public Actor getConfiguredDefaultActor() {
        return playButton;
    }
}
