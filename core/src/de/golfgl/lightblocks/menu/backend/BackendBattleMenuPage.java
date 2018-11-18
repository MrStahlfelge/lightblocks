package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class BackendBattleMenuPage extends Table implements MultiplayerMenuScreen.IMultiplayerModePage {
    private final Cell mainCell;
    private final ProgressDialog.WaitRotationImage progressIndicator;
    private final FaButton refreshButton;
    private final RoundedTextButton newMatchButton;
    private final LightBlocksGame app;
    private final MultiplayerMenuScreen parent;
    private Button websiteButton;
    private Cell progressOrRefreshCell;
    private Cell matchesListCell;

    public BackendBattleMenuPage(final LightBlocksGame app, MultiplayerMenuScreen parent) {
        progressIndicator = new ProgressDialog.WaitRotationImage(app);
        refreshButton = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
        newMatchButton = new RoundedTextButton("New match", app.skin);
        this.app = app;
        this.parent = parent;
        newMatchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.backendManager.getBackendClient().openNewMatch(null, 0,
                        new WaitForResponse<MatchEntity>(app, getStage()) {
                            @Override
                            protected void onSuccess() {
                                // TODO muss über den backendmanager gehen und der die Rückgabe dann selbst in
                                // die hashmap einsortieren
                            }
                        });
            }
        });

        parent.addFocusableActor(refreshButton);
        parent.addFocusableActor(newMatchButton);
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.backendManager.fetchMultiplayerMatches();
            }
        });


        add(new ScaledLabel(app.TEXTS.get("labelMultiplayerBackend"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE, .8f));

        row();
        mainCell = add().expand();
        if (!app.backendManager.hasUserId())
            mainCell.setActor(fillUnregistered()).fill();
        else {
            mainCell.setActor(fillMenu()).fill();
        }
    }

    private Actor fillMenu() {
        //TODO das ganze in einen ScrollPager

        Table buttonTable = new Table();
        buttonTable.add().uniform();
        buttonTable.add(newMatchButton);
        progressOrRefreshCell = buttonTable.add(progressIndicator).minSize(refreshButton.getPrefWidth() * 1.5f,
                refreshButton.getPrefHeight()).uniform();

        Table myGamesTable = new Table();

        myGamesTable.add(buttonTable);

        myGamesTable.row();
        matchesListCell = myGamesTable.add(new BackendMatchesTable(app)).expand();
        //TODO wenn noch kein Match vorhanden, statt leerer Tabelle Introtext anzeigen

        return myGamesTable;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        //TODO von unregistered auf registered wechseln können

        // TODO anzeigen wenn es einen Fehler gab

        if (app.backendManager.isFetchingMultiplayerMatches() && !progressIndicator.hasParent())
            progressOrRefreshCell.setActor(progressIndicator);
        else if (!app.backendManager.isFetchingMultiplayerMatches() && !refreshButton.hasParent())
            progressOrRefreshCell.setActor(refreshButton);
    }

    private Actor fillUnregistered() {
        Table unregistered = new Table();
        Label competitionIntro = new ScaledLabel(app.TEXTS.get("competitionIntro"), app.skin, app.SKIN_FONT_TITLE);
        competitionIntro.setAlignment(Align.center);
        competitionIntro.setWrap(true);

        if (websiteButton == null) {
            websiteButton = new RoundedTextButton(app.TEXTS.get("buttonWebsite"), app.skin);
            websiteButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.net.openURI(LightBlocksGame.GAME_URL);
                }
            });
            parent.addFocusableActor(websiteButton);
        }

        unregistered.row();
        unregistered.add(competitionIntro).fill().expand().pad(20);

        unregistered.row().padTop(30);
        unregistered.add(websiteButton);
        return unregistered;
    }

    @Override
    public Actor getDefaultActor() {
        return newMatchButton.hasParent() ? newMatchButton : websiteButton;
    }
}
