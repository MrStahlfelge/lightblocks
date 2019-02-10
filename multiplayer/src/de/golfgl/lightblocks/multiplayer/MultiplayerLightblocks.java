package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;

import java.util.ArrayList;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.menu.RtMultiplayerMenuScreen;

/**
 * Created by Benjamin Schulte on 16.03.2018.
 */

public class MultiplayerLightblocks extends LightBlocksGame {
    public NetUtils netUtils;

    // This registers objects that are going to be sent over the network.
    public static void register(Kryo kryo) {
        // *** HANDSHAKE MUSS DIE ERSTE REGISTRIERTE KLASSE SEIN!!! ***
        kryo.register(MultiPlayerObjects.Handshake.class);
        // und nun die weiteren Klassen registrieren =>
        kryo.register(MultiPlayerObjects.RoomState.class);
        kryo.register(MultiPlayerObjects.RoomStateChanged.class);
        kryo.register(MultiPlayerObjects.Player.class);
        kryo.register(MultiPlayerObjects.PlayerChanged.class);
        kryo.register(MultiPlayerObjects.GameParameters.class);
        kryo.register(ArrayList.class);
        kryo.register(MultiPlayerObjects.RelayToPlayer.class);
        kryo.register(MultiPlayerObjects.ChatMessage.class);
        kryo.register(MultiPlayerObjects.GeneralKryoMessage.class);
        kryo.register(MultiPlayerObjects.PlayerInMatch.class);
        kryo.register(boolean[].class);
        kryo.register(MultiPlayerObjects.PlayerInRoom.class);

        // GameModel
        kryo.register(MultiPlayerObjects.SwitchedPause.class);
        kryo.register(MultiPlayerObjects.PlayerIsOver.class);
        kryo.register(MultiPlayerObjects.GameIsOver.class);
        kryo.register(MultiPlayerObjects.BonusScore.class);
        kryo.register(MultiPlayerObjects.PlayerInGame.class);
        kryo.register(MultiPlayerObjects.InitGame.class);
        kryo.register(int[].class);
        kryo.register(MultiPlayerObjects.NextTetrosDrawn.class);
        kryo.register(MultiPlayerObjects.LinesRemoved.class);
        kryo.register(MultiPlayerObjects.GarbageForYou.class);

        // Watch play
        kryo.register(MultiPlayerObjects.WatchPlayInsertNewBlock.class);
        kryo.register(MultiPlayerObjects.WatchPlayMoveTetro.class);
        kryo.register(MultiPlayerObjects.WatchPlayRotateTetro.class);
        kryo.register(MultiPlayerObjects.WatchPlayClearAndInsertLines.class);
        kryo.register(MultiPlayerObjects.WatchPlayShowNextTetro.class);
        kryo.register(MultiPlayerObjects.WatchPlayActivateNextTetro.class);
        kryo.register(MultiPlayerObjects.WatchPlayPinTetromino.class);
        kryo.register(MultiPlayerObjects.WatchPlayMarkConflict.class);
    }

    @Override
    public void create() {
        if (!GAME_DEVMODE)
            Log.set(Log.LEVEL_WARN);

        super.create();
    }

    @Override
    public MultiplayerMenuScreen getNewMultiplayerMenu(Group actorToHide) {
        if (netUtils == null)
            netUtils = new NetUtils();

        return new RtMultiplayerMenuScreen(this, actorToHide);
    }

    @Override
    public void gsOnSessionActive() {
        super.gsOnSessionActive();

        // bei GPGS angemeldet => dann prüfen ob eventuell eine Multiplayer-Einladung angenommen wurde
        if (hasPendingInvitation())
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    switchToMultiplayerScreen();
                }
            });
    }

    /**
     * @return true wenn eine Einladung via GPGS akzeptiert wurde und noch keine anderen Verbindungen offen sind
     */
    public boolean hasPendingInvitation() {
        return gpgsClient != null && (gpgsClient instanceof IMultiplayerGsClient)
                && ((IMultiplayerGsClient) gpgsClient).hasPendingInvitation();
    }

    private void switchToMultiplayerScreen() {
        // Wechselt zum Multiplayerscreen, wenn wir uns auf dem Hauptlevel des Menüs befinden (oder auf dem Weg sind)

        if (getScreen() == mainMenuScreen && mainMenuScreen.isOnMainLevel()) {

            if (mainMenuScreen.isOnMainLevelAndWaitingForUserInput())
                // Multiplayerscreen öffnen, den Rest macht dieser dann selbst
                mainMenuScreen.showMultiplayerScreen();
            else
                // die Anfangsanimation spielt wahrscheinlich noch ab => abwarten
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        switchToMultiplayerScreen();
                    }
                }, .5f);

        }
    }

    @Override
    public boolean supportsRealTimeMultiplayer() {
        return true;
    }
}