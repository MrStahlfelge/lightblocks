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

public class GameCenterMultiplayerClient extends MyGameCenterClient implements IMultiplayerGsClient {

    private final UIViewController viewController;
    private GcMultiplayerRoom gcMultiplayerRoom;
    private GKInvite invitation;
    private IGameServiceListener gsListener;
    private boolean listenerInstalled;

    public GameCenterMultiplayerClient(UIViewController viewController) {
        super(viewController);
        this.viewController = viewController;
    }

    @Override
    public boolean isSessionActive() {
        // kleiner Hack: isSessionActive wird von Superklasse aufgerufen, wenn authenticate-Handler
        // zurückkommt. Also kann man diese Stelle nutzen, um möglichst früh den LocalPlayerListener
        // einzuhängen
        boolean isAuthenticated = super.isSessionActive();
        if (isAuthenticated)
            registerListener();
        return isAuthenticated;
    }

    private void registerListener() {
        if (listenerInstalled)
            return;

        GKLocalPlayer.getLocalPlayer().unregisterAllListeners();
        GKLocalPlayer.getLocalPlayer().registerListener(new GKLocalPlayerListenerAdapter() {

            @Override
            public void didAcceptInvite(GKPlayer player, GKInvite invite) {
                Gdx.app.debug(GAMESERVICE_ID, "Received invitation from player " + player.getDisplayName());
                invitation = invite;
                invite.retain();
                // nochmal listener aufrufen um die Invitation-Prüfung auszulösen
                if (gsListener != null)
                    gsListener.gsOnSessionActive();
            }
        });
        listenerInstalled = true;
    }

    @Override
    public AbstractMultiplayerRoom createMultiPlayerRoom() {
        if (gcMultiplayerRoom != null && gcMultiplayerRoom.isConnected())
            throw new IllegalStateException("GameCenter room open but new one should be created");

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
            gcMultiplayerRoom.acceptInvitation(invitation);
            invitation.release();
            invitation = null;
        }
    }

    @Override
    public void setListener(IGameServiceListener gsListener) {
        this.gsListener = gsListener;
        super.setListener(gsListener);
    }

    protected IGameServiceListener getGsListener() {
        return gsListener;
    }
}
