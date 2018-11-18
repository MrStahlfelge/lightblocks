package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class BackendMatchesTable extends WidgetGroup {
    private final LightBlocksGame app;
    private long listTimeStamp;

    public BackendMatchesTable(LightBlocksGame app) {
        this.app = app;

        refresh();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (app.backendManager.getMultiplayerMatchesLastFetchMs() > listTimeStamp)
            refresh();
    }

    private void refresh() {
        clear();
        // TODO die entfernten Actor von der focusable-List entfernen

        List<MatchEntity> multiplayerMatchesList = app.backendManager.getMultiplayerMatchesList();
        listTimeStamp = app.backendManager.getMultiplayerMatchesLastFetchMs();
        for (MatchEntity me : multiplayerMatchesList) {
            addActor(new ScaledLabel(String.valueOf(me.lastChangeTime), app.skin));
        }

    }
}
