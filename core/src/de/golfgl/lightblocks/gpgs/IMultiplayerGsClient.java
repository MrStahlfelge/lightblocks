package de.golfgl.lightblocks.gpgs;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.gamestate.ISaveGameStateResponseListener;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

/**
 * Core Interface for GpgsClient
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IMultiplayerGsClient extends de.golfgl.gdxgamesvcs.IGameServiceClient {

    public static final String NAME_SAVE_GAMESTATE = "gamestate.sav";

    AbstractMultiplayerRoom createMultiPlayerRoom();

}
