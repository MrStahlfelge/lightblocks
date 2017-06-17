package de.golfgl.lightblocks.gpgs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.example.games.basegameutils.BaseGameUtils;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.lightblocks.AndroidLauncher;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;

/**
 * Client für Google Play Games
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class GpgsClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, IGpgsClient {

    public static final String GAMESERVICE_ID = "GPGS";

    public static final String NAME_SAVE_GAMESTATE = "gamestate.sav";
    private static final int MAX_SNAPSHOT_RESOLVE_RETRIES = 3;
    private static final int MAX_CONNECTFAIL_RETRIES = 4;
    private Activity myContext;
    private IGameServiceListener gameListener;
    // Play Games
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;
    private int firstConnectAttempt;
    private GpgsMultiPlayerRoom gpgsMPRoom;

    public GpgsClient(Activity context) {
        myContext = context;
        firstConnectAttempt = MAX_CONNECTFAIL_RETRIES; // dreimal probieren in Play Games einzuloggen
        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // für Savegames
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                // add other APIs and scopes here as needed
                .build();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public String getGameServiceId() {
        return GAMESERVICE_ID;
    }

    @Override
    public void connect(boolean autoStart) {
        if (isConnected())
            return;

        Log.i(GAMESERVICE_ID, "Trying to connect with autostart " + autoStart);
        mAutoStartSignInflow = autoStart;
        mSignInClicked = !autoStart;
        mGoogleApiClient.connect();
    }

    @Override
    public void logOff() {
        this.disconnect(false);
    }

    @Override
    public void disconnect() {
        this.disconnect(true);
    }

    public void disconnect(boolean autoEnd) {

        // kein disconnect wenn Multiplayer-Aktionen laufen
        if (autoEnd && gpgsMPRoom != null && gpgsMPRoom.isConnected())
            return;

        if (isConnected()) {
            Log.i(GAMESERVICE_ID, "Disconnecting with autoEnd " + autoEnd);
            if (!autoEnd)
                try {
                    Games.signOut(mGoogleApiClient);
                } catch (Throwable t) {
                    // eat security exceptions when already signed out via gpgs ui
                }
            mGoogleApiClient.disconnect();
            gameListener.gsDisconnected();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        Log.i(GAMESERVICE_ID, "Successfully signed in with player id " + getPlayerDisplayName());
        // den Zähler für maximale Versuche wieder zurück setzen. Wenn die App nämlich nicht
        // beendet, aber länger nicht benutzt wird, kommt es wieder zu dem Problem dass
        // GPGS erst fälschlicherweise errCode 4 zurückgibt
        firstConnectAttempt = MAX_CONNECTFAIL_RETRIES;
        gameListener.gsConnected();

        // TODO Erhaltene Einladungen... gleich in Multiplayer gehen
        if (bundle != null) {
            Invitation inv =
                    bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null)
                Log.i(GAMESERVICE_ID, "Multiplayer Invitation: " + inv.getInvitationId() + " from "
                        + inv.getInviter().getParticipantId());
        }

    }

    @Override
    public String getPlayerDisplayName() {
        return Games.Players.getCurrentPlayer(mGoogleApiClient)
                .getDisplayName();
    }

    @Override
    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(GAMESERVICE_ID, "Connection suspended, trying to reconnect");
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }
        Log.w(GAMESERVICE_ID, "onConnectFailed: " + connectionResult.getErrorCode());

        // if the sign-in button was clicked
        // launch the sign-in flow
        if (mSignInClicked) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(myContext,
                    mGoogleApiClient, connectionResult,
                    AndroidLauncher.RC_GPGS_SIGNIN, "Unable to sign in.")) {
                mResolvingConnectionFailure = false;
            }
        }
        // Error code 4 tritt seit Zunahme Drive-API beim ersten Start auf. Dann einfach nochmal probieren?
        else if (firstConnectAttempt > 0 && connectionResult.getErrorCode() == 4) {
            firstConnectAttempt -= 1;
            Log.w(GAMESERVICE_ID, "Retrying to connect...");

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        // darf auch nicht zu schnell sein
                        Thread.sleep(200);
                        if (!mGoogleApiClient.isConnected())
                            mGoogleApiClient.connect();
                    } catch (InterruptedException e) {
                        //eat
                    }
                    return null;
                }
            };

            task.execute();

        }
    }

    public void signInResult(int resultCode, Intent data) {
        mSignInClicked = false;
        mResolvingConnectionFailure = false;
        if (resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        } else {
            Log.w(GAMESERVICE_ID, "SignInResult - Unable to sign in");
            // Bring up an error dialog to alert the user that sign-in
            // failed. The R.string.signin_failure should reference an error
            // string in your strings.xml file that tells the user they
            // could not be signed in, such as "Unable to sign in."

            String errorMsg;
            switch (resultCode) {
                case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                    errorMsg = myContext.getString(com.google.example.games.basegameutils.R.string.app_misconfigured);
                    break;
                case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                    errorMsg = myContext.getString(com.google.example.games.basegameutils.R.string.sign_in_failed);
                    break;
                default:
                    errorMsg = null;
            }

            if (errorMsg != null)
                gameListener.gsErrorMsg("Google Play Games: " + errorMsg);

        }
    }

    @Override
    public void showLeaderboards(String leaderBoardId) throws GameServiceException {
        if (isConnected())
            myContext.startActivityForResult(leaderBoardId != null ?
                    Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderBoardId) :
                    Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), AndroidLauncher.RC_LEADERBOARD);
        else
            throw new GameServiceException();
    }

    @Override
    public void showAchievements() throws GameServiceException {
        if (isConnected())
            myContext.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    AndroidLauncher.RC_ACHIEVEMENTS);
        else
            throw new GameServiceException();

    }

    @Override
    public void submitToLeaderboard(String leaderboardId, long score, String tag) throws GameServiceException {
        if (isConnected())
            if (tag != null)
                Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, score, tag);
            else
                Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, score);
        else
            throw new GameServiceException();
    }

    @Override
    public void submitEvent(String eventId, int increment) {
        // No exception, if not online events are dismissed
        if (!isConnected())
            return;

        Games.Events.increment(mGoogleApiClient, eventId, increment);
    }

    @Override
    public void unlockAchievement(String achievementId) {
        if (isConnected())
            Games.Achievements.unlock(mGoogleApiClient, achievementId);
    }

    @Override
    public void incrementAchievement(String achievementId, int incNum) {
        if (isConnected())
            Games.Achievements.increment(mGoogleApiClient, achievementId, incNum);
    }

    public void setGameListener(IGameServiceListener gameListener) {
        this.gameListener = gameListener;
    }

    @Override
    public void saveGameState(final byte[] gameState, final long progressValue) {

        if (isConnected()) {

            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return saveGameStateSync(gameState, progressValue);
                }
            };

            task.execute();
        }
    }

    @NonNull
    @Override
    public Boolean saveGameStateSync(byte[] gameState, long progressValue) {
        if (!isConnected())
            return false;

        // Open the snapshot, creating if necessary
        Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
                mGoogleApiClient, NAME_SAVE_GAMESTATE, true).await();

        Snapshot snapshot = processSnapshotOpenResult(open, 0);

        if (snapshot == null) {
            Log.w(GAMESERVICE_ID, "Could not open Snapshot.");
            return false;
        }

        if (progressValue < snapshot.getMetadata().getProgressValue()) {
            Log.e(GAMESERVICE_ID, "Progress of saved game state higher than current one. Did not save.");
            return false;
        }

        // Write the new data to the snapshot
        snapshot.getSnapshotContents().writeBytes(gameState);

        // Change metadata
        // Description wird in Play Games app angezeigt
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .fromMetadata(snapshot.getMetadata())
                .setDescription("Time to play again!")
                .setProgressValue(progressValue)
                .build();

        Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
                mGoogleApiClient, snapshot, metadataChange).await();

        if (!commit.getStatus().isSuccess()) {
            Log.w(GAMESERVICE_ID, "Failed to commit Snapshot:" + commit.getStatus().getStatusMessage());

            return false;
        }

        // No failures
        Log.i(GAMESERVICE_ID, "Successfully saved gamestate with " + gameState.length + "B");
        return true;
    }

    @Override
    public void loadGameState() {

        if (!isConnected())
            gameListener.gsGameStateLoaded(null);

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return loadGameStateSync();
            }
        };

        task.execute();
    }

    @Override
    public AbstractMultiplayerRoom getMultiPlayerRoom() {
        if (gpgsMPRoom == null) {
            gpgsMPRoom = new GpgsMultiPlayerRoom();
            gpgsMPRoom.setContext(myContext);
            gpgsMPRoom.setGpgsClient(this);
        }
        return gpgsMPRoom;
    }

    @NonNull
    public Boolean loadGameStateSync() {
        if (!isConnected())
            gameListener.gsGameStateLoaded(null);

        // Open the snapshot, creating if necessary
        Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
                mGoogleApiClient, NAME_SAVE_GAMESTATE, true).await();

        Snapshot snapshot = processSnapshotOpenResult(open, 0);

        if (snapshot == null) {
            Log.w(GAMESERVICE_ID, "Could not open Snapshot.");
            gameListener.gsGameStateLoaded(null);
            return false;
        }

        // Read
        try {
            byte[] mSaveGameData = null;
            mSaveGameData = snapshot.getSnapshotContents().readFully();
            gameListener.gsGameStateLoaded(mSaveGameData);
            return true;
        } catch (Throwable t) {
            Log.e(GAMESERVICE_ID, "Error while reading Snapshot.", t);
            gameListener.gsGameStateLoaded(null);
            return false;
        }

    }

    /**
     * Conflict resolution for when Snapshots are opened.  Must be run in an AsyncTask or in a
     * background thread,
     */
    public Snapshot processSnapshotOpenResult(Snapshots.OpenSnapshotResult result, int retryCount) {
        Snapshot mResolvedSnapshot = null;
        retryCount++;

        int status = result.getStatus().getStatusCode();
        Log.i(GAMESERVICE_ID, "Open Snapshot Result status: " + result.getStatus().getStatusMessage());

        if (status == GamesStatusCodes.STATUS_OK) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
            return result.getSnapshot();
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
            Snapshot snapshot = result.getSnapshot();
            Snapshot conflictSnapshot = result.getConflictingSnapshot();

            // Resolve between conflicts by selecting the highest progress or, if equal, newest of the conflicting
            // snapshots.
            mResolvedSnapshot = snapshot;

            if (snapshot.getMetadata().getProgressValue() < conflictSnapshot.getMetadata().getProgressValue()
                    || snapshot.getMetadata().getProgressValue() == conflictSnapshot.getMetadata().getProgressValue()
                    && snapshot.getMetadata().getLastModifiedTimestamp() <
                    conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
                mResolvedSnapshot = conflictSnapshot;
            }

            Snapshots.OpenSnapshotResult resolveResult = Games.Snapshots.resolveConflict(
                    mGoogleApiClient, result.getConflictId(), mResolvedSnapshot).await();

            if (retryCount < MAX_SNAPSHOT_RESOLVE_RETRIES) {
                // Recursively attempt again
                return processSnapshotOpenResult(resolveResult, retryCount);
            }

        }

        // Fail, return null.
        return null;
    }
}
