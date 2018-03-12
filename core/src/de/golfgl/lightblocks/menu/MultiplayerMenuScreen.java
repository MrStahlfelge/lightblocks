package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.KryonetMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.MultiplayerPlayScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.MultiplayerMatch;

/**
 * Multiplayer Screen where players fill rooms to play
 * <p>
 * Created by Benjamin Schulte on 24.02.2017.
 */

public class MultiplayerMenuScreen extends AbstractMenuDialog implements IRoomListener {

    protected Dialog waitForConnectionOverlay;
    private GlowLabelButton openRoomButton;
    private GlowLabelButton joinRoomButton;
    private Button startGameButton;
    private BeginningLevelChooser beginningLevelSlider;
    private InputButtonTable inputButtonTable;
    private Table initGameScreen;
    private Cell mainCell;
    private MultiplayerMatch matchStats = new MultiplayerMatch();
    private HashMap<String, boolean[]> availablePlayerInputs = new HashMap<String, boolean[]>();
    private boolean hasToRefresh = false;
    private ChangeListener gameParameterListener;
    private Table lanButtons;
    private Table gpgButtons;
    private GlowLabelButton gpgShowInvitationsButton;
    private GlowLabelButton gpgInviteButton;
    private Button shareAppButton;
    private boolean screenNotActive = false;

