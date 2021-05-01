package de.golfgl.lightblocks.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.TimeUtils;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import de.golfgl.lightblocks.server.model.ServerInfo;

public class LightblocksServer extends WebSocketServer implements ApplicationListener {
    public static final int SERVER_VERSION = 2108; // reported to the clients, don't mess with it
    public static final int STATS_AGGREGATION_TIME_HRS = 24;

    final ServerConfiguration serverConfig;
    final Serializer serializer = new Serializer();
    final ServerStats serverStats = new ServerStats();
    private final ServerInfo serverInfo;
    private final Match[] matches;
    private final Queue<Player> playerToConnectQueue = new Queue<>();
    private boolean running = true;
    private JmDNS jmdns;

    public LightblocksServer(InetSocketAddress address, ServerConfiguration serverConfiguration) {
        super(address, Math.max(1, serverConfiguration.threadNum / 2), null);
        this.serverConfig = serverConfiguration;
        this.serverInfo = serverConfiguration.getServerInfo();
        this.matches = new Match[serverConfig.threadNum - 1];
    }

    public static void main(String[] arg) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        ServerConfiguration serverConfig = new ServerConfiguration(arg);

        final LightblocksServer server = new LightblocksServer(new InetSocketAddress(serverConfig.port), serverConfig);
        new HeadlessApplication(server, config) {
            @Override
            public void exit() {
                server.running = false;
                // Unregister all services
                if (server.jmdns != null) {
                    server.jmdns.unregisterAllServices();
                }
                super.exit();
            }
        };

        server.startThreads();

        // this will block when successful, so don't do it inside create()
        server.setTcpNoDelay(true);
        server.run();

