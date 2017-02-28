package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.KryonetMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scenes.FATextButton;

/**
 * Multiplayer Screen where players fill rooms to play
 * <p>
 * Created by Benjamin Schulte on 24.02.2017.
 */

public class MultiplayerMenuScreen extends AbstractMenuScreen implements IRoomListener {

    private FATextButton openRoomButton;
    private FATextButton joinRoomButton;
    private Label lanHelp;
    private Cell mainCell;

    public MultiplayerMenuScreen(LightBlocksGame app) {
        super(app);

        initializeUI();

    }

    @Override
    protected void goBackToMenu() {
        if (app.multiRoom != null && app.multiRoom.isConnected())
            showDialog("You are still member of a room. Please leave it first.");
        else
            super.goBackToMenu();
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        openRoomButton = new FATextButton("", "", app.skin);
        openRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonOpenRoomPressed();
            }
        });
        joinRoomButton = new FATextButton("", "", app.skin);
        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                joinButtonPressed();
            }
        });

        setOpenJoinRoomButtons();

        buttons.add(openRoomButton).uniform();
        buttons.add(joinRoomButton).uniform();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PEOPLE;
    }

    @Override
    protected String getSubtitle() {
        return app.TEXTS.get("labelMultiplayerLan");
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMultiplayerButton");
    }

    @Override
    protected void fillMenuTable(Table menuTable) {

        lanHelp = new Label(app.TEXTS.get("multiplayerLanHelp"), app.skin);
        lanHelp.setWrap(true);


        mainCell = menuTable.add(lanHelp).fill().minWidth(LightBlocksGame.nativeGameWidth * .75f)
                .minHeight(LightBlocksGame.nativeGameHeight * .5f);

    }

    protected void buttonOpenRoomPressed() {
        try {
            if (app.multiRoom != null && app.multiRoom.isConnected()) {
                app.multiRoom.closeRoom(false);
                app.multiRoom = null;
            } else {
                initializeKryonetRoom();
                app.multiRoom.openRoom(app.player);
            }

        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    private void initializeKryonetRoom() {
        final KryonetMultiplayerRoom kryonetRoom = new KryonetMultiplayerRoom();
        kryonetRoom.setNsdHelper(app.nsdHelper);
        kryonetRoom.addListener(this);
        app.multiRoom = kryonetRoom;
    }

    private void setOpenJoinRoomButtons() {
        if (app.multiRoom == null || !app.multiRoom.isConnected()) {
            openRoomButton.setText(app.TEXTS.get("labelMultiplayerOpenRoom"));
            openRoomButton.getFaLabel().setText(FontAwesome.NET_SQUARELINK);
            joinRoomButton.setText(app.TEXTS.get("labelMultiplayerJoinRoom"));
            joinRoomButton.getFaLabel().setText(FontAwesome.NET_LOGIN);

            joinRoomButton.setDisabled(false);
            openRoomButton.setDisabled(false);
        } else {
            openRoomButton.setText(app.TEXTS.get("labelMultiplayerCloseRoom"));
            openRoomButton.getFaLabel().setText(FontAwesome.MISC_CROSS);
            joinRoomButton.setText(app.TEXTS.get("labelMultiplayerLeaveRoom"));
            joinRoomButton.getFaLabel().setText(FontAwesome.NET_LOGOUT);

            openRoomButton.setDisabled(!app.multiRoom.isOwner());
            joinRoomButton.setDisabled(app.multiRoom.isOwner());
        }
    }

    protected void joinButtonPressed() {
        try {
            if (app.multiRoom != null && app.multiRoom.isConnected()) {
                app.multiRoom.leaveRoom(true);
                app.multiRoom = null;
            } else {
                initializeKryonetRoom();
                final MultiplayerJoinRoomScreen joinScreen = new MultiplayerJoinRoomScreen(app);
                joinScreen.setBackScreen(this);
                joinScreen.initializeUI();
                app.setScreen(joinScreen);
            }

        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }


    @Override
    public void multiPlayerRoomStateChanged(final AbstractMultiplayerRoom.RoomState roomState) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                setOpenJoinRoomButtons();

                // wenn raus, dann playerlist neu machen
                if (roomState == AbstractMultiplayerRoom.RoomState.closed)
                    refreshPlayerList();

            }
        });

    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(final MultiPlayerObjects.PlayerChanged mpo) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                refreshPlayerList();
            }
        });
    }

    @Override
    public void multiPlayerGotErrorMessage(Object o) {
        // Got an error message from networking
    }

    protected void refreshPlayerList() {
        final Actor newActor;
        if (app.multiRoom == null || app.multiRoom.getNumberOfPlayers() == 0)
            newActor = lanHelp;
        else {
            Table playersTable = new Table();

            for (String player : app.multiRoom.getPlayers()) {
                playersTable.row();
                String playerLabel = player;
                if (player.equals(app.multiRoom.getMyPlayerId()))
                    playerLabel = "* " + playerLabel;

                playersTable.add(new Label(playerLabel, app.skin, LightBlocksGame.SKIN_FONT_BIG)).minWidth
                        (LightBlocksGame.nativeGameWidth * .5f);
            }

            newActor = playersTable;
        }

        mainCell.setActor(newActor);
    }
}
