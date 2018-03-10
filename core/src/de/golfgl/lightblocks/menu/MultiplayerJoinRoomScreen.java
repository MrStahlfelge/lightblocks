package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.KryonetRoomLocation;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.VetoException;

/**
 * Let the player choose a room
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class MultiplayerJoinRoomScreen extends ControllerMenuDialog implements IRoomListener, LifecycleListener {
    private final LightBlocksGame app;
    private List<IRoomLocation> hostList;
    private float timeSinceHostListRefresh;
    private IRoomLocation lastSelectedRoom;
    private Label selectedRoomLabel;
    private Button joinRoomButton;
    private Button leaveButton;

    public MultiplayerJoinRoomScreen(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        // Back button
        getButtonTable().defaults().uniform().fillX().expandX();
        getButtonTable().pad(10);
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        button(leaveButton);

        Table contentTable = getContentTable();
        contentTable.pad(15);
        contentTable.add(new ScaledLabel(app.TEXTS.get("labelMultiplayerLan") + ": " + app.TEXTS.get
                ("labelMultiplayerJoinRoom").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        contentTable.row().padTop(15);
        fillMenuTable(contentTable);

        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (lastSelectedRoom == null)
                    return;

                try {
                    app.multiRoom.joinRoom(lastSelectedRoom, app.player);
                    joinRoomButton.setDisabled(true);
                } catch (VetoException e) {
                    new VetoDialog(e.getMessage(), app.skin, LightBlocksGame.nativeGameWidth * .8f).show(getStage());
                }

                // wenn bis hierher gekommen, dann ist die Connection aufgebaut und der Handshake gesendet.
                // die Antwort kommt per roomStateChange

            }
        });
        joinRoomButton.setDisabled(true);

        addFocusableActor(joinRoomButton);
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return leaveButton;
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return hostList;
    }

    protected void fillMenuTable(Table menuTable) {
        hostList = new List<IRoomLocation>(app.skin);
        selectedRoomLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        final TextButton enterManually = new RoundedTextButton(app.TEXTS.get("multiplayerJoinManually"), app.skin);
        enterManually.getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
        addFocusableActor(enterManually);

        hostList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final IRoomLocation selectedRoom = hostList.getSelected();
                if (selectedRoom != null) {
                    roomSelect(selectedRoom);
                }
            }
        });

        enterManually.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            KryonetRoomLocation newRoom = new KryonetRoomLocation(text, InetAddress.getByName(text));
                            roomSelect(newRoom);
                        } catch (UnknownHostException e) {
                            new VetoDialog("Not valid - " + e.getMessage(), app.skin, .8f).show(getStage());
                        }
                    }

                    @Override
                    public void canceled() {
                        //nix
                    }
                }, app.TEXTS.get("gameTitle"), "", app.TEXTS.get("multiplayerJoinManually"));
            }
        });

        menuTable.add(new ScaledLabel(app.TEXTS.get("multiplayerJoinRoomHelp1"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f)).fill();
        menuTable.row();
        menuTable.add(hostList).minWidth(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .15f).pad(5);
        menuTable.row();
        menuTable.add(selectedRoomLabel);
        menuTable.row();
        menuTable.add(joinRoomButton);
        menuTable.row().padTop(15);
        final Label multiplayerJoinRoomHelp2 = new ScaledLabel(app.TEXTS.get("multiplayerJoinRoomHelp2"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f);
        multiplayerJoinRoomHelp2.setWrap(true);
        menuTable.add(multiplayerJoinRoomHelp2).fill();
        menuTable.row();
        menuTable.add(enterManually);
    }

    protected void roomSelect(IRoomLocation selectedRoom) {
        lastSelectedRoom = selectedRoom;
        joinRoomButton.setDisabled(false);
        selectedRoomLabel.setText(selectedRoom.getRoomName());
    }

    @Override
    public void pause() {
        // bei hide und vom LifecycleListener gestartet
        app.multiRoom.stopRoomDiscovery();
    }

    @Override
    public void resume() {
        // bei show und vom LifecycleListener gestartet
        try {
            app.multiRoom.startRoomDiscovery();
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, LightBlocksGame.nativeGameWidth * .9f).show(getStage());
        }
    }

    @Override
    public void dispose() {
        // nicht benötigt
    }

    @Override
    public void hide(Action action) {
        Gdx.app.removeLifecycleListener(this);
        pause();
        app.multiRoom.removeListener(this);

        // Verbindungsanfrage hängt oder es wurde keine gestellt
        if (app.multiRoom.getRoomState() == MultiPlayerObjects.RoomState.closed)
            try {
                app.multiRoom.leaveRoom(true);
            } catch (VetoException e) {
                // kann nichts kommen
            }

        super.hide(action);
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);

        Gdx.app.addLifecycleListener(this);

        app.multiRoom.addListener(this);
        resume();

        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

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
    public void multiPlayerRoomStateChanged(MultiPlayerObjects.RoomState roomState) {
        // join -> gut
        if (roomState.equals(MultiPlayerObjects.RoomState.join))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    hide();

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
                    new VetoDialog(((MultiPlayerObjects.Handshake) o).message, app.skin,
                            LightBlocksGame.nativeGameWidth * .9f).show(getStage());
                    joinRoomButton.setDisabled(false);
                }
            });
    }

    @Override
    public void multiPlayerGotModelMessage(Object o) {
        // interessiert mich nicht
    }

    @Override
    public void multiPlayerGotRoomMessage(Object o) {
        // interessiert mich nicht
    }

    @Override
    public void multiPlayerRoomEstablishingConnection() {
        // kann bei TCP-Connection derzeit nicht auftreten
    }
}
