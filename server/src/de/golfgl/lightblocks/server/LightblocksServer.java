package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import de.golfgl.lightblocks.server.model.Handshake;

public class LightblocksServer extends WebSocketServer implements ApplicationListener {

    private final Handshake handshake = new Handshake();
    private final Json json = new Json();

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

        handshake.authRequired = false;
        handshake.name = "Lightblocks Server";
        handshake.owner = "me";
        handshake.version = 1;

        json.setOutputType(JsonWriter.OutputType.json);
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
        synchronized (json) {
            conn.send(json.toJson(this.handshake)); //This method sends a message to the new client
            Gdx.app.debug("Server", "new connection to " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Gdx.app.debug("Server", "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Gdx.app.debug("Server", "received message from " + conn.getRemoteSocketAddress() + ": " + message);
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
