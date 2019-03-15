package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;

import org.robovm.apple.gamekit.GKInvite;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.gamekit.GKLocalPlayerListenerAdapter;
import org.robovm.apple.gamekit.GKPlayer;
import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;

public class GameCenterMultiplayerClient extends GameCenterClient implements IMultiplayerGsClient {

    private final UIViewController viewController;
    private GcMultiplayerRoom gcMultiplayerRoom;
    private GKInvite invitation;

    public GameCenterMultiplayerClient(UIViewController viewController) {
        super(viewController);
        this.viewController = viewController;
        GKLocalPlayer.getLocalPlayer().registerListener(new GKLocalPlayerListenerAdapter() {

            @Override
            public void didAcceptInvite(GKPlayer player, GKInvite invite) {
                // TODO eventuell muss hier nochmal listener.gsOnActive aufgerufen werden um die Invitation.Prüfung auszulösen
                invitation = invite;
            }
        });
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
        // TODO
        Gdx.app.error(GAMESERVICE_ID, "acceptPendingInvitation not implemented");
    }
}
