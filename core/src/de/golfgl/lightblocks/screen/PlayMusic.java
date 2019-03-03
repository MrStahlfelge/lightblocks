package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import de.golfgl.lightblocks.LightBlocksGame;

public class PlayMusic {
    private final LightBlocksGame app;
    private Music playingMusic;
    private Music startMusic;
    private boolean isPlayMusic;
    private MusicState state = MusicState.starting;

    public PlayMusic(LightBlocksGame app) {
        this.app = app;
    }

    public void setPlayingMusic(boolean playMusic) {
        if (playMusic && !isPlayMusic) {
            startMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameStart")));
            startMusic.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    state = MusicState.playingSlowly;
                    playingMusic.play();
                }
            });
            playingMusic = Gdx.audio.newMusic(Gdx.files.internal(app.getSoundAssetFilename("gameSlow")));
            playingMusic.setVolume(1f);                 // sets the volume to half the maximum volume
            playingMusic.setLooping(true);
        } else if (!playMusic && isPlayMusic) {
            playingMusic.dispose();
            playingMusic = null;
            startMusic.dispose();
            startMusic = null;
        }
        isPlayMusic = playMusic;
    }

    public void play() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    playingMusic.play();
                    break;
                case starting:
                    startMusic.play();
                    break;
            }
    }

    public void pause() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    playingMusic.pause();
                    break;
                case starting:
                    startMusic.pause();
                    break;
            }
    }

    public void stop() {
        if (isPlayMusic)
            switch (state) {
                case playingSlowly:
                    playingMusic.stop();
                    break;
                case starting:
                    startMusic.stop();
                    break;
            }
    }

    public void dispose() {
        setPlayingMusic(false);
    }

    private enum MusicState {starting, playingSlowly}
}
