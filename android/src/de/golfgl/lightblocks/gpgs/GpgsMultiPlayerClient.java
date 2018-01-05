package de.golfgl.lightblocks.gpgs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
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
    public void onConnected(@Nullable Bundle bundle) {
        super.onConnected(bundle);

        // TODO Erhaltene Einladungen... gleich in Multiplayer gehen
        if (bundle != null) {
            Invitation inv =
                    bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null)
                Log.i(GAMESERVICE_ID, "Multiplayer Invitation: " + inv.getInvitationId() + " from "
                        + inv.getInviter().getParticipantId());
        }

    }

    @Override
    public void disconnect(boolean autoEnd) {
        // kein disconnect wenn Multiplayer-Aktionen laufen
        if (autoEnd && gpgsMPRoom != null && gpgsMPRoom.isConnected())
            return;

        super.disconnect(autoEnd);
    }

    @Override
    public AbstractMultiplayerRoom createMultiPlayerRoom() {
        if (gpgsMPRoom != null && gpgsMPRoom.isConnected())
            throw new IllegalStateException("GPGS room open but new one should be created");

            gpgsMPRoom = new GpgsMultiPlayerRoom();
        gpgsMPRoom.setContext(myContext);
        gpgsMPRoom.setGpgsClient(this);

        return gpgsMPRoom;
    }

    public GpgsMultiPlayerRoom getMultiPlayerRoom() {
        return gpgsMPRoom;
    }

    @Override
    protected SnapshotMetadataChange.Builder setSaveGameMetaData(SnapshotMetadataChange.Builder metaDataBuilder,
                                                                 String id, byte[] gameState, long progressValue) {
        return super.setSaveGameMetaData(metaDataBuilder, id, gameState, progressValue)
                .setDescription("Time to play again!");
    }
}
