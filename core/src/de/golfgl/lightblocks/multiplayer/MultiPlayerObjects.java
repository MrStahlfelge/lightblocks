package de.golfgl.lightblocks.multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;

import java.util.ArrayList;
import java.util.Collection;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * The objects that are transferred via kryo
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiPlayerObjects {

    public static final int CHANGE_ADD = 1;
    public static final int CHANGE_REMOVE = 2;

    // This registers objects that are going to be sent over the network.
    public static void register(Kryo kryo) {
        kryo.register(Handshake.class);
        kryo.register(Player.class);
        kryo.register(PlayersChanged.class);
        kryo.register(ArrayList.class);
    }

    public static Handshake createHandshake(Player player) {
        // Handshake mit Spieler & Version
        Handshake handshake = new MultiPlayerObjects.Handshake();
        handshake.player = player;
        return (handshake);
    }

    public static class Handshake {
        public String version = LightBlocksGame.GAME_VERSIONSTRING;
        public Player player;
    }

    public static class Player {
        public String name;
    }

    public static class PlayersChanged {
        public Player changedPlayer;
        public int changeType;
        public ArrayList<Player> players;
    }

}
