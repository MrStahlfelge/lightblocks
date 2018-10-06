package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Created by Benjamin Schulte on 03.10.2018.
 */

public class BackendManager {

    private final LocalPrefs prefs;
    private final Queue<BackendScore> enqueuedScores = new Queue<BackendScore>();
    private final String platformString;
    private final BackendClient backendClient;
    private final HashMap<String, CachedScoreboard> latestScores = new HashMap<String, CachedScoreboard>();
    private final HashMap<String, CachedScoreboard> bestScores = new HashMap<String, CachedScoreboard>();
    private boolean authenticated;
    private BackendScore currentlySendingScore;

    public BackendManager(LocalPrefs prefs) {
        backendClient = new BackendClient();

        this.prefs = prefs;

        backendClient.setUserId(prefs.getBackendUserId());
        backendClient.setUserPass(prefs.getBackendUserPassKey());

        switch (Gdx.app.getType()) {
            case Android:
                platformString = "android";
                break;
            case WebGL:
                platformString = "web";
                break;
            default:
                platformString = "desktop";
        }
    }

    @Nonnull
    public BackendClient getBackendClient() {
        return backendClient;
    }

    public boolean hasUserId() {
        return backendClient.hasUserId();
    }

    public void setCredentials(String backendUserId, String backendUserKey) {
        if (!backendClient.getUserId().equals(backendClient.getUserId())) {
            prefs.saveBackendUser(backendUserId, backendUserKey);
            backendClient.setUserId(backendUserId);
            backendClient.setUserPass(backendUserKey);
        }
    }

    /**
     * gibt ein CachedScoreboard zurück
     *
     * @return null gdw das Modell gar kein BackendScoreboard hat, sonst garantiert ungleich null
     */
    public CachedScoreboard getCachedScoreboard(String gameMode, boolean latest) {
        if (!hasGamemodeScoreboard(gameMode))
            return null;

        CachedScoreboard scoreboard = (latest ? latestScores.get(gameMode) : bestScores.get(gameMode));
        if (scoreboard == null) {
            scoreboard = new CachedScoreboard(gameMode, latest);
            if (latest)
                latestScores.put(gameMode, scoreboard);
            else
                bestScores.put(gameMode, scoreboard);
        }
        return scoreboard;
    }

    public boolean isSendingScore() {
        synchronized (enqueuedScores) {
            return currentlySendingScore != null;
        }
    }

    /**
     * @return true, wenn ein Score noch in der Queue steht oder gerade gesendet wird
     */
    public boolean hasScoreEnqueued() {
        synchronized (enqueuedScores) {
            return  (currentlySendingScore != null || enqueuedScores.size > 0);
        }
    }

    /**
     * reiht den übergebenen Score in die Absendequeue ein, sofern er für ein Absenden ans Backend in Frage kommt.
     * Prüfungen werden hier durchgeführt
     *
     * @param score
     */
    public void enqueueAndSendScore(BackendScore score) {
        if (!hasGamemodeScoreboard(score.gameMode) || score.sortValue <= 0)
            return;

        synchronized (enqueuedScores) {
            enqueuedScores.addLast(score);
            score.scoreGainedMillis = TimeUtils.millis();
        }
        sendEnqueuedScores();
    }

    public boolean hasGamemodeScoreboard(String gameModelId) {
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_TOUCH_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_GRAVITY_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_GAMEPAD_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(PracticeModel.MODEL_PRACTICE_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(SprintModel.MODEL_SPRINT_ID))
            return true;

        return false;
    }

