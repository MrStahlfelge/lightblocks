package de.golfgl.lightblocks.gpgs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import de.golfgl.lightblocks.AndroidLauncher;

/**
 * Client für Google Play Games
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class GpgsClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, IGpgsClient {

    private Activity myContext;
    private IGpgsListener gameListener;

    // Play Games
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    public GpgsClient(Activity context) {
        myContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();
    }

    @Override
    public void connect(boolean autoStart) {
        mAutoStartSignInflow = autoStart;
        mSignInClicked = !autoStart;
        mGoogleApiClient.connect();
    }


    @Override
    public void disconnect(boolean autoEnd) {
        if (isConnected()) {
            if (!autoEnd)
                try {
                    Games.signOut(mGoogleApiClient);
                } catch (Throwable t) {
                    // eat security exceptions when already signed out via gpgs ui
                }
            mGoogleApiClient.disconnect();
            gameListener.gpgsDisconnected();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        Log.i("GPGS", "Successfully signed in with player id " + getPlayerDisplayName());
        gameListener.gpgsConnected();
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
        Log.i("GPGS", "Connection suspended, trying to reconnect");
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }
        Log.w("GPGS", "onConnectFailed");

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

        // Put code here to display the sign-in button

    }

    public void activityResult(int resultCode, Intent data) {
        mSignInClicked = false;
        mResolvingConnectionFailure = false;
        if (resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        } else {
            Log.w("GPGS", "ActivityResult - Unable to sign in");
            // Bring up an error dialog to alert the user that sign-in
            // failed. The R.string.signin_failure should reference an error
            // string in your strings.xml file that tells the user they
            // could not be signed in, such as "Unable to sign in."

            //TODO: UI bescheid geben

            //BaseGameUtils.showActivityResultError(this,
            //        AndroidLauncher.RC_GPGS_SIGNIN, resultCode, "Unable to sign in.");
        }
    }

    @Override
    public void showLeaderboards(String leaderBoardId) throws GpgsException {
        if (isConnected())
            myContext.startActivityForResult(leaderBoardId != null ?
                    Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderBoardId) :
                    Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), AndroidLauncher.RC_LEADERBOARD);
        else
            throw new GpgsException();
    }

    @Override
    public void showAchievements() throws GpgsException {
        if (isConnected())
            myContext.startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    AndroidLauncher.RC_ACHIEVEMENTS);
        else
            throw new GpgsException();

    }


    public void setGameListener(IGpgsListener gameListener) {
        this.gameListener = gameListener;
    }
}
