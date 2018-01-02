package de.golfgl.lightblocks.gpgs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
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
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Created by Benjamin Schulte on 25.04.2017.
 */

public class GpgsMultiPlayerRoom extends AbstractMultiplayerRoom implements RoomUpdateListener,
        RealTimeMessageReceivedListener, RoomStatusUpdateListener, RealTimeMultiplayer.ReliableMessageSentCallback {

    // ACHTUNG: Wenn das hier aktiviert wird, dann im MenuScreen das Achievement Friendly Multiplayer
    // nur nach Prüfung vergeben ob das aktuelle Spiel KEIN Automatching war
    private static final boolean AUTOMATCHING_ENABLED = LightBlocksGame.GAME_DEVMODE;

    // über GPGS nur zwei Spieler zulassen
    // dies ist vor allem so, weil ansonsten das Spiel nicht vorzeitig schon mit zwei Teilnehmern gestartet werden
    // kann. Ein nachträglich dazukommender Eingeladener kann nicht abgelehnt werden und tritt in den Raum ein.
    // Damit schon zwei Spieler vorher starten könnten, müsste ein Beobachtungsmodus implementiert werden.
    // Zweiter Grund: Ein Host/Owner ist für die Spiellogik nötig. Ab drei Spielern müsste dafür ein übergabemechanismus
    // implementiert werden.
    // Beides zusammen lohnt zunächst mal den Aufwand nicht, zu Dritt spielen ist auch nicht sooo toll gewesen.
    // Weiterhin drauf achten: AutoMatch und Invite darf auch zukünftig nicht gemischt werden, da Host bestimmen
    // dann bei den Eingeladenen schief laufen kann (in onJoinedRoom wird creatorId als Owner gesetzt)
    private static final int MAX_PLAYERS_GPGS = 2;
    private final Object lockObj = new Object();
    private Activity context;
    private GpgsMultiPlayerClient gpgsClient;
    private boolean roomCreationPending = false;
    private Room room;
    private Kryo kryo = new Kryo();
    private boolean roomWithAutoMatch;
    private String ownerParticipantId;
    private boolean iAmTheOwner;
    private boolean allPlayersConnected;

    // Participant ist anders als der Spielername... daher hier HashMaps
    private Map<String, String> connectionToPlayer = new HashMap<>(MAX_PLAYERS_GPGS);
    private Map<String, String> playerToConnection = new HashMap<>(MAX_PLAYERS_GPGS);
    private HashSet<String> allPlayers = new HashSet<>(MAX_PLAYERS_GPGS);


    public void setGpgsClient(GpgsMultiPlayerClient gpgsClient) {
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
        return iAmTheOwner;
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
                gpgsClient.getGoogleApiClient(), 1, MAX_PLAYERS_GPGS - 1, AUTOMATCHING_ENABLED);
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
                roomWithAutoMatch = true;
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                roomWithAutoMatch = false;
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            roomConfigBuilder.setVariant(MultiPlayerObjects.INTERFACE_VERSION * 10);
            if (autoMatchCriteria != null)
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(gpgsClient.getGoogleApiClient(), roomConfig);
            informEstablishingConnection();
        }
    }

    @Override
    public void closeRoom(boolean force) throws VetoException {
        if (roomCreationPending)
            throw new VetoException("GPGS multiplayer room creation pending. Please some patience.");

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
        roomWithAutoMatch = false;
        RoomConfig roomConfig = makeBasicRoomConfigBuilder()
                .setInvitationIdToAccept(((GpgsRoomLocation) roomLoc).getInvitationId())
                .build();
        Games.RealTimeMultiplayer.join(gpgsClient.getGoogleApiClient(), roomConfig);
        informEstablishingConnection();
    }

    @Override
    public void sendToPlayer(String playerId, Object message) {
        if (playerId.equals(myPlayerId))
            Log.w("GPGS", "Message to myself - ignored.");
        else {
            String participantId = playerToConnection.get(playerId);

            if (participantId != null)
                sendReliableMessage(participantId, serializeObject(message));
            else
                Log.e("GPGS", "sendToPlayer with unknown participant id for player " + playerId);
        }
    }

    protected void sendReliableMessage(String recipientId, byte[] message) {
        if (message.length > Multiplayer.MAX_RELIABLE_MESSAGE_LEN)
            Log.e("GPGS", "Message exceeded maximum size! Recipient " + recipientId + ", message: " + new String
                    (message));
        else if (message.length > (Multiplayer.MAX_RELIABLE_MESSAGE_LEN * .85f))
            Log.w("GPGS", "Message exceeds 85% of maximum size! Recipient " + recipientId + ", message: " + new
                    String(message));

        int tokenId = Games.RealTimeMultiplayer.sendReliableMessage(gpgsClient.getGoogleApiClient(),
                this,
                message, room.getRoomId(), recipientId);

        if (tokenId == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED)
            Log.e("GPGS", "Message sent failed, recipient was: " + recipientId + ", message: " + new String(message));
    }

    @Override
    public void onRealTimeMessageSent(int status, int token, String recipientId) {
        if (status != GamesStatusCodes.STATUS_OK)
            Log.e("GPGS", "Message sent failed, recipient was: " + recipientId + ", status: " + status);
    }

    @Override
    public void sendToAllPlayers(Object message) {
        sendToAllPlayersExcept(myPlayerId, message);
    }

    @Override
    public void sendToAllPlayersExcept(String playerId, Object message) {
        if (getRoomState() == MultiPlayerObjects.RoomState.closed) {
            Log.w("GPGS", "Room already closed - message not delivered");
            return;
        }

        if (room == null)
            throw new IllegalStateException("Room not initialized");

        String exceptParticipant = null;
        if (playerId != null)
            exceptParticipant = playerToConnection.get(playerId);

        final byte[] serializedMessage = serializeObject(message);

        for (Participant p : room.getParticipants())
            if (p.isConnectedToRoom() && (exceptParticipant == null || !exceptParticipant.equals(p.getParticipantId()
            ))) {
                sendReliableMessage(p.getParticipantId(), serializedMessage);
            }


    }

    @Override
    public void sendToReferee(Object message) {
        if (ownerParticipantId == null)
            Log.w("GPGS", "Message not sent to referee - not yet set." + message.getClass().getName());
        else if (iAmTheOwner)
            informGotRoomMessage(message);
        else
            sendReliableMessage(ownerParticipantId, serializeObject(message));
    }

    public byte[] serializeObject(Object message) {
        Output output = new Output(128, 1025);
        kryo.writeClassAndObject(output, message);
        output.close();
        return output.getBuffer();
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

    @Override
    public boolean isLocalGame() {
        return false;
    }

    @Override
    public void startGame(boolean force) throws VetoException {
        if (room == null)
            throw new VetoException("No room opened.");

        // Abbruch wenn noch nicht alle verbunden sind
        if (!allPlayersConnected)
            throw new VetoException("There are still players invited but neither connected nor declined their " +
                    "invitation.");

        super.startGame(force);
    }

    // RealtimeMessageReceivedListener

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        try {
            Input input = new Input(realTimeMessage.getMessageData());
            Object object = kryo.readClassAndObject(input);

            // ACHTUNG: connection kann null sein, zum Beispiel durch sendToReferee

            if (object instanceof MultiPlayerObjects.Handshake) {
                //HANDSHAKE ist gekommen
                handleHandshake(realTimeMessage.getSenderParticipantId(), (MultiPlayerObjects.Handshake) object);
                return;
            }

            if (object instanceof MultiPlayerObjects.PlayerChanged) {
                handlePlayersChanged((MultiPlayerObjects.PlayerChanged) object);
                return;
            }

            if (!isOwner() && object instanceof MultiPlayerObjects.RoomStateChanged) {
                com.esotericsoftware.minlog.Log.info("Multiplayer", "Received change of " + object.toString());
                setRoomState(((MultiPlayerObjects.RoomStateChanged) object).roomState);
                // die anderen Felder referee und deputy sind ungenutzt
                return;
            }

            informGotRoomMessage(object);
        } catch (Throwable t) {
            Log.e("GPGS", "Error deserializing message!");
        }
    }

    private void handlePlayersChanged(MultiPlayerObjects.PlayerChanged mpc) {
        // kann nur auf dem Client kommen - trotzdem, sicher ist sicher.
        if (isOwner()) {
            Log.w("GPGS", "Server received PlayerChanged");
            return;
        }

        Log.i("GPGS", "Player changed: " + mpc.changedPlayer.name);

        switch (mpc.changeType) {
            case MultiPlayerObjects.CHANGE_REMOVE:
                allPlayers.remove(mpc.changedPlayer.name);
                informRoomInhabitantsChanged(mpc);
                break;
            default:
                addPlayer(mpc.changedPlayer.name, mpc.changedPlayer.address);
        }
    }

    private void handleHandshake(String senderParticipantId, MultiPlayerObjects.Handshake handshake) {
        Log.i("GPGS", "Handshake received: " + handshake.lightblocksVersion + ", Participant " + senderParticipantId);

        if (isOwner()) {
            handshake.success = true;

            // wir ordnen den Spieler ein wenn er eine passende Version hat
            if (handshake.interfaceVersion != MultiPlayerObjects.INTERFACE_VERSION) {
                handshake.success = false;
                handshake.message = "Interface versions differ. Use same Lightblocks version.";
            }

            if (!handshake.success) {
                informGotErrorMessage(handshake);
                handshake.playerId = myPlayerId;
                handshake.lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;

                sendReliableMessage(senderParticipantId, serializeObject(handshake));

            } else {

                // ansonsten zur Spierliste dazu und über Spieler informieren
                addPlayer(handshake.playerId, senderParticipantId);

                // Info über bisherige Spieler an den neuen Spieler senden
                for (String playerId : allPlayers) {
                    MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
                    pc.changedPlayer = new MultiPlayerObjects.Player();
                    pc.changedPlayer.name = playerId;
                    pc.changedPlayer.address = playerToConnection.get(playerId);
                    sendToPlayer(handshake.playerId, pc);
                }

                // das eigene UI wurde in addPlayer informiert
                // TODO wenn es mehr als zwei Spieler geben würde müssten hier die anderen informiert werden

            }
        } else if (!isOwner() && !handshake.success) {
            // wenn ein Handshake als Antwort kam, hat etwas nicht geklappt :(
            informGotErrorMessage(handshake);
            try {
                leaveRoom(true);
            } catch (VetoException e) {
                // eat
            }
        }
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
        synchronized (lockObj) {
            this.room = room;
        }

        // das ist dann jetzt die Stelle wo ich den Handshake schicke
        // außer beim Automatching, denn dort ist der Owner hier noch nicht bekannt
        if (!isOwner() && !roomWithAutoMatch) {
            sendHandshake();
        }
    }

    protected void sendHandshake() {
        MultiPlayerObjects.Handshake handshake = new MultiPlayerObjects.Handshake();
        handshake.playerId = myPlayerId;
        sendToReferee(handshake);
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
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        // Called when one or more peer participants are disconnected from a room.
        Log.i("GPGS", "onPeersDisconnected");
        // nur setzen wenn nicht schon null ist (dann wurde bereits disconnected)
        this.room = room;

        // da nur zwei Spieler zugelassen sind muss das hier nicht behandelt werden
        // ein Disconnect führt immer zum Close des Raums
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
        // Achtung: die Liste der Participants ist bei AutoMatch noch nicht vollständig
        // daher kann der RoomOwner dann nur in roomConnected bestimmt werden
        // da hier auf zwei Spieler begrenzt wird, ist das aber ok
        Log.i("GPGS", "onRoomCreated " + statusCode);

        synchronized (lockObj) {
            this.room = room;
            roomCreationPending = false;
            resetRoomMembers();

            if (room != null) {
                for (Participant p : room.getParticipants())
                    Log.i("GPGS", "Participant " + p.getParticipantId() + "/" + p.getDisplayName());

                final String myParticipantId = getMyParticipantId();
                if (!roomWithAutoMatch)
                    setOwnerParticipantId(myParticipantId);

                addPlayer(myPlayerId, myParticipantId);
                setRoomState(MultiPlayerObjects.RoomState.join);
            }
        }

    }

    protected String getMyParticipantId() {
        // room.getParticipantId(myPlayerId) funktioniert nicht :-/
        if (room == null)
            return null;

        for (Participant p : room.getParticipants())
            if (p.getDisplayName().equals(myPlayerId))
                return p.getParticipantId();

        return null;
    }

    public void addPlayer(String playerId, String participantId) {

        if (!allPlayers.contains(playerId)) {
            allPlayers.add(playerId);
            connectionToPlayer.put(participantId, playerId);
            playerToConnection.put(playerId, participantId);

            final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
            final MultiPlayerObjects.Player player = new MultiPlayerObjects.Player();
            player.name = playerId;
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
        synchronized (lockObj) {
            this.room = room;
            resetRoomMembers();
            if (room != null) {
                setOwnerParticipantId(room.getCreatorId());
                addPlayer(myPlayerId, getMyParticipantId());
                roomCreationPending = false;
                setRoomState(MultiPlayerObjects.RoomState.join);
            }
        }
    }

    @Override
    public void onLeftRoom(int statusCode, String givenToLeave) {
        // Called when the client attempts to leaves the real-time room.
        Log.i("GPGS", "onLeftRoom " + statusCode);
        synchronized (lockObj) {
            this.room = null;
            setRoomState(MultiPlayerObjects.RoomState.closed);
        }
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        // Called when ALL the participants in a real-time room are fully connected.
        Log.i("GPGS", "onRoomConnected " + statusCode);
        synchronized (lockObj) {
            this.room = room;
            roomCreationPending = false;
            allPlayersConnected = true;

            if (roomWithAutoMatch) {
                // im AutoMatch ist der Owner die kleinste participant id
                String newOwnerId = getMyParticipantId();
                for (String pid : room.getParticipantIds())
                    if (pid.compareTo(newOwnerId) < 0) {
                        newOwnerId = pid;
                    }
                setOwnerParticipantId(newOwnerId);

                // beim Automatch kann dann erst jetzt der Handshake durchgeführt werden. Der Client meldet sich
                if (!isOwner())
                    sendHandshake();
            }

        }
    }

    protected void setOwnerParticipantId(String newOwner) {
        if (newOwner == null) {
            iAmTheOwner = false;
            ownerParticipantId = null;
            Log.i("GPGS", "Room owner reset.");
        } else {
            if (ownerParticipantId != null)
                throw new IllegalStateException("Tried to set owner, but is already set!");

            this.ownerParticipantId = newOwner;
            iAmTheOwner = (ownerParticipantId.equals(getMyParticipantId()));
            Log.i("GPGS", "Room owner is " + ownerParticipantId);

        }
    }

    protected void resetRoomMembers() {
        allPlayersConnected = false;
        setOwnerParticipantId(null);
    }
}
