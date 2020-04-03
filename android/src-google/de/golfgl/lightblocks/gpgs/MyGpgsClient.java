package de.golfgl.lightblocks.gpgs;

import com.google.android.gms.games.snapshot.SnapshotMetadataChange;

/**
 * Client für Google Play Games
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class MyGpgsClient extends de.golfgl.gdxgamesvcs.GpgsClient {

    @Override
    protected SnapshotMetadataChange.Builder setSaveGameMetaData(SnapshotMetadataChange.Builder metaDataBuilder,
                                                                 String id, byte[] gameState, long progressValue) {
        return super.setSaveGameMetaData(metaDataBuilder, id, gameState, progressValue)
                .setDescription("Time to play again!");
    }
}
