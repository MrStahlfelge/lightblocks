package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.GlowLabelButton;

/**
 * Screen for player's account: Scores, Sign in/out a.s.o.
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class PlayerAccountMenuScreen extends AbstractMenuDialog {

    private Label signInState;
    private TextButton logInOutButton;
    private GlowLabelButton leaderboardButton;
    private GlowLabelButton achievementsButton;

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
        return app.TEXTS.get("menuAccount");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        logInOutButton = new TextButton("", app.skin, "round");
        logInOutButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           performGpgsLoginout();
                                       }
                                   }
        );

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
        signInState = new Label(app.player.getGamerId(), app.skin, app.SKIN_FONT_BIG);
        signInState.setAlignment(Align.center);
        signInState.setWrap(true);
        leaderboardButton = new GlowLabelButton(FontAwesome.GPGS_LEADERBOARD, "Leader Boards", app.skin,
                .5f, GlowLabelButton.SMALL_SCALE_MENU);
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

        achievementsButton = new GlowLabelButton(FontAwesome.GPGS_ACHIEVEMENT, "Achievements", app.skin,
                .5f, GlowLabelButton.SMALL_SCALE_MENU);
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

        menuTable.row();
        menuTable.add(signInState).minHeight(120).fill();

        menuTable.row().padTop(30);
        menuTable.add(leaderboardButton);
        buttonsToAdd.add(leaderboardButton);
        menuTable.row();
        menuTable.add(achievementsButton);
        buttonsToAdd.add(achievementsButton);

        menuTable.row();
        menuTable.add().expandY();
    }

    private void refreshAccountChanged() {
        final boolean gpgsConnected = app.gpgsClient != null && app.gpgsClient.isConnected();
        final boolean gpgsConnectPending = app.gpgsClient != null && app.gpgsClient.isConnectionPending();
        //logInOutButton.setFaText(getLogInOutIcon(gpgsConnected));
        if (gpgsConnected) {
            logInOutButton.setText(app.TEXTS.get("menuSignOut"));
            signInState.setText(app.TEXTS.format("menuGPGSAccount", app.player.getName()));
        } else {
            logInOutButton.setText(app.TEXTS.get(gpgsConnectPending ? "menuGPGSConnecting" : "menuSignInGPGS"));
            signInState.setText(app.TEXTS.format("menuLocalAccount", app.player.getName()));
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
