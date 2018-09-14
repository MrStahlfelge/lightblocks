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
    private PlayerDetails lastFetchedPlayerDetails;

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
    public void createAndFetchPlayer() throws Exception {
        BackendClient backendClient = new BackendClient();

        backendClient.createPlayer("123", new CreationListener());
        waitWhileRequesting();
        Assert.assertNull(lastCreatedUserId);

        backendClient.createPlayer("user", new CreationListener());
        waitWhileRequesting();
        Assert.assertNotNull(lastCreatedUserId);

        WaitForResponseListener<PlayerDetails> fetchDetailResponse = new WaitForResponseListener<PlayerDetails>();
        backendClient.fetchPlayerDetails(lastCreatedUserId, fetchDetailResponse);
        waitWhileRequesting();
        Assert.assertTrue(fetchDetailResponse.retrievedData.nickName.startsWith("user"));

        backendClient.createPlayer("mümmelmann", new CreationListener());
        waitWhileRequesting();
        Assert.assertNotNull(lastCreatedUserId);

        fetchDetailResponse = new WaitForResponseListener<PlayerDetails>();
        backendClient.fetchPlayerDetails(lastCreatedUserId, fetchDetailResponse);
        waitWhileRequesting();
        Assert.assertTrue(fetchDetailResponse.retrievedData.nickName.startsWith("mummelmann"));

        //TODO ein Delete auch noch
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

    private class WaitForResponseListener<T> implements BackendClient.IBackendResponse<T> {
        T retrievedData;

        WaitForResponseListener() {
            requesting = true;
        }

        @Override
        public void onFail(String errorMsg) {
            requesting = false;
            lastFetchedPlayerDetails = null;
        }

        @Override
        public void onSuccess(T retrievedData) {
            requesting = false;
            this.retrievedData = retrievedData;
        }
    }
}