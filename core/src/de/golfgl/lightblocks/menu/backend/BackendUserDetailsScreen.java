package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendClient;
import de.golfgl.lightblocks.backend.PlayerDetails;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 12.10.2018.
 */

public class BackendUserDetailsScreen extends AbstractFullScreenDialog {

    private final ProgressDialog.WaitRotationImage waitRotationImage;
    private final Cell contentCell;
    private final String userId;

    public BackendUserDetailsScreen(final LightBlocksGame app, String userId) {
        super(app);
        this.userId = userId;

        // Fill Content
        Table contentTable = getContentTable();

        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PERSON, app.skin, FontAwesome.SKIN_FONT_FA));

        contentTable.row();
        waitRotationImage = new ProgressDialog.WaitRotationImage(app);
        contentCell = contentTable.add();

        reload();
    }

    protected void reload() {
        contentCell.setActor(waitRotationImage);
        app.backendManager.getBackendClient().fetchPlayerDetails(userId, new BackendClient
                .IBackendResponse<PlayerDetails>() {

            @Override
            public void onFail(final int statusCode, final String errorMsg) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        boolean isConnectionProblem = statusCode == BackendClient.SC_NO_CONNECTION;
                        String errorMessage = (isConnectionProblem ? app.TEXTS.get("errorNoInternetConnection") :
                                errorMsg);

                        Table errorTable = new Table();
                        Label errorMsgLabel = new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_BIG);
                        errorTable.add(errorMsgLabel).minHeight(errorMsgLabel.getPrefHeight() * 1.5f);

                        if (isConnectionProblem) {
                            FaButton retry = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
                            errorTable.add(retry).pad(10);
                            addFocusableActor(retry);
                            retry.addListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                    reload();
                                }
                            });
                        }

                        contentCell.setActor(errorTable);
                    }
                });
            }

            @Override
            public void onSuccess(final PlayerDetails retrievedData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Table mainTable = new Table();
                        BackendUserLabel userLabel = new BackendUserLabel(retrievedData, app, "default");
                        userLabel.getLabel().setFontScale(1f);
                        userLabel.setMaxLabelWidth(LightBlocksGame.nativeGameWidth - 50);
                        mainTable.add(userLabel);

                        if (retrievedData.donator > 0) {
                            mainTable.row().padTop(5).padBottom(5);
                            mainTable.add(new ScaledLabel(getDonatorLabelString(retrievedData.donator), app.skin,
                                    LightBlocksGame.SKIN_FONT_TITLE, .5f));
                        }

                        if (app.backendManager.hasUserId() && app.backendManager.ownUserId().equals(retrievedData
                                .getUserId())) {
                            app.localPrefs.setBackendNickname(retrievedData.nickName);

                            mainTable.row().padTop(5).padBottom(5);
                            mainTable.add(new ScaledLabel(app.TEXTS.get("profileItIsYouLabel"), app.skin,
                                    LightBlocksGame.SKIN_FONT_REG));
                            //TODO Button change Nickname, change decoration, Delete Account
                            // TODO möglichkeit wenn account nicht mehr gültig ist einzugreifen
                        }

                        mainTable.row().padTop(20);
                        mainTable.add(new PlayerDetailsTable(retrievedData)).expand();
                        mainTable.row().padTop(30);
                        mainTable.add(new ScaledLabel("Best scores", app.skin, LightBlocksGame.SKIN_FONT_TITLE));
                        mainTable.row().padTop(10);
                        mainTable.add(new HighscoresTable(retrievedData)).top().expand();

                        // ScrollPane hier
                        contentCell.setActor(mainTable).fill().maxWidth(LightBlocksGame.nativeGameWidth - 50);
                    }
                });
            }
        });
    }

    private String getDonatorLabelString(int supportLevel) {

        if (supportLevel >= 3)
            return app.TEXTS.get("donationType_lightblocks.patron");
        else if (supportLevel >= 2)
            return app.TEXTS.get("donationType_lightblocks.sponsor");
        else if (supportLevel >= 1)
            return app.TEXTS.get("donationType_lightblocks.supporter");
        else
            return "";
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }

    private class PlayerDetailsTable extends Table {
        public PlayerDetailsTable(PlayerDetails details) {
            addLine("profileXpLabel", String.valueOf(details.experience), LightBlocksGame.SKIN_FONT_TITLE, 0);

            addLine("labelBlocks", String.valueOf(details.countTotalBlocks), LightBlocksGame.SKIN_FONT_TITLE, 0);
            // TODO hier noch Hinweis das nur die serverseitigen gezählt sind

            // TODO Passwort E-Mail

            if (details.country != null && !details.country.isEmpty())
                addLine("profileCountryLabel", details.country,
                        LightBlocksGame.SKIN_FONT_REG);

            if (details.memberSince > 0)
                addLine("profileJoinedLabel", BackendScoreTable.formatTimePassedString(app, details.memberSince),
                        LightBlocksGame.SKIN_FONT_REG);

            if (details.lastActivity > 0)
                addLine("profileLastActivityLabel", BackendScoreTable.formatTimePassedString(app, details
                        .lastActivity), LightBlocksGame.SKIN_FONT_REG);

        }

        private void addLine(String label, String value, String style) {
            addLine(label, value, style, 10);
        }

        private void addLine(String label, String value, String style, float padTop) {
            row().padTop(padTop);
            add(new ScaledLabel(app.TEXTS.get(label).toUpperCase(), app.skin, style)).right().padRight(30);
            ScaledLabel scoreLabel = new ScaledLabel(value, app.skin, style);
            add(scoreLabel).left();
            // TODO dritter ist für Change button oder Infos
            add();
        }

    }

    private class HighscoresTable extends Table {
        public HighscoresTable(final PlayerDetails playerDetails) {
            String buttonDetailsLabel = app.TEXTS.get("buttonDetails").toUpperCase();
            defaults().pad(5, 10, 5, 10);

            for (final ScoreListEntry score : playerDetails.highscores) {
                row();
                add(new ScaledLabel(BackendScoreDetailsScreen.findI18NIfExistant(app.TEXTS, score.gameMode,
                        "labelModel_").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_BIG)).left();
                String scoreValue = SprintModel.MODEL_SPRINT_ID.equals(score.gameMode) ?
                        ScoreTable.formatTimeString(score.timePlayedMs, 2) :
                        String.valueOf(score.scoreValue);
                ScaledLabel valueLabel = new ScaledLabel(scoreValue, app.skin, LightBlocksGame.SKIN_FONT_BIG);
                valueLabel.setAlignment(Align.right);
                add(valueLabel).minWidth(100).right();
                FaTextButton detailsButton = new FaTextButton(buttonDetailsLabel, app.skin,
                        LightBlocksGame.SKIN_FONT_BIG);
                detailsButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        new BackendScoreDetailsScreen(app, score, playerDetails).show(getStage());
                    }
                });
                add(detailsButton);
                addFocusableActor(detailsButton);
            }

            if (playerDetails.highscores.isEmpty()) {
                add(new ScaledLabel(app.TEXTS.get("profileNoScores"), app.skin, LightBlocksGame.SKIN_FONT_BIG))
                        .center();
            }
        }
    }
}
