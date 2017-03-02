package de.golfgl.lightblocks.multiplayer;

import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MultiplayerModel;

/**
 * The objects that are transferred via kryo
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiPlayerObjects {

    // *** BEI JEDER ÄNDERUNG HOCHZÄHLEN!!! ***
    public static final int INTERFACE_VERSION = 2;

    public static final int CHANGE_ADD = 1;
    public static final int CHANGE_UPDATE = 2;
    public static final int CHANGE_REMOVE = 3;

    public static final String PLAYERS_ALL = "*";

    // This registers objects that are going to be sent over the network.
    public static void register(Kryo kryo) {
        // *** HANDSHAKE MUSS DIE ERSTE REGISTRIERTE KLASSE SEIN!!! ***
        kryo.register(Handshake.class);
        // und nun die weiteren Klassen registrieren =>
        kryo.register(AbstractMultiplayerRoom.RoomState.class);
        kryo.register(RoomStateChanged.class);
        kryo.register(Player.class);
        kryo.register(PlayerChanged.class);
        kryo.register(ArrayList.class);
        kryo.register(RelayToPlayer.class);

        // GameModel
        kryo.register(SwitchedPause.class);
        kryo.register(PlayerIsOver.class);
        kryo.register(GameIsOver.class);
        kryo.register(BonusScore.class);
        kryo.register(PlayerInGame.class);
    }

    public static class Handshake {
        // *** AN DIESER KLASSE DÜRFEN KEINERLEI VERÄNDERUNGEN GEMACHT WERDEN!!! ***
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

    public static class RoomStateChanged {
        public AbstractMultiplayerRoom.RoomState roomState;
        public String refereePlayerId;
        public String debutyPlayerId;

        @Override
        public String toString() {
            return "Room state: " + roomState + ", Host: " + refereePlayerId;
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

    public static class SwitchedPause {
        public boolean nowPaused;

        public SwitchedPause withPaused(boolean paused) {
            nowPaused = paused;
            return this;
        }
    }

    public static class PlayerIsOver {
        public String playerId;

        public PlayerIsOver withPlayerId(String playerId) {
            this.playerId = playerId;
            return this;
        }
    }

    public static class BonusScore {
        public int score;
    }

    public static class GameIsOver {
        public String winnerPlayerId;
    }

    public static class PlayerInGame {
        public String playerId;
        public int filledBlocks;
        public int drawnBlocks;
        public int score;
    }
}