    public void sendEnqueuedScores() {
        synchronized (enqueuedScores) {
            // ältere als 4 Stunden nicht absenden, sondern aussortieren
            while (enqueuedScores.size >= 1 && TimeUtils.timeSinceMillis(enqueuedScores.first()
                    .scoreGainedMillis) / 1000 > 60 * 60 * 4)
                enqueuedScores.removeFirst();

            if (backendClient.hasUserId() && currentlySendingScore == null && enqueuedScores.size >= 1) {
                currentlySendingScore = enqueuedScores.removeFirst();

                backendClient.postScore(currentlySendingScore, new BackendClient.IBackendResponse<Void>() {
                    @Override
                    public void onFail(int statusCode, String errorMsg) {
                        if (statusCode == BackendClient.SC_NO_CONNECTION)
                            synchronized (enqueuedScores) {
                                // wieder zurück in die Queue und ein andermal probieren
                                enqueuedScores.addFirst(currentlySendingScore);
                                currentlySendingScore = null;
                            }
                        else
                            // wenn es einen Fehler gab, wird der nicht von selbst verschwinden - also ignorieren
                            onSuccess(null);
                    }

                    @Override
                    public void onSuccess(Void retrievedData) {
                        // und weiter mit dem nächsten Score in der Queue
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                invalidateScoreboardCache(currentlySendingScore.gameMode);
                                currentlySendingScore = null;
                                sendEnqueuedScores();
                            }
                        });
                    }
                });
            }
        }
    }

    protected void invalidateScoreboardCache(String gameMode) {
        // invalidate eines caches
        getCachedScoreboard(gameMode, true).setExpired();
        getCachedScoreboard(gameMode, false).setExpired();
    }

    // TODO Fetch der Scores auch nur, wenn nicht gerade Score gesendet wird oder in Schlange steht (falls
    // authentifiziert)
    // - sonst Anfrage zurückstellen

    public String getPlatformString() {
        return platformString;
    }

    public class CachedScoreboard {
        private static final int EXPIRATION_SECONDS_SUCCESS = 60 * 5;
        private static final int EXPIRATION_SECONDS_NO_CONNECTION = 10;
        private final String gameMode;
        private final boolean isLatest;
        public long expirationTimeMs;
        public boolean isFetching;
        public String lastErrorMsg;
        private List<ScoreListEntry> scoreboard;

        public CachedScoreboard(String gameMode, boolean isLatest) {
            this.gameMode = gameMode;
            this.isLatest = isLatest;
        }

        public String getGameMode() {
            return gameMode;
        }

        /**
         * @return das Scoreboard wenn vorhanden. Null wenn es expired ist, oder
         */
        public List<ScoreListEntry> getScoreboard() {
            if (!isExpired())
                return scoreboard;

            return null;
        }

        protected boolean isExpired() {
            return expirationTimeMs < TimeUtils.millis();
        }

        public boolean fetchIfExpired() {
            if (isExpired() && !isFetching && !isSendingScore()) {
                isFetching = true;
                lastErrorMsg = null;
                BackendClient.IBackendResponse<List<ScoreListEntry>> callback = new BackendClient
                        .IBackendResponse<List<ScoreListEntry>>() {
                    @Override
                    public void onFail(int statusCode, String errorMsg) {
                        lastErrorMsg = (errorMsg != null ? errorMsg : "HTTP" + String.valueOf(statusCode));
                        isFetching = false;
                        scoreboard = null;
                        expirationTimeMs = TimeUtils.millis() + (1000 *
                                (statusCode != BackendClient.SC_NO_CONNECTION ? EXPIRATION_SECONDS_SUCCESS :
                                        EXPIRATION_SECONDS_NO_CONNECTION));
                    }

                    @Override
                    public void onSuccess(List<ScoreListEntry> retrievedData) {
                        isFetching = false;
                        scoreboard = retrievedData;
                        expirationTimeMs = TimeUtils.millis() + (1000 * EXPIRATION_SECONDS_SUCCESS);

                    }
                };

                if (isLatest)
                    backendClient.fetchLatestScores(gameMode, callback);
                else
                    backendClient.fetchBestScores(gameMode, callback);

                return true;
            }

            return false;
        }

        public boolean fetchForced() {
            setExpired();
            return fetchIfExpired();
        }

        public void setExpired() {
            expirationTimeMs = 0;
            scoreboard = null;
        }

        public boolean isFetching() {
            return isFetching;
        }

        public String getLastErrorMsg() {
            return lastErrorMsg;
        }

        public boolean hasError() {
            return lastErrorMsg != null;
        }
    }
}
