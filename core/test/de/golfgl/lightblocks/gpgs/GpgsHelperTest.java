package de.golfgl.lightblocks.gpgs;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.golfgl.gdxgamesvcs.GameJoltClient;

import static org.junit.Assert.*;

/**
 * Created by Benjamin Schulte on 07.04.2018.
 */
public class GpgsHelperTest {
    @BeforeClass
    public static void init() {
        // Note that we don't need to implement any of the listener's methods
        Gdx.app = new HeadlessApplication(new ApplicationListener() {
            @Override
            public void create() {
            }

            @Override
            public void resize(int width, int height) {
            }

            @Override
            public void render() {
            }

            @Override
            public void pause() {
            }

            @Override
            public void resume() {
            }

            @Override
            public void dispose() {
            }
        });

        // Use Mockito to mock the OpenGL methods since we are running headlessly
        Gdx.gl20 = Mockito.mock(GL20.class);
        Gdx.gl = Gdx.gl20;
    }

    @Test
    public void resetEventsWeb() throws InterruptedException {
        GameJoltClient gjClient = new GameJoltClient();
        gjClient.initialize(GpgsHelper.GJ_APP_ID, GpgsHelper.GJ_PRIVATE_KEY);
        gjClient.setEventKeyPrefix(GpgsHelper.GJ_WEBEVENT_PREFIX);
        resetEvents(gjClient);
    }

    @Test
    public void resetEventsAndroid() throws InterruptedException {
        GameJoltClient gjClient = new GameJoltClient();
        gjClient.initialize(GpgsHelper.GJ_APP_ID, GpgsHelper.GJ_PRIVATE_KEY);
        gjClient.setEventKeyPrefix(GpgsHelper.GJ_ANDROIDEVENT_PREFIX);
        resetEvents(gjClient);
    }

    @Test
    public void resetEventsMobile() throws InterruptedException {
        GameJoltClient gjClient = new GameJoltClient();
        gjClient.initialize(GpgsHelper.GJ_APP_ID, GpgsHelper.GJ_PRIVATE_KEY);
        gjClient.setEventKeyPrefix(GpgsHelper.GJ_MOBILEEVENT_PREFIX);
        resetEvents(gjClient);
    }

    private void resetEvents(GameJoltClient gjClient) throws InterruptedException {
        gjClient.initializeOrResetEventKey(GpgsHelper.GJ_EVENT_MISSION);
        gjClient.initializeOrResetEventKey(GpgsHelper.GJ_EVENT_MARATHON);
        gjClient.initializeOrResetEventKey(GpgsHelper.GJ_EVENT_LINES_CLEARED);
        gjClient.initializeOrResetEventKey(GpgsHelper.GJ_EVENT_LOCAL_MULTIPLAYER);
        Thread.sleep(1500); // give the HTTP request some time before app is killed
    }
}