package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.MyStage;

/**
 * Let the player choose a local multiplayer server
 * <p>
 * Created by Benjamin Schulte on 15.12.2020.
 */

public abstract class DetectLocalServersDialog extends ConnectServerDialog implements LifecycleListener {
    private float timeSinceHostListRefresh;

    public DetectLocalServersDialog(final LightBlocksGame app) {
        super(app);
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("multiplayerSearchLocal");
    }

    @Override
    protected String getHelpText() {
        return app.TEXTS.get("multiplayerLocalServerHelp");
    }

    @Override
    public void pause() {
        app.nsdHelper.stopDiscovery();
    }

    @Override
    public void resume() {
        app.nsdHelper.startDiscovery(false);
    }

    @Override
    public void dispose() {
        // nicht benötigt
    }

    @Override
    public void hide(Action action) {
        Gdx.app.removeLifecycleListener(this);
        pause();

        super.hide(action);
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);

        Gdx.app.addLifecycleListener(this);

        resume();

        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceHostListRefresh += delta;

        if (timeSinceHostListRefresh > .5f && hostList != null) {
            timeSinceHostListRefresh = timeSinceHostListRefresh - .5f;

            hostList.clearItems();

            final java.util.List<ServerAddress> discoveredRooms = app.nsdHelper.getDiscoveredMultiplayerServers();
            if (discoveredRooms != null)
                for (ServerAddress a : discoveredRooms)
                    hostList.getItems().add(a);

            if (lastSelectedRoom == null && hostList.getItems().size > 0) {
                hostList.setSelectedIndex(0);
                ((MyStage) getStage()).setFocusedActor(joinRoomButton);
            } else if (lastSelectedRoom != null && hostList.getItems().contains(lastSelectedRoom, false))
                hostList.setSelected(lastSelectedRoom);
        }

    }
}
