package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSErrorException;
import org.robovm.apple.gamekit.GKInvite;
import org.robovm.apple.gamekit.GKLocalPlayer;
import org.robovm.apple.gamekit.GKMatch;
import org.robovm.apple.gamekit.GKMatchDelegateAdapter;
import org.robovm.apple.gamekit.GKMatchRequest;
import org.robovm.apple.gamekit.GKMatchSendDataMode;
import org.robovm.apple.gamekit.GKMatchmaker;
import org.robovm.apple.gamekit.GKMatchmakerViewController;
import org.robovm.apple.gamekit.GKMatchmakerViewControllerDelegateAdapter;
import org.robovm.apple.gamekit.GKPlayer;
import org.robovm.apple.gamekit.GKPlayerConnectionState;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.block.VoidBlock2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

public class GcMultiplayerRoom extends AbstractMultiplayerRoom {

    public static final int MAX_PLAYERS = 2;
    private final GameCenterMultiplayerClient gameCenterClient;
    private final UIViewController gameViewController;
    private final Kryo kryo;
    private GKMatch runningMatch;
    private GKPlayer owner;
    private boolean iAmOwner;
    private HashSet<String> allPlayers = new HashSet<>(MAX_PLAYERS);
    private GKMatchmakerViewController matchmakerViewController;

    public GcMultiplayerRoom(GameCenterMultiplayerClient gameCenterClient, UIViewController viewController) {
        this.gameCenterClient = gameCenterClient;
        this.gameViewController = viewController;
        this.kryo = new Kryo();
        MultiplayerLightblocks.register(kryo);
    }

    @Override
    public boolean isOwner() {
        return owner != null && iAmOwner;
    }

    @Override
    public boolean isConnected() {
        return runningMatch != null;
    }

    @Override
    public int getNumberOfPlayers() {
        return allPlayers.size();
    }

    @Override
    public String getRoomName() {
        return GameCenterClient.GAMESERVICE_ID;
    }

    @Override
    public void openRoom(Player player) throws VetoException {
        if (!gameCenterClient.isSessionActive())
            throw new VetoException("Please sign in to Game Center before starting a game over this service.");

        if (isConnected() || runningMatch != null)
            throw new VetoException("You are already in a room.");

        GKMatchRequest request = new GKMatchRequest();
        request.setMaxPlayers(MAX_PLAYERS);
        request.setMinPlayers(2);
        request.setDefaultNumberOfPlayers(2);
        allPlayers.clear();

        matchmakerViewController = new GKMatchmakerViewController(request);
        matchmakerViewController.setMatchmakerDelegate(new GKMatchmakerViewControllerDelegateAdapter() {

            @Override
            public void didFindMatch(GKMatchmakerViewController viewController, GKMatch match) {
                matchWasOpened(match);
                dismissMatchmakerView(viewController);
            }

            @Override
            public void wasCancelled(GKMatchmakerViewController viewController) {
                dismissMatchmakerView(viewController);
            }

            @Override
            public void didFail(GKMatchmakerViewController viewController, NSError error) {
                if (viewController != null)
                    dismissMatchmakerView(viewController);

                if (error != null)
                    Gdx.app.error(GameCenterClient.GAMESERVICE_ID, "Match did fail: " + error.getCode());

                if (error != null && gameCenterClient.getGsListener() != null) {
                    gameCenterClient.getGsListener().gsShowErrorToUser(IGameServiceListener.GsErrorType.errorUnknown,
                            "Game Center match error: " + error.getCode(), null);
                }
            }
        });


        gameViewController.presentViewController(matchmakerViewController, true, null);
    }

    private void dismissMatchmakerView(GKMatchmakerViewController viewController) {
        if (viewController != null) {
            viewController.dismissViewController(true, null);
            matchmakerViewController = null;
        }
    }

    private void matchWasOpened(GKMatch match) {
        myPlayerId = gameCenterClient.getPlayerDisplayName();
        runningMatch = match;
        determineOwner();
        setRoomState(MultiPlayerObjects.RoomState.join);
        playersChanged(myPlayerId, true);
        for (GKPlayer player : match.getPlayers())
            playersChanged(player.getAlias(), true);
        match.setDelegate(new GKMatchDelegateAdapter() {
            @Override
            public void didChangeConnectionState(GKMatch match, GKPlayer player, GKPlayerConnectionState state) {
                playersChanged(player.getAlias(), state.equals(GKPlayerConnectionState.Connected));
                if (state.equals(GKPlayerConnectionState.Disconnected) && match.getPlayers().size() <= 1) {
                    closeRoom(false);
                }
            }

            @Override
            public void didReceiveData(GKMatch match, NSData data, GKPlayer player) {
                onMessageReceived(data.getBytes(), player.getAlias());
            }
        });
    }

    private void determineOwner() {
        // owner vorbegelen
        owner = GKLocalPlayer.getLocalPlayer();
        for (GKPlayer player : runningMatch.getPlayers())
            if (owner == null || owner.getAlias().compareTo(player.getAlias()) > 0)
                owner = player;

        if (owner != null)
            Gdx.app.debug(GameCenterClient.GAMESERVICE_ID, "Room owner is " + owner.getAlias());

        iAmOwner = owner != null && owner.getAlias().equals(myPlayerId);
    }

