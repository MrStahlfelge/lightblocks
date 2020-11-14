package de.golfgl.lightblocks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

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
        LightBlocksGame game = new MultiplayerLightblocks() {
                @Override
                protected void chooseZipFile() {
                // Create Swing JFileChooser
                JFileChooser fileChooser = new JFileChooser();

                String title = "such mal aus";
                if (title != null)
                    fileChooser.setDialogTitle(title);

                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isFile() || file.getName().endsWith(".zip");
                    }

                    @Override
                    public String getDescription() {
                        return "FileFilter";
                    }
                });
                fileChooser.setAcceptAllFileFilterUsed(false);

                // Present it to the world
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    FileHandle result = new FileHandle(file);
                    zipFileChosen(result.read());
                }
            }

        };
        game.gpgsClient = new MyTestClient();
        game.purchaseManager = new MyTestPurchaseManager();
        game.nsdHelper = new DesktopNsdHelper();
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
            return IGameServiceClient.GS_GAMECENTER_ID;
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
