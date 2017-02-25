package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.KryonetMultiplayerRoom;

/**
 * Created by Benjamin Schulte on 24.02.2017.
 */

public class MultiplayerMenuScreen extends AbstractMenuScreen {
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
        final TextButton startServer = new TextButton("Start server", app.skin);
        startServer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom == null)
                    app.multiRoom = new KryonetMultiplayerRoom();

                try {
                    app.multiRoom.initializeRoom();
                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }
            }
        });

        final TextButton connectToServer = new TextButton("Connect", app.skin);
        connectToServer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom == null)
                    app.multiRoom = new KryonetMultiplayerRoom();

                try {
                    List<AbstractMultiplayerRoom.RoomLocation> rooms = app.multiRoom.searchRooms();

                    if (rooms == null || rooms.isEmpty())
                        throw new VetoException("No rooms found. :-(");

                    app.multiRoom.joinRoom(rooms.get(0));

                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }
            }
        });

        menuTable.add(startServer);
        menuTable.add(connectToServer);
    }


}
