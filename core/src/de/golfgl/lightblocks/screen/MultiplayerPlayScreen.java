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
    public void playersInGameChanged() {
        // TODO Liste ändern
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
            //TODO hier sollten dann noch Game Over gezeigt werden bevor man am Ende ist
            // Auch die letzen Ergebnisse müssen dann im Raum noch angezeigt werden
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
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
}
