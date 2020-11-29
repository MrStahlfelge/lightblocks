package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.screen.PlayScreen;

public class ServerMultiplayerManager {
    public static final String ID_SERVERINFO = "HSH";
    public static final String ID_PLAYERINFO = "PIN";
    public static final String ID_MATCHINFO = "MCH";
    private final LightBlocksGame app;

    private WebSocket socket;
    private long startTimePing;
    private int pingMs = -1;
    private PlayState state;
    private String lastErrorMsg;
    private JsonReader jsonReader = new JsonReader();
    private ServerModels.ServerInfo serverInfo;
    private ServerMultiplayerModel gameModel;

    public ServerMultiplayerManager(LightBlocksGame app) {
        this.app = app;
        clear();
    }

    private void clear() {
        state = PlayState.CLOSED;
        socket = null;
        pingMs = -1;
    }

    public void connect(String address) {
        if (isClosed()) {
            lastErrorMsg = null;
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

    public ServerModels.ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean isConnecting() {
        return state == PlayState.CONNECTING;
    }

    public boolean isConnected() {
        return state == PlayState.LOBBY || state == PlayState.IN_GAME;
    }

    public void doPing() {
        if (isConnected()) {
            pingMs = -1;
            startTimePing = TimeUtils.millis();
            socket.sendKeepAlivePacket();
        }
    }

    public void doStartGame(ServerMultiplayerModel serverMultiplayerModel, PlayScreen userInterface, IGameModelListener uiGameboard) {
        this.gameModel = serverMultiplayerModel;
        // starting the game by sending player information
        JsonValue playerInfo = new JsonValue(JsonValue.ValueType.object);
        playerInfo.addChild("nickName", new JsonValue(app.player.getName()));
        if (app.backendManager.hasUserId()) {
            playerInfo.addChild("userId", new JsonValue(app.backendManager.ownUserId()));
        }

        socket.send(ID_PLAYERINFO + playerInfo.toJson(JsonWriter.OutputType.json));
    }

    protected void handlePong() {
        pingMs = (int) (TimeUtils.millis() - startTimePing);
        Gdx.app.log("WS", "Ping from " + socket.getUrl() + ": " + pingMs);
    }

    private void handleServerInfo(String json) {
        JsonValue jsonValue = this.jsonReader.parse(json);

        serverInfo = new ServerModels.ServerInfo();
        serverInfo.authRequired = jsonValue.getBoolean("authRequired");
        serverInfo.name = jsonValue.getString("name");
        serverInfo.owner = jsonValue.getString("owner", null);
        serverInfo.version = jsonValue.getInt("version");

        state = PlayState.LOBBY;
        doPing();
    }

    public int getLastPingTime() {
        return pingMs;
    }

    public enum PlayState {CONNECTING, LOBBY, IN_GAME, CLOSED}

    private class SocketListener implements WebSocketListener {
        @Override
        public boolean onOpen(WebSocket webSocket) {
            return true;
        }

        @Override
        public boolean onClose(WebSocket webSocket, WebSocketCloseCode code, String reason) {
            Gdx.app.debug("WS", "Closed: " + webSocket.getUrl());
            clear();

            if (code == WebSocketCloseCode.ABNORMAL) {
                lastErrorMsg = "Could not connect to server " + webSocket.getUrl();
            }

            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            Gdx.app.debug("WS", "Received: " + packet);

            try {
                if (pingMs < 0 && packet.equals("PONG")) {
                    handlePong();
                    return true;
                }

                if (packet.startsWith(ID_SERVERINFO)) {
                    handleServerInfo(packet.substring(ID_SERVERINFO.length()));
                    return true;
                } else if (gameModel != null) {
                    gameModel.queueMessage(packet);
                    return true;
                }
                Gdx.app.error("Server", "Unhandled message message: " + packet);
            } catch (Throwable t) {
                Gdx.app.error("Server", "Error handling message: " + packet, t);
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
