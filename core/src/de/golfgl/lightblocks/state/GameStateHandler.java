package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.gdxgamesvcs.gamestate.ISaveGameStateResponseListener;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;

/**
 * Die Klasse lädt oder speichert einen Json-String der den Spielstand abbildet
 * <p>
 * Created by Benjamin Schulte on 28.01.2017.
 */
public class GameStateHandler {

    private static final String FILENAME_SAVEGAME = "data/savegame.json";
    private static final String FILENAME_TOTALSCORE = "data/score_total.json";
    private static final String FILENAME_BESTSCORES = "data/score_best.json";
    private static final String FILENAME_PREFIX_MISSIONS = "missions/";
    private static final String SAVEGAMEKEY = "***REMOVED***";

    private final LightBlocksGame app;
    private TotalScore totalScore;
    private Object gameStateMonitor = new Object();
    // wurde bereits ein Cloud-Spielstand geladen?
    private boolean alreadyLoadedFromCloud;
    // store future use String - damit er nicht verloren geht!
    private String futureUseFromCloudSaveGame;
    private BestScore.BestScoreMap bestScores;
    private Preferences prefs;

    public GameStateHandler(LightBlocksGame app, Preferences prefs) {
        this.app = app;
        this.prefs = prefs;
    }

    public static String encode(String s, String key) {
        return new String(Base64Coder.encode(xorWithKey(s.getBytes(), key.getBytes())));
    }

