package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

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
        WaitForResponseListener<BackendWelcomeResponse> newMessagesResponse = new
                WaitForResponseListener<BackendWelcomeResponse>();
        backendClientNoAuth.fetchWelcomeMessages(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1023, 1,
                0, null, null, newMessagesResponse);
        waitWhileRequesting();

        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertFalse(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // veraltete Clientversion
        newMessagesResponse = new WaitForResponseListener<BackendWelcomeResponse>();
        backendClientNoAuth.fetchWelcomeMessages(1, "smarttv", "webgl", 1023, 1,
                0, "FCM", "token", newMessagesResponse);
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

        newMessagesResponse = new WaitForResponseListener<BackendWelcomeResponse>();
        backendClientPlayer.fetchWelcomeMessages(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1024, 1,
                0, null, null, newMessagesResponse);
        waitWhileRequesting();
        Assert.assertNotNull(newMessagesResponse.retrievedData);
        Assert.assertTrue(newMessagesResponse.retrievedData.authenticated);
        Assert.assertTrue(newMessagesResponse.retrievedData.responseTime > 0);
        Assert.assertNull(newMessagesResponse.retrievedData.warningMsg);

        // Anfrage mit falscher Authentifizierung
        newMessagesResponse = new WaitForResponseListener<BackendWelcomeResponse>();
        BackendClient noAuthPlayer = new BackendClient();
        noAuthPlayer.setUserId("----");
        noAuthPlayer.fetchWelcomeMessages(LightBlocksGame.GAME_VERSIONNUMBER, "smarttv", "webgl", 1024, 1,
                0, "FCM", "token", newMessagesResponse);
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
        WaitForResponseListener<Array<MatchEntity>> callback = new WaitForResponseListener<>();
        backendClientPlayer.listPlayerMatches(0, callback);
        waitWhileRequesting();
        Assert.assertNotNull(callback.retrievedData);
        Assert.assertTrue(callback.retrievedData.size == 0);

        //jetzt einfügen
        WaitForResponseListener<MatchEntity> addlistener = new WaitForResponseListener<>();
        backendClientPlayer.openNewMatch(null, 9, addlistener);
        waitWhileRequesting();
        Assert.assertTrue(addlistener.successful);

        callback = new WaitForResponseListener<>();
        backendClientPlayer.listPlayerMatches(0, callback);
        waitWhileRequesting();
        Assert.assertNotNull(callback.retrievedData);
        Assert.assertEquals(1, callback.retrievedData.size);

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
        turnInfo.replay = "1#N0:b8b9c1c2:#G330:1#H1e4:-1#Rc7:adb7b8c2#G65:1#H42:-1#H32:-1#H32:-1#G282:1#G28a:1#G21:1#G21:1#G22:1#G21:1#G22:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#D22:000a0b15:f#N0:b9c1c2c3:E         EE         E#R32:b7b8c2cc#H96:-1#Gfb:1#G21:1#G21:1#G22:1#G21:1#G21:1#G21:1#G22:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G21:1#G22:1#G192:1#H0:-1#D31d:01020c16:20#N0:b8c1c2c3:EDD       EED        ED#R227:b8c1c2cc#R86:c1c2c3cc#G74:1#G85:1#G22:1#G20:1#G23:1#G23:1#G1f:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G22:1#G21:1#G22:1#G21:1#D27:0304050e:32#N0:b7c1c2c3:EDDBBB    EED B      ED#Re8:b8c2cbcc#H33:1#R86:c2c3c4ce#H11:1#H63:1#Ha7:1#G75:1#Hd8:-1#Gdb:1#G1f:1#G21:1#G21:1#G23:1#G21:1#G21:1#G27:1#G1c:1#G21:1#G21:1#G22:1#G21:1#G21:1#G27:1#G1e:1#G20:1#G21:1#D20:06070812:44#N0:b7b8c2c3:EDDBBBCCC EED B   C  ED#H54:1#Ha7:1#H85:1#Gd9:1#G22:1#G26:1#G1c:1#G22:1#G21:1#G21:1#G21:1#G21:1#G21:1#G22:1#G22:1#G21:1#G22:1#G21:1#G22:1#G26:1#D1c:10111b1c:55#N0:c1c2c3c4:EDDBBBCCC EED B FFC  ED    FF#R85:b9c3cdd7#H21:-1#H15e:-1#H33:-1#H32:-1#H32:-1#G86:1#G2fe:1#G21:1#G21:1#G24:1#G1f:1#G21:1#G21:1#G23:1#G21:1#G21:1#G18:1#G2b:1#G21:1#G22:1#G20:1#D24:141e2832:64#N0:b8b9c2c3:EDDBBBCCC EED B FFC AED    FF A         A         A#H54:-1#H1b1:-1#Ha7:-1#G84:1#G74:1#G22:1#G22:1#G21:1#G26:1#G1d:1#G21:1#G21:1#G22:1#G21:1#G22:1#G27:1#G1b:1#G21:1#D21:1f20292a:72#N0:b8c1c2c3:EDDBBBCCC EED B FFC AED    FF AGG       AGG       A#R97:b8c2c3cc#H63:-1#G12c:1#G21:1#G22:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G22:1#D20:0d171821:83#N0:b8b9c1c2:EDDBBBCCC EEDBB FFC AEDBB  FF AGGB      AGG       A#R0:b8c2c3cd#H43:1#G111:1#G1b:1#G22:1#G22:1#G20:1#G21:1#G22:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G26:1#G1c:1#G22:1#D21:0f191a24:94#N0:b7b8c2c3:EDDBBBCCC EEDBBEFFC AEDBBEEFF AGGB  E   AGG       A#Ha7:1#Ge9:1#G22:1#G21:1#G21:1#G21:1#G21:1#G22:1#G21:1#G25:1#G1e:1#G21:1#G21:1#G22:1#G22:1#G22:1#D29:22232d2e:a3#N0:b9c1c2c3:EDDBBBCCC EEDBBEFFC AEDBBEEFF AGGBFFE   AGG  FF   A#R12:b7b8c2cc#H37:1#Hb1:1#H75:1#H86:1#H95:1#Ga7:1#H64:-1#Gc8:1#G21:1#G21:1#G23:1#G20:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#D21:2526303a:b1#N0:b8b9c2c3:EDDBBBCCC EEDBBEFFC AEDBBEEFF AGGBFFEDD AGG  FF D A       D#H0:-1#G14d:1#G21:1#G23:1#G20:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G19:1#G29:1#G22:1#G21:1#D21:2b2c3536:bf#N0:b7c1c2c3:EDDBBBCCC EEDBBEFFC AEDBBEEFF AGGBFFEDD AGGGGFF D A  GG   D#H11:-1#R78:b7b8c1cb#Hc5:-1#H74:-1#G14f:1#G20:1#G17:1#G2c:1#G21:1#G22:1#G22:1#G21:1#G20:1#G17:1#G2d:1#G20:1#G21:1#D22:33343d47:cc#N0:c1c2c3c4:EDDBBBCCC EEDBBEFFC AEDBBEEFF AGGBFFEDD AGGGGFF D ACCGG   D  C         C#R0:b9c3cdd7#H0:1#H16f:1#H22:1#H32:1#Gb9:1#G1f:1#G22:1#G21:1#G21:1#G21:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G25:1#G1e:1#G22:1#G23:1#G20:1#D22:09131d27:58e#N0:b8b9c1c2:AGGGGFF D ACCGG   D  C         C#H96:1#G180:1#G21:1#G21:1#G21:1#G22:1#G26:1#G1c:1#G22:1#G21:1#G21:1#G22:1#G21:1#G27:1#G1c:1#G20:1#G23:1#G21:1#D21:0f101819:59f#N0:b8c1c2c3:AGGGGFF D ACCGGEE D  C  EE     C#Reb:b8c1c2cc#H10:1#Hb7:1#Geb:1#G20:1#G21:1#G22:1#G21:1#G29:1#G19:1#G22:1#G21:1#G22:1#G21:1#G21:1#G21:1#G21:1#G22:1#G22:1#D21:1a23242e:5af#N0:b7c1c2c3:AGGGGFF D ACCGGEE D  C  EEB    C   BB         B#Hd8:-1#R33:b7b8c1cb#Hb7:-1#Gfa:1#G22:1#G20:1#G22:1#G22:1#G21:1#G16:1#G2c:1#G22:1#G21:1#G22:1#G21:1#G21:1#G17:1#G2c:1#G22:1#D21:1617202a:5bf#N0:b8b9c2c3:AGGGGFF D ACCGGEE D  CCCEEB    CC  BB     C   B#H43:-1#G11b:1#G22:1#G21:1#G18:1#G2b:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#D21:21222b2c:5ce#N0:c1c2c3c4:AGGGGFF D ACCGGEE D  CCCEEB    CCGGBB     CGG B#R32:b9c3cdd7#H33:-1#H16d:-1#H33:-1#H31:-1#H33:-1#Gba:1#G50:1#G21:1#G22:1#G21:1#G21:1#G25:1#G1e:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G22:1#G21:1#D21:141e2832:5dd#N0:b7b8c2c3:AGGGGFF D ACCGGEE D ACCCEEB   ACCGGBB   A CGG B   A#R22:b9c2c3cc#Ge9:1#G21:1#G21:1#G22:1#G21:1#G27:1#G1c:1#G21:1#G21:1#G22:1#G21:1#G22:1#G29:1#G18:1#D22:2d363740:5eb#N0:b9c1c2c3:AGGGGFF D ACCGGEE D ACCCEEB   ACCGGBB   A CGGFB   A   FF        F#R0:b8c2cccd#H31:1#Hb9:1#H93:1#Ga7:1#G21:1#G22:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G22:1#G21:1#G21:1#D28:07111b1c:5fd#N0:b8b9c1c2:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBB   A CGGFB   A   FF        F#H23e:1#H8e:1#G54:1#G96:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G24:1#G1e:1#G22:1#D21:38394142:609#N0:b7c1c2c3:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBB   A CGGFB   A   FFEE      FEE#H11:-1#Hd8:-1#Gbb:1#G2f:1#G21:1#G21:1#G22:1#G21:1#G21:1#G14:1#G2f:1#G21:1#G22:1#G10:1#G32:1#G22:1#D14:29333435:617#N0:b8b9c2c3:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBB   ACCGGFB   ACCCFFEE      FEE#H39:1#H157:1#H32:1#H33:1#Gb7:1#G21:1#G28:1#G1b:1#G21:1#G21:1#G22:1#G21:1#G21:1#G17:1#G2c:1#G22:1#G20:1#G22:1#G226:1#H0:-1#D12c:25262f30:625#N0:b7b8c2c3:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBBGG ACCGGFBGG ACCCFFEE      FEE#H1e:1#R86:bac3c4cd#H63:1#H96:1#Gc9:1#G21:1#G21:1#G22:1#G21:1#G21:1#G21:1#G22:1#G22:1#G21:1#G21:1#G21:1#G21:1#D21:3a43444d:632#N0:b8c1c2c3:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBBGG ACCGGFBGG ACCCFFEEF     FEEFF        F#R22:b8c1c2cc#H32:-1#Re9:c0c1c2cb#H86:-1#H32:-1#G10b:1#G20:1#G21:1#G14:1#G2f:1#G21:1#G22:1#G21:1#G21:1#G22:1#G24:1#G1f:1#G21:1#D21:3c3d3e47:63f#N0:b9c1c2c3:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBBGG ACCGGFBGG ACCCFFEEF BBB FEEFF  B     F#R11:b7b8c2cc#R85:c1c2c3cb#H0:1#Ha7:1#Gc9:1#G21:1#G21:1#G22:1#G21:1#G20:1#H76:-1#Gb8:1#G21:1#G21:1#G21:1#G17:1#G2b:1#D22:4a4b4c54:64b#N0:c1c2c3c4:AGGGGFFDD ACCGGEEDD ACCCEEBDD ACCGGBBGG ACCGGFBGG ACCCFFEEF BBB FEEFF  B  DDDF      D#R0:b9c3cdd7#H11:1#H15d:1#H32:1#H32:1#Gca:1#G20:1#G21:1#G22:1#G20:1#G22:1#G22:1#G17:1#G2a:1#G22:1#G21:1#G22:1#G22:1#G21:1#G27:1#G1c:1#G21:1#G21:1#D22:09131d27:d65#N0:b7c1c2c3:ACCGGFBGG ACCCFFEEF BBB FEEFF  B  DDDF      D#Rfd:b8b9c2cc#H82:1#Gfb:1#G21:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#D21:2d2e3741:d73#N0:b8b9c1c2:ACCGGFBGG ACCCFFEEF BBB FEEFF  B  DDDF      DCC        C         C#R0:b8c2c3cd#H33:-1#H16f:-1#H31:-1#H32:-1#G96:1#G21:1#G21:1#G22:1#G22:1#G21:1#G27:1#G1c:1#G21:1#G21:1#G21:1#G21:1#G22:1#G21:1#G21:1#D22:1e282933:d82#N0:b7b8c2c3:ACCGGFBGG ACCCFFEEF BBB FEEFF EB  DDDF  EE  DCC    E   C         C#R21:b9c2c3cc#H31:-1#Hb7:-1#Gea:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G21:1#G21:1#G22:1#G25:1#G1d:1#G22:1#G21:1#D21:1720212a:d92#N0:b8c1c2c3:ACCGGFBGG ACCCFFEEF BBBFFEEFF EBFFDDDF  EEF DCC    E   C         C#H75:-1#Gea:1#G21:1#G24:1#G1e:1#G22:1#G21:1#G22:1#G21:1#G21:1#G22:1#G16:1#G2c:1#G22:1#G21:1#D21:2b343536:da0#N0:b8b9c2c3:ACCGGFBGG ACCCFFEEF BBBFFEEFF EBFFDDDF  EEFBDCC    EBBBC         C#H53:-1#Gea:1#G21:1#G21:1#G22:1#G21:1#G21:1#G23:1#G20:1#G22:1#G27:1#G1c:1#G21:1#D21:3f40494a:dac#N0:c1c2c3c4:ACCGGFBGG ACCCFFEEF BBBFFEEFF EBFFDDDF  EEFBDCC    EBBBC       GGC       GG#R0:b9c3cdd7#H11:1#Hc7:1#H180:1#Gd8:1#G21:1#G23:1#G21:1#G21:1#G22:1#G21:1#G21:1#G28:1#G1b:1#G21:1#G22:1#G21:1#G22:1#G21:1#D20:26303a44:dba#N0:b9c1c2c3:ACCGGFBGG ACCCFFEEF BBBFFEEFF EBFFDDDFA EEFBDCC A  EBBBC  A    GGC  A    GG#R23:b7b8c2cc#H32:1#Hb5:1#H97:1#Ga7:1#G21:1#G21:1#G21:1#G22:1#G21:1#";
        turnInfo.platform = "UNITTEST";
        turnInfo.inputType = "keyboard";
        turnInfo.drawyer = "14564564156189741023104678940320131";

        backendClientPlayer1.postMatchPlayedTurn(turnInfo, addlistener2);
        waitWhileRequesting();
        Assert.assertTrue(addlistener2.successful);
        Assert.assertEquals(1, addlistener2.retrievedData.turns.size());

        // direkt danach kann man nicht mehr aufgeben, man ist nicht dran
        addlistener2 = new WaitForResponseListener<>();
        backendClientPlayer1.postMatchGiveUp(matchId, addlistener2);
        waitWhileRequesting();
        Assert.assertFalse(addlistener2.successful);

        // der andere kann aber aufgeben
        addlistener2 = new WaitForResponseListener<>();
        backendClientPlayer2.postMatchGiveUp(matchId, addlistener2);
        waitWhileRequesting();
        Assert.assertTrue(addlistener2.successful);
        Assert.assertTrue(addlistener2.retrievedData.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_GAVEUP));
        Assert.assertEquals(1, addlistener2.retrievedData.turns.size());

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