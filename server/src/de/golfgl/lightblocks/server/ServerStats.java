package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.concurrent.TimeUnit;

public class ServerStats {
    public static final String TAG_LOG = "STATS";
    public int matchesStarted;
    public int matchesEnded;
    private int playersConnectedOverall;
    private int playersCurrentlyConnected;
    private long secondsConnectedOverall;
    private long resetMs;

    public ServerStats() {
        reset();
    }

    public int getPlayersCurrentlyConnected() {
        return playersCurrentlyConnected;
    }

    public void outputAndResetAfter(long seconds) {
        if ((TimeUtils.millis() - resetMs) / 1000 >= seconds) {
            output();
            reset();
        }
    }

    public void reset() {
        matchesEnded = 0;
        matchesStarted = 0;
        playersConnectedOverall = 0;
        secondsConnectedOverall = 0;
        resetMs = TimeUtils.millis();
    }

    public void playerConnected() {
        synchronized (this) {
            playersCurrentlyConnected++;
            playersConnectedOverall++;
        }
    }

    public void playerDisconnected(long secondsConnected) {
        synchronized (this) {
            playersCurrentlyConnected--;
            secondsConnectedOverall = secondsConnectedOverall
                    + Math.max(0, secondsConnected);
        }
    }

    public void output() {
        long millis = TimeUtils.millis() - resetMs;
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        Gdx.app.log(TAG_LOG, "for the last " + String.format("%d days %d hours %d minutes %d seconds", days, hours, minutes, seconds));
        Gdx.app.log(TAG_LOG, "Matches started: " + matchesStarted);
        Gdx.app.log(TAG_LOG, "Matches ended: " + matchesEnded);
        Gdx.app.log(TAG_LOG, "Players connected: " + playersConnectedOverall + " overall, " + playersCurrentlyConnected + " currently connected");
        int playersOverallNotConnectedAnymore = playersConnectedOverall - playersCurrentlyConnected;
        if (playersOverallNotConnectedAnymore > 0)
            Gdx.app.log(TAG_LOG, "Player average connection time (sec): " + (secondsConnectedOverall / playersOverallNotConnectedAnymore));
    }
}
