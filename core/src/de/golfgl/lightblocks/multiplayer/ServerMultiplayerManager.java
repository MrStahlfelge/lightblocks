package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.data.WebSocketCloseCode;

public class ServerMultiplayerManager implements WebSocketListener {
    private long startTimePing;
    private int pingMs = -1;

    @Override
    public boolean onOpen(WebSocket webSocket) {
        startTimePing = TimeUtils.millis();
        webSocket.sendKeepAlivePacket();
        return true;
    }

    @Override
    public boolean onClose(WebSocket webSocket, WebSocketCloseCode code, String reason) {
        Gdx.app.debug("WS", "Closed: " + webSocket.getUrl());
        return false;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, String packet) {
        Gdx.app.debug("WS", "Received: " + packet);

        if (pingMs < 0) {
            pingMs = (int) (TimeUtils.millis() - startTimePing);
            Gdx.app.log("WS", "Connected to " + webSocket.getUrl() + " with a ping of " + pingMs);
        }

        return true;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, byte[] packet) {
        return false;
    }

    @Override
    public boolean onError(WebSocket webSocket, Throwable error) {
        Gdx.app.error("Server", "Error received", error);
        return false;
    }

}
