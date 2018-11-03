package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ReplayGameboard;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 03.11.2018.
 */

public class ReplayDialog extends AbstractFullScreenDialog {

    private final Cell replaysCell;
    private final FaButton playPause;
    private final FaButton fastOrStopPlay;
    private ReplayGameboard replayGameboard;
    private boolean isPlaying;

    public ReplayDialog(LightBlocksGame app) {
        super(app);

        replaysCell = getContentTable().add();

        FaButton rewind = new FaButton(FontAwesome.BIG_FASTBW, app.skin);
        rewind.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayGameboard.windToPreviousNextPiece();
            }
        });
        addFocusableActor(rewind);

        playPause = new FaButton(FontAwesome.BIG_PLAY, app.skin);
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

        fastOrStopPlay = new FaButton(FontAwesome.BIG_FORWARD, app.skin);
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

        FaButton forward = new FaButton(FontAwesome.BIG_FASTFW, app.skin);
        forward.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayGameboard.pauseReplay();
                replayGameboard.windToNextDrop();
            }
        });
        addFocusableActor(forward);

        // TODO das ganze rechts neben das Spielfeld, mit Scoreanzeige
        getButtonTable().add(playPause).padRight(0).padLeft(0);
        getButtonTable().add(fastOrStopPlay).padRight(0).padLeft(0);
        getButtonTable().add(rewind).padRight(0).padLeft(0);
        getButtonTable().add(forward).padRight(0).padLeft(0);
    }

    public ReplayDialog addReplay(Replay replay) {
        replayGameboard = new ReplayGameboard(app, replay);
        replayGameboard.setScale(.7f);
        replaysCell.setActor(replayGameboard);

        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean isNowPlaying = replayGameboard != null && replayGameboard.isPlaying();
        if (isNowPlaying != isPlaying) {
            isPlaying = isNowPlaying;
            if (isPlaying) {
                playPause.setFaText(FontAwesome.BIG_PAUSE);
                fastOrStopPlay.setFaText(FontAwesome.BIG_FORWARD);
            } else {
                playPause.setFaText(FontAwesome.BIG_PLAY);
                fastOrStopPlay.setFaText(FontAwesome.ROTATE_RIGHT);
            }
        }
    }
}