    public MultiplayerMenuScreen(LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected void result(Object object) {
        // der Leave-Button wurde gedrückt
        if (app.multiRoom != null && app.multiRoom.isConnected()) {
            leaveCurrentRoom();
            cancel();
        }
    }

    private void leaveCurrentRoom() {
        if (app.multiRoom == null || !app.multiRoom.isConnected())
            return;

        // die verfügbaren Buttons abgehen
        if (!app.multiRoom.isLocalGame()) {
            try {
                app.multiRoom.leaveRoom(true);
            } catch (VetoException e) {
                // eat
            }
        } else if (app.multiRoom.isOwner())
            buttonOpenLocalRoomPressed();
        else
            joinLocalButtonPressed();
    }

    @Override
    public void hide(Action action) {
        // und weg mit dem Zeug
        app.multiRoom = null;

        super.hide(action);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        //den folgenden Code nur ausführen, wenn wir aus dem PlayScreen zurück kommen
        if (!screenNotActive)
            return;

        screenNotActive = false;

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
        super.fillButtonTable(buttons);

        shareAppButton = new ShareButton(app);

        buttons.add(shareAppButton);
        addFocusableActor(shareAppButton);

        // Die folgenden Elemente sind nicht in der Buttontable, aber die Initialisierung hier macht Sinn

        startGameButton = new PlayButton(app);
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
        addFocusableActor(startGameButton);

        beginningLevelSlider = new BeginningLevelChooser(app, 0, 9);
        addFocusableActor(beginningLevelSlider.getSlider());

        gameParameterListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom != null && app.multiRoom.isOwner()) {
                    MultiPlayerObjects.GameParameters gp = new MultiPlayerObjects.GameParameters();
                    gp.beginningLevel = beginningLevelSlider.getValue();
                    gp.chosenInput = inputButtonTable.getSelectedInput();

                    app.multiRoom.sendToAllPlayers(gp);
                }
            }
        };

        inputButtonTable = new InputButtonTable(app, PlayScreenInput.KEY_INPUTTYPE_ALLAVAIL);
        addFocusableActor(inputButtonTable);

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

        Label lanHelp = new ScaledLabel(app.TEXTS.get("multiplayerLanHelp"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
        lanHelp.setWrap(true);

        Label gpgHelp = new ScaledLabel(app.TEXTS.get("multiplayerGpgHelp"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
        gpgHelp.setWrap(true);

        gpgButtons = new Table();
        gpgInviteButton = new GlowLabelButton(app.TEXTS.get
                ("menuInvitePlayers"), app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
        gpgInviteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (app.multiRoom == null || !app.multiRoom.isConnected())
                    initializeGpgsRoom();
            }
        });
        gpgButtons.add(gpgInviteButton);
        addFocusableActor(gpgInviteButton);

        gpgShowInvitationsButton = new GlowLabelButton(app.TEXTS.get
                ("menuShowInvitations"), app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
        gpgShowInvitationsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (checkNewGpgsConnPreConditions()) return;

                joinGpgsButtonPressed();
            }
        });
        gpgButtons.row();
        gpgButtons.add(gpgShowInvitationsButton);
        addFocusableActor(gpgShowInvitationsButton);

        lanButtons = new Table();
        openRoomButton = new GlowLabelButton(app.TEXTS.get("labelMultiplayerOpenRoom"),
                app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
        openRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonOpenLocalRoomPressed();
            }
        });
        joinRoomButton = new GlowLabelButton(app.TEXTS.get("labelMultiplayerJoinRoom"),
                app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
        joinRoomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                joinLocalButtonPressed();
            }
        });
        lanButtons.row();
        lanButtons.add(openRoomButton);
        addFocusableActor(openRoomButton);
        lanButtons.row();
        lanButtons.add(joinRoomButton);
        addFocusableActor(joinRoomButton);

        initGameScreen = new Table();

        if (app.gpgsClient != null && app.gpgsClient instanceof IMultiplayerGsClient) {
            initGameScreen.row();

            Table title = new Table();
            title.add(new ScaledLabel(FontAwesome.GPGS_LOGO, app.skin, FontAwesome.SKIN_FONT_FA));
            title.add(new ScaledLabel(app.TEXTS.get("menuAccountGpgs"), app.skin, LightBlocksGame
                    .SKIN_FONT_TITLE));
            initGameScreen.add(title);
            initGameScreen.row();
            initGameScreen.add(gpgHelp).fill().minWidth(LightBlocksGame.nativeGameWidth * .9f);
            initGameScreen.row();
            initGameScreen.add(gpgButtons);
        }

        initGameScreen.row().padTop(30);
        initGameScreen.add(new ScaledLabel(app.TEXTS.get("labelMultiplayerLan"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE));
        initGameScreen.row();
        initGameScreen.add(lanHelp).fill().minWidth(LightBlocksGame.nativeGameWidth * .9f);
        initGameScreen.row();
        initGameScreen.add(lanButtons);

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

    private void confirmForcedRoomClose() {
        // mehr als ein Spieler - dann nachfragen ob wirklich geschlossen werden soll
        ((AbstractScreen) app.getScreen()).showConfirmationDialog(app.TEXTS.get("multiplayerDisconnectClients"),
                new Runnable() {
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
            app.multiRoom = ((IMultiplayerGsClient) app.gpgsClient).createMultiPlayerRoom();
            app.multiRoom.addListener(this);
            app.multiRoom.openRoom(app.player);
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    private boolean checkNewGpgsConnPreConditions() {
        if (app.gpgsClient == null || !app.gpgsClient.isSessionActive()) {
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
        ((GlowLabelButton) getLeaveButton()).setFaText(app.multiRoom == null || !app.multiRoom.isConnected() ?
                FontAwesome.LEFT_ARROW : FontAwesome.MISC_CROSS);
    }

    protected void joinGpgsButtonPressed() {
        try {
            app.multiRoom = ((IMultiplayerGsClient) app.gpgsClient).createMultiPlayerRoom();
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
                new MultiplayerJoinRoomScreen(app).show(getStage());
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

        initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
        initGameParametersParams.setInputKey(inputButtonTable.getSelectedInput());
        initGameParametersParams.setMultiplayerRoom(app.multiRoom);

        try {
            MultiplayerPlayScreen mps = (MultiplayerPlayScreen) PlayScreen.gotoPlayScreen(
                    ((AbstractScreen) app.getScreen()), initGameParametersParams);
            screenNotActive = true;
            ((MultiplayerModel) mps.gameModel).setMatchStats(matchStats);

            app.multiRoom.addListener(mps);
            app.multiRoom.gameModelStarted();

            // Achievements
            if (app.gpgsClient != null && app.gpgsClient.isSessionActive()) {
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
                    if (app.getScreen() == app.mainMenuScreen)
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
        contentTable.add(messageLabel).width(.75f * getStage().getWidth());

        waitForConnectionOverlay.show(getStage());
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
        Actor toFocus = null;

        if (app.getScreen() != app.mainMenuScreen)
            hasToRefresh = true;

        if (matchStats.getNumberOfPlayers() == 0) {
            newActor = initGameScreen;
            toFocus = openRoomButton;
        } else {
            Table playersTable = new Table();

            playersTable.defaults().pad(5);

            playersTable.add();
            playersTable.add(new ScaledLabel("#OP", app.skin, LightBlocksGame.SKIN_FONT_BIG)).right();
            playersTable.add(new ScaledLabel(app.TEXTS.get("labelTotalScores"),
                    app.skin, LightBlocksGame.SKIN_FONT_BIG)).right();

            for (String playerId : matchStats.getPlayers()) {
                playersTable.row();
                final MultiplayerMatch.PlayerStat playerStat = matchStats.getPlayerStat(playerId);

                Color lineColor;

                if (!playerStat.isPresent())
                    lineColor = new Color(LightBlocksGame.COLOR_DISABLED);
                else if (app.multiRoom == null || !playerId.equals(app.multiRoom.getMyPlayerId()))
                    lineColor = new Color(LightBlocksGame.COLOR_UNSELECTED);
                else
                    lineColor = Color.WHITE;

                final Label playerIdLabel = new ScaledLabel(playerId, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
                playerIdLabel.setEllipsis(true);
                Label playerOutplaysLabel = new ScaledLabel(String.valueOf(playerStat.getNumberOutplays()), app.skin,
                        LightBlocksGame.SKIN_FONT_TITLE);
                Label playerScoreLabel = new ScaledLabel(String.valueOf(playerStat.getTotalScore()), app.skin,
                        LightBlocksGame.SKIN_FONT_TITLE);

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
                    toAdd = new ScaledLabel(app.TEXTS.get("multiplayerJoinNotEnoughPlayers"), app.skin,
                            LightBlocksGame.SKIN_FONT_BIG, .75f);
                } else if (app.multiRoom.isOwner()) {
                    toAdd = startGameButton;
                    toFocus = startGameButton;
                } else
                    toAdd = new ScaledLabel(app.TEXTS.get("multiplayerJoinWaitForStart"), app.skin,
                            LightBlocksGame.SKIN_FONT_BIG, .75f);
            } else
                toAdd = new ScaledLabel(app.TEXTS.get("multiplayerLanDisconnected"), app.skin,
                        LightBlocksGame.SKIN_FONT_BIG, .75f);

            playersTable.row().padTop(30);
            playersTable.add(toAdd).colspan(3).minWidth(150);

            if (app.multiRoom != null && !app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState
                    .closed)) {
                playersTable.row().padTop(25);
                playersTable.add(new Label(app.TEXTS.get("multiplayerRoundSettings"), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG)).colspan(3);
                playersTable.row();
                playersTable.add(beginningLevelSlider).colspan(3);
                playersTable.row().padTop(5);
                playersTable.add(inputButtonTable).colspan(3);

                beginningLevelSlider.setDisabled(!app.multiRoom.isOwner());
                if (toFocus == null && app.multiRoom.isOwner())
                    toFocus = beginningLevelSlider.getSlider();

                inputButtonTable.setAllDisabledButSelected();
            }

            newActor = playersTable;
        }

        mainCell.setActor(newActor);
        if (toFocus == null)
            toFocus = getLeaveButton();

        ((MyStage) getStage()).setFocusedActor(toFocus);

        hasToRefresh = false;
    }

    private void showDialog(String message) {
        ((AbstractScreen) app.getScreen()).showDialog(message);
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return openRoomButton;
    }
}