        Gdx.app.exit();
    }

    private void startThreads() {
        // thread 1 was started by HeadlessApplication - start up the other threads
        final long renderInterval = ((MockGraphics) Gdx.graphics).getTargetRenderInterval();
        for (int i = 1; i < serverConfig.threadNum; i++) {
            final int threadNum = i;
            matches[i - 1] = new Match(this);
            new Thread("Render" + i) {
                @Override
                public void run() {
                    try {
                        long lastTime = TimeUtils.nanoTime();
                        long nextTime = TimeUtils.nanoTime() + renderInterval;
                        if (renderInterval >= 0f) {
                            while (running) {
                                final long n = TimeUtils.nanoTime();
                                if (nextTime > n) {
                                    try {
                                        long sleep = nextTime - n;
                                        Thread.sleep(sleep / 1000000, (int) (sleep % 1000000));
                                    } catch (InterruptedException ignored) {
                                    }
                                    nextTime = nextTime + renderInterval;
                                } else {
                                    nextTime = n + renderInterval;
                                }

                                long now = System.nanoTime();
                                float deltaTime = (now - lastTime) / 1000000000.0f;
                                lastTime = now;

                                renderThread(threadNum, deltaTime);
                            }
                        }
                    } catch (Throwable t) {
                        if (t instanceof RuntimeException)
                            throw (RuntimeException) t;
                        else
                            throw new GdxRuntimeException(t);
                    }
                }
            }.start();
        }
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(serverConfig.loglevel);
    }

    @Override
    public void onStart() {
        Gdx.app.log("Server", "server started successfully, listening on port " + getPort());

        if (serverConfig.enableNsd) {
            try {
                jmdns = JmDNS.create(InetAddress.getLocalHost());
                // Register a service
                ServiceInfo serviceInfo = ServiceInfo.create("_lightblocks._tcp.local.", serverConfig.getServerInfo().name, getPort(), "");
                jmdns.registerService(serviceInfo);
                Gdx.app.log("Server", "Registered for service discovery " + jmdns.getInetAddress());
            } catch (IOException e) {
                Gdx.app.error("Server", "Could not register for service discovery.", e);
            }
        }

        Gdx.app.log("Server", "Server version: " + SERVER_VERSION);
    }

    @Override
    public void resize(int width, int height) {
        // will not happen
    }

    @Override
    public void render() {
        // update game state here
        try {
            connectWaitingPlayers();

            serverStats.outputAndResetAfter(60 * 60 * STATS_AGGREGATION_TIME_HRS);
        } catch (Throwable t) {
            Gdx.app.error("Server", "Uncaught error ", t);
        }
    }

    public void renderThread(int thread, float delta) {
        try {
            // update corresponding game model
            // Gdx.app.debug("Match" + thread, "update with delta " + delta);
            matches[thread - 1].update(delta);
        } catch (Throwable t) {
            Gdx.app.error("Server", "Uncaught error ", t);
        }
    }

    public void enqueueToFindMatchForPlayer(Player player) {
        synchronized (playerToConnectQueue) {
            // enqueue the player to the waitlist
            playerToConnectQueue.addLast(player);
            player.sendMessageToPlayer("Matchmaking...");
        }
    }

    private void connectWaitingPlayers() {
        synchronized (playerToConnectQueue) {
            if (playerToConnectQueue.isEmpty())
                return;

            Player first = playerToConnectQueue.first();
            if (first.state != Player.ConnectionState.WAITING) {
                playerToConnectQueue.removeFirst();
                Gdx.app.log("Server", "Removed player in state " + first.state + " from connect queue");
                return;
            }

            boolean connected;
            if (serverInfo.privateRooms && first.roomName != null) {
                // connect to a private room
                connected = connectPlayerToPrivateRoom(first);

            } else {
                // try to connect to occupied matches first...
                connected = connectWaitingPlayer(first, false);
                //... if not successful, use empty matches
                if (!connected)
                    connected = connectWaitingPlayer(first, true);
            }

            if (connected)
                playerToConnectQueue.removeFirst();
        }
    }

    private boolean connectPlayerToPrivateRoom(Player player) {
        boolean connected = false;
        // is there a room with the name?
        int roomNum = -1;
        for (int i = 1; i < serverConfig.threadNum; i++) {
            if (!connected && roomNum < 0 && player.roomName.equalsIgnoreCase(matches[i - 1].roomName)) {
                roomNum = i - 1;
                if (matches[i - 1].checkIfPlayerFitsMatch(player) && matches[i - 1].connectPlayer(player)) {
                    Gdx.app.log("Server", "Connected " + player.nickName + " to room on match " + i);
                    connected = true;
                } else {
                    Gdx.app.log("Server", "Room requested by " + player.nickName + " not connected.");
                }
            }
        }

        if (!connected && roomNum < 0) {
            // room does not exist yet, assign a fitting and empty room
            for (int i = 1; i < serverConfig.threadNum; i++) {
                if (!connected && matches[i - 1].getConnectedPlayerNum() == 0
                        && matches[i - 1].checkIfPlayerFitsMatch(player)
                        && matches[i - 1].connectPlayer(player)) {
                    Gdx.app.log("Server", "Created room and connected " + player.nickName + " on match " + i);
                    matches[i - 1].roomName = player.roomName;
                    connected = true;
                }
            }
        }
        return connected;
    }

    private boolean connectWaitingPlayer(Player player, boolean useEmptyMatches) {
        boolean connected = false;
        for (int i = 1; i < serverConfig.threadNum; i++) {
            if (!connected && (useEmptyMatches || matches[i - 1].getConnectedPlayerNum() > 0)
                    && matches[i - 1].checkIfPlayerFitsMatch(player)
                    && matches[i - 1].connectPlayer(player)) {
                Gdx.app.log("Server", "Connected " + player.nickName + " to match " + i);
                connected = true;
            }
        }
        return connected;
    }

    @Override
    public void pause() {
        // not needed
    }

    @Override
    public void resume() {
        // will not happen
    }

    @Override
    public void dispose() {
        try {
            stop();
        } catch (IOException | InterruptedException e) {
            Gdx.app.error("Server", "Error while stopping server.", e);
        }
        Gdx.app.log("Server", "Stopped.");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (serializer) {
            conn.setAttachment(new Player(this, conn));
            conn.send(serializer.serialize(this.serverInfo)); //This method sends a message to the new client
            Gdx.app.debug("Server", "new connection to " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Gdx.app.debug("Server", "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        if (conn.getAttachment() != null) {
            conn.<Player>getAttachment().disconnected();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Gdx.app.debug("Server", "received message from " + conn.getRemoteSocketAddress() + ": " + message);
        if (message.equals("PING")) {
            conn.send("PONG");
        } else if (message.startsWith("PING-")) {
            int clientVersion = 0;
            try {
                clientVersion = Integer.parseInt(message.substring(5));
            } catch (Throwable ignore) {
                Gdx.app.error("Server", "Could not detect client version: " + message);
            }
            conn.send("PONG" + sendServerStats(clientVersion));
        } else if (conn.getAttachment() != null) {
            Object object = serializer.deserialize(message);
            if (object != null) try {
                conn.<Player>getAttachment().onMessage(object);
            } catch (Player.UnexpectedException e) {
                Gdx.app.error("Server", "Unexpected message for player: " + message);
                conn.close(4101, "Message unexpected.");
            }
            else {
                conn.close(4101, "Message illegible.");
            }
        }
    }

    private String sendServerStats(int clientVersion) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("activePlayers", new JsonValue(serverStats.getPlayersCurrentlyConnected()));
        return json.toJson(JsonWriter.OutputType.json);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Gdx.app.debug("Server", "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Gdx.app.error("Server", "an error occurred on connection " + conn, ex);
    }
}
