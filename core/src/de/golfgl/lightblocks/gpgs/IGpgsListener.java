package de.golfgl.lightblocks.gpgs;

/**
 * Listener interface for Gpgs
 *
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IGpgsListener {

    /**
     * Called when gpgcs sucessfully connected
     */
    public void gpgsConnected();

    public void gpgsDisconnected();

    public void gpgsErrorMsg(String msg);
}
