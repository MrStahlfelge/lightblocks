package de.golfgl.lightblocks.gpgs;

/**
 * Listener interface for Gpgs
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IGpgsListener {

    /**
     * Called when gpgcs sucessfully connected
     */
    public void gpgsConnected();

    public void gpgsDisconnected();

    public void gpgsErrorMsg(String msg);

    /**
     * Returns a game state that was saved in Cloud services
     *
     * @param gameState null if loading failed
     */
    public void gpgsGameStateLoaded(byte[] gameState);
}
