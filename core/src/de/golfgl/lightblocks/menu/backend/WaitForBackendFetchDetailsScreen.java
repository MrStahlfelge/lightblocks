package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendClient;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 16.12.2018.
 */

abstract class WaitForBackendFetchDetailsScreen<T, S> extends AbstractFullScreenDialog {
    protected final ProgressDialog.WaitRotationImage waitRotationImage;
    protected final T backendId;
    protected final Cell contentCell;

    public WaitForBackendFetchDetailsScreen(LightBlocksGame app, T backendId) {
        super(app);
        waitRotationImage = new ProgressDialog.WaitRotationImage(app);
        this.backendId = backendId;

        // Fill Content
        fillFixContent();
        Table contentTable = getContentTable();
        contentTable.row();
        contentCell = contentTable.add().minHeight(waitRotationImage.getHeight() * 3);

        reload();
    }

    protected void fillFixContent() {
        //den festen Content einfügen (Titel etc)
    }

    protected abstract void reload();

    protected Table fillErrorScreen(int statusCode, String errorMsg) {
        boolean isConnectionProblem = statusCode == BackendClient.SC_NO_CONNECTION;
        String errorMessage = (isConnectionProblem ? app.TEXTS.get("errorNoInternetConnection") :
                errorMsg);

        Table errorTable = new Table();
        Label errorMsgLabel = new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        float noWrapHeight = errorMsgLabel.getPrefHeight();
        errorMsgLabel.setWrap(true);
        errorMsgLabel.setAlignment(Align.center);
        errorTable.add(errorMsgLabel).minHeight(noWrapHeight * 1.5f).fill()
                .minWidth(LightBlocksGame.nativeGameWidth - 50);

        if (isConnectionProblem) {
            FaButton retry = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
            errorTable.row();
            errorTable.add(retry);
            addFocusableActor(retry);
            retry.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    reload();
                }
            });
        }

        contentCell.setActor(errorTable).fillX();

        return errorTable;
    }
}
