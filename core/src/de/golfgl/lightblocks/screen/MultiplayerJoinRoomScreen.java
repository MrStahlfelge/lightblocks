package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scenes.FATextButton;

/**
 * Let the player choose a room
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiplayerJoinRoomScreen extends AbstractMenuScreen implements IRoomListener {
    private List<IRoomLocation> hostList;
    private float timeSinceHostListRefresh;
    private IRoomLocation lastSelectedRoom;
    private Label selectedRoomLabel;
    private TextButton joinRoomButton;


    public MultiplayerJoinRoomScreen(LightBlocksGame app) {
        super(app);
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PEOPLE;
    }

    @Override
    protected String getSubtitle() {
        return app.TEXTS.get("labelMultiplayerLan") + ": " + app.TEXTS.get("labelMultiplayerJoinRoom");
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMultiplayerButton");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);
        joinRoomButton = new FATextButton(FontAwesome.NET_LOGIN,
                app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);

        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (lastSelectedRoom == null)
                    return;

                try {
                    app.multiRoom.joinRoom(lastSelectedRoom, app.player);
                    joinRoomButton.setDisabled(true);
                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }

                // wenn bis hierher gekommen, dann ist die Connection aufgebaut und der Handshake gesendet.
                // die Antwort kommt per roomStateChange

            }
        });
        joinRoomButton.setDisabled(true);

        buttons.add(joinRoomButton).uniform();
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        hostList = new List<IRoomLocation>(app.skin, LightBlocksGame.SKIN_FONT_BIG);
        selectedRoomLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);

        hostList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final IRoomLocation selectedRoom = hostList.getSelected();
                if (selectedRoom != null) {
                    lastSelectedRoom = selectedRoom;
                    joinRoomButton.setDisabled(false);
                    selectedRoomLabel.setText(selectedRoom.getRoomName());
                }
            }
        });

        menuTable.add(new Label(app.TEXTS.get("multiplayerJoinRoomHelp1"), app.skin)).fill();
        menuTable.row();
        menuTable.add(hostList).minWidth(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .25f).pad(10);
        menuTable.row();
        menuTable.add(selectedRoomLabel);
        menuTable.row();
        menuTable.add(new Label(app.TEXTS.get("multiplayerJoinRoomHelp2"), app.skin)).fill();
    }

    @Override
    public void pause() {
        app.multiRoom.stopRoomDiscovery();

        super.pause();
    }

    @Override
    public void dispose() {
        app.multiRoom.stopRoomDiscovery();
        app.multiRoom.removeListener(this);

        // Verbindungsanfrage hängt oder es wurde keine gestellt
        if (app.multiRoom.getRoomState() == AbstractMultiplayerRoom.RoomState.closed)
            try {
                app.multiRoom.leaveRoom(true);
            } catch (VetoException e) {
                // kann nichts kommen
            }

        super.dispose();
    }

    @Override
    public void show() {
        super.show();

        try {
            app.multiRoom.addListener(this);
            app.multiRoom.startRoomDiscovery();
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        timeSinceHostListRefresh += delta;

        if (timeSinceHostListRefresh > .5f && hostList != null) {
            timeSinceHostListRefresh = timeSinceHostListRefresh - .5f;

            IRoomLocation lastSelected = hostList.getSelected();

            hostList.clearItems();

            final java.util.List<IRoomLocation> discoveredRooms = app.multiRoom.getDiscoveredRooms();
            if (discoveredRooms != null)
                for (IRoomLocation a : discoveredRooms)
                    hostList.getItems().add(a);


        }

    }

    @Override
    public void multiPlayerRoomStateChanged(AbstractMultiplayerRoom.RoomState roomState) {
        // join -> gut
        if (roomState.equals(AbstractMultiplayerRoom.RoomState.join))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    goBackToMenu();

                }
            });
    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(MultiPlayerObjects.PlayerChanged mpo) {
        // interessiert mich nicht
    }

    @Override
    public void multiPlayerGotErrorMessage(final Object o) {
        if (o instanceof MultiPlayerObjects.Handshake)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    showDialog(o.toString());
                    joinRoomButton.setDisabled(false);
                }
            });
    }
}
