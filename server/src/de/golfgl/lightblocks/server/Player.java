package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;

import org.java_websocket.WebSocket;

import de.golfgl.lightblocks.server.model.InGameMessage;
import de.golfgl.lightblocks.server.model.KeepAliveMessage;
import de.golfgl.lightblocks.server.model.PlayerInfo;

public class Player {
    private static final int SECONDS_INACTIVITY_WARNING = 10;
    private static final String GAME_TIMEOUT_WARNING = "Inactive players will be disconnected";
    private final LightblocksServer server;
    private final WebSocket conn;
    public String nickName;
    public String userId;
    public String token;
    public ConnectionState state = ConnectionState.CONNECTED;
    private Match match;
    private long startedPlayingMs;
    private long lastMessageReceived;
    private long lastGameMessageReceived;
    private String lastMessageToPlayer;
    private final Queue<String> outgoingQueue = new Queue<>();

    public Player(LightblocksServer server, WebSocket conn) {
        this.server = server;
        this.conn = conn;
    }

    private void doConnect(PlayerInfo playerInfo) {
        if (playerInfo.nickName == null || state != ConnectionState.CONNECTED) {
            // client error => just return;
            return;
        }

        nickName = playerInfo.nickName;
        userId = playerInfo.userId;
        token = playerInfo.authToken;

        state = ConnectionState.WAITING;
        // this will call addPlayerToMatch eventually
        server.enqueueToFindMatchForPlayer(this);
    }

    void addPlayerToMatch(Match match) {
        if (match != null && this.match == null) {
            this.match = match;
            state = ConnectionState.PLAYING;
            server.serverStats.playerConnected();
            startedPlayingMs = TimeUtils.millis();
            lastMessageReceived = startedPlayingMs;
            lastGameMessageReceived = startedPlayingMs;
            Gdx.app.log("Player", "Successfully connected " + nickName + "/" + userId
                    + " - " + server.serverStats.getPlayersCurrentlyConnected() + " connected overall");
            match.sendFullInformation();
        }
    }

    public void disconnected() {
        if (state != ConnectionState.CONNECTED) {
            Gdx.app.log("Player", "Disconnect: " + nickName + "/" + userId);
        }
        state = ConnectionState.DISCONNECTED;
        // dispose everything here
        if (match != null) {
            match.playerDisconnected(this);
            server.serverStats.playerDisconnected((TimeUtils.millis() - startedPlayingMs) / 1000);
        }
    }

    public void onMessage(Object object) throws UnexpectedException {
        lastMessageReceived = System.currentTimeMillis();
        if (object instanceof PlayerInfo && state == ConnectionState.CONNECTED) {
            doConnect((PlayerInfo) object);

            if (state != ConnectionState.PLAYING && state != ConnectionState.WAITING && conn.isOpen()) {
                // if connection was not successfully established and player does not wait, disconnect the player
                conn.close(4101, "Could not add you to a match");
            }
        } else if (object instanceof InGameMessage && state == ConnectionState.PLAYING) {
            lastGameMessageReceived = System.currentTimeMillis();
            if (GAME_TIMEOUT_WARNING.equals(lastMessageToPlayer)) {
                sendMessageToPlayer("");
            }
            match.gotMessage(this, (InGameMessage) object);
        } else if (!(object instanceof KeepAliveMessage))
            throw new UnexpectedException();
    }

    public void sendImmediately(String string) {
        if (conn.isOpen())
            conn.send(string);
    }

    public void enqueue(String string) {
        synchronized (outgoingQueue) {
            outgoingQueue.addLast(string);
        }
    }

    public void sendQueue() {
        synchronized (outgoingQueue) {
            if (conn.isOpen()) {
                while (!outgoingQueue.isEmpty()) {
                    conn.send(outgoingQueue.removeFirst());
                }
            } else
                outgoingQueue.clear();
        }
    }

    public void sendMessageToPlayer(String s) {
        if (state != ConnectionState.DISCONNECTED && !s.equals(lastMessageToPlayer)) {
            lastMessageToPlayer = s;
            sendImmediately("YMSG" + s);
        }
    }

    public boolean checkTimeOuts() {
        long time = System.currentTimeMillis();

        if (time - lastGameMessageReceived > (server.serverConfig.secondsInactivity - SECONDS_INACTIVITY_WARNING) * 1000L) {
            sendMessageToPlayer(GAME_TIMEOUT_WARNING);
        }

        if (time - lastMessageReceived > server.serverConfig.secondsTimeout * 1000L) {
            Gdx.app.log("Player", "Timeout: " + (time - lastMessageReceived));
            conn.close(4102, "Timeout");
        }

        if (time - lastGameMessageReceived > server.serverConfig.secondsInactivity * 1000L) {
            Gdx.app.log("Player", "Inactive: " + (time - lastGameMessageReceived));
            conn.close(4102, "Inactivity");
        }

        return false;
    }

    enum ConnectionState {CONNECTED, WAITING, PLAYING, DISCONNECTED}

    static class UnexpectedException extends Exception {

    }
}
