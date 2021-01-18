package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

import org.java_websocket.WebSocket;

import de.golfgl.lightblocks.server.model.InGameMessage;
import de.golfgl.lightblocks.server.model.PlayerInfo;

public class Player {
    private final LightblocksServer server;
    private final WebSocket conn;
    public String nickName;
    public String userId;
    public String token;
    public ConnectionState state = ConnectionState.CONNECTED;
    private Match match;
    private long startedPlayingMs;

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
        server.findMatchForPlayer(this);
    }

    void addPlayerToMatch(Match match) {
        if (match != null && this.match == null) {
            this.match = match;
            state = ConnectionState.PLAYING;
            server.serverStats.playerConnected();
            startedPlayingMs = TimeUtils.millis();
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
        if (object instanceof PlayerInfo && state == ConnectionState.CONNECTED) {
            doConnect((PlayerInfo) object);

            if (state != ConnectionState.PLAYING && state != ConnectionState.WAITING && conn.isOpen()) {
                // if connection was not successfully established and player does not wait, disconnect the player
                conn.close(4101, "Could not add you to a match");
            }
        } else if (object instanceof InGameMessage && state == ConnectionState.PLAYING) {
            match.gotMessage(this, (InGameMessage) object);
        } else
            throw new UnexpectedException();
    }

    public void send(String string) {
        if (conn.isOpen())
            conn.send(string);
    }

    public void sendMessageToPlayer(String s) {
        if (state != ConnectionState.DISCONNECTED) {
            send("YMSG" + s);
        }
    }

    enum ConnectionState {CONNECTED, WAITING, PLAYING, DISCONNECTED}

    static class UnexpectedException extends Exception {

    }
}
