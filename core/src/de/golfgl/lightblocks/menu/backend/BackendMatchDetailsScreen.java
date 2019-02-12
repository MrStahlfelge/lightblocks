package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.menu.AbstractMenuDialog;
import de.golfgl.lightblocks.menu.DonationDialog;
import de.golfgl.lightblocks.menu.PlayButton;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 16.12.2018.
 */

public class BackendMatchDetailsScreen extends WaitForBackendFetchDetailsScreen<String, MatchEntity> {
    private final Button playTurnButton;
    private final Button resignButton;
    private final Button showReplayButton;
    private final Button rematchButton;
    private final Button acceptChallengeButton;
    private final Button declineChallengeButton;
    private MatchEntity match;
    private boolean wasPlaying;

    public BackendMatchDetailsScreen(LightBlocksGame app, String matchId) {
        super(app, matchId);

        playTurnButton = new PlayButton(app);
//                =new RoundedTextButton("P", app.skin);
        playTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startPlaying();
            }
        });
        addFocusableActor(playTurnButton);

        resignButton = new FaButton(FontAwesome.MISC_CROSS, app.skin);
        resignButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resignMatch();
            }
        });
        addFocusableActor(resignButton);
        getButtonTable().add(resignButton);

        showReplayButton = new FaButton(FontAwesome.CIRCLE_PLAY, app.skin);
        showReplayButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showReplay(0);
            }
        });
        addFocusableActor(showReplayButton);
        getButtonTable().add(showReplayButton);

        acceptChallengeButton = new GlowLabelButton(FontAwesome.BIG_PLAY, app.TEXTS.get("labelAcceptChallenge"), app
                .skin);
        addFocusableActor(acceptChallengeButton);
        acceptChallengeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                acceptChallenge(true);
            }
        });
        declineChallengeButton = new FaTextButton(app.TEXTS.get("labelDeclineChallenge"), app.skin, "default");
        addFocusableActor(declineChallengeButton);
        declineChallengeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                acceptChallenge(false);
            }
        });

        // TODO Reload wenn gewartet wird

        rematchButton = new GlowLabelButton(FontAwesome.ROTATE_RIGHT, "Rematch", app.skin);
        addFocusableActor(rematchButton);
        rematchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                doRematch();
            }
        });

        resetButtonEnabling();
    }

    private void resetButtonEnabling() {
        if (resignButton != null)
            resignButton.setDisabled(true);
        if (showReplayButton != null)
            showReplayButton.setDisabled(true);
    }

    public void startPlaying() {
        if (app.backendManager.hasPlayedTurnToUpload() || app.backendManager.isUploadingPlayedTurn())
            new VetoDialog("You still have a turn not in sync with server. Please sync before playing another match.",
                    app.skin, .8f * LightBlocksGame.nativeGameWidth);

        else {
            final long timeRequestStarted = TimeUtils.millis();
            app.backendManager.getBackendClient().postMatchStartPlayingTurn(match.uuid,
                    new WaitForResponse<String>(app, getStage()) {
                        @Override
                        public void onRequestSuccess(String retrievedData) {
                            super.onRequestSuccess(retrievedData);
                            startPlayingWithKey(retrievedData,
                                    TimeUtils.timeSinceMillis(timeRequestStarted) > 4000);
                        }
                    });
        }
    }

    public void startPlayingWithKey(String playKey, boolean paused) {
        // das eigentliche Spiel beginnen
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameMode(InitGameParameters.GameMode.TurnbasedBattle);
        initGameParametersParams.setBeginningLevel(match.beginningLevel);
        initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYORTOUCH);
        initGameParametersParams.setMatchEntity(match);
        initGameParametersParams.setPlayKey(playKey);
        initGameParametersParams.setStartPaused(paused);
        try {
            PlayScreen ps = PlayScreen.gotoPlayScreen(BackendMatchDetailsScreen.this.app, initGameParametersParams);
            ps.setShowScoresWhenGameOver(false);
            wasPlaying = true;
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), getSkin(), LightBlocksGame.nativeGameWidth * .75f).show(getStage());
        }
    }

    public void resignMatch() {
        Dialog shouldResign = new AbstractScreen.RunnableDialog(app.skin, app.TEXTS.get("askIfResign"), new Runnable() {
            @Override
            public void run() {
                app.backendManager.getBackendClient().postMatchGiveUp(match.uuid, new WaitForResponse<MatchEntity>
                        (app, getStage()) {
                    @Override
                    public void onRequestSuccess(MatchEntity retrievedData) {
                        super.onRequestSuccess(retrievedData);
                        // zur Sicherheit einen zum Hochladen gespeicherten Turn vernichten. Das kann auch ein
                        // anderes Spiel
                        // treffen. Ist aber unwahrscheinlich und kann sogar genutzt werden
                        app.backendManager.resetTurnToUpload();
                        app.backendManager.updateMatchEntityInList(retrievedData);
                        fillMatchDetails(retrievedData);
                    }
                });
            }
        }, null, app.TEXTS.get("menuYes"), app.TEXTS.get("menuNo"));
        shouldResign.show(getStage());
    }

    private void acceptChallenge(boolean accepted) {
        app.backendManager.getBackendClient().postMatchAccepted(match.uuid, accepted, new WaitForResponse<MatchEntity>
                (app, getStage()) {
            @Override
            public void onRequestSuccess(MatchEntity retrievedData) {
                super.onRequestSuccess(retrievedData);
                app.backendManager.updateMatchEntityInList(retrievedData);
                fillMatchDetails(retrievedData);
            }
        });
    }

    private void doRematch() {
        app.backendManager.openNewMultiplayerMatch(match.opponentId, match.beginningLevel,
                new WaitForResponse<MatchEntity>(app, getStage()) {
                    @Override
                    protected void onSuccess() {
                        hide();
                    }
                });
    }

    private void showReplay(int turnNum) {
        Replay yourReplay = new Replay();
        yourReplay.fromString(match.yourReplay);

        ReplayDialog dialog = new ReplayDialog(app, yourReplay, "", null);

        if (match.opponentReplay != null) {
            Replay opponentReplay = new Replay();
            opponentReplay.fromString(match.opponentReplay);

            dialog.addSecondReplay(opponentReplay, match.opponentNick,
                    !(match.myTurn || match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_WAIT)));
        }
        dialog.windToTimePos(match.turnBlockCount * 1000 * turnNum);

        dialog.show(getStage());
    }

    @Override
    protected void fillFixContent() {
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PEOPLE, app.skin, FontAwesome.SKIN_FONT_FA));
    }

    protected void reload() {
        resetButtonEnabling();
        contentCell.setActor(waitRotationImage);
        app.backendManager.fetchFullMatchInfo(backendId, new BackendManager
                .AbstractQueuedBackendResponse<MatchEntity>(app) {

            @Override
            public void onRequestFailed(final int statusCode, final String errorMsg) {
                fillErrorScreen(statusCode, errorMsg);
            }

            @Override
            public void onRequestSuccess(final MatchEntity retrievedData) {
                fillMatchDetails(retrievedData);
            }
        });
    }

    private void fillMatchDetails(MatchEntity match) {
        this.match = match;
        Table matchDetailTable = new Table();

        matchDetailTable.add(new ScaledLabel(app.TEXTS.get("labelTurnAgainst"), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .6f));

        BackendUserLabel opponentLabel = new BackendUserLabel(match, app, "default");
        opponentLabel.getLabel().setFontScale(1f);
        opponentLabel.setMaxLabelWidth(LightBlocksGame.nativeGameWidth - 50);
        matchDetailTable.row().padBottom(5);
        matchDetailTable.add(opponentLabel);

        matchDetailTable.row();
        matchDetailTable.add(new ScaledLabel(BackendScoreDetailsScreen.findI18NIfExistant(app.TEXTS, match
                .matchState, "mmturn_"), app.skin, LightBlocksGame.SKIN_FONT_TITLE, .6f)).padTop(30);

        matchDetailTable.row().padTop(30);
        if (match.turns.size() > 1 || match.turns.size() == 1 && match.turns.get(0).youPlayed) {
            matchDetailTable.add(new MatchTurnsTable()).padBottom(30);
        } else if (match.myTurn && !match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_CHALLENGED) ||
                match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_WAIT) ||
                !match.myTurn && match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_CHALLENGED)) {
            ScaledLabel competitionHelp = new ScaledLabel(app.TEXTS.format("competitionHelp",
                    match.turnBlockCount), app.skin, LightBlocksGame.SKIN_FONT_REG, .75f);
            competitionHelp.setWrap(true);
            matchDetailTable.add(competitionHelp).width(LightBlocksGame.nativeGameWidth - 80).padBottom(15);

        } else if (!match.myTurn) {
            matchDetailTable.add(new ScaledLabel(app.TEXTS.get("labelNoTurnsPlayed"), app.skin,
                    LightBlocksGame.SKIN_FONT_BIG)).padBottom(30);
        }

        resetButtonEnabling();

        showReplayButton.setDisabled(match.yourReplay == null);
        Actor toFocus = null;
        if (match.myTurn) {
            matchDetailTable.row();
            if (match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_CHALLENGED)) {
                matchDetailTable.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel")
                        + " " + app.TEXTS.get("labelLevel") + " "
                        + match.beginningLevel, app.skin, LightBlocksGame.SKIN_FONT_BIG))
                        .padTop(30);
                matchDetailTable.row();
                matchDetailTable.add(acceptChallengeButton).pad(50, 0, 20, 0)
                        .minHeight(acceptChallengeButton.getPrefHeight() * 2f);
                matchDetailTable.row();
                matchDetailTable.add(declineChallengeButton).padBottom(20);
                toFocus = acceptChallengeButton;
            } else if (app.backendManager.hasTurnToUploadForMatch(match.uuid)) {
                Button syncButton = new GlowLabelButton(FontAwesome.NET_CLOUDSAVE, "Sync with server", app.skin,
                        GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
                syncButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        uploadTurn();
                    }
                });
                addFocusableActor(syncButton);
                matchDetailTable.add(syncButton).padTop(15).padBottom(20);

                // es gibt noch einen zum hochladen
                resignButton.setDisabled(true);
                toFocus = syncButton;
            } else {
                // okay, normaler Zustand zum Spielen
                matchDetailTable.add(playTurnButton).padTop(15).padBottom(20);
                resignButton.setDisabled(false);
                toFocus = playTurnButton;
            }
        } else if (!match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_WAIT)
                && !match.matchState.equalsIgnoreCase(MatchEntity.PLAYER_STATE_CHALLENGED)
                && match.turns.size() >= 1) {
            // Match um => retry button
            matchDetailTable.row();
            matchDetailTable.add(rematchButton).padTop(15).padBottom(20);
            toFocus = rematchButton;
        }

        contentCell.setActor(matchDetailTable);

        if (getStage() != null && toFocus != null && toFocus.hasParent()) {
            scrollToActor(toFocus);
            ((MyStage) getStage()).setFocusedActor(toFocus);
        }

        matchDetailTable.getColor().a = 0;
        matchDetailTable.addAction(Actions.fadeIn(.2f, Interpolation.fade));
    }

    private void scrollToActor(final Actor toFocus) {
        // zeitlich verzögert wegen der Eingangsanimation
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (toFocus.hasParent()) {
                    getContentTable().validate();
                    getScrollPane().scrollTo(toFocus.getX(), toFocus.getY(), toFocus.getWidth(), toFocus.getHeight());
                }
            }
        }, AbstractMenuDialog.TIME_SWOSHIN * 2);
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (wasPlaying) {
            // wieder zurückgekommen aus Playscreen
            wasPlaying = false;
            remindToDonate();
            reload();
        }
    }

    private void remindToDonate() {
        if (app.canDonate() && app.localPrefs.getSupportLevel() == 0
                && app.savegame.getTotalScore().getDrawnTetrominos() >= app.localPrefs.getNextDonationReminder()) {
            new DonationDialog(app).setForcedMode().show(getStage());
        }
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        if (playTurnButton.hasParent())
            return playTurnButton;
        else
            return super.getConfiguredDefaultActor();
    }

    private void uploadTurn() {
        app.backendManager.sendEnqueuedTurnToUpload(new WaitForResponse<MatchEntity>(app, getStage()) {
            @Override
            public void onRequestSuccess(MatchEntity retrievedData) {
                super.onRequestSuccess(retrievedData);
                fillMatchDetails(retrievedData);
            }
        });
    }

    private class MatchTurnsTable extends Table {
        public MatchTurnsTable() {
            defaults().pad(5).right();

            add();
            add(new ScaledLabel("YOU", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f)).expandX();
            add(new ScaledLabel("OPPONENT", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f)).expandX().padLeft(15);

            int linesSentYou = 0;
            int linesSentOpp = 0;
            int yourScore = 0;
            int oppScore = 0;

            for (final MatchEntity.MatchTurn turn : match.turns) {
                row();
                add(new ScaledLabel("TURN #" + String.valueOf(turn.turnNum + 1), app.skin, LightBlocksGame
                        .SKIN_FONT_REG));
                String yourScoreText = turn.youPlayed ? String.valueOf(turn.yourScore) : "";
                if (turn.youDroppedOut)
                    yourScoreText = "†" + yourScoreText;
                String opponentScoreText = turn.opponentPlayed ? String.valueOf(turn.opponentScore) : "";
                if (turn.opponentDroppedOut)
                    opponentScoreText = "†" + opponentScoreText;
                add(new ScaledLabel(yourScoreText, app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f)).uniform();
                add(new ScaledLabel(opponentScoreText, app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f)).uniform();

                if (turn.linesSent > 0)
                    linesSentYou = linesSentYou + turn.linesSent;
                else if (turn.youPlayed)
                    // nur zeigen, wenn bereits gespielt ist
                    linesSentOpp = linesSentOpp - turn.linesSent;

                yourScore = turn.yourScore;
                oppScore = turn.opponentScore;
            }

            if (match.opponentBonus > 0 || match.yourBonus > 0) {
                row();
                add(new ScaledLabel("BONUS", app.skin, LightBlocksGame.SKIN_FONT_REG));

                if (match.yourBonus > 0)
                    add(new ScaledLabel(String.valueOf(match.yourBonus), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                            .5f)).uniform();
                else
                    add();

                if (match.opponentBonus > 0)
                    add(new ScaledLabel(String.valueOf(match.opponentBonus), app.skin, LightBlocksGame
                            .SKIN_FONT_TITLE, .5f)).uniform();
                else
                    add();

                row();
                add(new ScaledLabel("TOTAL", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f));
                add(new ScaledLabel(String.valueOf(yourScore + match.yourBonus), app.skin,
                        LightBlocksGame.SKIN_FONT_TITLE, .65f));
                add(new ScaledLabel(String.valueOf(oppScore + match.opponentBonus), app.skin,
                        LightBlocksGame.SKIN_FONT_TITLE, .65f));
            }

            if (linesSentYou > 0 || linesSentOpp > 0) {
                row().padTop(10);
                add(new ScaledLabel("LINES SENT", app.skin, LightBlocksGame.SKIN_FONT_REG));
                add(new ScaledLabel(String.valueOf(linesSentYou), app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f))
                        .uniform();
                add(new ScaledLabel(String.valueOf(linesSentOpp), app.skin, LightBlocksGame.SKIN_FONT_TITLE, .5f))
                        .uniform();
            }
        }
    }
}
