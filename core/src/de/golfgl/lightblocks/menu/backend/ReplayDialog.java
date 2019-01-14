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
    private ReplayGameboard replayGameboard2;
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
                if (replayGameboard.isPlaying()) {
                    replayGameboard.playFast();
                    if (replayGameboard2 != null)
                        replayGameboard2.playFast();
                } else
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
                    int timeMs = (int) seekSlider.getValue() * 100;
                    replayGameboard.windToTimePos(timeMs);
                    if (replayGameboard2 != null)
                        replayGameboard2.windToTimePos(timeMs);
                }
            }
        });
        final LeftInfoGroup extraInfo = new LeftInfoGroup(gameModeLabel, performerLabel);
        final RightInfoGroup rightInfo = new RightInfoGroup();
        replayGameboard = new ReplayGameboard(app, replay) {
            @Override
            protected void onTimeChange(int timeMs) {
                currentTimeLabel.setText(ScoreTable.formatTimeString(timeMs, 1));
                programmaticChange = true;
                seekSlider.setValue(timeMs / 100);
                programmaticChange = false;

                if (replayGameboard2 != null && Math.abs(replayGameboard2.getCurrentTime() - timeMs) > 1000) {
                    replayGameboard2.windToTimePos(timeMs);
                }
            }

            @Override
            protected void onScoreChange(int score) {
                extraInfo.setScore(score);
            }

            @Override
            protected void onClearedLinesChange(int clearedLines) {
                rightInfo.setLines(clearedLines);
            }

            @Override
            protected void onAdditionalDelayTimeAdded(float additionalTime) {
                if (replayGameboard2 != null)
                    replayGameboard2.addAdditionalDelayTime(additionalTime);
            }
        };
        replayGameboard.setScale(.7f);

        Table contentTable = getContentTable();
        contentTable.add(extraInfo).uniformX().fill();
        replaysCell = contentTable.add(replayGameboard);
        setCellSizeToGameboard(replaysCell, replayGameboard);
        contentTable.add(rightInfo).uniformX().fill();

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

    private void setCellSizeToGameboard(Cell replaysCell, ReplayGameboard replayGameboard) {
        replaysCell.size(replayGameboard.getWidth() * replayGameboard
                .getScaleX(), replayGameboard.getHeight() * replayGameboard.getScaleY());
    }

    public void addSecondReplay(Replay replay2) {
        replayGameboard2 = new ReplayGameboard(app, replay2) {
            @Override
            protected void onAdditionalDelayTimeAdded(float additionalTime) {
                replayGameboard.addAdditionalDelayTime(additionalTime);
            }
        };
        replayGameboard.setScale(.35f);
        replayGameboard2.setScale(.35f);

        Table bothReplays = new Table();
        Cell<ReplayGameboard> cell1 = bothReplays.add(replayGameboard).pad(30, 0, 30, 30);
        Cell<ReplayGameboard> cell2 = bothReplays.add(replayGameboard2).pad(30, 30, 30, 0);
        setCellSizeToGameboard(cell1, replayGameboard);
        setCellSizeToGameboard(cell2, replayGameboard2);
        bothReplays.validate();
        replaysCell.setActor(bothReplays).size(bothReplays.getPrefWidth(), bothReplays.getPrefHeight());

        replayGameboard2.playReplay();

        // TODO wenn das zweite länger ist als das erste, dann bis dahin Abspielen zulassen (nach Spielende, DONE, TIMESUP etc)
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

        if (replayGameboard2 != null) {
            if (replayGameboard.isPlaying() && !replayGameboard2.isPlaying())
                replayGameboard2.playReplay();
            else if (!replayGameboard.isPlaying() && replayGameboard2.isPlaying())
                replayGameboard2.pauseReplay();
        }
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return playPause;
    }

    public void windToTimePos(int timePos) {
        replayGameboard.windToTimePos(timePos);
    }

    private class RightInfoGroup extends WidgetGroup {
        private final Label gameType;
        private final ScoreLabel linesNum;
        private final Table scoreTable;

        public RightInfoGroup() {
            gameType = new ScaledLabel("REPLAY", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            addActor(gameType);

            scoreTable = new Table();
            Label scoreLines = new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin);
            linesNum = new ScoreLabel(3, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            linesNum.setCountingSpeed(100);
            linesNum.setMaxCountingTime(.3f);
            scoreTable.add(scoreLines).padBottom(-2).right();
            scoreTable.row();
            scoreTable.add(linesNum).pad(-3, 0, -5, 0);

            addActor(scoreTable);
        }

        @Override
        public void act(float delta) {
            float oldPrefWidth = linesNum.getPrefWidth();
            super.act(delta);
            if (oldPrefWidth != linesNum.getPrefWidth()) {
                scoreTable.pack();
                scoreTable.setPosition(getWidth() - scoreTable.getPrefWidth(), 0);
            }
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            gameType.pack();
            scoreTable.pack();
            gameType.setPosition(getWidth() - gameType.getPrefWidth(),
                    getHeight() - 10 - gameType.getPrefHeight()
                            - (BlockActor.blockWidth * .8f - gameType.getPrefHeight()) / 2);
            scoreTable.setPosition(getWidth() - scoreTable.getPrefWidth(), 0);
        }

        void setLines(long lines) {
            linesNum.setScore(lines);
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
                        .pad(-1, 1, -3, 0);
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
