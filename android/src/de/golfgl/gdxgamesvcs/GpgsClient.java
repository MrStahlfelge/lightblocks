package de.golfgl.gdxgamesvcs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.Gdx;
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

/**
 * Client for Google Play Games
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class GpgsClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        IGameServiceClient {
    public static final int RC_GPGS_SIGNIN = 9001;
    public static final int RC_LEADERBOARD = 9002;
    public static final int RC_ACHIEVEMENTS = 9003;

    public static final String GAMESERVICE_ID = "GPGS";
    protected static final int MAX_CONNECTFAIL_RETRIES = 4;
    private static final int MAX_SNAPSHOT_RESOLVE_RETRIES = 3;
    protected Activity myContext;
    protected IGameServiceListener gameListener;
    // Play Games
    protected GoogleApiClient mGoogleApiClient;
    protected int firstConnectAttempt;
    protected boolean isConnectionPending;
    protected boolean driveApiEnabled;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    /**
     * Initializes the GoogleApiClient. Give your main AndroidLauncher as context.
     * <p>
     * Don't forget to add onActivityResult method there with call to onGpgsActivityResult.
     *
     * @param context        your AndroidLauncher class
     * @param enableDriveAPI yes if you activate save gamestate feature
     * @return this for method chunking
     */
    public GpgsClient initialize(Activity context, boolean enableDriveAPI) {

        if (mGoogleApiClient != null)
            throw new IllegalStateException("Already initialized.");

        myContext = context;
        firstConnectAttempt = MAX_CONNECTFAIL_RETRIES; // dreimal probieren in Play Games einzuloggen

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(myContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES);

        driveApiEnabled = enableDriveAPI;
        if (driveApiEnabled)
            builder.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);

        // add other APIs and scopes here as needed

        mGoogleApiClient = builder.build();

        return this;
    }

    /**
     * Call this in the onActivityResult of the context you gave to initialize()
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        Intent
     * @return yes if this was a Gpgs activity
     */
    public boolean onGpgsActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GPGS_SIGNIN) {
            signInResult(resultCode, data);
            return true;

            // check for "inconsistent state"
        } else if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED &&
                (requestCode == RC_LEADERBOARD || requestCode == RC_ACHIEVEMENTS)) {
            // force a disconnect to sync up state, ensuring that mClient reports "not connected"
            disconnect(false);
            return true;
        }
        return false;
    }

    @Override
    public String getGameServiceId() {
        return GAMESERVICE_ID;
    }

    @Override
    public boolean connect(boolean autoStart) {
        if (mGoogleApiClient == null) {
            Gdx.app.error(GAMESERVICE_ID, "Call initialize first.");
            throw new IllegalStateException();
        }

        if (isConnected())
            return true;

        Log.i(GAMESERVICE_ID, "Trying to connect with autostart " + autoStart);
        mAutoStartSignInflow = autoStart;
        mSignInClicked = !autoStart;
        isConnectionPending = true;
        mGoogleApiClient.connect();

        return true;
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
        isConnectionPending = false;
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
    public boolean isConnectionPending() {
        return isConnectionPending;
    }

    @Override
    public boolean providesLeaderboardUI() {
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(GAMESERVICE_ID, "Connection suspended, trying to reconnect");
        // Attempt to reconnect
        isConnectionPending = true;
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
                    RC_GPGS_SIGNIN, "Unable to sign in.")) {
                mResolvingConnectionFailure = false;
                isConnectionPending = false;
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

        } else
            isConnectionPending = false;
    }

    public void signInResult(int resultCode, Intent data) {
        mSignInClicked = false;
        mResolvingConnectionFailure = false;
        if (resultCode == Activity.RESULT_OK) {
            isConnectionPending = true;
            mGoogleApiClient.connect();
        } else {
            Log.w(GAMESERVICE_ID, "SignInResult - Unable to sign in");

            isConnectionPending = false;
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
                gameListener.gsErrorMsg(IGameServiceListener.GsErrorType.errorLoginFailed,
                        "Google Play Games: " + errorMsg);

        }
    }

    @Override
    public void showLeaderboards(String leaderBoardId) throws GameServiceException {
        if (isConnected())
            myContext.startActivityForResult(leaderBoardId != null ?
                    Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderBoardId) :
                    Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), RC_LEADERBOARD);
        else
            throw new GameServiceException();
    }

    @Override
    public boolean providesAchievementsUI() {
        return true;
    }

    @Override
    public void showAchievements() throws GameServiceException {
        if (isConnected())
            myContext.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_ACHIEVEMENTS);
        else
            throw new GameServiceException();

    }

    @Override
    public boolean submitToLeaderboard(String leaderboardId, long score, String tag) {
        if (isConnected()) {
            if (tag != null)
                Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, score, tag);
            else
                Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, score);
            return true;
        } else
            return false;
    }

    @Override
    public boolean submitEvent(String eventId, int increment) {
        // No exception, if not online events are dismissed
        if (!isConnected())
            return false;

        Games.Events.increment(mGoogleApiClient, eventId, increment);

        return true;
    }

    @Override
    public boolean unlockAchievement(String achievementId) {
        if (isConnected()) {
            Games.Achievements.unlock(mGoogleApiClient, achievementId);
            return true;
        } else
            return false;
    }

    @Override
    public boolean incrementAchievement(String achievementId, int incNum) {
        if (isConnected()) {
            Games.Achievements.increment(mGoogleApiClient, achievementId, incNum);
            return true;
        } else
            return false;
    }

    @Override
    public void setListener(IGameServiceListener gameListener) {
        this.gameListener = gameListener;
    }

    @Override
    public void saveGameState(final String id, final byte[] gameState, final long progressValue)
            throws GameServiceException {
        if (!driveApiEnabled)
            throw new GameServiceException.NotSupportedException();

        if (isConnected()) {

            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return saveGameStateSync(id, gameState, progressValue);
                }
            };

            task.execute();
        }
    }

    @NonNull
    public Boolean saveGameStateSync(String id, byte[] gameState, long progressValue) {
        if (!isConnected())
            return false;

        // Open the snapshot, creating if necessary
        Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
                mGoogleApiClient, id, true).await();

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
                .setDescription("Time to play again!") //TODO
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
    public void loadGameState(final String id) throws GameServiceException {

        if (!driveApiEnabled)
            throw new GameServiceException.NotSupportedException();

        if (!isConnected())
            gameListener.gsGameStateLoaded(null);

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return loadGameStateSync(id);
            }
        };

        task.execute();
    }

    @Override
    public CloudSaveCapability supportsCloudGameState() {
        return (driveApiEnabled ? CloudSaveCapability.MultipleFilesSupported : CloudSaveCapability.NotSupported);
    }

    @NonNull
    public Boolean loadGameStateSync(String id) {
        if (!isConnected())
            gameListener.gsGameStateLoaded(null);

        // Open the snapshot, creating if necessary
        Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
                mGoogleApiClient, id, true).await();

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
