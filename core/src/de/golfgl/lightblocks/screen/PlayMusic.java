package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Übernimmt die Steuerung der Music (an/aus) sowie den gerade angenommenen Zustand siehe MusicState)
 */
public class PlayMusic {
    private final LightBlocksGame app;
    private Music slowMusic;
    private Music startMusic;
    private Music transitionMusic;
    private Music fastMusic;
    private boolean isPlayMusic;
    private MusicState state;
    private boolean shouldPlayFast;

    public PlayMusic(LightBlocksGame app) {
        this.app = app;
    }

    public void setPlayingMusic(boolean playMusic) {
        if (playMusic && !isPlayMusic) {
            state = MusicState.starting;
            startMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameStart")));
            startMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    completedTransition();
                }
            });
            transitionMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameTransition")));
            transitionMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    completedTransition();
                }
            });
            slowMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameSlow")));
            slowMusic.setLooping(true);
            fastMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameFast")));
            fastMusic.setLooping(true);

        } else if (!playMusic && isPlayMusic) {
            slowMusic.dispose();
            slowMusic = null;
            startMusic.dispose();
            startMusic = null;
            fastMusic.dispose();
            fastMusic = null;
            transitionMusic.dispose();
            transitionMusic = null;
        }
        isPlayMusic = playMusic;
    }

    public void play() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.play();
                    break;
                case starting:
                    startMusic.play();
                    break;
                case transitioning:
                    transitionMusic.play();
                    break;
                case playingFast:
                    fastMusic.play();
                    break;
            }
    }

    public void pause() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.pause();
                    break;
                case starting:
                    startMusic.pause();
                    break;
                case transitioning:
                    transitionMusic.pause();
                    break;
                case playingFast:
                    fastMusic.pause();
                    break;
            }
    }

    public void stop() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    slowMusic.stop();
                    break;
                case starting:
                    startMusic.stop();
                    break;
                case transitioning:
                    transitionMusic.stop();
                    break;
                case playingFast:
                    fastMusic.stop();
                    break;
            }
    }

    public void dispose() {
        setPlayingMusic(false);
    }

    public void setFastPlay(boolean fastPlay) {
        if (this.shouldPlayFast != fastPlay) {

            switch (state) {
                case playingFast:
                case playingSlowly:
                    stop();
                    state = MusicState.transitioning;
                    play();
                    break;
            }

            this.shouldPlayFast = fastPlay;
        }
    }

    private void completedTransition() {
        if (shouldPlayFast) {
            state = MusicState.playingFast;
        } else {
            state = MusicState.playingSlowly;
        }
        play();
    }

    private enum MusicState {starting, playingSlowly, transitioning, playingFast}
}
