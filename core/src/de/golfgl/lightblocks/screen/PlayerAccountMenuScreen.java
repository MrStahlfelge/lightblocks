package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.FaButton;
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
        return app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                FontAwesome.GC_LOGO : FontAwesome.GPGS_LOGO;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get(app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                "menuAccountGc" : "menuAccountGpgs");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        // als erstes hinzufügen, damit Default
        buttonsToAdd.add(leaderboardButton);

        super.fillButtonTable(buttons);

        buttons.add(achievementsButton);
        buttonsToAdd.add(achievementsButton);
        buttons.add(leaderboardButton);

        // der hier nur Default, wenn nicht eingeloggt
        buttonsToAdd.add(logInOutButton);
        if (!app.gpgsClient.isConnected())
            buttonsToAdd.reverse();
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
        Label signInState = new ScaledLabel(app.TEXTS.get("menuGameService"), app.skin, app.SKIN_FONT_TITLE,
                LightBlocksGame.LABEL_SCALING);
        signInState.setAlignment(Align.center);
        signInState.setWrap(true);
        leaderboardButton = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
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

        achievementsButton = new FaButton(FontAwesome.GPGS_ACHIEVEMENT, app.skin);
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
        menuTable.add(logInOutButton);
        // hier noch nicht zu buttonsToAdd hinzufügen, damit er nicht default ist
    }

    public void refreshAccountChanged() {
        final boolean gpgsConnected = app.gpgsClient != null && app.gpgsClient.isConnected();
        final boolean gpgsConnectPending = app.gpgsClient != null && app.gpgsClient.isConnectionPending();

        //logInOutButton.setFaText(getLogInOutIcon(gpgsConnected));
        if (gpgsConnected) {
            String playerDisplayName = app.gpgsClient.getPlayerDisplayName();
            if (playerDisplayName != null && playerDisplayName.length() > 15)
                playerDisplayName = playerDisplayName.substring(0, 13) + "...";
            logInOutButton.setText(app.TEXTS.format("menuSignOut", playerDisplayName != null ? playerDisplayName : ""));
        } else {
            logInOutButton.setText(app.TEXTS.get(gpgsConnectPending ? "menuGPGSConnecting" : "menuSignIn"));
        }

        leaderboardButton.setDisabled(!gpgsConnected);
        achievementsButton.setDisabled(!gpgsConnected);
        //TODO auf canLogin oder ähnliches checken
        logInOutButton.setDisabled(app.gpgsClient == null);
        // Achievements etc auch
    }
}
