package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
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

    private TotalScore cachedTotalScore;

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
            return Gdx.files.local(FILENAME_SAVEGAME).readString();
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
            Gdx.files.local(FILENAME_SAVEGAME).writeString(jsonString, false);
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