    private void onMessageReceived(byte[] messageData, String fromPlayerId) {
        try {
            Input input = new Input(messageData);
            Object object = kryo.readClassAndObject(input);

            if (!isOwner() && object instanceof MultiPlayerObjects.RoomStateChanged) {
                setRoomState(((MultiPlayerObjects.RoomStateChanged) object).roomState);
                // die anderen Felder referee und deputy sind ungenutzt
                return;
            }

            informGotRoomMessage(object);
        } catch (Throwable t) {
            Gdx.app.error(GameCenterClient.GAMESERVICE_ID, "Error deserializing message!", t);
        }
    }

    private void playersChanged(String playerId, boolean isConnected) {
        if (runningMatch == null)
            return;

        Gdx.app.debug(GameCenterClient.GAMESERVICE_ID, "Player change: " +
                playerId + " " + (isConnected ? "joined" : "left"));

        if (isConnected && !allPlayers.contains(playerId)) {
            allPlayers.add(playerId);

            final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
            final MultiPlayerObjects.Player player = new MultiPlayerObjects.Player();
            player.name = playerId;
            pc.changeType = MultiPlayerObjects.CHANGE_ADD;
            pc.changedPlayer = player;

            informRoomInhabitantsChanged(pc);
        } else if (!isConnected && allPlayers.contains(playerId)) {
            allPlayers.remove(playerId);

            if (owner.getAlias().equals(playerId))
                determineOwner();

            final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
            final MultiPlayerObjects.Player player = new MultiPlayerObjects.Player();
            player.name = playerId;
            pc.changeType = MultiPlayerObjects.CHANGE_REMOVE;
            pc.changedPlayer = player;

            informRoomInhabitantsChanged(pc);
        }
    }

    @Override
    public void closeRoom(boolean force) {
        runningMatch.disconnect();
        runningMatch.setDelegate(null);
        allPlayers.clear();
        setRoomState(MultiPlayerObjects.RoomState.closed);
        owner = null;
        runningMatch = null;
    }

    @Override
    public void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException {
        // join aus Invitation hat anderen
    }

    @Override
    public void sendToPlayer(String playerId, Object message) {
        if (playerId.equals(myPlayerId))
            Gdx.app.log(GameCenterClient.GAMESERVICE_ID, "Message to myself - ignored.");
        else {
            for (GKPlayer player : runningMatch.getPlayers()) {
                if (playerId.equals(player.getAlias())) {
                    sendReliableMessage(player, message);
                    break;
                }
            }
        }
    }

    @Override
    public void sendToAllPlayers(Object message) {
        sendToAllPlayersExcept(myPlayerId, message);
    }

    @Override
    public void sendToAllPlayersExcept(String playerId, Object message) {
        for (GKPlayer player : runningMatch.getPlayers())
            sendReliableMessage(player, message);
    }

    @Override
    public void sendToReferee(Object message) {
        if (owner == null)
            Gdx.app.error(GameCenterClient.GAMESERVICE_ID, "Message not sent to referee - not yet set." + message.getClass().getName());
        else if (iAmOwner)
            informGotRoomMessage(message);
        else
            sendReliableMessage(owner, message);
    }

    private void sendReliableMessage(GKPlayer player, Object message) {
        try {
            runningMatch.sendDataToPlayers(serializeObject(message), new NSArray<>(player), GKMatchSendDataMode.Reliable);
        } catch (NSErrorException e) {
            Gdx.app.error(GameCenterClient.GAMESERVICE_ID, "Error sending data", e);
        }
    }

    public NSData serializeObject(Object message) {
        Output output = new Output(128, 1025);
        kryo.writeClassAndObject(output, message);
        output.close();
        return new NSData(output.getBuffer());
    }

    @Override
    public void leaveRoom(boolean force) throws VetoException {
        closeRoom(force);
    }

    @Override
    public void startRoomDiscovery() throws VetoException {
        if (!gameCenterClient.isSessionActive())
            throw new VetoException("Please sign in to Game Center before starting a game over this service.");

        if (isConnected())
            throw new VetoException("You are already in a room.");

        // TODO
    }

    @Override
    public void stopRoomDiscovery() {

    }

    @Override
    public List<IRoomLocation> getDiscoveredRooms() {
        return null;
    }

    @Override
    public Set<String> getPlayers() {
        return allPlayers;
    }

    @Override
    public boolean isLocalGame() {
        return false;
    }

    @Override
    public String getRoomTypeId() {
        return "agc";
    }

    public void acceptInvitation(GKInvite invitation) {
        GKMatchmaker.getSharedMatchmaker().match(invitation, new VoidBlock2<GKMatch, NSError>() {
            @Override
            public void invoke(GKMatch gkMatch, NSError nsError) {
                if (gkMatch != null) {
                    matchWasOpened(gkMatch);
                    dismissMatchmakerView(matchmakerViewController);
                }
            }
        });
    }

}
