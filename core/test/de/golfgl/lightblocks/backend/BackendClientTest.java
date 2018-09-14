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

        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("123", createdResponse);
        waitWhileRequesting();
        Assert.assertNull(createdResponse.retrievedData);

        createdResponse = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("user", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData.userId);

        WaitForResponseListener<PlayerDetails> fetchDetailResponse = new WaitForResponseListener<PlayerDetails>();
        backendClient.fetchPlayerDetails(createdResponse.retrievedData.userId, fetchDetailResponse);
        waitWhileRequesting();
        Assert.assertTrue(fetchDetailResponse.retrievedData.nickName.startsWith("user"));

        createdResponse = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("mümmelmann", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData.userId);

        fetchDetailResponse = new WaitForResponseListener<PlayerDetails>();
        backendClient.fetchPlayerDetails(createdResponse.retrievedData.userId, fetchDetailResponse);
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

    private class WaitForResponseListener<T> implements BackendClient.IBackendResponse<T> {
        T retrievedData;

        WaitForResponseListener() {
            requesting = true;
        }

        @Override
        public void onFail(String errorMsg) {
            requesting = false;
        }

        @Override
        public void onSuccess(T retrievedData) {
            requesting = false;
            this.retrievedData = retrievedData;
        }
    }
}