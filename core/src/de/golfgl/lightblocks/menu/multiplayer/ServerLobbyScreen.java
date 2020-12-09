package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.AbstractMenuDialog;
import de.golfgl.lightblocks.menu.PlayButton;
import de.golfgl.lightblocks.multiplayer.ServerModels;
import de.golfgl.lightblocks.multiplayer.ServerMultiplayerManager;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;

/**
 * Shows lobby of a multiplayer server
 */
public class ServerLobbyScreen extends AbstractFullScreenDialog {
    protected final ProgressDialog.WaitRotationImage waitRotationImage;
    protected final Cell contentCell;
    private final ServerMultiplayerManager serverMultiplayerManager;
    private boolean connecting;
    private long lastDoPingTime;
    private PlayButton playButton;

    public ServerLobbyScreen(LightBlocksGame app, String roomAddress) {
        super(app);
        waitRotationImage = new ProgressDialog.WaitRotationImage(app);
        closeButton.setFaText(FontAwesome.MISC_CROSS);

        // Fill Content
        fillFixContent();
        Table contentTable = getContentTable();
        contentTable.row();
        contentCell = contentTable.add().minHeight(waitRotationImage.getHeight() * 3);

        reload();
        serverMultiplayerManager = new ServerMultiplayerManager(app);
        serverMultiplayerManager.connect(roomAddress);
        connecting = true;
    }

    protected void fillFixContent() {
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PEOPLE, app.skin, FontAwesome.SKIN_FONT_FA));
    }

    protected void reload() {
        contentCell.setActor(waitRotationImage);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (connecting && !serverMultiplayerManager.isConnecting() && serverMultiplayerManager.getLastErrorMsg() != null) {
            fillErrorScreen(serverMultiplayerManager.getLastErrorMsg());
            connecting = false;
        } else if (connecting && serverMultiplayerManager.isConnected()) {
            connecting = false;
            lastDoPingTime = TimeUtils.millis();
            contentCell.setActor(new LobbyTable());
            ((MyStage) getStage()).setFocusedActor(playButton);
        } else if (!connecting && !serverMultiplayerManager.isConnected()) {
            if (serverMultiplayerManager.getLastErrorMsg() == null) {
                // hide without sound
                setOrigin(getWidth() / 2, getHeight() / 2);
                hide(parallel(Actions.scaleTo(1, 0, AbstractMenuDialog.TIME_SWOSHIN, Interpolation.circleIn),
                        Actions.fadeOut(AbstractMenuDialog.TIME_SWOSHIN, Interpolation.fade)));
            } else {
                fillErrorScreen(serverMultiplayerManager.getLastErrorMsg());
            }
        }

        if (playButton != null) {
            playButton.setVisible(serverMultiplayerManager.isConnected());
        }
    }

    protected void fillErrorScreen(String errorMessage) {
        Table errorTable = new Table();
        Label errorMsgLabel = new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        float noWrapHeight = errorMsgLabel.getPrefHeight();
        errorMsgLabel.setWrap(true);
        errorMsgLabel.setAlignment(Align.center);
        errorTable.add(errorMsgLabel).minHeight(noWrapHeight * 1.5f).fill()
                .minWidth(LightBlocksGame.nativeGameWidth - 50);

        contentCell.setActor(errorTable).fillX();
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (parent == null && serverMultiplayerManager.isConnected()) {
            serverMultiplayerManager.disconnect();
        }
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return playButton != null ? playButton : super.getConfiguredDefaultActor();
    }

    private void startGame() {
        try {
            InitGameParameters igp = new InitGameParameters();
            igp.setGameMode(InitGameParameters.GameMode.ServerMultiplayer);
            igp.setServerMultiplayerManager(serverMultiplayerManager);
            PlayScreen.gotoPlayScreen(app, igp);

        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, LightBlocksGame.nativeGameWidth * .75f).show(getStage());
        }
    }

    private class LobbyTable extends Table {

        private final Cell<Label> pingCell;

        public LobbyTable() {

            Table serverInfoTable = new Table(app.skin);

            ServerModels.ServerInfo serverInfo = serverMultiplayerManager.getServerInfo();

            serverInfoTable.defaults().padRight(5).padLeft(5);
            serverInfoTable.add("Server: ").right();
            serverInfoTable.add(serverInfo.name); // TODO ellipsis
            serverInfoTable.row();
            serverInfoTable.add("Version: ").right();
            serverInfoTable.add(String.valueOf(serverInfo.version)).left();
            serverInfoTable.row();
            serverInfoTable.add("Ping: ").right();
            pingCell = serverInfoTable.add("").left();

            add(serverInfoTable).expand();

            playButton = new PlayButton(app);
            row();
            add(playButton);
            addFocusableActor(playButton);
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (serverMultiplayerManager.getLastPingTime() > 60) {
                        Dialog dialog = new AbstractScreen.RunnableDialog(app.skin, app.TEXTS.get("highPingWarning"), new Runnable() {
                            @Override
                            public void run() {
                                startGame();
                            }
                        }, null,
                                app.TEXTS.get("menuYes"),
                                app.TEXTS.get("menuNo"));
                        dialog.show(getStage());
                    } else {
                        startGame();
                    }
                }
            });
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            int lastPing = serverMultiplayerManager.getLastPingTime();
            if (lastPing >= 0) {
                pingCell.getActor().setText(String.valueOf(lastPing));
                pingCell.getActor().setColor(lastPing > 50 ? LightBlocksGame.EMPHASIZE_COLOR : Color.WHITE);

                if (TimeUtils.millis() - lastDoPingTime >= 5000) {
                    lastDoPingTime = TimeUtils.millis();
                    serverMultiplayerManager.doPing();
                }
            }
        }
    }
}
