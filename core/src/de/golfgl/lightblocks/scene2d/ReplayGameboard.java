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
    private Replay replay;
    private float playSpeed = 0f;
    private Replay.ReplayGameboardStep shownGameboardStep;
    private Replay.ReplayStep shownStep;
    private Replay.ReplayStep nextStep;
    private float waitTime;
    private BlockActor[] currentShownBlocks;
    private BlockActor[] activePieceBlock;
    private int[] activePiecePos;

    public ReplayGameboard(LightBlocksGame app) {
        super(app);
        setGhostPieceVisibility(false);

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
                transitionGameboard(replay.getCurrentGameboardStep());
                nextStep = replay.getNextStep();
                if (nextStep != null)
                    waitTime = (float) (nextStep.timeMs - shownStep.timeMs) / 1000;

                transitionToStep(shownStep);

                if (shownStep instanceof Replay.ReplayDropPieceStep) {
                    // TODO Score anzeigen

                    // falls Clear oder Insert: DURATION_REMOVE_DELAY dazu
                    waitTime = waitTime + .2f / playSpeed;
                }
            }
        }

        super.act(delta);
    }

    private void transitionToStep(Replay.ReplayStep shownStep) {
        if (shownStep instanceof Replay.ReplayActivePieceStep) {
            int[] activePiecePosition = ((Replay.ReplayActivePieceStep) shownStep).activePiecePosition;
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePosition[i];
                setBlockActorToPosition(activePieceBlock[i], activePiecePosition[i],
                        shownStep instanceof Replay.ReplayNextPieceStep);
            }
        } else if (shownStep instanceof Replay.MovePieceStep) {
            int move = ((Replay.MovePieceStep) shownStep).getMoveX()
                    - Gameboard.GAMEBOARD_COLUMNS * ((Replay.MovePieceStep) shownStep).getMoveY();
            for (int i = 0; i < activePieceBlock.length; i++) {
                activePiecePos[i] = activePiecePos[i] + move;
                setBlockActorToPosition(activePieceBlock[i], activePiecePos[i], false);
            }
        }

        // TODO bei Drop ganze Zeilen highlighten und bei NewPieceStep "abbauen"
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
                    currentShownBlocks[i].setEnlightened(true, true);
                    setBlockActorToPosition(currentShownBlocks[i], i, true);
                    currentShownBlocks[i].addAction(Actions.delay(.05f,
                            Actions.run(currentShownBlocks[i].getDislightenAction())));
                }
            }
        }
    }

    private void setBlockActorToPosition(BlockActor actor, int newXY, boolean immediately) {
        addActor(actor);
        int posX = newXY % Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth;
        int posY = (int) newXY / Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth;
        if (immediately)
            actor.setPosition(posX, posY);
        else
            actor.setMoveAction(Actions.moveTo(posX, posY, PlayScreen.DURATION_TETRO_MOVE / playSpeed));
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
