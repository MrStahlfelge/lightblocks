package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.RetroMarathonModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Created by Benjamin Schulte on 03.10.2018.
 */

public class BackendManager {

    public static final String PLATFORM_TV = "smarttv";
    public static final String PLATFORM_MOBILE = "mobile";
    public static final String PLATFORM_DESKTOP = "desktop";
    private final LocalPrefs prefs;
    private final Queue<BackendScore> enqueuedScores = new Queue<BackendScore>();
    private final String platformString;
    private final String osString;
    private final BackendClient backendClient;
    private final HashMap<String, CachedScoreboard> latestScores = new HashMap<String, CachedScoreboard>();
    private final HashMap<String, CachedScoreboard> bestScores = new HashMap<String, CachedScoreboard>();
    private List<MatchEntity> multiplayerMatchesList;
    private boolean authenticated;
    private BackendScore currentlySendingScore;
    private BackendClient.WelcomeResponse lastWelcomeResponse;
    private boolean isFetchingWelcomes;
    private long fetchWelcomesSinceTime;
    private long multiplayerMatchesLastFetchMs;
    private boolean isFetchingMultiplayerMatches;
    private boolean multiplayerMatchesLastFetchSuccessful;
    private String multiplayerLastFetchError;
    private MatchTurnRequestInfo playedTurnToUpload;
    private boolean uploadingPlayedTurn;
    private boolean fetchingFullMatchInfo;

    public BackendManager(LocalPrefs prefs) {
        backendClient = new BackendClient();

        this.prefs = prefs;

        backendClient.setUserId(prefs.getBackendUserId());
        backendClient.setUserPass(prefs.getBackendUserPassKey());

        switch (Gdx.app.getType()) {
            case Android:
                platformString = LightBlocksGame.isOnAndroidTV() ? PLATFORM_TV : PLATFORM_MOBILE;
                osString = "android";
                break;
            case iOS:
                platformString = PLATFORM_MOBILE;
                osString = "ios";
                break;
            case WebGL:
                platformString = LightBlocksGame.isWebAppOnMobileDevice() ? PLATFORM_MOBILE : PLATFORM_DESKTOP;
                osString = "webgl";
                break;
            default:
                platformString = PLATFORM_DESKTOP;
                osString = "desktop";
        }

        multiplayerMatchesList = new ArrayList<>();
        if (hasUserId()) {
            // einen eventuell noch zum Hochladen vorgemerkten laden
            playedTurnToUpload = prefs.getTurnToUpload();
        }

        fetchWelcomesSinceTime = prefs.getWelcomeMessagesTime();
    }

    @Nonnull
    public BackendClient getBackendClient() {
        return backendClient;
    }

