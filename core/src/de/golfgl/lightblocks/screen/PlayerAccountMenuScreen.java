package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsException;
import de.golfgl.lightblocks.scenes.FATextButton;

/**
 * Screen for player's account: Scores, Sign in/out a.s.o.
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class PlayerAccountMenuScreen extends AbstractMenuScreen {

    private Label signInState;
    private FATextButton logInOutButton;
    private TextButton leaderboardButton;
    private TextButton achievementsButton;

    public PlayerAccountMenuScreen(LightBlocksGame app) {
        super(app);

        initializeUI();
        refreshAccountChanged();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.GPGS_LOGO;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuAccount");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        logInOutButton = new FATextButton("", "Google Play Games", app.skin);
        logInOutButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        performGpgsLoginout();
                                    }
                                }
        );

        buttons.add(logInOutButton).padLeft(50).minWidth(220);
    }

    private void performGpgsLoginout() {
        if (app.gpgsClient.isConnected()) {
            app.setGpgsAutoLogin(false);
            app.gpgsClient.disconnect(false);
        } else
            app.gpgsClient.connect(false);

        // Die AutoLogin Einstellung wird bei erfolgtem Connect wieder zurück gesetzt

    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        app.setAccountScreen(this);

        signInState = new Label(app.player.getGamerId(), app.skin, app.SKIN_FONT_BIG);
        signInState.setAlignment(Align.center);
        signInState.setWrap(true);
        TextButton scoreButton = new TextButton(FontAwesome.COMMENT_STAR_TROPHY, app.skin, FontAwesome.SKIN_FONT_FA);
        scoreButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        gotoTotalScoreScreen();
                                    }
                                }
        );
        leaderboardButton = new TextButton(FontAwesome.GPGS_LEADERBOARD, app.skin, FontAwesome.SKIN_FONT_FA);
        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showLeaderboards(null);
                } catch (GpgsException e) {
                    showDialog("Error showing leaderboards.");
                }
            }
        });

        achievementsButton = new TextButton(FontAwesome.GPGS_ACHIEVEMENT, app.skin, FontAwesome.SKIN_FONT_FA);
        achievementsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showAchievements();
                } catch (GpgsException e) {
                    showDialog("Error showing achievements.");
                }
            }
        });

        menuTable.defaults().fill();

        menuTable.row();
        menuTable.add(signInState).colspan(2);

        menuTable.row().padTop(30);
        menuTable.add(leaderboardButton).uniform();
        menuTable.add(new Label("Leader Boards", app.skin, app.SKIN_FONT_BIG)).expandX();
        menuTable.row();
        menuTable.add(achievementsButton).uniform();
        menuTable.add(new Label("Achievements", app.skin, app.SKIN_FONT_BIG));

        menuTable.row().padTop(30);
        menuTable.add(scoreButton).uniform();
        menuTable.add(new Label(app.TEXTS.get("labelScores"), app.skin, app.SKIN_FONT_BIG));

    }

    @Override
    public void dispose() {
        app.setAccountScreen(null);
        super.dispose();
    }

    public void refreshAccountChanged() {
        final boolean gpgsConnected = app.gpgsClient != null && app.gpgsClient.isConnected();
        if (gpgsConnected) {
            logInOutButton.getFaLabel().setText(FontAwesome.NET_LOGOUT);
            logInOutButton.setText(app.TEXTS.get("menuSignOut"));
            signInState.setText(app.TEXTS.format("menuGPGSAccount", app.player.getName()));
        } else {
            logInOutButton.getFaLabel().setText(FontAwesome.NET_LOGIN);
            logInOutButton.setText(app.TEXTS.get("menuSignInGPGS"));
            signInState.setText(app.TEXTS.format("menuLocalAccount", app.player.getName()));
        }

        leaderboardButton.setDisabled(!gpgsConnected);
        achievementsButton.setDisabled(!gpgsConnected);
        logInOutButton.setDisabled(app.gpgsClient == null);
        // Achievements etc auch
    }

    private void gotoTotalScoreScreen() {

        if (!app.savegame.canSaveState()) {
            showDialog("Sorry, highscores are only saved in the native Android version of Lightblocks. Download it to" +
                    " your mobile!");
            return;
        }

        TotalScoreScreen scoreScreen = new TotalScoreScreen(app);
        scoreScreen.setTotal(app.savegame.loadTotalScore());
        scoreScreen.setMaxCountingTime(1);
        scoreScreen.setBackScreen(this);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);
    }


}
