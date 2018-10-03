package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 12.09.2018.
 */
public class BackendClientTest {
    private static final String KEY_GAMEMODE1 = "testmode1";
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
        Assert.assertTrue(createdResponse.retrievedData.nickName.startsWith("user"));

        createdResponse = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("mümmelmann", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData.userId);
        Assert.assertTrue(createdResponse.retrievedData.nickName.startsWith("mummelmann"));

        WaitForResponseListener<List<PlayerDetails>> listPlayerResponse = new WaitForResponseListener<List
                <PlayerDetails>>();
        backendClient.fetchPlayerByNicknamePrefixList("user", listPlayerResponse);
        waitWhileRequesting();
        Assert.assertNotNull(listPlayerResponse.retrievedData);
        Assert.assertFalse(listPlayerResponse.retrievedData.isEmpty());
        Assert.assertTrue(listPlayerResponse.retrievedData.get(0).nickName.startsWith("user"));

        createdResponse = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("gpgsuser", "gpgs", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData.userId);
        Assert.assertTrue(createdResponse.retrievedData.nickName.startsWith("gpgsuser"));
        Assert.assertTrue(createdResponse.retrievedData.nickName.indexOf("@gpgs") > 0);

        backendClient.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
    }

    @Test
    public void testPostScore() throws InterruptedException {
        BackendClient backendClientPlayer1 = new BackendClient();
        BackendClient backendClientPlayer2 = new BackendClient();

        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClientPlayer1.createPlayer("player1", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);
        backendClientPlayer2.createPlayer("player2", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);

        int mode1Player1Best = 0;
        int mode2Player1Best = 0;
        int mode1Player2Best = 0;
        int mode2Player2Best = 0;
        int blocksPlayer1 = 0;
        int blocksPlayer2 = 0;

        for (int i = 0; i < 1000; i++) {
            Thread.sleep(50);
            int player1Mode1 = MathUtils.random(1, 200000);
            int player1Mode2 = MathUtils.random(1, 200000);
            int player2Mode1 = MathUtils.random(1, 200000);
            int player2Mode2 = MathUtils.random(1, 200000);

            int blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer1.postScore(player1Mode1, KEY_GAMEMODE1, "android", 0, "params", "replay",
                    blocksNowPlayer);
            blocksPlayer1 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer1.postScore(player1Mode2, "testmode2", "android", 0, "params", "replay",
                    blocksNowPlayer);
            blocksPlayer1 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer2.postScore(player2Mode1, KEY_GAMEMODE1, "android", 0, "params", "replay",
                    blocksNowPlayer);
            blocksPlayer2 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer2.postScore(player2Mode2, "testmode2", "android", 0, "params", "replay",
                    blocksNowPlayer);
            blocksPlayer2 += blocksNowPlayer;

            mode1Player1Best = Math.max(mode1Player1Best, player1Mode1);
            mode2Player1Best = Math.max(mode2Player1Best, player1Mode2);
            mode1Player2Best = Math.max(mode1Player2Best, player2Mode1);
            mode2Player2Best = Math.max(mode2Player2Best, player2Mode2);
        }

        WaitForResponseListener<List<ScoreListEntry>> scoreListResponse = new
                WaitForResponseListener<List<ScoreListEntry>>();
        backendClientPlayer1.fetchBestScores(KEY_GAMEMODE1, scoreListResponse);
        waitWhileRequesting();

        // gucken, ob die Scores passen und durchgehen
        long lastScore = Long.MAX_VALUE;
        boolean player1Found = false;
        boolean player2Found = false;
        for (ScoreListEntry score : scoreListResponse.retrievedData) {
            Assert.assertTrue(lastScore >= score.scoreValue);
            lastScore = score.scoreValue;

            if (score.userId.equals(backendClientPlayer1.getUserId())) {
                // jeder nur einmal da
                Assert.assertFalse(player1Found);
                Assert.assertEquals(mode1Player1Best, score.scoreValue);
                player1Found = true;
            }
            if (score.userId.equals(backendClientPlayer2.getUserId())) {
                // jeder nur einmal da
                Assert.assertFalse(player2Found);
                Assert.assertEquals(mode1Player2Best, score.scoreValue);
                player2Found = true;
            }
        }

        scoreListResponse = new WaitForResponseListener<List<ScoreListEntry>>();
        backendClientPlayer1.fetchLatestScores(KEY_GAMEMODE1, scoreListResponse);
        waitWhileRequesting();

        // gucken, ob die Scores passen und durchgehen
        lastScore = Long.MAX_VALUE;
        player1Found = false;
        player2Found = false;
        for (ScoreListEntry score : scoreListResponse.retrievedData) {
            Assert.assertTrue(lastScore >= score.scoreValue);
            lastScore = score.scoreValue;

            if (score.userId.equals(backendClientPlayer1.getUserId()) && !player1Found) {
                // jeder nur einmal da
                Assert.assertEquals(mode1Player1Best, score.scoreValue);
                player1Found = true;
            }
            if (score.userId.equals(backendClientPlayer2.getUserId()) && !player2Found) {
                // jeder nur einmal da
                Assert.assertEquals(mode1Player2Best, score.scoreValue);
                player2Found = true;
            }
        }
        Assert.assertTrue(player1Found);
        Assert.assertTrue(player2Found);

        // Playerdetails abholen und gesamte Blocks prüfen
        WaitForResponseListener<PlayerDetails> playerDetailsResponse = new WaitForResponseListener<PlayerDetails>();
        backendClientPlayer1.fetchPlayerDetails(backendClientPlayer2.getUserId(), playerDetailsResponse);
        waitWhileRequesting();
        Assert.assertEquals(blocksPlayer2, playerDetailsResponse.retrievedData.countTotalBlocks);

        backendClientPlayer1.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
        backendClientPlayer2.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
    }

    @Test
    public void testFetchNewMessages() throws InterruptedException {
        BackendClient backendClientNoAuth = new BackendClient();

        // anonyme Anfrage
        WaitForResponseListener<BackendClient.NewMessagesResponse> newMessagesResponse = new
                WaitForResponseListener<BackendClient.NewMessagesResponse>();
        backendClientNoAuth.fetchNewMessages(0, LightBlocksGame.GAME_VERSIONNUMBER, 1023, 1,
                newMessagesResponse);
        waitWhileRequesting();

        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // veraltete Clientversion
        newMessagesResponse = new WaitForResponseListener<BackendClient.NewMessagesResponse>();
        backendClientNoAuth.fetchNewMessages(0, 1, 1023, 1,
                newMessagesResponse);
        waitWhileRequesting();

        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNotNull(newMessagesResponse.retrievedData.warningMsg);

        // Anfrage mit Spieler
        BackendClient backendClientPlayer = new BackendClient();
        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClientPlayer.createPlayer("player", createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);

        newMessagesResponse = new WaitForResponseListener<BackendClient.NewMessagesResponse>();
        backendClientPlayer.fetchNewMessages(0, LightBlocksGame.GAME_VERSIONNUMBER, 1024, 1, newMessagesResponse);
        waitWhileRequesting();
        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertTrue(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // Anfrage mit falscher Authentifizierung
        newMessagesResponse = new WaitForResponseListener<BackendClient.NewMessagesResponse>();
        BackendClient noAuthPlayer = new BackendClient();
        noAuthPlayer.setUserId("----");
        noAuthPlayer.fetchNewMessages(0, LightBlocksGame.GAME_VERSIONNUMBER, 1024, 1, newMessagesResponse);
        waitWhileRequesting();
        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNotNull(newMessagesResponse.retrievedData.warningMsg);


        backendClientPlayer.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
    }

    @After
    public void waitWhileRequesting() throws InterruptedException {
        while (requesting) {
            Thread.sleep(100);
        }
    }

    private class WaitForResponseListener<T> implements BackendClient.IBackendResponse<T> {
        T retrievedData;
        boolean successful;

        WaitForResponseListener() {
            requesting = true;
        }

        @Override
        public void onFail(int statusCode, String errorMsg) {
            requesting = false;
            successful = false;
        }

        @Override
        public void onSuccess(T retrievedData) {
            requesting = false;
            this.retrievedData = retrievedData;
            successful = true;
        }
    }
}