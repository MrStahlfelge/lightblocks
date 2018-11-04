package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyActions;
import de.golfgl.lightblocks.scene2d.ReplayGameboard;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
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

    public ReplayDialog(LightBlocksGame app, Replay replay) {
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

        fastOrStopPlay = new FaTextButton(FontAwesome.BIG_FORWARD, app.skin, FontAwesome.SKIN_FONT_FA);
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
        replayGameboard = new ReplayGameboard(app, replay) {
            @Override
            protected void onTimeChange(int timeMs) {
                currentTimeLabel.setText(ScoreTable.formatTimeString(timeMs, 1));
                programmaticChange = true;
                seekSlider.setValue(timeMs / 100);
                programmaticChange = false;
            }
        };
        replayGameboard.setScale(.7f);

        Table contentTable = getContentTable();
        contentTable.add().expandX();
        replaysCell = contentTable.add(replayGameboard).size(replayGameboard.getWidth() * replayGameboard
                .getScaleX(), replayGameboard.getHeight() * replayGameboard.getScaleY());
        contentTable.add().expandX();

        contentTable.row();
        contentTable.add(currentTimeLabel).right();
        contentTable.add(seekSlider).fillX();
        contentTable.add(new ScaledLabel(ScoreTable.formatTimeString(lastStepTimeMs, 0), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .5f)).left();
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
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean isNowPlaying = replayGameboard != null && replayGameboard.isPlaying();
        if (isNowPlaying != isPlaying) {
            isPlaying = isNowPlaying;
            playPause.clearActions();
            playPause.setOrigin(Align.center);
            playPause.addAction(MyActions.getChangeSequence(new Runnable() {
                @Override
                public void run() {
                    playPause.setText(isPlaying ? FontAwesome.BIG_PAUSE : FontAwesome.BIG_PLAY);
                }
            }));
            fastOrStopPlay.clearActions();
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
}
