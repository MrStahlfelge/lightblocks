package de.golfgl.lightblocks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.MockGameServiceClient;
import de.golfgl.gdxgamesvcs.achievement.IAchievement;
import de.golfgl.gdxgamesvcs.leaderboard.ILeaderBoardEntry;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = LightBlocksGame.nativeGameWidth;
        config.height = LightBlocksGame.nativeGameHeight;
        LightBlocksGame game = new MultiplayerLightblocks();
        game.gpgsClient = new MyTestClient();
        game.purchaseManager = new MyTestPurchaseManager();
        new LwjglApplication(game, config);
    }

    private static class MyTestPurchaseManager implements PurchaseManager {
        PurchaseObserver observer;

        @Override
        public String storeName() {
            return "TEST";
        }

        @Override
        public void install(PurchaseObserver observer, PurchaseManagerConfig config, boolean autoFetchInformation) {
            this.observer = observer;
            observer.handleInstall();
        }

        @Override
        public boolean installed() {
            return true;
        }

        @Override
        public void dispose() {

        }

        @Override
        public void purchase(String identifier) {
            Transaction transaction = new Transaction();
            transaction.setIdentifier(identifier);
            observer.handlePurchase(transaction);
        }

        @Override
        public void purchaseRestore() {

        }

        @Override
        public Information getInformation(String identifier) {
            return null;
        }
    }

    private static class MyTestClient extends MockGameServiceClient implements IMultiplayerGsClient {

        public MyTestClient() {
            super(.5f);
        }

        @Override
        protected String getPlayerName() {
            return "12345678901234567890";
        }

        @Override
        public AbstractMultiplayerRoom createMultiPlayerRoom() {
            return null;
        }

        @Override
        protected Array<ILeaderBoardEntry> getLeaderboardEntries() {
            return null;
        }

        @Override
        protected Array<String> getGameStates() {
            return null;
        }

        @Override
        protected byte[] getGameState() {
            return new byte[0];
        }

        @Override
        protected Array<IAchievement> getAchievements() {
            return null;
        }

        @Override
        public String getGameServiceId() {
            return IGameServiceClient.GS_GOOGLEPLAYGAMES_ID;
        }

        @Override
        public boolean hasPendingInvitation() {
            return false;
        }

        @Override
        public void acceptPendingInvitation() {

        }
    }
}
