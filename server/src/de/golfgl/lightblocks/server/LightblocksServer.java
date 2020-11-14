package de.golfgl.lightblocks.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;
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
    final ServerConfiguration serverConfig;
    final Serializer serializer = new Serializer();
    private final ServerInfo serverInfo = new ServerInfo();
    private final Match[] matches;
    private boolean running = true;
    private JmDNS jmdns;

    public LightblocksServer(InetSocketAddress address, ServerConfiguration serverConfiguration) {
        super(address);
        this.serverConfig = serverConfiguration;
        this.matches = new Match[serverConfig.threadNum];
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
            matches[i] = new Match(this);
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

        matches[0] = new Match(this);

        serverInfo.authRequired = false;
        serverInfo.name = "Lightblocks Server";
        serverInfo.owner = "me";
        serverInfo.version = 1;
    }

    @Override
    public void onStart() {
        Gdx.app.log("Server", "server started successfully, listening on port " + getPort());

        if (serverConfig.enableNsd) {
            try {
                jmdns = JmDNS.create(InetAddress.getLocalHost());
                // Register a service
                ServiceInfo serviceInfo = ServiceInfo.create("_lightblocks._tcp.local.", "lbserver-" + serverConfig.name, getPort(), "");
                jmdns.registerService(serviceInfo);
                Gdx.app.log("Server", "Registered for service discovery " + jmdns.getInetAddress());
            } catch (IOException e) {
                Gdx.app.error("Server", "Could not register for service discovery.", e);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // will not happen
    }

    @Override
    public void render() {
        // update game state here
        try {
            renderThread(0, Gdx.graphics.getDeltaTime());
        } catch (Throwable t) {
            Gdx.app.error("Server", "Uncaught error ", t);
        }
    }

    public void renderThread(int thread, float delta) {
        // update corresponding game model
        // Gdx.app.debug("Match" + thread, "update with delta " + delta);
        matches[thread].update(delta);
    }

    public synchronized Match findMatchForPlayer(Player player) {
        for (int i = 0; i < serverConfig.threadNum; i++) {
            if (matches[i].connectPlayer(player)) {
                Gdx.app.debug("Server", "Connected player to match " + i);
                return matches[i];
            }
        }

        return null;
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
        if (conn.getAttachment() != null) {
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