    public static String decode(String s, String key) {
        return new String(xorWithKey(Base64Coder.decode(s), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }

        return out;
    }

    public boolean hasSavedGame() {
        if (Gdx.files.isLocalStorageAvailable())
            return Gdx.files.local(FILENAME_SAVEGAME).exists();
        else
            return prefs.contains(FILENAME_SAVEGAME);
    }

    public String loadGame() {
        if (!hasSavedGame())
            throw new IndexOutOfBoundsException("cannot load game");

        try {
            return decode(Gdx.files.isLocalStorageAvailable() ?
                    Gdx.files.local(FILENAME_SAVEGAME).readString()
                    : prefs.getString(FILENAME_SAVEGAME), SAVEGAMEKEY);
        } catch (Throwable t) {
            return null;
        }

    }

    /**
     * Saves the string to the savegamefile. If null, savegame is resetted
     *
     * @return true when successful
     */
    public boolean saveGame(String jsonString) {
        if (jsonString == null)
            return resetGame();

        if (LightBlocksGame.GAME_DEVMODE)
            System.out.println(jsonString);

        try {
            String encoded = encode(jsonString, SAVEGAMEKEY);
            if (Gdx.files.isLocalStorageAvailable()) {
                Gdx.files.local(FILENAME_SAVEGAME).writeString(encoded, false);
            } else {
                prefs.putString(FILENAME_SAVEGAME, encoded);
                prefs.flush();
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean resetGame() {
        try {
            if (Gdx.files.isLocalStorageAvailable())
                return Gdx.files.local(FILENAME_SAVEGAME).delete();
            else {
                prefs.remove(FILENAME_SAVEGAME);
                prefs.flush();
                return true;
            }

        } catch (Throwable t) {
            return false;
        }
    }

    public TotalScore getTotalScore() {
        synchronized (gameStateMonitor) {
            if (totalScore == null)
                loadTotalScore();

            return totalScore;
        }
    }

    /**
     * determines if there is a game state, if the game has already been played
     *
     * @return true if there already was played
     */
    public boolean hasGameState() {
        if (Gdx.files.isLocalStorageAvailable())
            return Gdx.files.local(FILENAME_TOTALSCORE).exists();
        else
            return prefs.contains(FILENAME_TOTALSCORE);
    }

    protected void loadTotalScore() {
        synchronized (gameStateMonitor) {
            if (!hasGameState()) {
                totalScore = new TotalScore();
            } else {
                Json json = new Json();
                try {
                    if (Gdx.files.isLocalStorageAvailable())
                        totalScore = json.fromJson(TotalScore.class, Gdx.files.local(FILENAME_TOTALSCORE));
                    else
                        totalScore = json.fromJson(TotalScore.class, prefs.getString(FILENAME_TOTALSCORE));
                } catch (Throwable t) {
                    totalScore = new TotalScore();
                }
            }
        }
    }

    /**
     * saves the total score to file. Does not save it to cloud storage.
     */
    public void saveTotalScore() {
        synchronized (gameStateMonitor) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            if (Gdx.files.isLocalStorageAvailable())
                json.toJson(totalScore, Gdx.files.local(FILENAME_TOTALSCORE));
            else {
                prefs.putString(FILENAME_TOTALSCORE, json.toJson(totalScore));
                prefs.flush();
            }
        }
    }

    /**
     * saves total score und progression to cloud. Does not save it to files.
     */
    public void gpgsSaveGameState(ISaveGameStateResponseListener success) {
        if (app.gpgsClient != null && app.gpgsClient.isSessionActive()) {
            synchronized (gameStateMonitor) {
                getTotalScore(); // sicherstellen dass er geladen ist
                loadBestScores(); // hier ebenso
                // Wenn eh schon gesynct wird, auch Achievements auffrischen
                totalScore.checkAchievements(app.gpgsClient);
                bestScores.checkAchievements(app.gpgsClient, app);

                CloudGameState cgs = new CloudGameState();
                cgs.version = LightBlocksGame.GAME_VERSIONSTRING;
                cgs.totalScore = totalScore;
                cgs.bestScores = bestScores;
                cgs.futureUse = futureUseFromCloudSaveGame;

                Json json = new Json();
                json.setOutputType(JsonWriter.OutputType.minimal);

                String jsonString = json.toJson(cgs);

                if (LightBlocksGame.GAME_DEVMODE)
                    Gdx.app.log("GameState", jsonString);

                app.gpgsClient.saveGameState(IMultiplayerGsClient.NAME_SAVE_GAMESTATE,
                        xorWithKey(jsonString.getBytes(), SAVEGAMEKEY.getBytes()),
                        totalScore.getScore(), success);
            }
        }
    }

    public void gpgsLoadGameState(byte[] gameState) {

        // Übergabe von null: beim Laden ist ein Fehler aufgetreten
        if (gameState == null)
            return;

        alreadyLoadedFromCloud = true;

        synchronized (gameStateMonitor) {
            getTotalScore(); // sicherstellen dass er geladen ist
            loadBestScores(); // hier ebenso

            Json json = new Json();

            try {
                final String jsonString = new String(xorWithKey(gameState, SAVEGAMEKEY.getBytes()));

                if (LightBlocksGame.GAME_DEVMODE)
                    Gdx.app.log("GameState", jsonString);

                CloudGameState cgs = json.fromJson(CloudGameState.class, jsonString);
                futureUseFromCloudSaveGame = cgs.futureUse;

                // Stand zusammenmergen
                totalScore.mergeWithOther(cgs.totalScore);
                bestScores.mergeWithOther(cgs.bestScores);

            } catch (Throwable t) {
                Gdx.app.error("GameState", "Error reading saved gamestate. Ignored.", t);
            }

            totalScore.checkAchievements(app.gpgsClient);
            bestScores.checkAchievements(app.gpgsClient, app);
        }

    }

    /**
     * @param identifier (Mission, Marathon oder ähnlich)
     * @return niemals null
     */
    public BestScore getBestScore(String identifier) {
        if (bestScores == null)
            loadBestScores();

        if (!bestScores.containsKey(identifier))
            bestScores.put(identifier, new BestScore());

        return bestScores.get(identifier);
    }

    protected void loadBestScores() {
        if (bestScores != null)
            return;

        synchronized (gameStateMonitor) {
            if (!(Gdx.files.isLocalStorageAvailable() ? Gdx.files.local(FILENAME_BESTSCORES).exists()
                    : prefs.contains(FILENAME_BESTSCORES))) {
                Gdx.app.log("Gamestate", "No scores found.");
                bestScores = new BestScore.BestScoreMap();
            } else {
                Json json = new Json();
                try {
                    String decoded;
                    if (Gdx.files.isLocalStorageAvailable())
                        decoded = decode(Gdx.files.local(FILENAME_BESTSCORES).readString(), SAVEGAMEKEY);
                    else
                        decoded = decode(prefs.getString(FILENAME_BESTSCORES), SAVEGAMEKEY);
                    bestScores = json.fromJson(BestScore.BestScoreMap.class, decoded);
                } catch (Throwable t) {
                    Gdx.app.error("Gamestate", "Error loading best scores - resetting.", t);
                    bestScores = null;
                }
                if (bestScores == null)
                    bestScores = new BestScore.BestScoreMap();
            }
        }
    }

    public void saveBestScores() {
        synchronized (gameStateMonitor) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.minimal);
            String encoded = encode(json.toJson(bestScores), SAVEGAMEKEY);
            if (Gdx.files.isLocalStorageAvailable())
                Gdx.files.local(FILENAME_BESTSCORES).writeString(encoded, false);
            else {
                prefs.putString(FILENAME_BESTSCORES, encoded);
                prefs.flush();
            }
        }
    }

    public boolean isAlreadyLoadedFromCloud() {
        return alreadyLoadedFromCloud;
    }

    public void resetLoadedFromCloud() {
        alreadyLoadedFromCloud = false;
    }

    public String loadMission(String missionId) {
        try {
            return Gdx.files.internal(FILENAME_PREFIX_MISSIONS + missionId).readString();
        } catch (Throwable t) {
            return null;
        }
    }
}
