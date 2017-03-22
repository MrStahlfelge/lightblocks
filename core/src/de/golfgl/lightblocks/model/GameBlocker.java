package de.golfgl.lightblocks.model;

/**
 * A gameblocker prevents the game to play.
 *
 * Created by Benjamin Schulte on 22.03.2017.
 */

public abstract class GameBlocker {

    public static class InputGameBlocker extends GameBlocker {

    }

    public static class OtherPlayerPausedGameBlocker extends GameBlocker {

    }
}
