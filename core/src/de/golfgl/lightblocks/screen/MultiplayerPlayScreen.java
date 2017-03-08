package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scenes.ScoreLabel;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Playscren für Multiplayerspiele um die Dinge einfach zu halten
 * <p>
 * Created by Benjamin Schulte on 01.03.2017.
 */

public class MultiplayerPlayScreen extends PlayScreen implements IRoomListener {

    private HashMap<String, ScoreLabel> playerLabels;

    public MultiplayerPlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app, initGameParametersParams);
    }

    @Override
    protected void populateScoreTable(Table scoreTable) {
        super.populateScoreTable(scoreTable);

        //TODO (eventuell) Bei disconnect haben die Clients keinen Zugriff mehr auf ihre Punkte die erreicht wurden
        // könnte man bei dem Event eventuell sonderbehandeln

        // Für die verschiedenen Spieler eine Zelle vorsehen. Noch nicht füllen, Infos stehen noch nicht zur Verfügung
        // das eingefügte ScoreLabel dient nur dazu den Platzbedarf festzulegen
        scoreTable.row();
        Label fillLabel = new Label(app.TEXTS.get("labelFill").toUpperCase(), app.skin);
        scoreTable.add(fillLabel).right().bottom().padBottom(3).spaceRight(3);

        // noch eine Tabelle für die Spieler
        Table fillingTable = new Table();
        playerLabels = new HashMap<String, ScoreLabel>(app.multiRoom.getNumberOfPlayers());

        for (String playerId : app.multiRoom.getPlayers()) {
            ScoreLabel lblFilling = new ScoreLabel(2, 100, app.skin, LightBlocksGame.SKIN_FONT_BIG);
            lblFilling.setExceedChar('X');
            fillingTable.add(lblFilling);
            playerLabels.put(playerId, lblFilling);
            fillingTable.add(new Label("%", app.skin)).padRight(10).bottom().padBottom(3);
        }

        scoreTable.add(fillingTable).colspan(3).align(Align.left);
    }

    @Override
    public void goBackToMenu() {
        if (!((MultiplayerModel) gameModel).isCompletelyOver()) {
            //TODO hier vor dem Verlust der Ehre warnen und ob man wirklich möchte Ja/Nein

        } else {
            // ist eventuell doppelt, aber der unten im dispose kommt u.U. zu spät
            app.multiRoom.removeListener(this);
            super.goBackToMenu();
        }
    }

    @Override
    protected boolean goToScoresWhenOver() {
        return false;
    }

    @Override
    public void dispose() {
        app.multiRoom.removeListener(this);

        super.dispose();
    }

    @Override
    public void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig) {
        ScoreLabel lblPlayerFill = playerLabels.get(pig.playerId);

        if (lblPlayerFill != null) {
            boolean notInitialized = (lblPlayerFill.getScore() == 100);
            lblPlayerFill.setScore(pig.filledBlocks * 100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard
                    .GAMEBOARD_NORMALROWS));

            if (notInitialized) {
                // geht nicht beim Init, da dieser mit 100 erfolgt und dann auf 0 zurückgesetzt wird
                lblPlayerFill.setEmphasizeTreshold(15, EMPHASIZE_COLOR);
                lblPlayerFill.setCountingSpeed(30);
                lblPlayerFill.setMaxCountingTime(.3f);
            }
        }
    }

    @Override
    public void switchPause(boolean immediately) {
        super.switchPause(immediately);

        // Pause gedrückt oder App in den Hintergrund gelegt... die anderen informieren
        app.multiRoom.sendToAllPlayers(new MultiPlayerObjects.SwitchedPause().withPaused(isPaused()));
    }

    @Override
    public void multiPlayerRoomStateChanged(AbstractMultiplayerRoom.RoomState roomState) {
        if (!roomState.equals(AbstractMultiplayerRoom.RoomState.inGame))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (app.getScreen() == MultiplayerPlayScreen.this)
                        MultiplayerPlayScreen.super.goBackToMenu();
                }
            });
    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(MultiPlayerObjects.PlayerChanged mpo) {
        //TODO anzeigen - deckt sich aber teilweise mit playersInGameChanged

        if (app.multiRoom.isOwner())
            ((MultiplayerModel) gameModel).handleMessagesFromOthers(mpo);
    }

    @Override
    public void multiPlayerGotErrorMessage(Object o) {
        //TODO anzeigen
    }

    @Override
    public void multiPlayerGotModelMessage(final Object o) {
        if (o instanceof MultiPlayerObjects.SwitchedPause)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (isPaused() != ((MultiPlayerObjects.SwitchedPause) o).nowPaused)
                        MultiplayerPlayScreen.super.switchPause(false);
                }
            });

        //ansonsten weiter an das Spiel
        ((MultiplayerModel) gameModel).handleMessagesFromOthers(o);
    }

    @Override
    public void multiPlayerGotRoomMessage(Object o) {
        // bisher keine die hier zu verarbeiten sind.
    }
}
