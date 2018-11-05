package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyActions;
import de.golfgl.lightblocks.scene2d.ReplayGameboard;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.scene2d.TouchableSlider;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 03.11.2018.
 */

public class ReplayDialog extends AbstractFullScreenDialog {

    private final Cell replaysCell;
    private final TextButton playPause;
    private final TextButton fastOrStopPlay;
    private ReplayGameboard replayGameboard;
    private boolean isPlaying;
    private boolean programmaticChange;

    public ReplayDialog(LightBlocksGame app, Replay replay, String gameModeLabel, String performerLabel) {
        super(app);

        TextButton rewind = new FaTextButton(FontAwesome.BIG_FASTBW, app.skin, FontAwesome.SKIN_FONT_FA);
        rewind.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayGameboard.windToPreviousNextPiece();
            }
        });
        addFocusableActor(rewind);
        rewind.getLabel().setFontScale(.7f);

        playPause = new FaTextButton(FontAwesome.BIG_PLAY, app.skin, FontAwesome.SKIN_FONT_FA);
        playPause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (replayGameboard.isPlaying())
                    replayGameboard.pauseReplay();
                else
                    replayGameboard.playReplay();
            }
        });
        addFocusableActor(playPause);
        playPause.setTransform(true);
        playPause.getLabel().setFontScale(.7f);

        fastOrStopPlay = new FaTextButton(FontAwesome.ROTATE_RIGHT, app.skin, FontAwesome.SKIN_FONT_FA);
        fastOrStopPlay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (replayGameboard.isPlaying())
                    replayGameboard.playFast();
                else
                    replayGameboard.windToFirstStep();
            }
        });
        addFocusableActor(fastOrStopPlay);
        fastOrStopPlay.setTransform(true);
        fastOrStopPlay.getLabel().setFontScale(.7f);

        TextButton forward = new FaTextButton(FontAwesome.BIG_FASTFW, app.skin, FontAwesome.SKIN_FONT_FA);
        forward.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayGameboard.pauseReplay();
                replayGameboard.windToNextDrop();
            }
        });
        addFocusableActor(forward);
        forward.getLabel().setFontScale(.7f);

        final ScaledLabel currentTimeLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f);
        Replay.ReplayStep lastStep = replay.getLastStep();
        int lastStepTimeMs = lastStep != null ? lastStep.timeMs : 0;
        final TouchableSlider seekSlider = new TouchableSlider(0, lastStepTimeMs / 100, 1, false, app.skin) {
            @Override
            protected float getControllerScrollStepSize() {
                return Math.max(getStepSize(), getMaxValue() / 15);
            }
        };
        seekSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!programmaticChange) {
                    replayGameboard.windToTimePos((int) seekSlider.getValue() * 100);
                }
            }
        });
        final LeftInfoGroup extraInfo = new LeftInfoGroup(gameModeLabel, performerLabel);
        replayGameboard = new ReplayGameboard(app, replay) {
            @Override
            protected void onTimeChange(int timeMs) {
                currentTimeLabel.setText(ScoreTable.formatTimeString(timeMs, 1));
                programmaticChange = true;
                seekSlider.setValue(timeMs / 100);
                programmaticChange = false;
            }

            @Override
            protected void onScoreChange(int score) {
                extraInfo.setScore(score);
            }
        };
        replayGameboard.setScale(.7f);

        Table contentTable = getContentTable();
        contentTable.add(extraInfo).uniformX().fill();
        replaysCell = contentTable.add(replayGameboard).size(replayGameboard.getWidth() * replayGameboard
                .getScaleX(), replayGameboard.getHeight() * replayGameboard.getScaleY());
        contentTable.add(new RightInfoGroup()).uniformX().fill();

        contentTable.row();
        contentTable.add(currentTimeLabel).uniformX().right();
        contentTable.add(seekSlider).fillX();
        contentTable.add(new ScaledLabel(ScoreTable.formatTimeString(lastStepTimeMs, 1), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .5f)).uniformX().left();
        addFocusableActor(seekSlider);

        Table buttonsLeft = new Table();
        buttonsLeft.defaults().uniform().pad(10);
        buttonsLeft.add(playPause);
        buttonsLeft.add(fastOrStopPlay);
        buttonsLeft.add(rewind);
        buttonsLeft.add(forward);

        getButtonTable().add(buttonsLeft).width(replayGameboard.getWidth() * replayGameboard.getScaleX())
                .padLeft(0).padRight(0);
        // mittige Ausrichtung erzwingen
        getButtonTable().add().width(closeButton.getPrefWidth());

        replayGameboard.playReplay();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean isNowPlaying = replayGameboard != null && replayGameboard.isPlaying();
        if (isNowPlaying != isPlaying) {
            isPlaying = isNowPlaying;
            playPause.setOrigin(Align.center);
            playPause.addAction(MyActions.getChangeSequence(new Runnable() {
                @Override
                public void run() {
                    playPause.setText(isPlaying ? FontAwesome.BIG_PAUSE : FontAwesome.BIG_PLAY);
                }
            }));
            fastOrStopPlay.setOrigin(Align.center);
            fastOrStopPlay.addAction(MyActions.getChangeSequence(new Runnable() {
                @Override
                public void run() {
                    fastOrStopPlay.setText(isPlaying ? FontAwesome.BIG_FORWARD : FontAwesome.ROTATE_RIGHT);
                }
            }));
        }
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return playPause;
    }

    private class RightInfoGroup extends WidgetGroup {
        private final Label gameType;

        public RightInfoGroup() {
            gameType = new ScaledLabel("REPLAY", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            addActor(gameType);
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            gameType.pack();
            gameType.setPosition(getWidth() - gameType.getPrefWidth(),
                    getHeight() - 10 - gameType.getPrefHeight()
                            - (BlockActor.blockWidth * .8f - gameType.getPrefHeight()) / 2);
        }
    }

    private class LeftInfoGroup extends WidgetGroup {
        private final ScoreLabel scoreNum;
        private final Table scoreTable;
        private final Table gameInfoLabels;

        public LeftInfoGroup(String gameModeLabel, String performerLabel) {
            scoreTable = new Table();
            scoreTable.defaults().height(BlockActor.blockWidth * .8f);
            scoreTable.row();
            Label scoreLabel = new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin);
            scoreTable.add(scoreLabel).right().bottom().padBottom(-2).spaceRight(3);

            scoreNum = new ScoreLabel(8, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            scoreNum.setMaxCountingTime(.3f);
            scoreNum.setCountingSpeed(2000);
            scoreTable.add(scoreNum).left().colspan(3);

            addActor(scoreTable);

            gameInfoLabels = new Table();

            if (gameModeLabel != null) {
                gameInfoLabels.row();
                gameInfoLabels.add(new ScaledLabel(gameModeLabel, app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f))
                        .left().pad(-5, 0, -3, 0);
            }
            if (performerLabel != null) {
                gameInfoLabels.row();
                gameInfoLabels.add(new ScaledLabel(performerLabel, app.skin, LightBlocksGame.SKIN_FONT_REG)).left()
                        .pad(-3, 0, -3, 0);
            }
            gameInfoLabels.setRotation(90);
            gameInfoLabels.setTransform(true);

            addActor(gameInfoLabels);
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            scoreTable.pack();
            scoreTable.setPosition(0, getHeight() - scoreTable.getHeight() - 10);

            gameInfoLabels.pack();
            gameInfoLabels.setPosition(gameInfoLabels.getHeight(), 0);
        }

        public void setScore(int score) {
            scoreNum.setScore(score);
        }

        @Override
        public float getPrefWidth() {
            return Math.max(super.getPrefWidth(), gameInfoLabels.getHeight());
        }
    }
}
