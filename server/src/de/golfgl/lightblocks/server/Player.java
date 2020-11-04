package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;

import org.java_websocket.WebSocket;

import de.golfgl.lightblocks.server.model.PlayerInfo;

public class Player {
    private final WebSocket conn;
    public String nickName;
    public String userId;
    public String token;
    public ConnectionState state = ConnectionState.CONNECTING;

    public Player(WebSocket conn) {
        this.conn = conn;
    }

    private void connected(PlayerInfo playerInfo) {
        nickName = playerInfo.nickName;
        userId = playerInfo.userId;
        token = playerInfo.token;

        state = ConnectionState.CONNECTED;
        Gdx.app.log("Player", "Successfully connected " + nickName + "/" + userId);
    }

    public void disconnected() {
        if (state != ConnectionState.CONNECTING) {
            Gdx.app.log("Player", "Disconnect: " + nickName + "/" + userId);
        }
        state = ConnectionState.DISCONNECTED;
    }

    public void onMessage(Object object) throws UnexpectedException {
        if (object instanceof PlayerInfo && state == ConnectionState.CONNECTING)
            connected((PlayerInfo) object);
        else
            throw new UnexpectedException();
    }

    enum ConnectionState {CONNECTING, CONNECTED, PLAYING, DISCONNECTED}

    static class UnexpectedException extends Exception {

    }
}
