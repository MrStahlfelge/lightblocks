package de.golfgl.lightblocks.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;
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
    public static final int SERVER_VERSION = 2103; // reported to the clients, don't mess with it

    final ServerConfiguration serverConfig;
    final Serializer serializer = new Serializer();
    final ServerStats serverStats = new ServerStats();
    private final ServerInfo serverInfo;
    private final Match[] matches;
    private final Queue<Player> playerToConnectQueue = new Queue<>();
    private boolean running = true;
    private JmDNS jmdns;

    public LightblocksServer(InetSocketAddress address, ServerConfiguration serverConfiguration) {
        super(address);
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

        server.startThreads(config.renderInterval);

        // this will block when successful, so don't do it inside create()
        server.run();

        Gdx.app.exit();
    }

    private void startThreads(float configRenderInterval) {
        // thread 1 was started by HeadlessApplication - start up the other threads
        final long renderInterval = configRenderInterval > 0 ? (long) (configRenderInterval * 1000000000f) : (configRenderInterval < 0 ? -1 : 0);
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
                ServiceInfo serviceInfo = ServiceInfo.create("_lightblocks._tcp.local.", serverConfig.name, getPort(), "");
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

            serverStats.outputAndResetAfter(60 * 60 * 2);
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

    public void findMatchForPlayer(Player player) {
        synchronized (playerToConnectQueue) {
            // not found, enqueue the player to the waitlist
            playerToConnectQueue.addLast(player);
            player.sendMessageToPlayer("Matchmaking...");
        }
    }

    public void connectWaitingPlayers() {
        synchronized (playerToConnectQueue) {
            if (playerToConnectQueue.isEmpty())
                return;

            Player first = playerToConnectQueue.first();
            boolean connected = false;
            for (int i = 1; i < serverConfig.threadNum; i++) {
                if (!connected && matches[i - 1].connectPlayer(first)) {
                    Gdx.app.debug("Server", "Connected player to match " + i);
                    first.addPlayerToMatch(matches[i - 1]);
                    connected = true;
                }
            }

            if (connected)
                playerToConnectQueue.removeFirst();
        }
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
        if (message.equals("PING") || message.isEmpty()) {
            conn.send("PONG");
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

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Gdx.app.debug("Server", "received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Gdx.app.error("Server", "an error occurred on connection " + conn, ex);
    }
}
