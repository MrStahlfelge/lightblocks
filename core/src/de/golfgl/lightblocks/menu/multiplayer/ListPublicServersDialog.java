package de.golfgl.lightblocks.menu.multiplayer;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;

public abstract class ListPublicServersDialog extends ConnectServerDialog {

    private List<ServerAddress> lastShownAdressList;

    public ListPublicServersDialog(LightBlocksGame app) {
        super(app);
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("multiplayerListPublic");
    }

    @Override
    protected String getHelpText() {
        return app.TEXTS.get("multiplayerPublicServerHelp");
    }

    @Override
    protected void fillMenuTable() {
        lastShownAdressList = app.backendManager.getMultiplayerServerAddressList();
        if (lastShownAdressList != null) {
            super.fillMenuTable();

            hostList.setItems(app.backendManager.getMultiplayerServerAddressList().toArray(new ServerAddress[0]));
            roomSelect(hostList.getSelected());

        } else
            getContentTable().add(new ProgressDialog.WaitRotationImage(app));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (lastShownAdressList == null && app.backendManager.getMultiplayerServerAddressList() != null) {
            getContentTable().clear();
            fillMenuTable();
            if (getStage() != null) {
                pack();
                setPosition(Math.round((getStage().getWidth() - getWidth()) / 2), Math.round((getStage().getHeight() - getHeight()) / 2));
                ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
            }
        }
    }


}
