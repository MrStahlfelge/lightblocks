package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.esotericsoftware.minlog.Log;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Die Klasse lädt oder speichert einen Json-String der den Spielstand abbildet
 * <p>
 * Created by Benjamin Schulte on 28.01.2017.
 */
public class GameStateHandler {

    private static final String FILENAME_SAVEGAME = "data/savegame.json";
    private static final String FILENAME_TOTALSCORE = "data/score_total.json";
    private static final String FILENAMEPREFIX_BESTSCORE = "data/score_";
    private static final String SAVEGAMEKEY = "***REMOVED***";

    private final LightBlocksGame app;
    private TotalScore totalScore;
    private Object gameStateMonitor = new Object();
    // wurde bereits ein Cloud-Spielstand geladen?
    private boolean alreadyLoadedFromCloud;
    // store future use String - damit er nicht verloren geht!
    private String futureUseFromCloudSaveGame;

    public GameStateHandler(LightBlocksGame app) {
        this.app = app;
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

    public boolean canSaveState() {
        return Gdx.files.isLocalStorageAvailable();
    }

    public boolean hasSavedGame() {
        return canSaveState() && Gdx.files.local(FILENAME_SAVEGAME).exists();
    }

    public String loadGame() {
        if (!hasSavedGame())
            throw new IndexOutOfBoundsException("cannot load game");

        try {
            return decode(Gdx.files.local(FILENAME_SAVEGAME).readString(), SAVEGAMEKEY);
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
        if (!canSaveState())
            return false;


        if (jsonString == null)
            return resetGame();

        try {
            Gdx.files.local(FILENAME_SAVEGAME).writeString(encode(jsonString, SAVEGAMEKEY), false);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean resetGame() {
        try {
            Json json;
            return Gdx.files.local(FILENAME_SAVEGAME).delete();
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

    protected void loadTotalScore() {
        synchronized (gameStateMonitor) {
            if (!canSaveState() || !Gdx.files.local(FILENAME_TOTALSCORE).exists()) {
                totalScore = new TotalScore();
            } else {
                Json json = new Json();
                try {
                    totalScore = json.fromJson(TotalScore.class, Gdx.files.local(FILENAME_TOTALSCORE));
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
        if (canSaveState()) {
            synchronized (gameStateMonitor) {
                Json json = new Json();
                json.setOutputType(JsonWriter.OutputType.json);
                json.toJson(totalScore, Gdx.files.local(FILENAME_TOTALSCORE));
            }
        }
    }

    /**
     * saves total score und progression to cloud. Does not save it to files.
     */
    public void gpgsSaveGameState(boolean sync) {
        if (app.gpgsClient != null && app.gpgsClient.isConnected()) {
            synchronized (gameStateMonitor) {
                getTotalScore(); // sicherstellen dass er geladen ist
                // Wenn eh schon gesynct wird, auch Achievements auffrischen
                totalScore.checkAchievements(app.gpgsClient);

                CloudGameState cgs = new CloudGameState();
                cgs.version = LightBlocksGame.GAME_VERSIONSTRING;
                cgs.totalScore = totalScore;
                cgs.futureUse = futureUseFromCloudSaveGame;

                Json json = new Json();
                json.setOutputType(JsonWriter.OutputType.minimal);

                String jsonString = json.toJson(cgs);

                if (LightBlocksGame.GAME_DEVMODE)
                    Log.info("GameState", jsonString);

                app.gpgsClient.saveGameState(sync, xorWithKey(jsonString.getBytes(), SAVEGAMEKEY.getBytes()),
                        totalScore.getScore());
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

            Json json = new Json();

            try {
                final String jsonString = new String(xorWithKey(gameState, SAVEGAMEKEY.getBytes()));

                if (LightBlocksGame.GAME_DEVMODE)
                    Log.info("GameState", jsonString);

                CloudGameState cgs = json.fromJson(CloudGameState.class, jsonString);
                futureUseFromCloudSaveGame = cgs.futureUse;

                // Stand zusammenmergen
                totalScore.mergeWithOther(cgs.totalScore);

            } catch (Throwable t) {
                Log.error("GameState", "Error reading saved gamestate. Ignored.");
            }

            totalScore.checkAchievements(app.gpgsClient);
        }

    }

    public BestScore loadBestScore(String identifier) {
        String filename = FILENAMEPREFIX_BESTSCORE + identifier;
        if (!canSaveState() || !Gdx.files.local(filename).exists())
            return new BestScore();
        else {
            Json json = new Json();
            try {
                return json.fromJson(BestScore.class, Gdx.files.local(filename));
            } catch (Throwable t) {
                return new BestScore();
            }
        }
    }

    public void saveBestScore(BestScore score, String identifier) {
        String filename = FILENAMEPREFIX_BESTSCORE + identifier;

        if (canSaveState()) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.toJson(score, Gdx.files.local(filename));

        }
    }

    public boolean isAlreadyLoadedFromCloud() {
        return alreadyLoadedFromCloud;
    }

    public void resetLoadedFromCloud() {
        alreadyLoadedFromCloud = false;
    }
}
