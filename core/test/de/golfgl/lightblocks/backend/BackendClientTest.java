package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by Benjamin Schulte on 12.09.2018.
 */
public class BackendClientTest {
    boolean requesting = false;
    private String lastCreatedUserId;

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

        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }

    @Test
    public void createPlayer() throws Exception {
        new BackendClient().createPlayer("123", new CreationListener());
        waitWhileRequesting();
        Assert.assertNull(lastCreatedUserId);

        new BackendClient().createPlayer("user", new CreationListener());
        waitWhileRequesting();
        Assert.assertNotNull(lastCreatedUserId);

        new BackendClient().createPlayer("mümmelmann", new CreationListener());
        waitWhileRequesting();
        Assert.assertNotNull(lastCreatedUserId);
    }

    @After
    public void waitWhileRequesting() throws InterruptedException {
        while (requesting) {
            Thread.sleep(100);
        }
    }

    private class CreationListener implements BackendClient.ICreatePlayerResponse {
        public CreationListener() {
            requesting = true;
        }

        @Override
        public void onFail(String errorMsg) {
            requesting = false;
            lastCreatedUserId = null;
        }

        @Override
        public void onCreated(String userId, String userKey) {
            requesting = false;
            lastCreatedUserId = userId;
        }
    }
}