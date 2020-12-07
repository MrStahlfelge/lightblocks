package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TextInputDialog;
import de.golfgl.lightblocks.scene2d.TouchableList;

public class ServerMultiplayerPage extends Table implements MultiplayerMenuScreen.IMultiplayerModePage {
    private final TouchableList<ServerAddress> hostList;
    private final ControllerScrollPane hostListScrollPane;
    private final RoundedTextButton enterManually;
    private final LightBlocksGame app;
    private TextButton joinRoomButton;
    private float timeSinceRefresh;

    public ServerMultiplayerPage(final LightBlocksGame app, MultiplayerMenuScreen parent) {
        this.app = app;
        Label serverHelp = new ScaledLabel(app.TEXTS.get("multiplayerServerHelp"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
        serverHelp.setWrap(true);

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
                super.setup();
            }
        };
        parent.addFocusableActor(hostList);
        enterManually = new RoundedTextButton(app.TEXTS.get("multiplayerJoinManually"), app.skin);
        enterManually.getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
        parent.addFocusableActor(enterManually);

        hostListScrollPane = new ControllerScrollPane(hostList, app.skin, LightBlocksGame.SKIN_LIST);

        enterManually.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextInputDialog.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        ServerAddress newServer = new ServerAddress(text);
                        roomSelect(newServer);
                        ((MyStage) getStage()).setFocusedActor(joinRoomButton);
                    }

                    @Override
                    public void canceled() {
                        // do nothing
                    }
                }, app.TEXTS.get("multiplayerJoinManually"), "", app.skin, getStage());
            }
        });

        Table serverButtons = new Table();
        joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"),
                app.skin);
        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                IRoomLocation selected = hostList.getSelected();
                if (selected != null)
                    roomSelect(selected);
            }
        });
        serverButtons.row();
        serverButtons.add(joinRoomButton);
        serverButtons.row();
        serverButtons.add(enterManually);
        parent.addFocusableActor(joinRoomButton);

        add(new ScaledLabel(app.TEXTS.get("labelMultiplayerServer"), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .8f));
        row();
        add(serverHelp).fill().expandX().pad(10, 20, 10, 20);
        row();
        add(hostListScrollPane).minWidth(LightBlocksGame.nativeGameWidth * .75f).minHeight(LightBlocksGame
                .nativeGameHeight * .15f).pad(5).expand().fill();
        row();
        add(serverButtons);

        fillHostList();

        // TODO link How to add a server man page
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceRefresh = timeSinceRefresh + delta;

        if (timeSinceRefresh > 1f) {
            fillHostList();
        }
    }

    private void fillHostList() {
        Array<ServerAddress> servers = new Array<>();

        ServerAddress selected = hostList.getSelected();
        int selectedIndex = hostList.getSelectedIndex();

        List<ServerAddress> discoveredMultiplayerServers = (app.nsdHelper != null) ?
                app.nsdHelper.getDiscoveredMultiplayerServers() : new ArrayList<ServerAddress>();

        // TODO only for testing at the moment
        discoveredMultiplayerServers.add(new ServerAddress("Heroku (US)", "lightblocks-server.herokuapp.com:80"));
        discoveredMultiplayerServers.add(new ServerAddress("Volume6 (DE)", "volume6.de:8080"));

        for (ServerAddress discoveredMultiplayerServer : discoveredMultiplayerServers) {
            servers.add(discoveredMultiplayerServer);
            if (discoveredMultiplayerServer.equals(selected))
                selectedIndex = servers.size - 1;
        }
        hostList.setItems(servers);
        if (selectedIndex >= 0)
            hostList.setSelectedIndex(selectedIndex);

        timeSinceRefresh = 0;
    }

    private void roomSelect(IRoomLocation address) {
        new ServerLobbyScreen(app, address.getRoomAddress()).show(getStage());
    }

    @Override
    public Actor getDefaultActor() {
        return joinRoomButton;
    }

    @Override
    public Actor getSecondMenuButton() {
        return null;
    }

    @Override
    protected void setParent(Group parent) {
        if (parent != getParent() && app.nsdHelper != null) {
            // FIXME - stop is not fired at all because page is not removed
            if (parent != null) {
                app.nsdHelper.startDiscovery(false);
            } else {
                app.nsdHelper.stopDiscovery();
            }
        }

        super.setParent(parent);
    }
}
