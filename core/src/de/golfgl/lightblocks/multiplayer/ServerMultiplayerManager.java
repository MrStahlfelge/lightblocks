package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;

public class ServerMultiplayerManager {
    private WebSocket socket;
    private long startTimePing;
    private int pingMs = -1;
    private PlayState state;
    private String lastErrorMsg;

    public ServerMultiplayerManager() {
        clear();
    }

    private void clear() {
        state = PlayState.CLOSED;
        socket = null;
        pingMs = -1;
    }

    public void connect(String address) {
        if (isClosed()) {
            this.socket = WebSockets.newSocket(address);
            socket.setSendGracefully(true);
            socket.addListener(new SocketListener());
            try {
                socket.connect();
                state = PlayState.CONNECTING;
            } catch (Throwable t) {
                lastErrorMsg = t.getMessage();
                clear();
            }
        }
    }

    public void disconnect() {
        if (!isClosed() && socket.isClosed()) {
            clear();
        } else if (!isClosed() && !socket.isClosed() && !socket.isClosing()) {
            socket.close();
        }
    }

    public boolean isClosed() {
        return state == PlayState.CLOSED;
    }

    public String getLastErrorMsg() {
        return lastErrorMsg;
    }

    public boolean isConnecting() {
        return state == PlayState.CONNECTING;
    }

    public boolean isConnected() {
        return state == PlayState.LOBBY || state == PlayState.IN_GAME;
    }

    public enum PlayState {CONNECTING, LOBBY, IN_GAME, CLOSED}

    private class SocketListener implements WebSocketListener {
        @Override
        public boolean onOpen(WebSocket webSocket) {
            state = PlayState.LOBBY;
            startTimePing = TimeUtils.millis();
            webSocket.sendKeepAlivePacket();
            return true;
        }

        @Override
        public boolean onClose(WebSocket webSocket, WebSocketCloseCode code, String reason) {
            Gdx.app.debug("WS", "Closed: " + webSocket.getUrl());
            clear();
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

            lastErrorMsg = error.getMessage();

            if (socket.isClosed()) {
                clear();
            }

            return true;
        }
    }
}
