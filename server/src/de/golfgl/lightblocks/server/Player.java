package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;

import org.java_websocket.WebSocket;

import de.golfgl.lightblocks.server.model.PlayerInfo;

public class Player {
    private final LightblocksServer server;
    private final WebSocket conn;
    public String nickName;
    public String userId;
    public String token;
    public ConnectionState state = ConnectionState.CONNECTING;
    private Match match;

    public Player(LightblocksServer server, WebSocket conn) {
        this.server = server;
        this.conn = conn;
    }

    private void connected(PlayerInfo playerInfo) {
        if (playerInfo.nickName == null || state != ConnectionState.CONNECTING) {
            // client error => just return;
            return;
        }

        nickName = playerInfo.nickName;
        userId = playerInfo.userId;
        token = playerInfo.token;

        match = server.findMatchForPlayer(this);

        if (match != null) {
            Gdx.app.log("Player", "Successfully connected " + nickName + "/" + userId);
            state = ConnectionState.CONNECTED;
        } else {
            conn.close(4100, "No match found for you.");
        }
    }

    public void disconnected() {
        if (state != ConnectionState.CONNECTING) {
            Gdx.app.log("Player", "Disconnect: " + nickName + "/" + userId);
        }
        state = ConnectionState.DISCONNECTED;
        // dispose everything here
        if (match != null) {
            match.playerDisconnected(this);
        }
    }

    public void onMessage(Object object) throws UnexpectedException {
        if (object instanceof PlayerInfo && state == ConnectionState.CONNECTING) {
            connected((PlayerInfo) object);

            if (state != ConnectionState.CONNECTED && conn.isOpen()) {
                // if connection was not successfully established, disconnect the player
                conn.close();
            }
        } else
            throw new UnexpectedException();
    }

    enum ConnectionState {CONNECTING, CONNECTED, PLAYING, DISCONNECTED}

    static class UnexpectedException extends Exception {

    }
}
