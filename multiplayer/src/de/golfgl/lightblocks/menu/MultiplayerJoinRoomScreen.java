package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.KryonetRoomLocation;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableList;
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
    private TouchableList<IRoomLocation> hostList;
    private float timeSinceHostListRefresh;
    private IRoomLocation lastSelectedRoom;
    private TextButton joinRoomButton;
    private Button leaveButton;
    private TextButton enterManually;
    private ScrollPane hostListScrollPane;

    public MultiplayerJoinRoomScreen(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        // Back button
        getButtonTable().defaults().uniform().fillX().expandX();
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        button(leaveButton);

        Table contentTable = getContentTable();
        contentTable.pad(15);
        contentTable.add(new ScaledLabel(app.TEXTS.get("labelMultiplayerLan") + ": " + app.TEXTS.get
                ("labelMultiplayerJoinRoom"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
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
        joinRoomButton.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // automatische Selektion funktioniert nicht immer wegen der umgebenden ScrollPane
                // also hier hart bei nach oben-Button wechseln
                if (((MyStage) getStage()).isGoUpKeyCode(keycode)) {
                    ((MyStage) getStage()).setFocusedActor(hostList);
                    return true;
                }
                return super.keyDown(event, keycode);
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
        return hostList.getSelectedIndex() >= 0 ? joinRoomButton : enterManually;
    }

    protected void fillMenuTable(Table menuTable) {
        hostList = new TouchableList<IRoomLocation>(app.skin) {
            @Override
            public boolean onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(joinRoomButton);
                return true;
            }

            @Override
            public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
                boolean scrolled = super.onControllerScroll(direction);

                if (scrolled && hostListScrollPane != null)
                    hostListScrollPane.scrollTo(0, hostList.getHeight() -
                                    hostList.getItemHeight() * (1 + hostList.getSelectedIndex()),
                            hostList.getWidth(), hostList.getItemHeight());

                return scrolled;
            }

            @Override
            protected void setup() {
                getStyle().font.getData().setScale(LightBlocksGame.LABEL_SCALING);
                //kein super, hier nicht den ersten selektieren
            }
        };
        addFocusableActor(hostList);
        enterManually = new RoundedTextButton(app.TEXTS.get("multiplayerJoinManually"), app.skin);
        enterManually.getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
        addFocusableActor(enterManually);

        hostListScrollPane = new ControllerScrollPane(hostList, app.skin, LightBlocksGame.SKIN_LIST);

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
                String proposal = "";

                if (lastSelectedRoom != null && lastSelectedRoom instanceof KryonetRoomLocation) {
                    // Desktop hat Darstellungsfehler, aber unter Android ist es ok
                    proposal = ((KryonetRoomLocation) lastSelectedRoom).address.getHostAddress();
                }

                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        try {
                            KryonetRoomLocation newRoom = new KryonetRoomLocation(text, InetAddress.getByName(text));
                            roomSelect(newRoom);
                            ((MyStage) getStage()).setFocusedActor(joinRoomButton);
                        } catch (UnknownHostException e) {
                            new VetoDialog("Not valid - " + e.getMessage(), app.skin,
                                    LightBlocksGame.nativeGameWidth * .8f).show(getStage());
                        }
                    }

                    @Override
                    public void canceled() {
                        //nix
                    }
                }, app.TEXTS.get("gameTitle"), proposal, app.TEXTS.get("multiplayerJoinManually"));
            }
        });

        menuTable.add(new ScaledLabel(app.TEXTS.get("multiplayerJoinRoomHelp1"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f)).fill();
        menuTable.row();
        menuTable.add(hostListScrollPane).minWidth(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .15f).pad(5).expand();
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
        String shownRoomName = selectedRoom.getRoomName();
        if (shownRoomName.length() >= 13)
            shownRoomName = shownRoomName.substring(0, 10) + "...";
        joinRoomButton.setText(app.TEXTS.get("labelMultiplayerJoinRoom") + ": " + shownRoomName);
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

            hostList.clearItems();

            final java.util.List<IRoomLocation> discoveredRooms = app.multiRoom.getDiscoveredRooms();
            if (discoveredRooms != null)
                for (IRoomLocation a : discoveredRooms)
                    hostList.getItems().add(a);

            if (lastSelectedRoom == null && hostList.getItems().size > 0) {
                hostList.setSelectedIndex(0);
                ((MyStage) getStage()).setFocusedActor(joinRoomButton);
            } else if (lastSelectedRoom != null && hostList.getItems().contains(lastSelectedRoom, false))
                hostList.setSelected(lastSelectedRoom);
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
