package de.golfgl.lightblocks.multiplayer;

/**
 * Room location for Multiplayer rooms. The implementation depends on the multiplayer implementation
 * (Kryonet needs IP addresses, GPGS needs something else)
 *
 * Created by Benjamin Schulte on 25.02.2017.
 */
public interface IRoomLocation {

    /**
     * returns name of the room
     *
     */
    String getRoomName();

    String getRoomAddress();
}
