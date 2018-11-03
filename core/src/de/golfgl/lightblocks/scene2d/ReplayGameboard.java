package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 01.11.2018.
 */

public class ReplayGameboard extends BlockGroup {
    private final Replay replay;
    private float playSpeed = 0f;
    private Replay.ReplayStep shownStep;
    private Replay.ReplayStep nextStep;
    private float waitTime;
    private BlockActor[] currentShownBlocks;
    private BlockActor[] activePieceBlock;
    private int[] activePiecePos;
    private Array<BlockActor> actorsToRemove;

    public ReplayGameboard(LightBlocksGame app, Replay replay) {
        super(app);
        setGhostPieceVisibility(false);

        this.replay = replay;
        nextStep = replay.getFirstStep();

        currentShownBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_ALLROWS];
        activePieceBlock = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        activePiecePos = new int[Tetromino.TETROMINO_BLOCKCOUNT];
        actorsToRemove = new Array<BlockActor>();
        for (int i = 0; i < activePieceBlock.length; i++) {
            activePieceBlock[i] = new BlockActor(app, Tetromino.TETRO_IDX_L);
            activePieceBlock[i].setEnlightened(true, true);
        }
    }

    @Override
    public void act(float delta) {
        if (nextStep != null && playSpeed > 0) {
            waitTime = waitTime - delta * playSpeed;

            if (waitTime <= 0) {
                transitionToNextStep();
            }
        }

        super.act(delta * playSpeed);
    }

    private void transitionToNextStep() {
        // remove cleared actors
        for (BlockActor actor : actorsToRemove) {
            actor.clearActions();
            actor.addAction(Actions.sequence(Actions.fadeOut(PlayScreen.DURATION_TETRO_MOVE), Actions.removeActor()));
        }
        actorsToRemove.clear();

        // Gameboard umbauen
        shownStep = nextStep;
        transitionGameboard(replay.getCurrentGameboard());
        nextStep = replay.getNextStep();
        if (nextStep != null)
            waitTime = (float) (nextStep.timeMs - shownStep.timeMs) / 1000;

        // Aktiven Block verschieben
        transitionActivePiece(shownStep);

        // Anzeige aktualisieren
        onTimeChange(shownStep.timeMs);
        if (shownStep.isDropStep()) {
            onScoreChange(replay.getCurrentScore());
            // ARE
            waitTime = waitTime + .2f;

            // Abzubauende Reihen verstärken
            clearLinesOnGameboard();
        }
    }

    private void transitionActivePiece(Replay.ReplayStep shownStep) {
        if (shownStep.hasActivePiecePosition()) {
            int[] activePiecePosition = shownStep.getActivePiecePosition();
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePosition[i];
                setBlockActorToPosition(activePieceBlock[i], activePiecePosition[i], shownStep.isNextPieceStep());
            }
        } else if (shownStep.isMovementStep()) {
            int move = shownStep.getMoveX() - Gameboard.GAMEBOARD_COLUMNS * shownStep.getMoveY();
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePos[i] + move;
                setBlockActorToPosition(activePieceBlock[i], activePiecePos[i], false);
            }
        }

        // TODO bei Drop ganze Zeilen highlighten und bei NewPieceStep "abbauen"
    }

    /**
     * entfernt nicht mehr benötigte Blöcke vom Spielfeld und fügt neue ein. neu eingefügte leuchten kurz auf (für
     * Piece Drop)
     */
    private void transitionGameboard(byte[] gameboard) {
        if (gameboard == null)
            return;

        for (int i = 0; i < gameboard.length; i++) {
            boolean shouldHaveActor = gameboard[i] != Gameboard.SQUARE_EMPTY;
            if (currentShownBlocks[i] != null && !shouldHaveActor) {
                // entfernen
                currentShownBlocks[i].clearActions();
                currentShownBlocks[i].remove();
                currentShownBlocks[i] = null;
            } else if (currentShownBlocks[i] == null && shouldHaveActor) {
                // eventuell für das aufleuchten noch abgleich mit activePiecePos[]?
                currentShownBlocks[i] = new BlockActor(app, Tetromino.TETRO_IDX_L);
                currentShownBlocks[i].setEnlightened(true, true);
                setBlockActorToPosition(currentShownBlocks[i], i, true);
                currentShownBlocks[i].addAction(Actions.delay(.05f,
                        Actions.run(currentShownBlocks[i].getDislightenAction())));
            } else if (currentShownBlocks[i] != null) {
                // könnte an der falschen Position sein!
                setBlockActorToPosition(currentShownBlocks[i], i, false);
            }
        }
    }

    private boolean clearLinesOnGameboard() {
        if (!shownStep.isDropStep())
            return false;

        // interessant sind nur die Zeilen, wo das aktive Piece ist
        int linesCleared = 0;
        for (int row = 0; row < Gameboard.GAMEBOARD_ALLROWS; row++) {
            boolean rowIsFull = true;
            for (int col = 0; col < Gameboard.GAMEBOARD_COLUMNS && rowIsFull; col++) {
                int posOriginal = getIntFromRowAndCol(row, col);
                int posCorrected = getIntFromRowAndCol(row - linesCleared, col);
                boolean colIsFul = currentShownBlocks[posCorrected] != null;
                if (!colIsFul)
                    for (int i = 0; i < activePiecePos.length; i++) {
                        if (activePiecePos[i] == posOriginal)
                            colIsFul = true;
                    }

                rowIsFull = rowIsFull && colIsFul;
            }

            if (rowIsFull) {
                for (int col = 0; col < Gameboard.GAMEBOARD_COLUMNS && rowIsFull; col++) {
                    int pos = getIntFromRowAndCol(row - linesCleared, col);
                    if (currentShownBlocks[pos] != null) {
                        currentShownBlocks[pos].addAction(Actions.forever(Actions.sequence(Actions.alpha(.2f, PlayScreen
                                        .DURATION_REMOVE_DELAY, Interpolation.fade),
                                Actions.fadeIn(PlayScreen.DURATION_REMOVE_DELAY, Interpolation.fade))));
                        actorsToRemove.add(currentShownBlocks[pos]);

                        // die Zuordnungen korrigieren
                        for (int rowsAbove = row - linesCleared + 1; rowsAbove <= Gameboard.GAMEBOARD_ALLROWS;
                             rowsAbove++) {
                            int overWritePos = getIntFromRowAndCol(rowsAbove - 1, col);
                            if (rowsAbove < Gameboard.GAMEBOARD_ALLROWS)
                                currentShownBlocks[overWritePos] = currentShownBlocks[getIntFromRowAndCol(rowsAbove,
                                        col)];
                            else
                                currentShownBlocks[overWritePos] = null;
                        }
                    }
                }
                linesCleared++;
            }
        }

        if (linesCleared > 0)
            waitTime = waitTime + PlayScreen.DURATION_REMOVE_DELAY + PlayScreen.DURATION_REMOVE_FADEOUT;

        return linesCleared > 0;
    }

    private int getIntFromRowAndCol(int row, int col) {
        return row * Gameboard.GAMEBOARD_COLUMNS + col;
    }

    private void setBlockActorToPosition(BlockActor actor, int newXY, boolean immediately) {
        addActor(actor);
        int posX = getColumnFromInt(newXY) * BlockActor.blockWidth;
        int posY = getRowFromInt(newXY) * BlockActor.blockWidth;
        if (immediately)
            actor.setPosition(posX, posY);
        else if (actor.getX() != posX || actor.getY() != posY)
            actor.setMoveAction(Actions.moveTo(posX, posY, PlayScreen.DURATION_TETRO_MOVE));
    }

    private int getRowFromInt(int newXY) {
        return newXY / Gameboard.GAMEBOARD_COLUMNS;
    }

    private int getColumnFromInt(int newXY) {
        return newXY % Gameboard.GAMEBOARD_COLUMNS;
    }

    public void playReplay() {
        playSpeed = 1f;
        waitTime = 0;
    }

    protected void onTimeChange(int timeMs) {

    }

    protected void onScoreChange(int score) {

    }
}
