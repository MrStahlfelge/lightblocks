package de.golfgl.lightblocks.gpgs;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

/**
 * Core Interface for GpgsClient
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IGpgsClient extends de.golfgl.gdxgamesvcs.IGameServiceClient {

    public AbstractMultiplayerRoom getMultiPlayerRoom();
}
