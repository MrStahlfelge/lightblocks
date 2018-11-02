package de.golfgl.lightblocks.scene2d;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 01.11.2018.
 */

public class ReplayGameboard extends BlockGroup {
    private Replay replay;
    private float playSpeed = 0f;
    private Replay.ReplayGameboardStep shownGameboardStep;
    private Replay.ReplayStep shownStep;
    private Replay.ReplayStep nextStep;
    private float waitTime;
    private BlockActor[] currentShownBlocks;
    private BlockActor[] activePiece;

    public ReplayGameboard(LightBlocksGame app) {
        super(app);
        setGhostPieceVisibility(false);

        currentShownBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_ALLROWS];
        activePiece = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        for (int i = 0; i < activePiece.length; i++) {
            activePiece[i] = new BlockActor(app, Tetromino.TETRO_IDX_L);
            activePiece[i].setEnlightened(true, true);
        }
    }

    @Override
    public void act(float delta) {
        if (nextStep != null && playSpeed > 0) {
            waitTime = waitTime - delta * playSpeed;

            if (waitTime <= 0) {
                shownStep = nextStep;
                transitionGameboard(replay.getCurrentGameboardStep());
                nextStep = replay.getNextStep();
                if (nextStep != null)
                    waitTime = (float) (nextStep.timeMs - shownStep.timeMs) / 1000;

                transitionToStep(shownStep);

                // TODO wait time bei Drop/Line Clear etwas erhöhen
            }
        }

        super.act(delta);
    }

    private void transitionToStep(Replay.ReplayStep shownStep) {
        if (shownStep instanceof Replay.ReplayActivePieceStep) {
            int[] activePiecePosition = ((Replay.ReplayActivePieceStep) shownStep).activePiecePosition;
            for (int i = 0; i < activePiece.length; i++)
                setBlockActorToPosition(activePiece[i], activePiecePosition[i]);
        } else if (shownStep instanceof Replay.MovePieceStep) {
            int moveX = ((Replay.MovePieceStep) shownStep).getMoveX() * BlockActor.blockWidth;
            int moveY = ((Replay.MovePieceStep) shownStep).getMoveY() * BlockActor.blockWidth;
            for (int i = 0; i < activePiece.length; i++) {
                activePiece[i].setX(activePiece[i].getX() + moveX);
                activePiece[i].setY(activePiece[i].getY() - moveY);
            }
        }
    }

    private void transitionGameboard(Replay.ReplayGameboardStep gameboardStep) {
        if (this.shownGameboardStep != gameboardStep && gameboardStep != null) {
            this.shownGameboardStep = gameboardStep;

            for (int i = 0; i < gameboardStep.gameboard.length; i++) {
                boolean shouldHaveActor = gameboardStep.gameboard[i] != Gameboard.SQUARE_EMPTY;
                if (currentShownBlocks[i] != null && !shouldHaveActor) {
                    // entfernen
                    currentShownBlocks[i].clearActions();
                    currentShownBlocks[i].remove();
                    currentShownBlocks[i] = null;
                } else if (currentShownBlocks[i] == null && shouldHaveActor) {
                    currentShownBlocks[i] = new BlockActor(app, Tetromino.TETRO_IDX_L);
                    setBlockActorToPosition(currentShownBlocks[i], i);
                }
            }

            //TODO volle Reihen highlighten
        }
    }

    private void setBlockActorToPosition(BlockActor actor, int newXY) {
        addActor(actor);
        actor.setPosition(newXY % Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth,
                (int) newXY / Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth);
    }

    public void setReplay(Replay replay) {
        this.replay = replay;
    }

    public void playReplay() {
        playSpeed = 1f;
        nextStep = replay.getFirstStep();
        waitTime = 0;
    }
}
