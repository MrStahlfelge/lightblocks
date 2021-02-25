package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableList;
import de.golfgl.lightblocks.screen.FontAwesome;

public abstract class ConnectServerDialog extends ControllerMenuDialog {
    protected final LightBlocksGame app;
    protected TouchableList<ServerAddress> hostList;
    protected TextButton joinRoomButton;
    protected Button leaveButton;
    protected ServerAddress lastSelectedRoom;
    private ScrollPane hostListScrollPane;

    public ConnectServerDialog(LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        // Back button
        getButtonTable().defaults().uniform().fillX().expandX();
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        joinRoomButton.setDisabled(true);
        button(leaveButton);

        fillMenuTable();

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
        menuTable.add(new ScaledLabel(getTitle(), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
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

        FaTextButton moreInformation = new FaTextButton(app.TEXTS.get("buttonMoreInformation").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        moreInformation.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.openOrShowUri(LightBlocksGame.GAME_URL + "#server");
            }
        });
        moreInformation.getLabel().setAlignment(Align.right);

        menuTable.add(new ScaledLabel(app.TEXTS.get("multiplayerJoinRoomHelp1"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f)).fill();
        menuTable.row();
        menuTable.add(hostListScrollPane).width(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .15f).pad(5).expand();
        menuTable.row();
        menuTable.add(joinRoomButton);
        menuTable.row().padTop(15);
        final Label multiplayerJoinRoomHelp2 = new ScaledLabel(getHelpText(), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .7f);
        multiplayerJoinRoomHelp2.setWrap(true);
        menuTable.add(multiplayerJoinRoomHelp2).fill();
        menuTable.row();
        menuTable.add(moreInformation).fillX();
        addFocusableActor(moreInformation);
    }

    protected abstract String getHelpText();

    protected abstract String getTitle();

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
