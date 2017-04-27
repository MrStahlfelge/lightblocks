package de.golfgl.lightblocks.gpgs;

import de.golfgl.lightblocks.multiplayer.IRoomLocation;

/**
 * Created by Benjamin Schulte on 26.04.2017.
 */

public class GpgsRoomLocation implements IRoomLocation {
    private String invitationId;
    private String parcipantId;

    public GpgsRoomLocation(String invitationId, String parcipantId) {
        this.invitationId = invitationId;
        this.parcipantId = parcipantId;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public String getParcipantId() {
        return parcipantId;
    }

    @Override
    public String getRoomName() {
        return parcipantId;
    }
}
