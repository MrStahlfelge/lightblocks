package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

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

    private TotalScore cachedTotalScore;

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

    public TotalScore loadTotalScore() {
        if (!canSaveState() || !Gdx.files.local(FILENAME_TOTALSCORE).exists()) {
            if (cachedTotalScore == null)
                cachedTotalScore = new TotalScore();
        } else {
            Json json = new Json();
            try {
                cachedTotalScore = json.fromJson(TotalScore.class, Gdx.files.local(FILENAME_TOTALSCORE));
            } catch (Throwable t) {
                cachedTotalScore = new TotalScore();
            }
        }
        return cachedTotalScore;
    }

    public void saveTotalScore(TotalScore score) {
        cachedTotalScore = score;

        if (canSaveState()) {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.toJson(score, Gdx.files.local(FILENAME_TOTALSCORE));
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

}
