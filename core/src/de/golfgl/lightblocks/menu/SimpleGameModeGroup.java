package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.CleanGarbageModel;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.ModernFreezeModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.RetroMarathonModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.scene2d.FaRadioButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableSlider;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.input.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 15.02.2018.
 */

public abstract class SimpleGameModeGroup extends Table implements SinglePlayerScreen.IGameModeGroup {
    protected Table params;
    protected SinglePlayerScreen menuScreen;
    protected BeginningLevelChooser beginningLevelSlider;
    protected Button playButton;
    protected Cell playButtonCell;
    protected ScoresGroup scoresGroup;
    protected LightBlocksGame app;

    public SimpleGameModeGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
        this.menuScreen = myParentScreen;
        this.app = app;

        params = new Table();
        beginningLevelSlider = new BeginningLevelChooser(app, getPreselectedBeginningLevel(app),
                getMaxBeginningValue()) {
            @Override
            protected void onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(playButton);
            }
        };

        row();
        add(new ScaledLabel(getGameModeTitle(), app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        fillParamsTable(app);

        row();
        add(params).expandY().fill().pad(0, 20, 0, 20);

        row();
        scoresGroup = new ScoresGroup(app, isShowBestTime());
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();

        refreshScores(0);
    }

    protected int getPreselectedBeginningLevel(LightBlocksGame app) {
        return app.localPrefs.getMarathonBeginningLevel();
    }

    protected boolean isShowBestTime() {
        return false;
    }

    protected void fillParamsTable(LightBlocksGame app) {
        if (getMaxBeginningValue() > 0) {
            Table levelSliderTable = new Table();

            params.row().padTop(15);
            levelSliderTable.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel"), app.skin, LightBlocksGame.SKIN_FONT_BIG))

                    .left();
            levelSliderTable.row();
            levelSliderTable.add(beginningLevelSlider);
            params.add(levelSliderTable).expand().fill();
        }

        params.row();
        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        playButtonCell = params.add(playButton).minHeight(playButton.getPrefHeight() * 2f).center().fillX().expandY();
        menuScreen.addFocusableActor(playButton);

        menuScreen.addFocusableActor(beginningLevelSlider.getSlider());
    }

    protected abstract int getMaxBeginningValue();

    protected abstract String getGameModeTitle();

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

        savePreselectionSettings();

        try {
            PlayScreen.gotoPlayScreen(app, initGameParametersParams);
            menuScreen.gameStarted(true);
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, menuScreen.getAvailableContentWidth() * .75f).show(getStage());
        }
    }

    protected void savePreselectionSettings() {
        // Einstellungen speichern
        app.localPrefs.saveMarathonLevel(beginningLevelSlider.getValue());
    }

    protected abstract InitGameParameters getInitGameParameters();

    public Actor getConfiguredDefaultActor() {
        return playButton;
    }

    public static class MarathonGroup extends SimpleGameModeGroup {

        private static final int MARATHON_NORMAL = 1;
        private static final int MARATHON_GRAVITY = 2;
        private static final int MARATHON_RETRO = 3;
        private FaRadioButton<Integer> marathonType;
        private ScaledLabel marathonTypeDescription;

        public MarathonGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
            super(myParentScreen, app);
        }

        @Override
        public String getGameModelId() {
            switch (marathonType.getValue()) {
                case MARATHON_NORMAL:
                    return MarathonModel.MODEL_MARATHON_NORMAL_ID;
                case MARATHON_GRAVITY:
                    return MarathonModel.MODEL_MARATHON_GRAVITY_ID;
                case MARATHON_RETRO:
                    return RetroMarathonModel.MODEL_MARATHON_RETRO89;
            }

            return null;
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
            int intType = marathonType.getValue();
            initGameParametersParams.setGameMode(intType == MARATHON_RETRO ?
                    InitGameParameters.GameMode.MarathonRetro89 : InitGameParameters.GameMode.Marathon);
            initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
            initGameParametersParams.setInputKey(intType == MARATHON_GRAVITY ?
                    PlayScreenInput.KEY_ACCELEROMETER : PlayScreenInput.KEY_KEYORTOUCH);
            return initGameParametersParams;
        }

        @Override
        protected void fillParamsTable(LightBlocksGame app) {
            row().pad(10, 20, 10, 20);

            marathonType = new FaRadioButton<Integer>(app.skin, false);
            marathonType.addEntry(MARATHON_NORMAL, "",
                    app.TEXTS.get("marathonChooseTypeTitle" + String.valueOf(MARATHON_NORMAL)));
            marathonType.addEntry(MARATHON_RETRO, "",
                    app.TEXTS.get("marathonChooseTypeTitle" + String.valueOf(MARATHON_RETRO)));
            if (PlayGesturesInput.isInputTypeAvailable(PlayScreenInput.KEY_ACCELEROMETER))
                marathonType.addEntry(MARATHON_GRAVITY, "",
                        app.TEXTS.get("marathonChooseTypeTitle" + String.valueOf(MARATHON_GRAVITY)));
            marathonType.setValue(app.localPrefs.getMarathonLastUsedType());
            marathonType.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             refreshScores(0);
                                             changeDescription();
                                             menuScreen.onGameModelIdChanged();
                                         }
                                     }
            );

            Table marathonTypeTabel = new Table();
            ScaledLabel marathonTypeLabel = new ScaledLabel(app.TEXTS.get("marathonChooseTypeLabel"),
                    app.skin, app.SKIN_FONT_BIG);
            marathonTypeLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    marathonType.changeValue();
                }
            });
            marathonTypeTabel.add(marathonTypeLabel);
            marathonTypeTabel.row();
            marathonTypeTabel.add(marathonType);
            marathonTypeTabel.row().padTop(10);
            marathonTypeDescription = new ScaledLabel("\n", app.skin, LightBlocksGame.SKIN_FONT_REG);
            marathonTypeDescription.setWrap(true);
            marathonTypeDescription.setAlignment(Align.top);
            marathonTypeTabel.add(marathonTypeDescription).fill().expandX().height(marathonTypeDescription.getPrefHeight());

            add(marathonTypeTabel).expandX().fill();
            menuScreen.addFocusableActor(marathonType);

            changeDescription();

            super.fillParamsTable(app);
        }

        private void changeDescription() {
            marathonTypeDescription.setText(app.TEXTS.get("marathonChooseTypeDesc" + String.valueOf(marathonType.getValue())));
        }

        @Override
        protected void savePreselectionSettings() {
            app.localPrefs.saveMarathonLevelAndType(beginningLevelSlider.getValue(), marathonType.getValue());
        }
    }

    public static class PracticeModeGroup extends SimpleGameModeGroup {

        private FaRadioButton<Integer> modeType;

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
            row();
            modeType = new FaRadioButton<>(app.skin, false);
            modeType.setShowIndicator(false);
            modeType.addEntry(InitGameParameters.TYPE_CLASSIC, "", app.TEXTS.get("modeTypeClassic"));
            modeType.addEntry(InitGameParameters.TYPE_MODERN, "", app.TEXTS.get("modeTypeModern"));
            modeType.setValue(app.localPrefs.getLastUsedModeType());

            add(modeType).expandX().fill();
            menuScreen.addFocusableActor(modeType);

            row().pad(10, 20, 0, 20);
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
            initGameParametersParams.setModeType(modeType.getValue());
            return initGameParametersParams;
        }

        @Override
        protected int getMaxBeginningValue() {
            return PracticeModel.getMaxBeginningLevel(app);
        }

        @Override
        protected void savePreselectionSettings() {
            super.savePreselectionSettings();
            app.localPrefs.saveLastUsedModeType(modeType.getValue());
        }
    }

    public static class CleanGarbageModeGroup extends SimpleGameModeGroup {
        protected TouchableSlider garbageSizeSlider;

        public CleanGarbageModeGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
            super(myParentScreen, app);
        }

        @Override
        public String getGameModelId() {
            return CleanGarbageModel.MODEL_CLEAN_GARBAGE_ID;
        }

        @Override
        protected String getGameModeTitle() {
            return app.TEXTS.get("labelModel_typeB");
        }

        @Override
        protected boolean isShowBestTime() {
            return true;
        }

        @Override
        protected void fillParamsTable(LightBlocksGame app) {
            garbageSizeSlider = new TouchableSlider(1, 16, 1, false, app.skin);
            garbageSizeSlider.setValue(5);

            row().pad(40, 20, 0, 20);
            ScaledLabel introLabel = new ScaledLabel(app.TEXTS.get("goalModelCleanGarbage"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
            introLabel.setWrap(true);
            introLabel.setAlignment(Align.center);
            add(introLabel).bottom().fillX().expandX();

            params.row().padTop(15);
            params.add(getGarbagParamTable()).expand().fill();

            super.fillParamsTable(app);

            menuScreen.addFocusableActor(playButton);
        }

        private Table getGarbagParamTable() {
            final ScaledLabel beginningLevelLabel = new ScaledLabel("5 lines", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            garbageSizeSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    beginningLevelLabel.setText(String.valueOf((int)garbageSizeSlider.getValue()) + " lines");
                }
            });

            Table paramTable = new Table();
            paramTable.row();
            paramTable.add(garbageSizeSlider).minHeight(30).minWidth(200).right().fill();
            paramTable.add(beginningLevelLabel).left().spaceLeft(10).minWidth(beginningLevelLabel.getPrefWidth() * 1.1f);

            return paramTable;
        }

        @Override
        protected InitGameParameters getInitGameParameters() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.Clean);
            initGameParametersParams.setBeginningLevel(0);
            initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYORTOUCH);
            initGameParametersParams.setInitialGarbage((int)garbageSizeSlider.getValue());
            return initGameParametersParams;
        }

        @Override
        protected int getMaxBeginningValue() {
            return 0;
        }

        @Override
        protected void savePreselectionSettings() {
            // es gibt keine
        }
    }

    public static class SprintModeGroup extends SimpleGameModeGroup {
        private boolean isLocked = false;
        private TextButton lockMessage;

        public SprintModeGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
            super(myParentScreen, app);
        }

        @Override
        public String getGameModelId() {
            return SprintModel.MODEL_SPRINT_ID;
        }

        @Override
        protected String getGameModeTitle() {
            return app.TEXTS.get("labelModel_sprint40");
        }

        @Override
        protected boolean isShowBestTime() {
            return true;
        }

        @Override
        protected void fillParamsTable(LightBlocksGame app) {
            row().pad(40, 20, 0, 20);
            ScaledLabel introLabel = new ScaledLabel(app.TEXTS.get("goalModelSprint"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            introLabel.setWrap(true);
            introLabel.setAlignment(Align.center);
            add(introLabel).bottom().fillX().expandX();

            super.fillParamsTable(app);

            lockMessage = new FaTextButton(app.TEXTS.get("lockModelSprint"), app.skin, LightBlocksGame.SKIN_DEFAULT);
            lockMessage.getLabel().setWrap(true);
            menuScreen.addFocusableActor(lockMessage);
            switchLockedMode();
        }

        @Override
        protected InitGameParameters getInitGameParameters() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.Sprint);
            initGameParametersParams.setBeginningLevel(0);
            initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYORTOUCH);
            return initGameParametersParams;
        }

        @Override
        protected int getMaxBeginningValue() {
            return 0;
        }

        @Override
        protected void savePreselectionSettings() {
            // es gibt keine
        }

        @Override
        public Actor getConfiguredDefaultActor() {
            // diese Methode wird aufgerufen, wenn auf diese Seite gewechselt wird. Falls also die Mission
            // freigeschaltet wurde, wird hier aktualisiert
            switchLockedMode();

            return isLocked ? lockMessage : playButton;
        }

        private void switchLockedMode() {
            boolean isLocked = !SprintModel.isUnlocked(app);

            if (isLocked == this.isLocked)
                return;

            this.isLocked = isLocked;

            playButtonCell.setActor(isLocked ? lockMessage : playButton);
            playButtonCell.expandX().fillX();
        }
    }

    public static class ModernFreezeModeGroup extends SimpleGameModeGroup {
        private FaRadioButton<Integer> difficultyButton;

        public ModernFreezeModeGroup(SinglePlayerScreen singlePlayerScreen, LightBlocksGame app) {
            super(singlePlayerScreen, app);
        }

        @Override
        protected int getMaxBeginningValue() {
            return 0;
        }

        @Override
        protected String getGameModeTitle() {
            return app.TEXTS.get("labelModel_modernfreeze2");
        }

        @Override
        public String getGameModelId() {
            return ModernFreezeModel.MODEL_ID;
        }

        @Override
        protected void fillParamsTable(LightBlocksGame app) {
            row().pad(20, 20, 0, 20);
            ScaledLabel introLabel = new ScaledLabel(app.TEXTS.get("introModelModernFreeze"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            introLabel.setWrap(true);
            introLabel.setAlignment(Align.center);
            add(introLabel).bottom().fillX().expandX();

            params.row().padTop(15);
            params.add(new ScaledLabel(app.TEXTS.get("labelDifficulty"), app.skin,
                    LightBlocksGame.SKIN_FONT_BIG)).expandY().bottom();
            difficultyButton = new FaRadioButton<>(app.skin, false);
            difficultyButton.addEntry(ModernFreezeModel.DIFFICULTY_EASY, "",
                    app.TEXTS.get("labelDifficulty" + String.valueOf(ModernFreezeModel.DIFFICULTY_EASY)));
            difficultyButton.addEntry(ModernFreezeModel.DIFFICULTY_NORMAL, "",
                    app.TEXTS.get("labelDifficulty" + String.valueOf(ModernFreezeModel.DIFFICULTY_NORMAL)));
            difficultyButton.addEntry(ModernFreezeModel.DIFFICULTY_HARD, "",
                    app.TEXTS.get("labelDifficulty" + String.valueOf(ModernFreezeModel.DIFFICULTY_HARD)));
            difficultyButton.setValue(app.localPrefs.getFreezeDifficulty());

            params.row();
            params.add(difficultyButton);
            menuScreen.addFocusableActor(difficultyButton);

            super.fillParamsTable(app);
        }

        @Override
        protected void savePreselectionSettings() {
            app.localPrefs.saveFreezeDifficulty(difficultyButton.getValue());
        }

        @Override
        protected InitGameParameters getInitGameParameters() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.ModernFreeze);
            initGameParametersParams.setBeginningLevel(difficultyButton.getValue());
            initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYORTOUCH);
            return initGameParametersParams;
        }
    }
}