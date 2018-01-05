package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.KryonetMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.scenes.InputButtonTable;
import de.golfgl.lightblocks.scenes.ScoreLabel;
import de.golfgl.lightblocks.scenes.ShareButton;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.MultiplayerMatch;

/**
 * Multiplayer Screen where players fill rooms to play
 * <p>
 * Created by Benjamin Schulte on 24.02.2017.
 */

public class MultiplayerMenuScreen extends AbstractMenuScreen implements IRoomListener {

    protected Dialog waitForConnectionOverlay;
    private FATextButton openRoomButton;
    private FATextButton joinRoomButton;
    private FATextButton startGameButton;
    private Slider beginningLevelSlider;
    private Table beginningLevelTable;
    private InputButtonTable inputButtonTable;
    private Table initGameScreen;
    private Cell mainCell;
    private MultiplayerMatch matchStats = new MultiplayerMatch();
    private HashMap<String, boolean[]> availablePlayerInputs = new HashMap<String, boolean[]>();
    private boolean hasToRefresh = false;
    private ChangeListener gameParameterListener;
    private Cell buttonTableCell;
    private Cell initScreenLanButtonCell;
    private Cell initScreenGpgButtonCell;
    private Table lanButtons;
    private Table gpgButtons;
    private FATextButton gpgShowInvitationsButton;
    private FATextButton gpgInviteButton;
    private FATextButton shareAppButton;

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
    public void dispose() {
        // und weg mit dem Zeug
        app.multiRoom = null;

        super.dispose();
    }

