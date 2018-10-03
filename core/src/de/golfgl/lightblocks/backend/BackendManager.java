package de.golfgl.lightblocks.backend;

import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Created by Benjamin Schulte on 03.10.2018.
 */

public class BackendManager extends BackendClient {

    private final LocalPrefs prefs;
    private boolean authenticated;

    public BackendManager(LocalPrefs prefs) {
        this.prefs = prefs;

        setUserId(prefs.getBackendUserId());
        setUserPass(prefs.getBackendUserPassKey());
    }

    @Override
    protected void onPlayerCreated(PlayerCreatedInfo playerCreatedInfo) {
        super.onPlayerCreated(playerCreatedInfo);
        authenticated = true;
        prefs.saveBackendUser(getUserId(), getUserPass());
    }
}
