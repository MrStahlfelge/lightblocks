package de.golfgl.lightblocks.multiplayer;

import java.util.List;

/**
 * Interface for NsdAdapter in Android project
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public interface INsdHelper {
    String SERVICE_NAME = "Lightblocks";

    void registerService();

    void unregisterService();

    void startDiscovery();

    void stopDiscovery();

    /**
     * returns list of currently discovered services.
     *
     * @return Copy of the list at the time the method is called
     */
    List<IRoomLocation> getDiscoveredServices();

}
