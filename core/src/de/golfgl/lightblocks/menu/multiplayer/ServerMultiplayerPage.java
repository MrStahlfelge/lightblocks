package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.ServerAddress;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TextInputDialog;

public class ServerMultiplayerPage extends Table implements MultiplayerMenuScreen.IMultiplayerModePage {
    private final LightBlocksGame app;
    private final FaTextButton publicServersButton;
    private String lastManualAddress;

    public ServerMultiplayerPage(final LightBlocksGame app, MultiplayerMenuScreen parent) {
        this.app = app;
        Label serverHelp = new ScaledLabel(app.TEXTS.get("multiplayerServerHelp"), app.skin,
                LightBlocksGame.SKIN_FONT_REG, .75f);
        serverHelp.setWrap(true);

        Label betaFeature = new ScaledLabel(app.TEXTS.get("labelBetaFeature"), app.skin,
                LightBlocksGame.SKIN_FONT_REG);
        betaFeature.setWrap(true);
        betaFeature.setAlignment(Align.center);
        betaFeature.setColor(LightBlocksGame.EMPHASIZE_COLOR);

        FaTextButton enterManually = new FaTextButton(app.TEXTS.get("multiplayerJoinManually"), app.skin,
                LightBlocksGame.SKIN_BUTTON_CHECKBOX);
        enterManually.getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);

        enterManually.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextInputDialog.getTextInput(new Input.TextInputListener() {
                                                 @Override
                                                 public void input(String text) {
                                                     lastManualAddress = text;
                                                     ServerAddress newServer = new ServerAddress(text);
                                                     connectToServer(newServer);
                                                 }

                                                 @Override
                                                 public void canceled() {
                                                     // do nothing
                                                 }
                                             }, app.TEXTS.get("multiplayerJoinManually"), lastManualAddress != null ? lastManualAddress : "",
                        app.skin, getStage(), Input.OnscreenKeyboardType.Default);
            }
        });

        FaTextButton detectLocalServersButton = new FaTextButton(app.TEXTS.get("multiplayerSearchLocal"), app.skin,
                LightBlocksGame.SKIN_BUTTON_CHECKBOX);
        detectLocalServersButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new DetectLocalServersDialog(app) {

                    @Override
                    protected void joinRoom(ServerAddress lastSelectedRoom) {
                        hide(null); // needed for handling escape actor cleanly
                        connectToServer(lastSelectedRoom);
                    }
                }.show(getStage());
            }
        });

        publicServersButton = new FaTextButton(app.TEXTS.get("multiplayerListPublic"), app.skin,
                LightBlocksGame.SKIN_BUTTON_CHECKBOX);
        publicServersButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new ListPublicServersDialog(app) {
                    @Override
                    protected void joinRoom(ServerAddress lastSelectedRoom) {
                        hide(null); // needed for handling escape actor cleanly
                        connectToServer(lastSelectedRoom);
                    }
                }.show(getStage());
            }
        });

        Table serverButtons = new Table();
        serverButtons.defaults().pad(5);
        serverButtons.row();
        serverButtons.add(publicServersButton);
        if (app.nsdHelper != null) {
            serverButtons.row();
            serverButtons.add(detectLocalServersButton);
            parent.addFocusableActor(detectLocalServersButton);
        }
        serverButtons.row();
        serverButtons.add(enterManually);
        parent.addFocusableActor(publicServersButton);
        parent.addFocusableActor(enterManually);

        add(new ScaledLabel(app.TEXTS.get("labelMultiplayerServer"), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .8f));
        row();
        add(betaFeature).fill().expandX().padLeft(10).padRight(10);
        row();
        add(serverHelp).fill().expandX().pad(10, 20, 10, 20);
        row();
        add(serverButtons).fill().padLeft(20).padRight(20).expand();
    }

    private void connectToServer(IRoomLocation address) {
        new ServerLobbyScreen(app, address.getRoomAddress()).show(getStage());
    }

    @Override
    public Actor getDefaultActor() {
        return publicServersButton;
    }

    @Override
    public Actor getSecondMenuButton() {
        return null;
    }
}
