package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import de.golfgl.lightblocks.server.model.ServerInfo;

public class LightblocksServer extends WebSocketServer implements ApplicationListener {

    private final ServerInfo serverInfo = new ServerInfo();
    private final Serializer serializer = new Serializer();

    public LightblocksServer(InetSocketAddress address) {
        super(address);
    }

    public static void main(String[] arg) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        int port = 8887;

        LightblocksServer server = new LightblocksServer(new InetSocketAddress(port));
        new HeadlessApplication(server, config);

        // this will block when successful, so don't do it inside create()
        server.run();

        Gdx.app.exit();
    }

    @Override
    public void create() {
        Gdx.app.log("Server", "Starting, binding on port " + getPort());
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        serverInfo.authRequired = false;
        serverInfo.name = "Lightblocks Server";
        serverInfo.owner = "me";
        serverInfo.version = 1;
    }

    @Override
    public void onStart() {
        Gdx.app.log("Server", "server started successfully, listening on port " + getPort());
    }

    @Override
    public void resize(int width, int height) {
        // will not happen
    }

    @Override
    public void render() {
        // update game state here
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
            conn.setAttachment(new Player(conn));
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
            try {
                conn.<Player>getAttachment().onMessage(object);
            } catch (Player.UnexpectedException e) {
                Gdx.app.error("Server", "Unexpected message for player: " + message);
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
