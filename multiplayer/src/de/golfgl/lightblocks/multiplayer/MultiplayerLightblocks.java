package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import com.github.czyzby.websocket.CommonWebSockets;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.menu.RtMultiplayerMenuScreen;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.state.Theme;

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

        CommonWebSockets.initiate();

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

    @Override
    public boolean canInstallTheme() {
        return Gdx.files.isLocalStorageAvailable();
    }

    @Override
    public void doInstallTheme(InputStream zipFile) {
        if (!canInstallTheme())
            return;

        if (zipFile == null)
            chooseZipFile();
        else
            zipFileChosen(zipFile);
    }

    public void zipFileChosen(InputStream zipFile) {
        try {
            final int BUFFER = 2048;
            ZipInputStream zis = new ZipInputStream(zipFile);

            ZipEntry entry;
            boolean foundJson = false;

            // theme-Verzeichnis anlegen, wenn nicht da
            Gdx.files.local(Theme.FOLDER_NAME).mkdirs();
            theme.resetTheme();

            BufferedOutputStream dest = null;
            while ((entry = zis.getNextEntry()) != null) {
                FileHandle file = Gdx.files.local(Theme.FOLDER_NAME + "/" + entry.getName());
                if (entry.getName().equals(Theme.THEME_FILE_NAME))
                    foundJson = true;

                if (entry.isDirectory()) {
                    if (!file.exists())
                        file.mkdirs();
                    continue;
                }

                if (!theme.isThemeFile(entry.getName())) {
                    Gdx.app.log(Theme.LOG_TAG, "Skipping theme archive file " + entry.getName());
                    continue;
                }

                int count;
                byte data[] = new byte[BUFFER];
                OutputStream fos = file.write(false);

                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();

            if (!foundJson) {
                theme.resetTheme();
                throw new RuntimeException("This is not a Lightblocks theme archive file.");
            } else {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        theme.loadThemeIfPresent();
                        String errorMsg = theme.getLastLoadThemeErrorMessage();
                        if (!theme.isThemePresent())
                            ((AbstractScreen) getScreen()).showDialog("Error applying theme.\n"
                                    + (errorMsg != null ? errorMsg : ""));
                    }
                });
            }

        } catch (final Throwable t) {
            Gdx.app.error(Theme.LOG_TAG, t.getMessage(), t);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    ((AbstractScreen) getScreen()).showDialog("Error installing theme:\n" + t.getMessage());
                }
            });
        }
    }

    protected void chooseZipFile() {
    }
}