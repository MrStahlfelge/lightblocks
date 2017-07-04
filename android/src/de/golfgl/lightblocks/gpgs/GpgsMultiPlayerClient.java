package de.golfgl.lightblocks.gpgs;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;

import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

/**
 * Client für Google Play Games
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class GpgsMultiPlayerClient extends de.golfgl.gdxgamesvcs.GpgsClient implements IGpgsClient {

    private GpgsMultiPlayerRoom gpgsMPRoom;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void disconnect(boolean autoEnd) {
        // kein disconnect wenn Multiplayer-Aktionen laufen
        if (autoEnd && gpgsMPRoom != null && gpgsMPRoom.isConnected())
            return;

        super.disconnect(autoEnd);
    }

    @Override
    public AbstractMultiplayerRoom getMultiPlayerRoom() {
        if (gpgsMPRoom == null) {
            gpgsMPRoom = new GpgsMultiPlayerRoom();
            gpgsMPRoom.setContext(myContext);
            gpgsMPRoom.setGpgsClient(this);
        }
        return gpgsMPRoom;
    }

    @Override
    protected SnapshotMetadataChange.Builder setSaveGameMetaData(SnapshotMetadataChange.Builder metaDataBuilder,
                                                                 String id, byte[] gameState, long progressValue) {
        return super.setSaveGameMetaData(metaDataBuilder, id, gameState, progressValue)
                .setDescription("Time to play again!");
    }
}
