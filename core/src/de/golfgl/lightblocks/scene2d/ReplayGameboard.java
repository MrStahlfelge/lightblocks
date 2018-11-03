package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;

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

    public ReplayGameboard(LightBlocksGame app, Replay replay) {
        super(app);
        setGhostPieceVisibility(false);

        this.replay = replay;
        nextStep = replay.getNextStep();

        currentShownBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_ALLROWS];
        activePieceBlock = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        activePiecePos = new int[Tetromino.TETROMINO_BLOCKCOUNT];
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
                shownStep = nextStep;
                transitionGameboard(replay.getCurrentGameboard());
                nextStep = replay.getNextStep();
                if (nextStep != null)
                    waitTime = (float) (nextStep.timeMs - shownStep.timeMs) / 1000;

                transitionActivePiece(shownStep);

                onTimeChange(shownStep.timeMs);
                if (shownStep.isDropStep()) {
                    onScoreChange(replay.getCurrentScore());
                    clearLinesOnGameboard();

                    // falls Clear oder Insert: DURATION_REMOVE_DELAY dazu
                    waitTime = waitTime + .2f;
                }
            }
        }

        super.act(delta * playSpeed);
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
        for (int i = 0; i < gameboard.length; i++) {
            boolean shouldHaveActor = gameboard[i] != Gameboard.SQUARE_EMPTY;
            if (currentShownBlocks[i] != null && !shouldHaveActor) {
                // entfernen
                currentShownBlocks[i].clearActions();
                currentShownBlocks[i].remove();
                currentShownBlocks[i] = null;
            } else if (currentShownBlocks[i] == null && shouldHaveActor) {
                currentShownBlocks[i] = new BlockActor(app, Tetromino.TETRO_IDX_L);
                currentShownBlocks[i].setEnlightened(true, true);
                setBlockActorToPosition(currentShownBlocks[i], i, true);
                currentShownBlocks[i].addAction(Actions.delay(.05f,
                        Actions.run(currentShownBlocks[i].getDislightenAction())));
            }
        }
    }

    private void clearLinesOnGameboard() {

    }

    private void setBlockActorToPosition(BlockActor actor, int newXY, boolean immediately) {
        addActor(actor);
        int posX = newXY % Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth;
        int posY = (int) newXY / Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth;
        if (immediately)
            actor.setPosition(posX, posY);
        else
            actor.setMoveAction(Actions.moveTo(posX, posY, PlayScreen.DURATION_TETRO_MOVE));
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