    public boolean hasUserId() {
        return backendClient.hasUserId();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String ownUserId() {
        return backendClient.getUserId();
    }

    public void setCredentials(String backendUserId, String backendUserKey) {
        boolean deleted = (backendUserId == null && backendClient.getUserId() != null);

        prefs.saveBackendUser(backendUserId, backendUserKey);
        backendClient.setUserId(backendUserId);
        backendClient.setUserPass(backendUserKey);

        if (deleted) {
            multiplayerMatchesList.clear();
        }
    }

    public List<MatchEntity> getMultiplayerMatchesList() {
        // Aufforderung in Main-Screen "gelesen" markieren
        if (lastWelcomeResponse != null && lastWelcomeResponse.competitionActionRequired) {
            lastWelcomeResponse = new BackendClient.WelcomeResponse(lastWelcomeResponse);
        }

        return multiplayerMatchesList;
    }

    public long getMultiplayerMatchesLastFetchMs() {
        return multiplayerMatchesLastFetchMs;
    }

    public boolean isMultiplayerMatchesLastFetchSuccessful() {
        return multiplayerMatchesLastFetchSuccessful;
    }

    public String getMultiplayerLastFetchError() {
        return multiplayerLastFetchError;
    }

    public BackendClient.WelcomeResponse getLastWelcomeResponse() {
        return lastWelcomeResponse;
    }

    public boolean hasLastWelcomeResponse() {
        return lastWelcomeResponse != null;
    }

    public boolean isFetchingMultiplayerMatches() {
        return isFetchingMultiplayerMatches;
    }

    public boolean isFetchingWelcomes() {
        return isFetchingWelcomes;
    }

    public void openNewMultiplayerMatch(final String opponentId, final int maxLevel,
                                        final AbstractQueuedBackendResponse<MatchEntity> callback) {
        if (!hasUserId())
            return;

        if (isFetchingMultiplayerMatches)
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    openNewMultiplayerMatch(opponentId, maxLevel, callback);
                }
            }, 1f);
        else
            backendClient.openNewMatch(opponentId, maxLevel, new BackendClient.IBackendResponse<MatchEntity>() {
                @Override
                public void onFail(int statusCode, String errorMsg) {
                    callback.onFail(statusCode, errorMsg);
                }

                @Override
                public void onSuccess(MatchEntity retrievedData) {
                    multiplayerMatchesList.add(0, retrievedData);
                    multiplayerMatchesLastFetchMs = TimeUtils.millis();
                    callback.onSuccess(retrievedData);
                }
            });
    }

    public void fetchMultiplayerMatches() {
        // wenn zu oft gedrückt wird nichts machen
        if (TimeUtils.timeSinceMillis(multiplayerMatchesLastFetchMs) < 1000 * 10L)
            return;

        if (!isFetchingMultiplayerMatches && hasUserId()) {
            isFetchingMultiplayerMatches = true;

            //TODO: sinceTime den ersten in der bisherigen Liste übergeben und dann im onSuccess die Listen
            // zusammenführen

            backendClient.listPlayerMatches(0, new BackendClient
                    .IBackendResponse<List<MatchEntity>>() {
                @Override
                public void onFail(int statusCode, String errorMsg) {
                    isFetchingMultiplayerMatches = false;
                    multiplayerMatchesLastFetchSuccessful = false;
                    multiplayerLastFetchError = errorMsg;
                }

                @Override
                public void onSuccess(final List<MatchEntity> retrievedData) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            isFetchingMultiplayerMatches = false;
                            multiplayerMatchesLastFetchSuccessful = true;
                            multiplayerMatchesList = retrievedData;
                            multiplayerMatchesLastFetchMs = TimeUtils.millis();
                        }
                    });
                }
            });
        }
    }

    /**
     * Wenn die Info schon vorhanden ist, wird der bisher gecachete Wert zurückgegeben. Falls nicht, wird sie abgefragt
     * Sollte jedoch gerade ein Upload des Turns passieren, wird die Antwort in beiden Fällen zurückgehalten
     */
    public void fetchFullMatchInfo(final String matchId, final BackendClient.IBackendResponse<MatchEntity> callback) {
        if (fetchingFullMatchInfo || isUploadingPlayedTurn() && hasTurnToUploadForMatch(matchId)) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    fetchFullMatchInfo(matchId, callback);
                }
            }, 1f);
            return;
        }

        for (int i = 0; i < multiplayerMatchesList.size(); i++)
            if (multiplayerMatchesList.get(i).uuid.equalsIgnoreCase(matchId)
                    && multiplayerMatchesList.get(i).isFullMatchInfo) {
                callback.onSuccess(multiplayerMatchesList.get(i));
                return;
            }

        fetchingFullMatchInfo = true;
        backendClient.fetchMatchWithTurns(matchId, new BackendClient.IBackendResponse<MatchEntity>() {
            @Override
            public void onFail(int statusCode, String errorMsg) {
                fetchingFullMatchInfo = false;
                callback.onFail(statusCode, errorMsg);
            }

            @Override
            public void onSuccess(final MatchEntity retrievedData) {
                callback.onSuccess(retrievedData);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fetchingFullMatchInfo = false;
                        updateMatchEntityInList(retrievedData);
                    }
                });
            }
        });
    }

    public void fetchNewWelcomeResponseIfExpired(int expirationTimeSeconds, long drawnBlocks, int donatorState) {
        if (!isFetchingWelcomes && (lastWelcomeResponse == null || (TimeUtils.millis() + lastWelcomeResponse
                .timeDelta - lastWelcomeResponse
                .responseTime) / 1000 > expirationTimeSeconds)) {
            isFetchingWelcomes = true;
            backendClient.fetchWelcomeMessages(LightBlocksGame.GAME_VERSIONNUMBER, platformString, osString,
                    drawnBlocks, donatorState, fetchWelcomesSinceTime, new BackendClient.IBackendResponse<BackendClient.WelcomeResponse>() {
                        @Override
                        public void onFail(int statusCode, String errorMsg) {
                            // kann man nix machen
                            isFetchingWelcomes = false;
                        }

                        @Override
                        public void onSuccess(BackendClient.WelcomeResponse retrievedData) {
                            lastWelcomeResponse = retrievedData;
                            authenticated = lastWelcomeResponse.authenticated;
                            isFetchingWelcomes = false;
                            // falls es offene Spiele gibt zurücksetzen, wann das letzte Mal die Liste der
                            // Multiplayer-Matches gezogen wurde, um einen Refresh zu erzwingen
                            if (lastWelcomeResponse.competitionActionRequired)
                                multiplayerMatchesLastFetchMs = 0;
                        }
                    });
        }
    }

    /**
     * gibt ein CachedScoreboard zurück
     *
     * @return null gdw das Modell gar kein BackendScoreboard hat, sonst garantiert ungleich null
     */
    public CachedScoreboard getCachedScoreboard(String gameMode, boolean latest) {
        if (!hasGameModeScoreboard(gameMode))
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
            return (currentlySendingScore != null || enqueuedScores.size > 0);
        }
    }

    /**
     * reiht den übergebenen Score in die Absendequeue ein, sofern er für ein Absenden ans Backend in Frage kommt.
     * Prüfungen werden hier durchgeführt
     *
     * @param score
     */
    public void enqueueAndSendScore(BackendScore score) {
        if (!hasGameModeScoreboard(score.gameMode) || score.sortValue <= 0)
            return;

        synchronized (enqueuedScores) {
            enqueuedScores.addLast(score);
            score.scoreGainedMillis = TimeUtils.millis();
        }
        sendEnqueuedScores();
    }

    public boolean hasGameModeScoreboard(String gameModelId) {
        if (gameModelId == null)
            return false;

        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_NORMAL_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_GRAVITY_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(RetroMarathonModel.MODEL_MARATHON_RETRO89))
            return true;
        if (gameModelId.equalsIgnoreCase(PracticeModel.MODEL_PRACTICE_ID))
            return true;
        if (gameModelId.equalsIgnoreCase(SprintModel.MODEL_SPRINT_ID))
            return true;

        return false;
    }

    /**
     * Nur im Main Thread!
     */
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
                        // Ein Fehler ist irgendeine Art von Server- oder Verbindungsfehler (das Backend nimmt alle
                        // Scores mit 200 an). Daher wieder einreihen für den nächsten Versuch, aber ans Ende
                        synchronized (enqueuedScores) {
                            // wieder zurück in die Queue und ein andermal probieren
                            enqueuedScores.addLast(currentlySendingScore);
                            currentlySendingScore = null;
                        }
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

    public String getPlatformString() {
        return platformString;
    }

    public boolean hasPlayedTurnToUpload() {
        return playedTurnToUpload != null && !uploadingPlayedTurn;
    }

    public boolean hasTurnToUploadForMatch(String matchId) {
        return playedTurnToUpload != null && playedTurnToUpload.matchId.equalsIgnoreCase(matchId);
    }

    public boolean isUploadingPlayedTurn() {
        return uploadingPlayedTurn;
    }

    public void queueAndUploadPlayedTurn(MatchTurnRequestInfo playedTurnToUpload) {
        if (this.playedTurnToUpload != null && playedTurnToUpload != this.playedTurnToUpload)
            throw new IllegalStateException("Cannot upload new turn data while other turn data is still queued.");

        this.playedTurnToUpload = playedTurnToUpload;
        prefs.saveTurnToUpload(playedTurnToUpload);

        sendEnqueuedTurnToUpload(null);
    }

    public void resetTurnToUpload() {
        if (!uploadingPlayedTurn) {
            playedTurnToUpload = null;
            prefs.saveTurnToUpload(null);
        }
    }

    public void sendEnqueuedTurnToUpload(@Nullable final BackendClient.IBackendResponse<MatchEntity> callback) {
        if (!hasPlayedTurnToUpload())
            return;

        uploadingPlayedTurn = true;
        getBackendClient().postMatchPlayedTurn(playedTurnToUpload, new BackendClient.IBackendResponse<MatchEntity>() {
            @Override
            public void onFail(final int statusCode, final String errorMsg) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        uploadingPlayedTurn = false;
                        if (statusCode < 500 && statusCode >= HttpStatus.SC_OK) {
                            resetTurnToUpload();
                        }

                        if (callback != null)
                            callback.onFail(statusCode, errorMsg);
                    }
                });
            }

            @Override
            public void onSuccess(final MatchEntity retrievedData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        uploadingPlayedTurn = false;
                        resetTurnToUpload();
                        updateMatchEntityInList(retrievedData);

                        if (callback != null)
                            callback.onSuccess(retrievedData);
                    }
                });
            }
        });
    }

    public void updateMatchEntityInList(MatchEntity matchToInsert) {
        // erstmal aus der Liste entfernen, falls das Match schon enthalten ist
        for (int i = multiplayerMatchesList.size() - 1; i >= 0; i--)
            if (multiplayerMatchesList.get(i).uuid.equalsIgnoreCase(matchToInsert.uuid))
                multiplayerMatchesList.remove(i);

        // und dann an der richtigen Stelle einfügen
        boolean added = false;
        for (int i = 0; i < multiplayerMatchesList.size(); i++) {
            if (multiplayerMatchesList.get(i).lastChangeTime < matchToInsert.lastChangeTime) {
                multiplayerMatchesList.add(i, matchToInsert);
                added = true;
                break;
            }
        }

        if (!added)
            multiplayerMatchesList.add(matchToInsert);

        multiplayerMatchesLastFetchMs = TimeUtils.millis();
    }

    public abstract static class AbstractQueuedBackendResponse<T> implements BackendClient.IBackendResponse<T> {
        private final LightBlocksGame app;

        public AbstractQueuedBackendResponse(LightBlocksGame app) {
            this.app = app;
        }

        @Override
        public void onFail(final int statusCode, final String errorMsg) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onRequestFailed(statusCode, statusCode < 500 ? errorMsg :
                            "Error on Lightblocks' backend server: " + statusCode);
                }
            });
        }

        @Override
        public void onSuccess(final T retrievedData) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    app.backendManager.sendEnqueuedScores();
                    onRequestSuccess(retrievedData);
                }
            });
        }

        /**
         * auf dem Main-Thread
         */
        protected abstract void onRequestSuccess(T retrievedData);

        /**
         * auf dem Main-Thread
         */
        protected abstract void onRequestFailed(int statusCode, String errorMsg);
    }

    public class CachedScoreboard {
        private static final int EXPIRATION_SECONDS_SUCCESS = 60 * 5;
        private static final int EXPIRATION_SECONDS_NO_CONNECTION = 10;
        private final String gameMode;
        private final boolean isLatest;
        public long expirationTimeMs;
        public boolean isFetching;
        private boolean lastErrorIsConnectionProblem;
        private String lastErrorMsg;
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

        public boolean isExpired() {
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
                        lastErrorIsConnectionProblem = statusCode == BackendClient.SC_NO_CONNECTION;
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

        public boolean isLastErrorConnectionProblem() {
            return lastErrorIsConnectionProblem;
        }
    }
}
