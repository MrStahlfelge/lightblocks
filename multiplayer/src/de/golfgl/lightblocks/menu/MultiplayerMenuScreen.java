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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
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
    private Button startGameButton;
    private BeginningLevelChooser beginningLevelSlider;
    private InputButtonTable inputButtonTable;
    private Cell mainCell;
    private MultiplayerMatch matchStats = new MultiplayerMatch();
    private HashMap<String, boolean[]> availablePlayerInputs = new HashMap<String, boolean[]>();
    private boolean hasToRefresh = false;
    private ChangeListener gameParameterListener;
    private Button shareAppButton;
    private boolean screenNotActive = false;
    private PagedScrollPane modePager;
    private PagedScrollPane.PageIndicator pageIndicator;

    public MultiplayerMenuScreen(MultiplayerLightblocks app, Actor actorToHide) {
        super(app, actorToHide);
    }

    public MultiplayerLightblocks getApp() {
        return (MultiplayerLightblocks) app;
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

        pageIndicator = modePager.getPageIndicator();
        buttons.add(pageIndicator)
                .minWidth(modePager.getPageIndicator().getPrefWidth() * 2)
                .uniform(false, false);

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

        inputButtonTable = new InputButtonTable(app, PlayScreenInput.KEY_INPUTTYPE_MIN);
        inputButtonTable.setAutoEnableGamepad(false);
        addFocusableActor(inputButtonTable);

        beginningLevelSlider.addListener(gameParameterListener);
        inputButtonTable.setExternalChangeListener(gameParameterListener);

        setOpenJoinRoomButtons();

        validate();
        modePager.scrollToPage(app.localPrefs.getLastMultiPlayerMenuPage());
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

        modePager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        modePager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == modePager && getStage() != null) {
                    ((MyStage) getStage()).setFocusedActor(((IMultiplayerModePage) modePager.getCurrentPage())
                            .getDefaultActor());
                    app.localPrefs.saveLastUsedMultiPlayerMenuPage(modePager.getCurrentPageIndex());
                }
            }
        });
        modePager.addPage(new LocalGameTable());

        if (app.gpgsClient != null && app.gpgsClient instanceof IMultiplayerGsClient)
            modePager.addPage(new GpgsGameTable());

        mainCell = menuTable.add(modePager).fill().expand();

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
        initGameParametersParams.setGameMode(InitGameParameters.GameMode.Multiplayer);

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
                        && app.localPrefs.isPlaySounds())
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
                    int chosenInput = ((MultiPlayerObjects.GameParameters) o).chosenInput;

                    if (PlayScreenInput.isInputTypeAvailable(chosenInput)) {
                        inputButtonTable.setInputChecked(chosenInput);
                        inputButtonTable.setAllDisabledButSelected();
                    } else
                        inputButtonTable.resetEnabledInputs();
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

        Label messageLabel = new ScaledLabel(app.TEXTS.get("labelMultiplayerConnecting"), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE);
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

        // checken ob mindestens ein gemeinsamer Inputtyp verfügbar ist
        boolean hasEnabledOne = false;
        for (int i = 0; i < allSupportedInputs.length; i++)
            hasEnabledOne = hasEnabledOne || allSupportedInputs[i];

        // wenn es eh keinen gemeinsamen gibt, dann zurückfallen alle verfügbaren zu aktivieren
        if (hasEnabledOne)
            inputButtonTable.setEnabledInputs(allSupportedInputs);
        else
            inputButtonTable.resetEnabledInputs();
    }

    protected void refreshPlayerList() {
        final Actor newActor;
        Actor toFocus;

        if (app.getScreen() != app.mainMenuScreen)
            hasToRefresh = true;

        if (matchStats.getNumberOfPlayers() == 0) {
            newActor = modePager;
            toFocus = ((IMultiplayerModePage) modePager.getCurrentPage()).getDefaultActor();
            pageIndicator.setVisible(true);
        } else {
            RoomTable playersTable = new RoomTable();
            newActor = playersTable;
            toFocus = playersTable.getDefaultActor();
            pageIndicator.setVisible(false);
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
        return ((IMultiplayerModePage) modePager.getCurrentPage()).getDefaultActor();
    }

    private interface IMultiplayerModePage {
        Actor getDefaultActor();
    }

    private class RoomTable extends Table {
        private Actor defaultActor;

        public RoomTable() {
            defaults().pad(5);

            Table playerStats = new Table();
            playerStats.defaults().pad(0, 5, 0, 5);
            playerStats.add();
            playerStats.add(new ScaledLabel("#OP", app.skin, LightBlocksGame.SKIN_FONT_BIG)).right();
            playerStats.add(new ScaledLabel(app.TEXTS.get("labelTotalScores"),
                    app.skin, LightBlocksGame.SKIN_FONT_BIG)).right();

            for (String playerId : matchStats.getPlayers()) {
                playerStats.row();
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

                playerStats.add(playerIdLabel).width(LightBlocksGame.nativeGameWidth * .33f).left().expandX();
                playerStats.add(playerOutplaysLabel).right();
                playerStats.add(playerScoreLabel).right();

            }

            add(playerStats).pad(20);

            Actor toAdd;

            if (app.multiRoom != null && !app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState
                    .closed)) {
                if (app.multiRoom.getNumberOfPlayers() < 2) {
                    String labelText = app.TEXTS.get("multiplayerJoinNotEnoughPlayers");

                    if (app.multiRoom.isLocalGame()) {
                        String ipAddress = getApp().netUtils.getLocalIpAsString();
                        if (ipAddress.length() > 20)
                            ipAddress = "\n" + ipAddress;

                        labelText = labelText + "\n" + app.TEXTS.format("multiplayerJoinIpAddress", ipAddress);
                    }
                    toAdd = new ScaledLabel(labelText, app.skin, LightBlocksGame.SKIN_FONT_BIG, .75f);
                } else if (app.multiRoom.isOwner()) {
                    toAdd = startGameButton;
                    defaultActor = startGameButton;
                } else
                    toAdd = new ScaledLabel(app.TEXTS.get("multiplayerJoinWaitForStart"), app.skin,
                            LightBlocksGame.SKIN_FONT_BIG, .75f);
            } else
                toAdd = new ScaledLabel(app.TEXTS.get("multiplayerLanDisconnected"), app.skin,
                        LightBlocksGame.SKIN_FONT_BIG, .75f);

            row().padTop(10);
            add(toAdd).minWidth(150);

            if (app.multiRoom != null && !app.multiRoom.getRoomState().equals(MultiPlayerObjects.RoomState
                    .closed)) {
                row().padTop(25);
                add(new ScaledLabel(app.TEXTS.get("multiplayerRoundSettings"), app.skin, LightBlocksGame
                        .SKIN_FONT_TITLE)).colspan(3);
                row();
                add(beginningLevelSlider).colspan(3);
                row().padTop(5);
                add(inputButtonTable).colspan(3);

                beginningLevelSlider.setDisabled(!app.multiRoom.isOwner());
                if (defaultActor == null && app.multiRoom.isOwner())
                    defaultActor = beginningLevelSlider.getSlider();

                inputButtonTable.setAllDisabledButSelected();
            }
        }

        public Actor getDefaultActor() {
            return defaultActor;
        }
    }

    private class LocalGameTable extends Table implements IMultiplayerModePage {
        private TextButton openRoomButton;
        private TextButton joinRoomButton;

        public LocalGameTable() {
            Label lanHelp = new ScaledLabel(app.TEXTS.get("multiplayerLanHelp"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            lanHelp.setWrap(true);

            Table lanButtons = new Table();
            openRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerOpenRoom"),
                    app.skin);
            openRoomButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    buttonOpenLocalRoomPressed();
                }
            });
            joinRoomButton = new RoundedTextButton(app.TEXTS.get("labelMultiplayerJoinRoom"),
                    app.skin);
            joinRoomButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    joinLocalButtonPressed();
                }
            });
            lanButtons.row();
            lanButtons.add(openRoomButton);
            addFocusableActor(openRoomButton);
            lanButtons.row().padTop(10);

            lanButtons.add(joinRoomButton);
            addFocusableActor(joinRoomButton);

            if (getApp().netUtils.shouldShowAdvancedWifiSettings()) {
                Button openWifiSettings = new FaTextButton("Open Wifi settings", app.skin, "default");
                openWifiSettings.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        ((MultiplayerLightblocks) app).netUtils.showAdvancedWifiSettings();
                    }
                });

                lanButtons.row().padTop(10);
                lanButtons.add(openWifiSettings);
                addFocusableActor(openWifiSettings);
            }

            add(new ScaledLabel(app.TEXTS.get("labelMultiplayerLan"), app.skin, LightBlocksGame
                    .SKIN_FONT_TITLE, .8f));
            row();
            add(lanHelp).fill().expandX().pad(10, 20, 10, 20);
            row();
            add(lanButtons).expandY();
        }

        @Override
        public Actor getDefaultActor() {
            return openRoomButton;
        }
    }

    private class GpgsGameTable extends Table implements IMultiplayerModePage {
        private final Button gpgInviteButton;

        public GpgsGameTable() {
            Label gpgHelp = new ScaledLabel(app.TEXTS.get("multiplayerGpgHelp"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            gpgHelp.setWrap(true);

            Table gpgButtons = new Table();
            gpgInviteButton = new RoundedTextButton(app.TEXTS.get
                    ("menuInvitePlayers"), app.skin);
            gpgInviteButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (app.multiRoom == null || !app.multiRoom.isConnected())
                        initializeGpgsRoom();
                }
            });
            gpgButtons.add(gpgInviteButton);
            addFocusableActor(gpgInviteButton);

            Button gpgShowInvitationsButton = new RoundedTextButton(app.TEXTS.get
                    ("menuShowInvitations"), app.skin);
            gpgShowInvitationsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (checkNewGpgsConnPreConditions()) return;

                    joinGpgsButtonPressed();
                }
            });
            gpgButtons.row().padTop(10);
            gpgButtons.add(gpgShowInvitationsButton);
            addFocusableActor(gpgShowInvitationsButton);

            row();

            Table title = new Table();
            title.add(new ScaledLabel(FontAwesome.GPGS_LOGO, app.skin, FontAwesome.SKIN_FONT_FA)).padRight(5);
            title.add(new ScaledLabel(app.TEXTS.get("menuAccountGpgs"), app.skin, LightBlocksGame
                    .SKIN_FONT_TITLE, .8f));
            add(title);
            row();
            add(gpgHelp).fill().expandX().pad(20);
            row();
            add(gpgButtons).expandY();
        }

        @Override
        public Actor getDefaultActor() {
            return gpgInviteButton;
        }

    }
}
