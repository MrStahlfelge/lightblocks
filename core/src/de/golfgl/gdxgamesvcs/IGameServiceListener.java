package de.golfgl.gdxgamesvcs;

/**
 * Listener interface for Game Services
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IGameServiceListener {

    /**
     * Called when gpgcs sucessfully connected
     */
    public void gsConnected();

    public void gsDisconnected();

    public void gsErrorMsg(String msg);

    /**
     * Returns a game state that was saved in Cloud services
     *
     * @param gameState null if loading failed
     */
    public void gsGameStateLoaded(byte[] gameState);

}
