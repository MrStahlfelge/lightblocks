package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;

/**
 * Die Klasse lädt oder speichert einen Json-String der den Spielstand abbildet
 * <p>
 * Created by Benjamin Schulte on 28.01.2017.
 */
public class SaveGameHandler {

    private static final String FILENAME = "savegames/lightblockssavegame.json";

    public boolean canSaveGame() {
        return Gdx.files.isLocalStorageAvailable();
    }

    public boolean hasSavedGame() {
        return Gdx.files.local(FILENAME).exists();
    }

    public String loadGame() {
        if (!hasSavedGame())
            throw new IndexOutOfBoundsException("cannot load game");

        try {
            return Gdx.files.local(FILENAME).readString();
        } catch (Throwable t) {
            return null;
        }

    }

    /**
     * Saves the string to the savegamefile
     *
     * @return true when successful
     */
    public boolean saveGame(String jsonString) {
        if (!canSaveGame())
            return false;

        try {
            Gdx.files.local(FILENAME).writeString(jsonString, false);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public boolean resetGame() {
        try {
            return Gdx.files.local(FILENAME).delete();
        } catch (Throwable t) {
            return false;
        }

    }
}
