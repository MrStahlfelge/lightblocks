package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Playscren für Multiplayerspiele um die Dinge einfach zu halten
 * <p>
 * Created by Benjamin Schulte on 01.03.2017.
 */

public class MultiplayerPlayScreen extends PlayScreen implements IRoomListener {

    public MultiplayerPlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app, initGameParametersParams);
    }

    @Override
    public void goBackToMenu() {
        if (!gameModel.isGameOver()) {
            //TODO hier vor dem Verlust der Ehre warnen und ob man wirklich möchte Ja/Nein

        }

        //wird nicht benötigt, macht der Listener unten
        // super.goBackToMenu();
    }

    @Override
    public void dispose() {
        app.multiRoom.removeListener(this);

        super.dispose();
    }

    @Override
    public void switchPause(boolean immediately) {
        super.switchPause(immediately);

        // Pause gedrückt oder App in den Hintergrund gelegt... die anderen informieren
        app.multiRoom.sendToAllPlayers(new MultiPlayerObjects.SwitchedPause().withPaused(isPaused()));
    }

    @Override
    public void setGameOver() {

        final MultiPlayerObjects.PlayerIsOver playerOverMsg = new MultiPlayerObjects.PlayerIsOver();
        playerOverMsg.finalScore = gameModel.getScore().getScore();
        playerOverMsg.playerId = app.multiRoom.getMyPlayerId();
        app.multiRoom.sendToReferee(playerOverMsg);

        super.setGameOver();
        // dispose wird aufgerufen, hier ist also schon alles gelaufen
    }

    @Override
    public void multiPlayerRoomStateChanged(AbstractMultiplayerRoom.RoomState roomState) {
        if (!roomState.equals(AbstractMultiplayerRoom.RoomState.inGame))
            //TODO hier sollten dann noch zum Score gewechselt werden bevor man am Ende ist
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MultiplayerPlayScreen.super.goBackToMenu();
                }
            });
    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(MultiPlayerObjects.PlayerChanged mpo) {
        //TODO anzeigen

        if (app.multiRoom.isOwner()) {
            //TODO bei Abgängen abbilden auf multiPlayerGotModelMessage(PlayerIsOver)
        }
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

        if (o instanceof MultiPlayerObjects.PlayerIsOver) {
            // okay, einer ist alle...

            if (app.multiRoom.isOwner()) {
                //TODO der Meister teilt das Geld dann auf und informiert alle über den Abgang


                //TODO Bei zwei Spielern ist nur noch einer über - das muss aber anders werden
                final MultiPlayerObjects.GameIsOver gameOverMsg = new MultiPlayerObjects.GameIsOver();
                app.multiRoom.sendToAllPlayers(gameOverMsg);
                multiPlayerGotModelMessage(gameOverMsg);
            }
        }

        if (o instanceof MultiPlayerObjects.GameIsOver) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (!gameModel.isGameOver())
                        ((MultiplayerModel) gameModel).allOtherPlayersHaveGone();
                }
            });

        }
    }
}
