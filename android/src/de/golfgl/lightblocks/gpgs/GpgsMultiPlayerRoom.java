package de.golfgl.lightblocks.gpgs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.golfgl.lightblocks.AndroidLauncher;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Created by Benjamin Schulte on 25.04.2017.
 */

public class GpgsMultiPlayerRoom extends AbstractMultiplayerRoom implements RoomUpdateListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener {
    private Activity context;
    private GpgsClient gpgsClient;
    private boolean roomCreationPending = false;
    private Room room;
    private Kryo kryo = new Kryo();

    // Participant ist anders als der Spielername... daher hier HashMaps
    private Map<String, String> connectionToPlayer = new HashMap<>(MAX_PLAYERS);
    private Map<String, String> playerToConnection = new HashMap<>(MAX_PLAYERS);
    private HashSet<String> allPlayers = new HashSet<>(MAX_PLAYERS);


    public void setGpgsClient(GpgsClient gpgsClient) {
        this.gpgsClient = gpgsClient;
        MultiPlayerObjects.register(kryo);
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    protected RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }


    @Override
    public boolean isOwner() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return room != null || roomCreationPending;
    }

    @Override
    public int getNumberOfPlayers() {
        return allPlayers.size();
    }

    @Override
    public String getRoomName() {
        return room.getRoomId();
    }

    @Override
    public void openRoom(Player player) throws VetoException {
        if (!gpgsClient.isConnected())
            throw new VetoException("Please sign in to Google Play Games before starting a game over this service.");

        if (isConnected())
            throw new VetoException("You are already in a room.");

        roomCreationPending = true;

        myPlayerId = player.getName();

        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(
                gpgsClient.getGoogleApiClient(), 1, MAX_PLAYERS - 1);
        context.startActivityForResult(intent, AndroidLauncher.RC_SELECT_PLAYERS);

    }

    public void selectPlayersResult(int resultCode, final Intent data) {
        Log.i("GPGS", "SelectPlayersResult: " + resultCode);

        // Go on when user did not cancel
        if (resultCode != Activity.RESULT_OK)
            roomCreationPending = false;

        else {
            // get the invitee list
            Bundle extras = data.getExtras();
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            Log.i("GPGS", "invitees: #" + invitees.size());

            // get auto-match criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            Log.i("GPGS", "AutoMatchPlayers: " + minAutoMatchPlayers + " to " + maxAutoMatchPlayers);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null)
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(gpgsClient.getGoogleApiClient(), roomConfig);
        }
    }

    @Override
    public void closeRoom(boolean force) throws VetoException {
        allPlayers.clear();
        connectionToPlayer.clear();
        playerToConnection.clear();
        if (room != null && gpgsClient.isConnected())
            Games.RealTimeMultiplayer.leave(gpgsClient.getGoogleApiClient(), this, room.getRoomId());
    }

    @Override
    public void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException {
        if (!(roomLoc instanceof GpgsRoomLocation))
            throw new IllegalArgumentException("GPGS room must be joined with GPGS invitation");

        roomCreationPending = true;
        RoomConfig roomConfig = makeBasicRoomConfigBuilder()
                .setInvitationIdToAccept(((GpgsRoomLocation) roomLoc).getInvitationId())
                .build();
        Games.RealTimeMultiplayer.join(gpgsClient.getGoogleApiClient(), roomConfig);

    }

    @Override
    public void sendToPlayer(String playerId, Object message) {

    }

    @Override
    public void sendToAllPlayers(Object message) {

    }

    @Override
    public void sendToAllPlayersExcept(String playerId, Object message) {

    }

    @Override
    public void sendToReferee(Object message) {

    }

    @Override
    public void leaveRoom(boolean force) throws VetoException {
        closeRoom(force);
    }

    @Override
    public void startRoomDiscovery() throws VetoException {
        if (!gpgsClient.isConnected())
            throw new VetoException("Please sign in to Google Play Games before starting a game over this service.");

        if (isConnected())
            throw new VetoException("You are already in a room.");

        roomCreationPending = true;

        // launch the intent to show the invitation inbox screen
        Intent intent = Games.Invitations.getInvitationInboxIntent(gpgsClient.getGoogleApiClient());
        context.startActivityForResult(intent, AndroidLauncher.RC_INVITATION_INBOX);

    }

    public void selectInvitationResult(int resultCode, Intent data) {
        Log.i("GPGS", "selectInvitationResult: " + resultCode);

        if (resultCode != Activity.RESULT_OK)
            roomCreationPending = false;

        else {
// get the selected invitation
            Bundle extras = data.getExtras();
            Invitation invitation =
                    extras.getParcelable(Multiplayer.EXTRA_INVITATION);

            GpgsRoomLocation roomLoc = new GpgsRoomLocation(invitation.getInvitationId(),
                    invitation.getInviter().getParticipantId());

            Player player = new Player();
            myPlayerId = gpgsClient.getPlayerDisplayName();
            player.setGamerId(myPlayerId);

            try {
                joinRoom(roomLoc, player);
            } catch (VetoException e) {
                // tritt nicht auf
            }
        }
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

    // RealtimeMessageReceivedListener

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        Log.i("GPGS", "onRealTimeMessageReceived");
        System.out.println(new String(realTimeMessage.getMessageData()));
    }

    // RoomStatusUpdateListener

    @Override
    public void onRoomConnecting(Room room) {
        Log.i("GPGS", "onRoomConnecting");

    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.i("GPGS", "onRoomAutoMatching");

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Log.i("GPGS", "onPeerInvitedToRoom");

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        Log.i("GPGS", "onPeerDeclined");

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Log.i("GPGS", "onPeerJoined");

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        // Called when one or more peer participant leave a room.
        Log.i("GPGS", "onPeerLeft");

    }

    @Override
    public void onConnectedToRoom(Room room) {
        // Called when the client is connected to the connected set in a room.
        // auch beim Server erst dann wenn Spieler eintreten
        // danach folgt onPeersConnected
        Log.i("GPGS", "onConnectedToRoom");
        synchronized (room) {
            this.room = room;
        }

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.i("GPGS", "onDisconnectedFromRoom");
        this.room = room;
        try {
            leaveRoom(true);
        } catch (VetoException e) {
            //eat
        }
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        Log.i("GPGS", "onPeersConnected");
        this.room = room;

        refreshPlayersList();

    }

    private void refreshPlayersList() {
        // first, add the new arrived guys
        for (Participant p : room.getParticipants()) {
            String pid = p.getParticipantId();
            System.out.println(pid + ": " + p.isConnectedToRoom());
            Games.RealTimeMultiplayer.sendReliableMessage(gpgsClient.getGoogleApiClient(), null, "Test".getBytes(),
                    room.getRoomId(), p.getParticipantId());

        }

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        // Called when one or more peer participants are disconnected from a room.
        Log.i("GPGS", "onPeersDisconnected");
        // nur setzen wenn nicht schon null ist (dann wurde bereits disconnected)
        this.room = room;
        refreshPlayersList();
    }

    @Override
    public void onP2PConnected(String s) {
        Log.i("GPGS", "onP2PConnected");

    }

    @Override
    public void onP2PDisconnected(String s) {
        Log.i("GPGS", "onP2PDisconnected");

    }

    // RoomUpdateListener

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        // bekommt nur der Einladende
        Log.i("GPGS", "onRoomCreated " + statusCode);
        synchronized (room) {
            this.room = room;
            roomCreationPending = false;
            addPlayer(myPlayerId, "");
            setRoomState(MultiPlayerObjects.RoomState.join);
        }

    }

    public void addPlayer(String playerId, String participantId) {

        if (!allPlayers.contains(playerId)) {
            allPlayers.add(myPlayerId);
            if (!participantId.isEmpty()) {
                //TODO
            }

            final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
            final MultiPlayerObjects.Player player = new MultiPlayerObjects.Player();
            player.name = myPlayerId;
            pc.changeType = MultiPlayerObjects.CHANGE_ADD;
            pc.changedPlayer = player;

            informRoomInhabitantsChanged(pc);
        }
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        // Called when the client attempts to join a real-time room.
        // (nur bei Engeladenen)
        Log.i("GPGS", "onJoinedRoom " + statusCode);
        synchronized (room) {
            this.room = room;
            addPlayer(myPlayerId, "");
            roomCreationPending = false;
            setRoomState(MultiPlayerObjects.RoomState.join);
        }
    }

    @Override
    public void onLeftRoom(int statusCode, String givenToLeave) {
        // Called when the client attempts to leaves the real-time room.
        Log.i("GPGS", "onLeftRoom " + statusCode);
        synchronized (room) {
            this.room = null;
            setRoomState(MultiPlayerObjects.RoomState.closed);
        }
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        // Called when ALL the participants in a real-time room are fully connected.
        Log.i("GPGS", "onRoomConnected " + statusCode);
        synchronized (room) {
            this.room = room;
            roomCreationPending = false;
        }
    }
}
