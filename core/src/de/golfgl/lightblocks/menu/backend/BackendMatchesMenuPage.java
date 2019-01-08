package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

import de.golfgl.gdx.controllers.ControllerScrollPane;
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

public class BackendMatchesMenuPage extends Table implements MultiplayerMenuScreen.IMultiplayerModePage {
    private final Cell mainCell;
    private final ProgressDialog.WaitRotationImage progressIndicator;
    private final FaButton refreshButton;
    private final RoundedTextButton newMatchButton;
    private final LightBlocksGame app;
    private final MultiplayerMenuScreen parent;
    private Button websiteButton;
    private Cell progressOrRefreshCell;
    private Cell errorLabelCell;

    public BackendMatchesMenuPage(final LightBlocksGame app, MultiplayerMenuScreen parent) {
        progressIndicator = new ProgressDialog.WaitRotationImage(app);
        refreshButton = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
        this.app = app;
        this.parent = parent;
        newMatchButton = new RoundedTextButton("Challenge new battle", app.skin);
        newMatchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.backendManager.openNewMultiplayerMatch(null, 0,
                        new WaitForResponse<MatchEntity>(app, getStage()));
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
            if (TimeUtils.timeSinceMillis(app.backendManager.getMultiplayerMatchesLastFetchMs()) > 5 * 60 * 1000L)
                app.backendManager.fetchMultiplayerMatches();
        }
    }

    private Actor fillMenu() {
        Table buttonTable = new Table();
        buttonTable.add().uniform();
        buttonTable.add(newMatchButton);
        progressOrRefreshCell = buttonTable.add(progressIndicator).minSize(refreshButton.getPrefWidth() * 1.5f,
                progressIndicator.getHeight()).uniform();

        Table myGamesTable = new Table();

        myGamesTable.add(buttonTable);
        myGamesTable.row();
        errorLabelCell = myGamesTable.add();

        myGamesTable.row();
        ControllerScrollPane scrollPane = new ControllerScrollPane(new BackendMatchesTable(app), app.skin);
        scrollPane.setScrollingDisabled(true, false);
        myGamesTable.add(scrollPane).expand().width(LightBlocksGame.nativeGameWidth);
        //TODO wenn noch kein Match vorhanden, statt leerer Tabelle Introtext anzeigen

        // einen eventuell noch nicht abgesendeten Turn abgesenden
        if (app.backendManager.hasPlayedTurnToUpload())
            app.backendManager.sendEnqueuedTurnToUpload(null);

        return myGamesTable;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        //TODO von unregistered auf registered wechseln können

        if (progressOrRefreshCell != null) {
            if (app.backendManager.isFetchingMultiplayerMatches() && !progressIndicator.hasParent())
                progressOrRefreshCell.setActor(progressIndicator);
            else if (!app.backendManager.isFetchingMultiplayerMatches() && !refreshButton.hasParent())
                progressOrRefreshCell.setActor(refreshButton);
        }

        if (errorLabelCell != null && !app.backendManager.isFetchingMultiplayerMatches()) {
            if (errorLabelCell.hasActor() && app.backendManager
                    .isMultiplayerMatchesLastFetchSuccessful())
                errorLabelCell.setActor(null);
            else if (!errorLabelCell.hasActor() && !app.backendManager.isMultiplayerMatchesLastFetchSuccessful() &&
                    app.backendManager.getMultiplayerLastFetchError() != null)
                errorLabelCell.setActor(new ScaledLabel(app.backendManager.getMultiplayerLastFetchError(), app.skin));
        }
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
