package de.golfgl.lightblocks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.MockGameServiceClient;
import de.golfgl.gdxgamesvcs.achievement.IAchievement;
import de.golfgl.gdxgamesvcs.gamestate.ISaveGameStateResponseListener;
import de.golfgl.gdxgamesvcs.leaderboard.ILeaderBoardEntry;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 540; //LightBlocksGame.nativeGameWidth;
        config.height = 960; //LightBlocksGame.nativeGameHeight;
        LightBlocksGame game = new LightBlocksGame();
        game.gpgsClient = new MyTestClient();
        game.gpgsClient.setListener(game);
        new LwjglApplication(game, config);
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
            return IGameServiceClient.GS_AMAZONGC_ID;
        }
    }
}
