package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import de.golfgl.lightblocks.scene2d.FaRadioButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TextInputDialog;
import de.golfgl.lightblocks.scene2d.VetoDialog;
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
            contentCell.setActor(new LobbyTable()).expandX().fill();
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

    private void startGame(String gameMode) {
        try {
            InitGameParameters igp = new InitGameParameters();
            igp.setGameMode(InitGameParameters.GameMode.ServerMultiplayer);
            igp.setServerMultiplayerManager(serverMultiplayerManager);
            serverMultiplayerManager.setGameMode(gameMode);
            PlayScreen.gotoPlayScreen(app, igp);

        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, LightBlocksGame.nativeGameWidth * .75f).show(getStage());
        }
    }

    private class LobbyTable extends Table {

        private final Cell<Label> pingCell;
        private final Cell<Label> playersCell;
        private final RepeatAction pingWarningAction;
        private final FaRadioButton<String> gameModeList;
        private final Label pingWarningLabel;
        private int lastShownPing;

        public LobbyTable() {

            pingWarningAction = Actions.forever(Actions.sequence(Actions.delay(.4f),
                    Actions.fadeOut(.4f, Interpolation.fade), Actions.fadeIn(.4f, Interpolation.fade)));
            Table serverInfoTable = new Table(app.skin);

            ServerModels.ServerInfo serverInfo = serverMultiplayerManager.getServerInfo();

            serverInfoTable.defaults().padRight(5).padLeft(5);
            serverInfoTable.add("Server: ").right();
            String name = serverInfo.name;
            serverInfoTable.add(name.length() <= 25 ? name : name.substring(0, 23) + "...").left();
            serverInfoTable.row();
            serverInfoTable.add("Ping: ").right();
            Table pingTable = new Table(app.skin);
            pingCell = pingTable.add("").minWidth(50);
            pingWarningLabel = pingTable.add(app.TEXTS.get("highPingWarning")).padLeft(10).getActor();
            pingWarningLabel.setColor(LightBlocksGame.EMPHASIZE_COLOR);
            pingWarningLabel.addAction(pingWarningAction);
            serverInfoTable.add(pingTable).left();
            serverInfoTable.row();
            serverInfoTable.add("Active: ").right();
            playersCell = serverInfoTable.add("").left();
            if (serverInfo.modes.size() == 1) {
                serverInfoTable.row();
                serverInfoTable.add("Type: ").right();
                serverInfoTable.add(serverInfo.modes.get(0).toUpperCase()).left();
            }

            add(serverInfoTable).expand();

            if (serverInfo.description != null) {
                Label description = new Label(serverInfo.description, app.skin, LightBlocksGame.SKIN_FONT_REG);
                description.setWrap(true);
                description.setAlignment(Align.center);
                row().expand().padTop(20).padBottom(20);
                add(description).colspan(2).fill();
            }

            if (serverInfo.modes.size() > 1) {
                gameModeList = new FaRadioButton<>(app.skin, false);
                for (String mode : serverInfo.modes) {
                    gameModeList.addEntry(mode, null, mode.toUpperCase());
                }
                row().padTop(30);
                add(new ScaledLabel(app.TEXTS.get("marathonChooseTypeLabel"),
                        app.skin, LightBlocksGame.SKIN_FONT_BIG));
                row().padBottom(30);
                add(gameModeList);
                addFocusableActor(gameModeList);
            } else {
                gameModeList = null;
            }

            if (serverInfo.privateRooms) {
                final String title = "Set private room passphrase";
                FaTextButton privateRoomButton = new FaTextButton(title, app.skin,
                        LightBlocksGame.SKIN_BUTTON_CHECKBOX);
                privateRoomButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        TextInputDialog.getTextInput(new Input.TextInputListener() {
                                                         @Override
                                                         public void input(final String text) {
                                                             serverMultiplayerManager.setRoomName(text);
                                                         }

                                                         @Override
                                                         public void canceled() {
                                                             // do nothing
                                                         }
                                                     }, title, serverMultiplayerManager.getRoomName(), app.skin,
                                getStage(), Input.OnscreenKeyboardType.Default);
                    }
                });
                privateRoomButton.getLabel().setFontScale(.55f);

                row().padBottom(30);
                add(privateRoomButton);
                addFocusableActor(privateRoomButton);
            }

            playButton = new PlayButton(app);
            row();
            add(playButton).minHeight(100);
            addFocusableActor(playButton);
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    startGame(gameModeList != null ? gameModeList.getValue() : null);
                }
            });
        }

        @Override
        public void act(float delta) {
            int lastPing = serverMultiplayerManager.getLastPingTime();
            if (lastPing != lastShownPing && lastPing >= 0) {
                Label pingLabel = pingCell.getActor();
                lastShownPing = lastPing;
                pingLabel.setText(String.valueOf(lastPing));
                boolean highPing = lastPing > 60;
                pingLabel.setColor(highPing ? LightBlocksGame.EMPHASIZE_COLOR : Color.WHITE);
                if (highPing && !pingWarningLabel.isVisible()) {
                    pingWarningAction.restart();
                }
                pingWarningLabel.setVisible(highPing);

                int activePlayers = serverMultiplayerManager.getServerInfo().activePlayers;
                playersCell.getActor().setText(activePlayers >= 0 ? activePlayers + " players" : "");

            }

            if (TimeUtils.millis() - lastDoPingTime >= 5000) {
                lastDoPingTime = TimeUtils.millis();
                serverMultiplayerManager.doPing();
            }
            super.act(delta);
        }
    }
}
