package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.KryonetMultiplayerRoom;

/**
 * Created by Benjamin Schulte on 24.02.2017.
 */

public class MultiplayerMenuScreen extends AbstractMenuScreen {

    private List<IRoomLocation> hostList;
    private float timeSinceHostListRefresh;

    public MultiplayerMenuScreen(LightBlocksGame app) {
        super(app);

        initializeUI();

    }

    @Override
    protected void fillButtonTable(Table buttons) {

    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PEOPLE;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMultiplayerButton");
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        final TextButton startServer = new TextButton("Start/stop server", app.skin);
        startServer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    if (app.multiRoom != null) {
                        app.multiRoom.stopRoomDiscovery();
                        app.multiRoom.leaveRoom(true);
                    }

                    final KryonetMultiplayerRoom kryonetRoom = new KryonetMultiplayerRoom();
                    kryonetRoom.setNsdHelper(app.nsdHelper);
                    app.multiRoom = kryonetRoom;


                    app.multiRoom.initializeRoom();
                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }
            }
        });

        final TextButton connectToServer = new TextButton("Search for rooms", app.skin);
        connectToServer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    //TODO muss natürlich noch besser
                    if (app.multiRoom != null) {
                        app.multiRoom.stopRoomDiscovery();
                        app.multiRoom.leaveRoom(true);
                    }

                    final KryonetMultiplayerRoom kryonetRoom = new KryonetMultiplayerRoom();
                    kryonetRoom.setNsdHelper(app.nsdHelper);
                    app.multiRoom = kryonetRoom;

                    app.multiRoom.startRoomDiscovery();

                    //app.multiRoom.joinRoom(rooms.get(0));

                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }
            }
        });

        hostList = new List<IRoomLocation>(app.skin);

        menuTable.add(startServer);
        menuTable.add(connectToServer);
        menuTable.row();
        menuTable.add(hostList).colspan(2).minWidth(300).minHeight(300);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        timeSinceHostListRefresh += delta;

        if (timeSinceHostListRefresh > .15f && app.multiRoom != null) {
            timeSinceHostListRefresh = timeSinceHostListRefresh - .15f;

            hostList.clearItems();

            final java.util.List<IRoomLocation> discoveredRooms = app.multiRoom.getDiscoveredRooms();
            if (discoveredRooms != null)
                for (IRoomLocation a : discoveredRooms) {
                    hostList.getItems().add(a);
                }
        }
    }
}
