package de.golfgl.lightblocks.multiplayer;

import java.util.List;

/**
 * Interface for NsdAdapter in Android project
 *
 * Created by Benjamin Schulte on 25.02.2017.
 */

public interface INsdHelper {

    public void registerService();

    public void unregisterService();

    public void startDiscovery();

    public void stopDiscovery();

    public List<IRoomLocation> getDiscoveredServices();

}
