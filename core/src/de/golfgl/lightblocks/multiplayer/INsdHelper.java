package de.golfgl.lightblocks.multiplayer;

import java.util.List;

/**
 * Interface for NsdAdapter in Android project
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public interface INsdHelper {
    String SERVICE_NAME = "Lightblocks";
    String LIGHTBLOCKS_TYPE_NAME = "_lightblocks._tcp";

    void registerService();

    void unregisterService();

    void startDiscovery(boolean legacy);

    void stopDiscovery();

    /**
     * returns list of currently discovered services.
     *
     * @return Copy of the list at the time the method is called
     */
    List<IRoomLocation> getDiscoveredLegacyServices();

    List<ServerAddress> getDiscoveredMultiplayerServers();
}
