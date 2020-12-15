package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.Gdx;
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

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableList;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Let the player choose a local multiplayer server
 * <p>
 * Created by Benjamin Schulte on 15.12.2020.
 */

public abstract class DetectLocalServersDialog extends ControllerMenuDialog implements LifecycleListener {
    private final LightBlocksGame app;
    private TouchableList<ServerAddress> hostList;
    private float timeSinceHostListRefresh;
    private ServerAddress lastSelectedRoom;
    private TextButton joinRoomButton;
    private Button leaveButton;
    private ScrollPane hostListScrollPane;

    public DetectLocalServersDialog(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        // Back button
        getButtonTable().defaults().uniform().fillX().expandX();
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        button(leaveButton);

        Table contentTable = getContentTable();
        contentTable.pad(15);
        contentTable.add(new ScaledLabel(app.TEXTS.get("multiplayerSearchLocal"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        contentTable.row().padTop(15);
        fillMenuTable(contentTable);

        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (lastSelectedRoom == null)
                    return;

                joinRoom(lastSelectedRoom);

                hide();
            }
        });
        joinRoomButton.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // automatic selection does not work because of surrounding ScrollPane
                // so we hardwire it with go up keycode
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

    protected abstract void joinRoom(ServerAddress lastSelectedRoom);

    @Override
    protected Actor getConfiguredEscapeActor() {
        return leaveButton;
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return joinRoomButton;
    }

    protected void fillMenuTable(Table menuTable) {
        hostList = new TouchableList<ServerAddress>(app.skin) {
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
                //no super call here, we don't want to select the first one
            }
        };
        addFocusableActor(hostList);

        hostListScrollPane = new ControllerScrollPane(hostList, app.skin, LightBlocksGame.SKIN_LIST);

        hostList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final ServerAddress selectedRoom = hostList.getSelected();
                if (selectedRoom != null) {
                    roomSelect(selectedRoom);
                }
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
        final Label multiplayerJoinRoomHelp2 = new ScaledLabel(app.TEXTS.get("multiplayerLocalServerHelp"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f);
        multiplayerJoinRoomHelp2.setWrap(true);
        menuTable.add(multiplayerJoinRoomHelp2).fill();
    }

    protected void roomSelect(ServerAddress selectedRoom) {
        lastSelectedRoom = selectedRoom;
        joinRoomButton.setDisabled(false);
        String shownRoomName = selectedRoom.getRoomName();
        if (shownRoomName.length() >= 13)
            shownRoomName = shownRoomName.substring(0, 10) + "...";
        joinRoomButton.setText(app.TEXTS.get("labelMultiplayerJoinRoom") + ": " + shownRoomName);
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