    @Override
    public void show() {

        super.show();

        if (hasToRefresh)
            refreshPlayerList();

        if (app.multiRoom != null && app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState.inGame))
            try {
                //TODO: Das darf nicht ausgelöst werden wenn 3 Spieler aktiv sind und die anderen beiden noch spielen
                app.multiRoom.gameStopped();
            } catch (VetoException e) {
                // eat
            }
    }

    @Override
    protected void fillButtonTable(Table buttons) {

        shareAppButton = new ShareButton(app);

        buttonTableCell = buttons.add(shareAppButton);

        // Die folgenden Elemente sind nicht in der Buttontable, aber die Initialisierung hier macht Sinn

        startGameButton = new FATextButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        startGameButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            try {
                                                app.multiRoom.startGame(false);
                                            } catch (VetoException e) {
                                                showDialog(e.getMessage());
                                            }
                                        }
                                    }
        );

        final Label beginningLevelLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        beginningLevelSlider = constructBeginningLevelSlider(beginningLevelLabel, 0, 5);

        gameParameterListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom != null && app.multiRoom.isOwner()) {
                    MultiPlayerObjects.GameParameters gp = new MultiPlayerObjects.GameParameters();
                    gp.beginningLevel = (int) beginningLevelSlider.getValue();
                    gp.chosenInput = inputButtonTable.getSelectedInput();

                    app.multiRoom.sendToAllPlayers(gp);
                }
            }
        };

        beginningLevelTable = new Table();
        beginningLevelTable.add(beginningLevelSlider).minHeight(30).minWidth(200).right().fill();
        beginningLevelTable.add(beginningLevelLabel).left().spaceLeft(10);

        inputButtonTable = new InputButtonTable(app, 0);

        beginningLevelSlider.addListener(gameParameterListener);
        inputButtonTable.setExternalChangeListener(gameParameterListener);

        setOpenJoinRoomButtons();

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

        Label lanHelp = new Label(app.TEXTS.get("multiplayerLanHelp"), app.skin);
        lanHelp.setWrap(true);

        Label gpgHelp = new Label(app.TEXTS.get("multiplayerGpgHelp"), app.skin);
        gpgHelp.setWrap(true);

        gpgButtons = new Table();
        gpgInviteButton = new FATextButton(FontAwesome.GPGS_LOGO, app.TEXTS.get
                ("menuInvitePlayers"), app.skin);
        gpgInviteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom != null && app.multiRoom.isConnected())
                    try {
                        app.multiRoom.leaveRoom(true);
                    } catch (VetoException e) {
                        // eat
                    }
                else
                    initializeGpgsRoom();
            }
        });
        gpgButtons.add(gpgInviteButton);

        gpgShowInvitationsButton = new FATextButton(FontAwesome.NET_LOGIN, app.TEXTS.get
                ("menuShowInvitations"), app.skin);
        gpgShowInvitationsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (checkNewGpgsConnPreConditions()) return;

                joinGpgsButtonPressed();
            }
        });
        gpgButtons.add(gpgShowInvitationsButton);

        lanButtons = new Table();
        openRoomButton = new FATextButton(FontAwesome.NET_SQUARELINK, app.TEXTS.get("labelMultiplayerOpenRoom"), app
                .skin);
        openRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonOpenLocalRoomPressed();
            }
        });
        joinRoomButton = new FATextButton(FontAwesome.NET_LOGIN, app.TEXTS.get("labelMultiplayerJoinRoom"), app.skin);
        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                joinLocalButtonPressed();
            }
        });
        lanButtons.row();
        lanButtons.add(openRoomButton).uniform().fill();
        lanButtons.add(joinRoomButton).uniform().fill();

        initGameScreen = new Table();
        initGameScreen.row();
        initGameScreen.add(new Label(app.TEXTS.get("menuSignInGPGS"), app.skin, LightBlocksGame.SKIN_FONT_BIG));
        initGameScreen.row();
        initScreenGpgButtonCell = initGameScreen.add(gpgButtons);
        initGameScreen.row();
        initGameScreen.add(gpgHelp).fill().minWidth(LightBlocksGame.nativeGameWidth * .9f);

        initGameScreen.row().padTop(30);
        initGameScreen.add(new Label(app.TEXTS.get("labelMultiplayerLan"), app.skin, LightBlocksGame.SKIN_FONT_BIG));
        initGameScreen.row();
        initScreenLanButtonCell = initGameScreen.add(lanButtons);
        initGameScreen.row();
        initGameScreen.add(lanHelp).fill().minWidth(LightBlocksGame.nativeGameWidth * .9f);

        mainCell = menuTable.add(initGameScreen);

    }

    protected void buttonOpenLocalRoomPressed() {
        try {
            if (app.multiRoom != null && app.multiRoom.isConnected()) {
                if (app.multiRoom.getNumberOfPlayers() > 1)
                    confirmForcedRoomClose();

                else {
                    app.multiRoom.closeRoom(false);
                    app.multiRoom = null;
                }
            } else {
                initializeKryonetRoom();
                app.multiRoom.openRoom(app.player);
            }

        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    private void setLocalMode() {
        initScreenGpgButtonCell.setActor(gpgButtons);
        buttonTableCell.setActor(lanButtons);
    }

    private void setInitMode() {
        initScreenGpgButtonCell.setActor(gpgButtons);
        initScreenLanButtonCell.setActor(lanButtons);
        buttonTableCell.setActor(shareAppButton);
    }

    private void setGpgsMode() {
        initScreenLanButtonCell.setActor(lanButtons);
        buttonTableCell.setActor(gpgButtons);
    }

    private void confirmForcedRoomClose() {
        // mehr als ein Spieler - dann nachfragen ob wirklich geschlossen werden soll
        showConfirmationDialog(app.TEXTS.get("multiplayerDisconnectClients"), new Runnable() {
            @Override
            public void run() {
                try {
                    app.multiRoom.closeRoom(true);
                    app.multiRoom = null;
                } catch (VetoException e) {
                    showDialog(e.getMessage());
                }
            }
        });
    }

    private void initializeKryonetRoom() {
        // falls schon matches gelaufen, dann zurücksetzen
        matchStats.clearStats();

        final KryonetMultiplayerRoom kryonetRoom = new KryonetMultiplayerRoom();
        kryonetRoom.setNsdHelper(app.nsdHelper);
        kryonetRoom.addListener(this);
        app.multiRoom = kryonetRoom;
    }

    protected void initializeGpgsRoom() {
        if (checkNewGpgsConnPreConditions()) return;

        // falls schon matches gelaufen, dann zurücksetzen
        matchStats.clearStats();

        try {
            app.multiRoom = app.gpgsClient.createMultiPlayerRoom();
            app.multiRoom.addListener(this);
            app.multiRoom.openRoom(app.player);
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    private boolean checkNewGpgsConnPreConditions() {
        if (app.gpgsClient == null || !app.gpgsClient.isConnected()) {
            showDialog(app.TEXTS.get("labelFirstSignIn"));
            return true;
        }

        if (app.multiRoom != null && app.multiRoom.isConnected()) {
            showDialog("You are already in a multiplayer room. Please leave first.");
            return true;
        }
        return false;
    }

    private void setOpenJoinRoomButtons() {
        if (app.multiRoom == null || !app.multiRoom.isConnected()) {
            openRoomButton.setText(app.TEXTS.get("labelMultiplayerOpenRoom"));
            openRoomButton.getFaLabel().setText(FontAwesome.NET_SQUARELINK);
            joinRoomButton.setText(app.TEXTS.get("labelMultiplayerJoinRoom"));
            joinRoomButton.getFaLabel().setText(FontAwesome.NET_LOGIN);

            joinRoomButton.setDisabled(false);
            openRoomButton.setDisabled(false);

            gpgInviteButton.setText(app.TEXTS.get("menuInvitePlayers"));
            gpgShowInvitationsButton.setDisabled(false);

        } else {
            openRoomButton.setText(app.TEXTS.get("labelMultiplayerCloseRoom"));
            openRoomButton.getFaLabel().setText(FontAwesome.MISC_CROSS);
            joinRoomButton.setText(app.TEXTS.get("labelMultiplayerLeaveRoom"));
            joinRoomButton.getFaLabel().setText(FontAwesome.NET_LOGOUT);

            openRoomButton.setDisabled(!app.multiRoom.isOwner());
            joinRoomButton.setDisabled(app.multiRoom.isOwner());

            gpgInviteButton.setText(app.TEXTS.get("labelMultiplayerLeaveRoom"));
            gpgShowInvitationsButton.setDisabled(true);

            if (app.multiRoom.getRoomState() == MultiPlayerObjects.RoomState.join) {
                if (app.multiRoom.isLocalGame())
                    setLocalMode();
                else
                    setGpgsMode();
            }
        }
    }

    protected void joinGpgsButtonPressed() {
        try {
            app.multiRoom = app.gpgsClient.createMultiPlayerRoom();
            app.multiRoom.addListener(this);
            app.multiRoom.startRoomDiscovery();
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    protected void joinLocalButtonPressed() {
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
    public void multiPlayerRoomStateChanged(final MultiPlayerObjects.RoomState roomState) {

        // Raum ist ins Spiel gewechselt
        if (roomState.equals(MultiPlayerObjects.RoomState.inGame))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    beginNewMultiplayerGame();
                }
            });

            // ansonsten entweder in Join gewechselt oder in Spiel
        else
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    hideOverlay();
                    setOpenJoinRoomButtons();

                    // wenn raus, dann playerlist neu machen
                    if (roomState == MultiPlayerObjects.RoomState.closed) {
                        if (matchStats.getNumberOfPlayers() == 1)
                            matchStats.clearStats();
                        else
                            for (String playerId : matchStats.getPlayers())
                                matchStats.getPlayerStat(playerId).setPresent(false);
                    }

                    refreshPlayerList();

                }
            });

    }

    private void beginNewMultiplayerGame() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameModelClass(MultiplayerModel.class);

        initGameParametersParams.setBeginningLevel((int) beginningLevelSlider.getValue());
        initGameParametersParams.setInputKey(inputButtonTable.getSelectedInput());
        initGameParametersParams.setMultiplayerRoom(app.multiRoom);

        try {
            MultiplayerPlayScreen mps = (MultiplayerPlayScreen) PlayScreen.gotoPlayScreen(this,
                    initGameParametersParams);
            mps.setBackScreen(this);
            ((MultiplayerModel) mps.gameModel).setMatchStats(matchStats);

            app.multiRoom.addListener(mps);
            app.multiRoom.gameModelStarted();

            // Achievements
            if (app.gpgsClient != null && app.gpgsClient.isConnected()) {
                if (app.multiRoom.getNumberOfPlayers() >= 3)
                    app.gpgsClient.unlockAchievement(GpgsHelper.ACH_MEGA_MULTI_PLAYER);

                // TODO dieses hier nicht bei Automatching!
                app.gpgsClient.unlockAchievement(GpgsHelper.ACH_FRIENDLY_MULTIPLAYER);
            }
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }

    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(final MultiPlayerObjects.PlayerChanged mpo) {

        // Liste aktualisieren
        synchronized (matchStats) {
            MultiplayerMatch.PlayerStat playerStat = matchStats.getPlayerStat(mpo.changedPlayer.name);
            playerStat.setPresent(!(mpo.changeType == MultiPlayerObjects.CHANGE_REMOVE));
        }

        // Refresh des UI lostreten
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if ((mpo.changeType == MultiPlayerObjects.CHANGE_ADD
                        || mpo.changeType == MultiPlayerObjects.CHANGE_REMOVE)
                        && app.isPlaySounds())
                    app.rotateSound.play();

                refreshPlayerList();
                if (app.multiRoom != null && app.multiRoom.isOwner())
                    refreshAvailableInputs();
            }
        });

        // Neuankömmling und ich bin der Host? Dann matchStats und gameParameters schicken
        if (mpo.changeType == MultiPlayerObjects.CHANGE_ADD && app.multiRoom.isOwner()) {
            // Das ginge theoretisch auch ohne neuen Thread. Aber dann ist das Problem dass bei LAN-Spiel der
            // Handshake noch nicht durch ist (wird erst nach Ausführen dieser Methode gemacht) und der Client
            // die Daten daher ablehnt
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // kurze Verzögerung, der Handshake soll zuerst kommen
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        // eat
                    }

                    synchronized (matchStats) {
                        for (String player : matchStats.getPlayers())
                            app.multiRoom.sendToPlayer(mpo.changedPlayer.name, matchStats.getPlayerStat(player)
                                    .toPlayerInMatch());
                    }

                    // Spielparameter schicken
                    gameParameterListener.changed(null, null);

                }
            }).start();

        }

    }

    @Override
    public void multiPlayerGotErrorMessage(final Object o) {
        // Got an error message from networking
        if (o instanceof MultiPlayerObjects.Handshake)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (app.getScreen() == MultiplayerMenuScreen.this)
                        showDialog("Error connecting with " + ((MultiPlayerObjects.Handshake) o).playerId + ": " +
                                ((MultiPlayerObjects.Handshake) o).message);
                }
            });

    }

    @Override
    public void multiPlayerGotModelMessage(Object o) {
        // interessiert mich nicht
    }

    @Override
    public void multiPlayerGotRoomMessage(final Object o) {
        if (o instanceof MultiPlayerObjects.PlayerInMatch) {
            synchronized (matchStats) {
                matchStats.getPlayerStat(((MultiPlayerObjects.PlayerInMatch) o).playerId).setFromPlayerInMatch(
                        (MultiPlayerObjects.PlayerInMatch) o);
            }

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    refreshPlayerList();
                }
            });
        }

        if (o instanceof MultiPlayerObjects.PlayerInRoom) {
            synchronized (availablePlayerInputs) {
                availablePlayerInputs.put(((MultiPlayerObjects.PlayerInRoom) o).playerId,
                        ((MultiPlayerObjects.PlayerInRoom) o).supportedInputTypes);

                if (app.multiRoom.isOwner())
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            refreshAvailableInputs();
                        }
                    });
            }
        }

        if (o instanceof MultiPlayerObjects.GameParameters && !app.multiRoom.isOwner()) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    beginningLevelSlider.setValue(((MultiPlayerObjects.GameParameters) o).beginningLevel);
                    inputButtonTable.setInputChecked(((MultiPlayerObjects.GameParameters) o).chosenInput);
                    inputButtonTable.setAllDisabledButSelected();
                }
            });

        }
    }

    @Override
    public void multiPlayerRoomEstablishingConnection() {
        showOverlay();
    }

    protected void showOverlay() {
        if (waitForConnectionOverlay != null)
            return;

        waitForConnectionOverlay = new Dialog("", app.skin);

        Label messageLabel = new Label(app.TEXTS.get("labelMultiplayerConnecting"), app.skin,
                LightBlocksGame.SKIN_FONT_BIG);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);

        final Table contentTable = waitForConnectionOverlay.getContentTable();
        contentTable.defaults().pad(15);
        contentTable.add(messageLabel).width(.75f * stage.getWidth());

        waitForConnectionOverlay.show(stage);
    }

    protected void hideOverlay() {
        if (waitForConnectionOverlay != null) {
            waitForConnectionOverlay.hide();
            waitForConnectionOverlay = null;
        }
    }

    private void refreshAvailableInputs() {
        // nur relevant für Owner => raus
        if (app.multiRoom == null || !app.multiRoom.isOwner())
            return;

        boolean[] allSupportedInputs = PlayScreenInput.getInputAvailableBitset();

        for (String playerId : app.multiRoom.getPlayers()) {

            boolean[] playerInputAvail = availablePlayerInputs.get(playerId);

            if (playerInputAvail != null)
                for (int i = 0; i < Math.min(allSupportedInputs.length, playerInputAvail.length); i++)
                    allSupportedInputs[i] = allSupportedInputs[i] && playerInputAvail[i];
        }

        for (int i = 0; i < allSupportedInputs.length; i++)
            inputButtonTable.setInputDisabled(i, !allSupportedInputs[i]);
    }

    protected void refreshPlayerList() {
        final Actor newActor;

        if (app.getScreen() != this)
            hasToRefresh = true;

        if (matchStats.getNumberOfPlayers() == 0) {
            newActor = initGameScreen;

            setInitMode();
        } else {
            Table playersTable = new Table();

            playersTable.defaults().pad(5);

            playersTable.add();
            playersTable.add(new Label("#OP", app.skin)).right();
            playersTable.add(new Label(app.TEXTS.get("labelTotalScores"), app.skin)).right();

            for (String playerId : matchStats.getPlayers()) {
                playersTable.row();
                final MultiplayerMatch.PlayerStat playerStat = matchStats.getPlayerStat(playerId);

                Color lineColor;

                if (!playerStat.isPresent())
                    lineColor = new Color(COLOR_TABLE_DEACTIVATED);
                else if (app.multiRoom == null || !playerId.equals(app.multiRoom.getMyPlayerId()))
                    lineColor = new Color(COLOR_TABLE_NORMAL);
                else
                    lineColor = new Color(COLOR_TABLE_HIGHLIGHTED);

                final Label playerIdLabel = new Label(playerId, app.skin, LightBlocksGame.SKIN_FONT_BIG);
                playerIdLabel.setEllipsis(true);
                Label playerOutplaysLabel = new ScoreLabel(1, playerStat.getNumberOutplays(), app.skin,
                        LightBlocksGame.SKIN_FONT_BIG);
                Label playerScoreLabel = new ScoreLabel(1, playerStat.getTotalScore(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG);

                playerIdLabel.setColor(lineColor);
                playerOutplaysLabel.setColor(lineColor);
                playerScoreLabel.setColor(lineColor);

                playersTable.add(playerIdLabel).width(LightBlocksGame.nativeGameWidth * .33f).left();
                playersTable.add(playerOutplaysLabel).right();
                playersTable.add(playerScoreLabel).right();

            }

            Actor toAdd;

            if (app.multiRoom != null && !app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState
                    .closed)) {
                if (app.multiRoom.getNumberOfPlayers() < 2) {
                    toAdd = new Label(app.TEXTS.get("multiplayerJoinNotEnoughPlayers"), app.skin);
                } else if (app.multiRoom.isOwner())
                    toAdd = startGameButton;
                else
                    toAdd = new Label(app.TEXTS.get("multiplayerJoinWaitForStart"), app.skin);
            } else
                toAdd = new Label(app.TEXTS.get("multiplayerLanDisconnected"), app.skin);

            playersTable.row().padTop(30);
            playersTable.add(toAdd).colspan(3).minWidth(150);

            if (app.multiRoom != null && !app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState
                    .closed)) {
                playersTable.row().padTop(25);
                playersTable.add(new Label(app.TEXTS.get("multiplayerRoundSettings"), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG)).colspan(3);
                playersTable.row();
                playersTable.add(beginningLevelTable).colspan(3);
                playersTable.row().padTop(5);
                playersTable.add(inputButtonTable).colspan(3);

                beginningLevelSlider.setDisabled(!app.multiRoom.isOwner());
                inputButtonTable.setAllDisabledButSelected();
            }

            newActor = playersTable;
        }

        mainCell.setActor(newActor);

        hasToRefresh = false;
    }
}
