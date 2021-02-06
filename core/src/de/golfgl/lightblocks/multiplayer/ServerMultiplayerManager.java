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

import java.util.ArrayList;

import de.golfgl.lightblocks.LightBlocksGame;

public class ServerMultiplayerManager {
    public static final String ID_SERVERINFO = "HSH";
    public static final String ID_PLAYERINFO = "PIN";
    public static final String ID_MATCHINFO = "MCH";
    private static final long SECONDS_TIMEOUT = 3000L;
    private final LightBlocksGame app;

    private WebSocket socket;
    private long startTimePing;
    private int pingMs = -1;
    private PlayState state;
    private String gameMode;
    private String roomName;
    private String lastErrorMsg;
    private JsonReader jsonReader = new JsonReader();
    private ServerModels.ServerInfo serverInfo;
    private ServerMultiplayerModel gameModel;
    private long lastQueueProcessedMs;
    private long lastMessageSendMs;

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
                Gdx.app.error("WS", t.getMessage(), t);
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
            socket.send("PING-" + LightBlocksGame.GAME_VERSIONNUMBER);
            lastMessageSendMs = startTimePing;
        }
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void doStartGame(ServerMultiplayerModel serverMultiplayerModel) {
        this.gameModel = serverMultiplayerModel;
        // starting the game by sending player information
        JsonValue playerInfo = new JsonValue(JsonValue.ValueType.object);
        playerInfo.addChild("nickName", new JsonValue(app.player.getName()));
        playerInfo.addChild("clientVersion", new JsonValue(LightBlocksGame.GAME_VERSIONNUMBER));
        if (app.backendManager.hasUserId()) {
            playerInfo.addChild("userId", new JsonValue(app.backendManager.ownUserId()));
            if (serverInfo.authRequired) {
                playerInfo.addChild("authToken", new JsonValue(app.backendManager.getToken()));
            }
        }
        if (gameMode != null) {
            playerInfo.addChild("gameMode", new JsonValue(gameMode));
        }
        if (roomName != null && !roomName.isEmpty()) {
            playerInfo.addChild("roomName", new JsonValue(roomName));
        }

        socket.send(ID_PLAYERINFO + playerInfo.toJson(JsonWriter.OutputType.json));
        lastMessageSendMs = TimeUtils.millis();
        state = PlayState.IN_GAME;
        processedQueue();
    }

    public void processedQueue() {
        long timeNow = TimeUtils.millis();
        lastQueueProcessedMs = timeNow;
        if (socket != null && (timeNow - lastMessageSendMs) > 1500L) {
            socket.sendKeepAlivePacket();
            lastMessageSendMs = timeNow;
        }
    }

    public void doStopGame() {
        this.gameModel = null;
        if (isConnected()) {
            socket.close();
        }
    }

    protected void handlePong(String information) {
        pingMs = (int) (TimeUtils.millis() - startTimePing);
        Gdx.app.debug("WS", "Ping from " + socket.getUrl() + ": " + pingMs + " " + information);
        if (serverInfo != null && information != null) {
            JsonValue jsonValue = this.jsonReader.parse(information);
            serverInfo.activePlayers = jsonValue.getInt("activePlayers", -1);
        }
    }

    private void handleServerInfo(String json) {
        JsonValue jsonValue = this.jsonReader.parse(json);

        serverInfo = new ServerModels.ServerInfo();
        serverInfo.authRequired = jsonValue.getBoolean("authRequired");
        serverInfo.name = jsonValue.getString("name");
        serverInfo.owner = jsonValue.getString("owner", null);
        serverInfo.description = jsonValue.getString("description", null);
        serverInfo.version = jsonValue.getInt("version");
        serverInfo.modes = new ArrayList<>();
        serverInfo.privateRooms = jsonValue.getBoolean("privateRooms", false);

        if (jsonValue.has("modes")) {
            for (JsonValue mode = jsonValue.get("modes").child; mode != null; mode = mode.next) {
                serverInfo.modes.add(mode.asString());
            }
        }

        state = PlayState.LOBBY;
        doPing();
    }

    public int getLastPingTime() {
        return pingMs;
    }

    public void doSendGameMessage(String message) {
        if (state == PlayState.IN_GAME) {
            socket.send("IGM" + message);
            lastMessageSendMs = TimeUtils.millis();
        }
    }

    public enum PlayState {CONNECTING, LOBBY, IN_GAME, CLOSED}

    private class SocketListener implements WebSocketListener {
        @Override
        public boolean onOpen(WebSocket webSocket) {
            return true;
        }

        @Override
        public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
            Gdx.app.debug("WS", "Closed: " + webSocket.getUrl());
            clear();

            if (closeCode != WebSocketCloseCode.NORMAL && reason != null) {
                lastErrorMsg = "Server closed connection: " + reason;
            } else if (closeCode != WebSocketCloseCode.NORMAL) {
                lastErrorMsg = "Could not connect to server " + webSocket.getUrl() + "(" + closeCode + ")";
            }

            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            Gdx.app.debug("WS", "Received: " + packet);

            try {
                if (pingMs < 0 && packet.startsWith("PONG")) {
                    handlePong(packet.length() > 4 ? packet.substring(4) : null);
                    return true;
                }

                if (packet.startsWith(ID_SERVERINFO)) {
                    handleServerInfo(packet.substring(ID_SERVERINFO.length()));
                    return true;
                } else if (gameModel != null) {
                    gameModel.queueMessage(packet);

                    if (TimeUtils.timeSinceMillis(lastQueueProcessedMs) > SECONDS_TIMEOUT) {
                        socket.close(3000, "Timeout");
                        gameModel.clearMessageQueue();
                    }

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
