package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;

import org.robovm.apple.gamekit.GKInvite;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.gamekit.GKLocalPlayerListenerAdapter;
import org.robovm.apple.gamekit.GKPlayer;
import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.lightblocks.MyGameCenterClient;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;

public class GameCenterMultiplayerClient extends MyGameCenterClient implements IMultiplayerGsClient, IGameServiceListener {

    private final UIViewController viewController;
    private GcMultiplayerRoom gcMultiplayerRoom;
    private GKInvite invitation;
    private IGameServiceListener gsListener;
    private boolean listenerInstalled;

    public GameCenterMultiplayerClient(UIViewController viewController) {
        super(viewController);
        this.viewController = viewController;

        // diese Klasse selbst als Listener registrieren, damit der GKLocalPlayerListener nach
        // erster Authentifizierung installiert werden kann.
        setListener(this);
    }

    private void registerListener() {
        if (listenerInstalled)
            return;

        GKLocalPlayer.getLocalPlayer().registerListener(new GKLocalPlayerListenerAdapter() {

            @Override
            public void didAcceptInvite(GKPlayer player, GKInvite invite) {
                Gdx.app.debug(GAMESERVICE_ID, "Received invitation from player " + player.getDisplayName());
                invitation = invite;
                // nochmal listener aufrufen um die Invitation-Prüfung auszulösen
                if (gsListener != null)
                    gsListener.gsOnSessionActive();
            }
        });
        listenerInstalled = true;
    }

    @Override
    public AbstractMultiplayerRoom createMultiPlayerRoom() {
        if (gcMultiplayerRoom == null)
            gcMultiplayerRoom = new GcMultiplayerRoom(this, viewController);

        return gcMultiplayerRoom;
    }

    @Override
    public boolean hasPendingInvitation() {
        return invitation != null;
    }

    @Override
    public void acceptPendingInvitation() {
        if (invitation != null) {
            createMultiPlayerRoom();
            gcMultiplayerRoom.acceptInvitation(invitation);
            invitation = null;
        }
    }

    @Override
    public void setListener(IGameServiceListener gsListener) {
        this.gsListener = gsListener;
    }

    protected IGameServiceListener getGsListener() {
        return gsListener;
    }

    @Override
    public void gsOnSessionActive() {
        registerListener();
        if (gsListener != null)
            gsListener.gsOnSessionActive();
    }

    @Override
    public void gsOnSessionInactive() {
        if (gsListener != null)
            gsListener.gsOnSessionInactive();
    }

    @Override
    public void gsShowErrorToUser(GsErrorType et, String msg, Throwable t) {
        if (gsListener != null)
            gsListener.gsShowErrorToUser(et, msg, t);
    }
}
