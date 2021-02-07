package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableList;
import de.golfgl.lightblocks.screen.FontAwesome;

public abstract class ListPublicServersDialog extends ControllerMenuDialog {

    private final LightBlocksGame app;
    private TouchableList<ServerAddress> hostList;
    private TextButton joinRoomButton;
    private Button leaveButton;
    private ScrollPane hostListScrollPane;
    private ServerAddress lastSelectedRoom;
    private List<ServerAddress> lastShownAdressList;

    public ListPublicServersDialog(LightBlocksGame app) {
        super("", app.skin);
        this.app = app;

        // Back button
        getButtonTable().defaults().uniform().fillX().expandX();
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        joinRoomButton.setDisabled(true);
        button(leaveButton);

        lastShownAdressList = app.backendManager.getMultiplayerServerAddressList();
        if (lastShownAdressList != null)
            fillMenuTable();
        else
            getContentTable().add(new ProgressDialog.WaitRotationImage(app));

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

        addFocusableActor(joinRoomButton);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (lastShownAdressList == null && app.backendManager.getMultiplayerServerAddressList() != null) {
            lastShownAdressList = app.backendManager.getMultiplayerServerAddressList();
            getContentTable().clear();
            fillMenuTable();
            if (getStage() != null) {
                pack();
                setPosition(Math.round((getStage().getWidth() - getWidth()) / 2), Math.round((getStage().getHeight() - getHeight()) / 2));
                ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
            }
        }
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

    protected void fillMenuTable() {
        Table menuTable = getContentTable();
        menuTable.pad(15);
        menuTable.add(new ScaledLabel(app.TEXTS.get("multiplayerListPublic"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        menuTable.row().padTop(15);

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
        menuTable.add(hostListScrollPane).width(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .3f).pad(0).expand();
        menuTable.row();
        menuTable.add(joinRoomButton);

        hostList.setItems(app.backendManager.getMultiplayerServerAddressList().toArray(new ServerAddress[0]));
        roomSelect(hostList.getSelected());
    }

    protected void roomSelect(ServerAddress selectedRoom) {
        lastSelectedRoom = selectedRoom;

        if (selectedRoom == null)
            return;

        joinRoomButton.setDisabled(false);
        String shownRoomName = selectedRoom.getRoomName();
        if (shownRoomName.length() >= 13)
            shownRoomName = shownRoomName.substring(0, 10) + "...";
        joinRoomButton.setText(app.TEXTS.get("labelMultiplayerJoinRoom") + ": " + shownRoomName);
    }


}
