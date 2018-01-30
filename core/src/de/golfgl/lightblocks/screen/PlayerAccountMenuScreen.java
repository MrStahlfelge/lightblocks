package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.RoundedTextButton;
import de.golfgl.lightblocks.scenes.ScaledLabel;

/**
 * Screen for player's account: Scores, Sign in/out a.s.o.
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class PlayerAccountMenuScreen extends AbstractMenuDialog {

    private TextButton logInOutButton;
    private Button leaderboardButton;
    private Button achievementsButton;

    public PlayerAccountMenuScreen(LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);

        refreshAccountChanged();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.GPGS_LOGO;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuAccountGpgs");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        buttons.add(logInOutButton).padLeft(50).minWidth(220);
        buttonsToAdd.add(logInOutButton);
    }

    private void performGpgsLoginout() {
        if (app.gpgsClient.isConnected()) {
            app.savegame.gpgsSaveGameState(true);
            app.setGpgsAutoLogin(false);
            app.gpgsClient.logOff();
            app.savegame.resetLoadedFromCloud();
        } else {
            app.gpgsClient.connect(false);
            refreshAccountChanged();
        }

        // Die AutoLogin Einstellung wird bei erfolgtem Connect wieder zurück gesetzt

    }

    protected void fillMenuTable(Table menuTable) {
        Label signInState = new ScaledLabel(app.TEXTS.get("menuGameService"), app.skin, app.SKIN_FONT_TITLE, LightBlocksGame.LABEL_SCALING);
        signInState.setAlignment(Align.center);
        signInState.setWrap(true);
        leaderboardButton = new RoundedTextButton("Leader Boards", app.skin);
        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showLeaderboards(null);
                } catch (GameServiceException e) {
                    //showDialog("Error showing leaderboards.");
                }
            }
        });

        achievementsButton = new RoundedTextButton("Achievements", app.skin);
        achievementsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showAchievements();
                } catch (GameServiceException e) {
                    //showDialog("Error showing achievements.");
                }
            }
        });

        logInOutButton = new RoundedTextButton("", app.skin);
        logInOutButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           performGpgsLoginout();
                                       }
                                   }
        );

        menuTable.row();
        menuTable.add(signInState).fill().expandX().pad(20);

        menuTable.row().padTop(30);
        menuTable.add(leaderboardButton);
        buttonsToAdd.add(leaderboardButton);
        menuTable.row();
        menuTable.add(achievementsButton);
        buttonsToAdd.add(achievementsButton);

    }

    private void refreshAccountChanged() {
        final boolean gpgsConnected = app.gpgsClient != null && app.gpgsClient.isConnected();
        final boolean gpgsConnectPending = app.gpgsClient != null && app.gpgsClient.isConnectionPending();
        //logInOutButton.setFaText(getLogInOutIcon(gpgsConnected));
        if (gpgsConnected) {
            logInOutButton.setText(app.TEXTS.get("menuSignOut"));
        } else {
            logInOutButton.setText(app.TEXTS.get(gpgsConnectPending ? "menuGPGSConnecting" : "menuSignIn"));
        }

        leaderboardButton.setDisabled(!gpgsConnected);
        achievementsButton.setDisabled(!gpgsConnected);
        logInOutButton.setDisabled(app.gpgsClient == null);
        // Achievements etc auch
    }

    private String getLogInOutIcon(boolean gpgsConnected) {
        return gpgsConnected ? FontAwesome.NET_LOGOUT : FontAwesome.NET_LOGIN;
    }

}
