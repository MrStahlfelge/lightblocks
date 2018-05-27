package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 15.02.2018.
 */

public abstract class SimpleGameModeGroup extends Table implements SinglePlayerScreen.IGameModeGroup {
    protected ScaledLabel choseInputLabel;
    protected Table params;
    protected Cell choseInputCell;
    protected SinglePlayerScreen menuScreen;
    protected BeginningLevelChooser beginningLevelSlider;
    protected InputButtonTable inputButtons;
    protected Button playButton;
    protected ScoresGroup scoresGroup;
    protected LightBlocksGame app;

    public SimpleGameModeGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
        this.menuScreen = myParentScreen;
        this.app = app;

        params = new Table();
        beginningLevelSlider = new BeginningLevelChooser(app, app.localPrefs.getMarathonBeginningLevel(),
                getMaxBeginningValue()) {
            @Override
            protected void onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(playButton);
            }
        };

        // die möglichen Inputs aufzählen
        inputButtons = new InputButtonTable(app, app.localPrefs.getMarathonLastUsedInput()) {
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

        row();
        add(new ScaledLabel(getGameModeTitle(), app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        fillParamsTable(app);

        row();
        add(params).expandY();

        row();
        scoresGroup = new ScoresGroup(app, isShowBestTime());
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();

        // TODO erst auslösen wenn Seite erstmals angezeigt wird
        refreshScores(0);
    }

    protected boolean isShowBestTime() {
        return false;
    }

    protected void fillParamsTable(LightBlocksGame app) {
        params.row().padTop(15);
        params.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel"), app.skin, LightBlocksGame.SKIN_FONT_BIG))
                .left();
        params.row();
        params.add(beginningLevelSlider);
        params.row().padTop(15);
        choseInputLabel = new ScaledLabel(app.TEXTS.get("menuInputControl"), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        addInputButtonsToParams();
        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        params.add(playButton).minHeight(playButton.getPrefHeight() * 2f).top().fillX();
        menuScreen.addFocusableActor(playButton);

        menuScreen.addFocusableActor(inputButtons);
        menuScreen.addFocusableActor(beginningLevelSlider.getSlider());
        setInputButtonTableVisibility();
    }

    protected void addInputButtonsToParams() {
        params.add(choseInputLabel).left();
        params.row();
        choseInputCell = params.add();
        params.row();
        params.add(inputButtons.getInputLabel()).center();
        params.row();
    }

    protected abstract int getMaxBeginningValue();

    protected abstract String getGameModeTitle();

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

    public abstract String getGameModelId();

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
        InitGameParameters initGameParametersParams = getInitGameParameters();

        // Einstellungen speichern
        app.localPrefs.saveMarathonLevelAndInput(initGameParametersParams.getBeginningLevel(), inputButtons
                .getSelectedInput());

        try {
            PlayScreen.gotoPlayScreen((AbstractScreen) app.getScreen(), initGameParametersParams);
            menuScreen.gameStarted(true);
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, menuScreen.getAvailableContentWidth() * .75f).show(getStage());
        }
    }

    protected abstract InitGameParameters getInitGameParameters();

    public Actor getConfiguredDefaultActor() {
        return playButton;
    }

    public static class MarathonGroup extends SimpleGameModeGroup {

        public MarathonGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
            super(myParentScreen, app);
        }

        @Override
        public String getGameModelId() {
            return MarathonModel.MODEL_MARATHON_ID + inputButtons.getSelectedInput();
        }

        @Override
        protected String getGameModeTitle() {
            return app.TEXTS.get("labelMarathon");
        }

        @Override
        protected int getMaxBeginningValue() {
            return 9;
        }

        @Override
        protected InitGameParameters getInitGameParameters() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.Marathon);
            initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
            initGameParametersParams.setInputKey(inputButtons.getSelectedInput());
            return initGameParametersParams;
        }
    }

    public static class PracticeModeGroup extends SimpleGameModeGroup {

        public PracticeModeGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
            super(myParentScreen, app);
        }

        @Override
        public String getGameModelId() {
            return PracticeModel.MODEL_PRACTICE_ID;
        }

        @Override
        protected String getGameModeTitle() {
            return app.TEXTS.get("labelModel_practice");
        }

        @Override
        protected boolean isShowBestTime() {
            return true;
        }

        @Override
        protected void fillParamsTable(LightBlocksGame app) {
            row().pad(20, 20, 0, 20);
            ScaledLabel introLabel = new ScaledLabel(app.TEXTS.get("introModelPractice"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            introLabel.setWrap(true);
            introLabel.setAlignment(Align.center);
            add(introLabel).bottom().fillX().expandX();

            super.fillParamsTable(app);
        }

        @Override
        protected InitGameParameters getInitGameParameters() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.Practice);
            initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
            initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYORTOUCH);
            return initGameParametersParams;
        }

        @Override
        protected int getMaxBeginningValue() {
            return 14;
        }

        @Override
        protected void addInputButtonsToParams() {
            // nichts tun
        }

        @Override
        protected void setInputButtonTableVisibility() {
            // nichts tun, es gibt keine
        }
    }
}