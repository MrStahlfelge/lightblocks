package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;

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
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "1"))
            return true;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "2"))
            return true;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "3"))
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
                                currentlySendingScore = null;
                                sendEnqueuedScores();
                            }
                        });
                    }
                });
            }
        }
    }

    // TODO Fetch der Scores auch nur, wenn nicht gerade Score gesendet wird - sonst Anfrage zurückstellen

    public String getPlatformString() {
        return platformString;
    }
}
