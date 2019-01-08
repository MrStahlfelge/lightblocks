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
    private static final String KEY_GAMEMODE1 = "practice";
    private static final String KEY_GAMEMODE2 = "marathon2";
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
        backendClient.createPlayer("user" + String.valueOf(MathUtils.random(100, 999)), createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData.userId);
        Assert.assertTrue(createdResponse.retrievedData.nickName.startsWith("user"));

        createdResponse = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClient.createPlayer("mümmelmann" + String.valueOf(MathUtils.random(100, 999)), createdResponse);
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
        backendClient.createPlayer("gpgsuser" + String.valueOf(MathUtils.random(100, 999)), "gpgs", createdResponse);
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

        int random = MathUtils.random(1000, 2000);

        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClientPlayer1.createPlayer("player1player1player1neu" + random, createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);
        backendClientPlayer2.createPlayer("player2player1player1neu" + random, "gpgs", createdResponse);
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
            int player1Mode1 = MathUtils.random(1, 9200000);
            int player1Mode2 = MathUtils.random(1, 1200000);
            int player2Mode1 = MathUtils.random(1, 9200000);
            int player2Mode2 = MathUtils.random(1, 1200000);

            int blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer1.postScore(new BackendScore(player1Mode1, KEY_GAMEMODE1, "android", "", "params",
                    null, blocksNowPlayer, 0, player1Mode1, 0), null);
            blocksPlayer1 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer1.postScore(new BackendScore(player1Mode2, KEY_GAMEMODE2, "android", "", "params",
                    null,
                    blocksNowPlayer, 0, player1Mode2, 0), null);
            blocksPlayer1 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer2.postScore(new BackendScore(player2Mode1, KEY_GAMEMODE1, "android", "", "params",
                    null, blocksNowPlayer, 0, player2Mode1, 0), null);
            blocksPlayer2 += blocksNowPlayer;

            blocksNowPlayer = MathUtils.random(20, 1000);
            backendClientPlayer2.postScore(new BackendScore(player2Mode2, KEY_GAMEMODE2, "android", "", "params",
                    null,
                    blocksNowPlayer, 0, player2Mode2, 0), null);
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
        WaitForResponseListener<BackendClient.WelcomeResponse> newMessagesResponse = new
                WaitForResponseListener<BackendClient.WelcomeResponse>();
        backendClientNoAuth.fetchWelcomeMessage(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1023, 1,
                newMessagesResponse);
        waitWhileRequesting();

        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // veraltete Clientversion
        newMessagesResponse = new WaitForResponseListener<BackendClient.WelcomeResponse>();
        backendClientNoAuth.fetchWelcomeMessage(1, "smarttv", "webgl", 1023, 1,
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
        backendClientPlayer.createPlayer("player" + MathUtils.random(1000, 2000), createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);

        newMessagesResponse = new WaitForResponseListener<BackendClient.WelcomeResponse>();
        backendClientPlayer.fetchWelcomeMessage(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1024, 1,
                newMessagesResponse);
        waitWhileRequesting();
        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertTrue(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // Anfrage mit falscher Authentifizierung
        newMessagesResponse = new WaitForResponseListener<BackendClient.WelcomeResponse>();
        BackendClient noAuthPlayer = new BackendClient();
        noAuthPlayer.setUserId("----");
        noAuthPlayer.fetchWelcomeMessage(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1024, 1,
                newMessagesResponse);
        waitWhileRequesting();
        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNotNull(newMessagesResponse.retrievedData.warningMsg);


        backendClientPlayer.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
    }

    @Test
    public void testChangePlayerInfo() throws InterruptedException {
        BackendClient backendClientPlayer = new BackendClient();
        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClientPlayer.createPlayer("player" + MathUtils.random(1000, 2000), createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);

        WaitForResponseListener<Void> callback = new WaitForResponseListener<Void>();
        String newMailAddress = "bs@golfgl.de";
        backendClientPlayer.changePlayerDetails("newnick", newMailAddress, 1, null, null,
                callback);
        waitWhileRequesting();
        Assert.assertTrue(callback.successful);

        WaitForResponseListener<PlayerDetails> playerDetails = new WaitForResponseListener<PlayerDetails>();
        backendClientPlayer.fetchPlayerDetails(backendClientPlayer.getUserId(), playerDetails);
        waitWhileRequesting();
        Assert.assertEquals(newMailAddress, playerDetails.retrievedData.passwordEmail);

        playerDetails = new WaitForResponseListener<PlayerDetails>();
        new BackendClient().fetchPlayerDetails(backendClientPlayer.getUserId(), playerDetails);
        waitWhileRequesting();
        Assert.assertNull(playerDetails.retrievedData.passwordEmail);

        callback = new WaitForResponseListener<Void>();
        backendClientPlayer.changePlayerDetails("1", null, 1, null, null,
                callback);
        waitWhileRequesting();
        Assert.assertFalse(callback.successful);

        callback = new WaitForResponseListener<Void>();
        backendClientPlayer.changePlayerDetails(null, "bbs", 1, null, null,
                callback);
        waitWhileRequesting();
        Assert.assertFalse(callback.successful);

        backendClientPlayer.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();

    }

    @Test
    public void testListOwnMatches() throws InterruptedException {
        BackendClient backendClientPlayer = new BackendClient();
        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<>();
        backendClientPlayer.createPlayer("player" + MathUtils.random(1000, 2000), createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);

        // neuer Spieler hat noch keine
        WaitForResponseListener<List<MatchEntity>> callback = new WaitForResponseListener<>();
        backendClientPlayer.listPlayerMatches(0, callback);
        waitWhileRequesting();
        Assert.assertNotNull(callback.retrievedData);
        Assert.assertTrue(callback.retrievedData.isEmpty());

        //jetzt einfügen
        WaitForResponseListener<MatchEntity> addlistener = new WaitForResponseListener<>();
        backendClientPlayer.openNewMatch(null, 9, addlistener);
        waitWhileRequesting();
        Assert.assertTrue(addlistener.successful);

        callback = new WaitForResponseListener<>();
        backendClientPlayer.listPlayerMatches(0, callback);
        waitWhileRequesting();
        Assert.assertNotNull(callback.retrievedData);
        Assert.assertEquals(1, callback.retrievedData.size());

        // und einen abholen
        WaitForResponseListener<MatchEntity> matchCallback = new WaitForResponseListener<>();
        String matchid = callback.retrievedData.get(0).uuid;
        backendClientPlayer.fetchMatchWithTurns(matchid, matchCallback);
        waitWhileRequesting();
        Assert.assertNotNull(matchCallback.retrievedData);
        Assert.assertEquals(matchid, matchCallback.retrievedData.uuid);

        backendClientPlayer.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();

    }

    @Test
    public void testMatchTwoPlayers() throws InterruptedException {
        BackendClient backendClientPlayer1 = new BackendClient();
        BackendClient backendClientPlayer2 = new BackendClient();

        int random = MathUtils.random(1000, 2000);

        WaitForResponseListener<BackendClient.PlayerCreatedInfo> createdResponse
                = new WaitForResponseListener<BackendClient.PlayerCreatedInfo>();
        backendClientPlayer1.createPlayer("player1player1player1neu" + random, createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);
        backendClientPlayer2.createPlayer("player2player1player2neu" + random, createdResponse);
        waitWhileRequesting();
        Assert.assertNotNull(createdResponse.retrievedData);
        Thread.sleep(50);

        //jetzt einfügen
        String matchId = null;

        while (matchId == null) {
            WaitForResponseListener<MatchEntity> addlistener = new WaitForResponseListener<>();
            backendClientPlayer2.openNewMatch(null, 9, addlistener);
            waitWhileRequesting();
            Assert.assertTrue("Received HTTP " + addlistener.lastCode, addlistener.successful);
            Thread.sleep(50);

            if (addlistener.retrievedData.opponentId == null)
                matchId = addlistener.retrievedData.uuid;
        }

        WaitForResponseListener<MatchEntity> addlistener2 = new WaitForResponseListener<>();
        backendClientPlayer1.openNewMatch(null, 9, addlistener2);
        waitWhileRequesting();
        Assert.assertTrue(addlistener2.successful);
        Assert.assertNotNull(addlistener2.retrievedData.opponentId);
        Assert.assertEquals(backendClientPlayer2.getUserId(), addlistener2.retrievedData.opponentId);


        WaitForResponseListener<String> turnKeyListener = new WaitForResponseListener<>();
        backendClientPlayer1.postMatchStartPlayingTurn(matchId, turnKeyListener);
        waitWhileRequesting();
        Assert.assertNotNull(turnKeyListener.retrievedData);
        String turnKey = turnKeyListener.retrievedData;

        addlistener2 = new WaitForResponseListener<>();
        MatchTurnRequestInfo turnInfo = new MatchTurnRequestInfo();
        turnInfo.matchId = matchId;
        turnInfo.turnKey = turnKey;
        turnInfo.score1 = 123455;
        turnInfo.replay = "SOMEREPLAY";

        backendClientPlayer1.postMatchPlayedTurn(turnInfo, addlistener2);
        waitWhileRequesting();
        Assert.assertTrue(addlistener2.successful);

        backendClientPlayer1.deletePlayer(new WaitForResponseListener<Void>());
        waitWhileRequesting();
        backendClientPlayer2.deletePlayer(new WaitForResponseListener<Void>());
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
        int lastCode;

        WaitForResponseListener() {
            requesting = true;
        }

        @Override
        public void onFail(int statusCode, String errorMsg) {
            requesting = false;
            successful = false;
            lastCode = statusCode;
        }

        @Override
        public void onSuccess(T retrievedData) {
            this.retrievedData = retrievedData;
            successful = true;
            lastCode = 0;
            requesting = false;
        }
    }
}