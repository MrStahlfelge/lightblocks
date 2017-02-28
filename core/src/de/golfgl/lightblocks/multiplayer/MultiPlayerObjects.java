package de.golfgl.lightblocks.multiplayer;

import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * The objects that are transferred via kryo
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiPlayerObjects {

    public static final int INTERFACE_VERSION = 1;

    public static final int CHANGE_ADD = 1;
    public static final int CHANGE_UPDATE = 2;
    public static final int CHANGE_REMOVE = 3;

    public static final String PLAYERS_ALL = "*";

    // This registers objects that are going to be sent over the network.
    public static void register(Kryo kryo) {
        kryo.register(Handshake.class);
        kryo.register(AbstractMultiplayerRoom.RoomState.class);
        kryo.register(Player.class);
        kryo.register(PlayerChanged.class);
        kryo.register(ArrayList.class);
        kryo.register(RelayToPlayer.class);
    }

    public static class Handshake {
        public byte interfaceVersion = INTERFACE_VERSION;
        public String lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;
        public boolean success;
        public String message;
        public String playerId;

        @Override
        public String toString() {
            if (!success)
                return "Room refused join, reason: " + message;
            else
                return "Joined room with player name " + playerId;
        }
    }

    public static class Player {
        public String name;
        public String lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;
    }

    public static class PlayerChanged {
        public Player changedPlayer;
        public int changeType;
    }

    public static class RelayToPlayer {
        public String recipient;
        public Object message;
    }

}
