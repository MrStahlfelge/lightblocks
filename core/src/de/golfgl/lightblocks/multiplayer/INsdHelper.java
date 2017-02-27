package de.golfgl.lightblocks.multiplayer;

import java.util.List;

/**
 * Interface for NsdAdapter in Android project
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public interface INsdHelper {

    public void registerService();

    public void unregisterService();

    public void startDiscovery();

    public void stopDiscovery();

    /**
     * returns list of currently discovered services.
     *
     * @return Copy of the list at the time the method is called
     */
    public List<IRoomLocation> getDiscoveredServices();

}
