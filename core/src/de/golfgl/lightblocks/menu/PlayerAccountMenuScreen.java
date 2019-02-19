package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.gamestate.ISaveGameStateResponseListener;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Screen for player's account: Scores, Sign in/out a.s.o.
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class PlayerAccountMenuScreen extends AbstractMenuDialog {

    private TextButton logInOutButton;
    private Button leaderboardButton;
    private Button achievementsButton;

    public PlayerAccountMenuScreen(LightBlocksGame app, Group actorToHide) {
        super(app, actorToHide);

        refreshAccountChanged();
    }

    public static String getGameServiceLogo(String gameServiceId) {
        if (gameServiceId.equals(IGameServiceClient.GS_AMAZONGC_ID))
            return FontAwesome.GC_LOGO;
        if (gameServiceId.equals(IGameServiceClient.GS_GOOGLEPLAYGAMES_ID))
            return FontAwesome.GPGS_LOGO;
        if (gameServiceId.equals(IGameServiceClient.GS_GAMECENTER_ID))
            return FontAwesome.APPLE_LOGO;

        return FontAwesome.NET_CLOUDSAVE;
    }

    public static Color getGameServiceColor(String gameServiceId) {
        if (gameServiceId.equals(IGameServiceClient.GS_AMAZONGC_ID))
            return Color.ORANGE;
        if (gameServiceId.equals(IGameServiceClient.GS_GOOGLEPLAYGAMES_ID))
            return Color.GREEN;

        return null;
    }

    @Override
    protected String getTitleIcon() {
        return app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                FontAwesome.GC_LOGO : FontAwesome.GPGS_LOGO;
    }

    @Override
    protected String getTitle() {
        String gameServiceId = app.gpgsClient.getGameServiceId();
        if (gameServiceId.equals(IGameServiceClient.GS_GAMEJOLT_ID)) {
            return "GameJolt";
        } else
            return app.TEXTS.get(gameServiceId.equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                    "menuAccountGc" : gameServiceId.equals(IGameServiceClient.GS_GAMECENTER_ID) ?
                    "menuAccountApple" : "menuAccountGpgs");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        buttons.add(leaderboardButton);
        addFocusableActor(leaderboardButton);
        buttons.add(achievementsButton);
        addFocusableActor(achievementsButton);
    }

    private void performGpgsLoginout() {
        if (app.gpgsClient.isSessionActive()) {
            app.localPrefs.setGpgsAutoLogin(false);
            logInOutButton.setDisabled(true);
            app.savegame.resetLoadedFromCloud();
            app.savegame.gpgsSaveGameState(new ISaveGameStateResponseListener() {
                @Override
                public void onGameStateSaved(boolean success, String errorCode) {
                    app.gpgsClient.logOff();
                }
            });
        } else {
            app.gpgsClient.logIn();
            refreshAccountChanged();
        }

        // Die AutoLogin Einstellung wird bei erfolgtem Connect wieder zurück gesetzt

    }

    protected void fillMenuTable(Table menuTable) {
        Label signInState = new ScaledLabel(app.TEXTS.get("menuGameService"), app.skin, app.SKIN_FONT_TITLE);
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
        leaderboardButton.setVisible(app.gpgsClient.isFeatureSupported(
                IGameServiceClient.GameServiceFeature.ShowAllLeaderboardsUI));

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
        achievementsButton.setVisible(app.gpgsClient.isFeatureSupported(
                IGameServiceClient.GameServiceFeature.ShowAchievementsUI));

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
        addFocusableActor(logInOutButton);
    }

    public void refreshAccountChanged() {
        final boolean gpgsConnected = app.gpgsClient != null && app.gpgsClient.isSessionActive();
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
        logInOutButton.setDisabled(app.gpgsClient == null
                || app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_GAMEJOLT_ID)
                || gpgsConnected && !app.gpgsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.PlayerLogOut));
        // Achievements etc auch
    }
}
