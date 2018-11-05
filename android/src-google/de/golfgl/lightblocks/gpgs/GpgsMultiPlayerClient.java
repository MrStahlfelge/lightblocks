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

public class GpgsMultiPlayerClient extends de.golfgl.gdxgamesvcs.GpgsClient implements IMultiplayerGsClient {

    private GpgsMultiPlayerRoom gpgsMPRoom;
    private Invitation invitationOnConnect;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Erhaltene Einladungen speichern, um gleich in Multiplayer zu gehen
        invitationOnConnect = null;
        if (bundle != null) {
            invitationOnConnect = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (invitationOnConnect != null)
                Log.i(GAMESERVICE_ID, "Multiplayer Invitation: " + invitationOnConnect.getInvitationId() + " from "
                        + invitationOnConnect.getInviter().getParticipantId());
        }

        super.onConnected(bundle);
    }

    @Override
    public boolean hasPendingInvitation() {
        return invitationOnConnect != null && (gpgsMPRoom == null || !gpgsMPRoom.isConnected());
    }

    @Override
    public void acceptPendingInvitation() {
        if (gpgsMPRoom != null && hasPendingInvitation()) {
            gpgsMPRoom.acceptInvitation(invitationOnConnect);
            invitationOnConnect = null;
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
