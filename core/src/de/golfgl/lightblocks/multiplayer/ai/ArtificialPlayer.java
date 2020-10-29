package de.golfgl.lightblocks.multiplayer.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;

/**
 * An AI based on https://github.com/LeeYiyuan/tetrisai /
 * https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/
 * arranged with the intent to let the opponent human have fun
 */
public class ArtificialPlayer {
    private final AiAcessibleGameModel aiGameModel;
    private final AiAcessibleGameModel opponentGameModel;

    private final Vector2 tempPos = new Vector2();
    private final Queue<Movement> movementArrayList = new Queue<>();
    private float slowDown;

    public ArtificialPlayer(AiAcessibleGameModel aiGameModel, AiAcessibleGameModel opponentGameModel) {
        this.aiGameModel = aiGameModel;
        this.opponentGameModel = opponentGameModel;
    }

    public void onNextPiece(Gameboard gameboard, Tetromino activePiece) {
        // we have a new active piece. check how to place it best and add the needed movements to
        // the movement queue
        // future:
        // - take next piece into account
        // - hold I piece or pieces over a certain treshold and take hold piece into account
        // - always hold beginning Z or S
        // - take waiting garbage into account by adding it to height, but not for clears (when modern)
        // - keep the center over row 15 free


        float bestScore = Float.NEGATIVE_INFINITY;
        int bestRotation = 0;
        int bestHorizontalMove = 0;

        Tetromino nextPiece = aiGameModel.getNextTetromino();

        // check all rotations and all drop places
        for (int rotation = 0; rotation < 4; rotation++) {
            if (activePiece.hasRotation(rotation)) {
                for (int horizontalMove = -Gameboard.GAMEBOARD_COLUMNS; horizontalMove <= Gameboard.GAMEBOARD_COLUMNS; horizontalMove++) {
                    int dropVerticalMove = -1;

                    for (int verticalMove = 0; verticalMove <= Gameboard.GAMEBOARD_ALLROWS &&
                            dropVerticalMove + 1 == verticalMove; verticalMove++) {
                        tempPos.set(activePiece.getPosition().x + horizontalMove,
                                activePiece.getPosition().y - verticalMove);
                        if (gameboard.isValidPosition(activePiece, tempPos, rotation)) {
                            dropVerticalMove = verticalMove;
                        }
                    }

                    if (dropVerticalMove >= 0) {
                        // we have found the drop position for the current rotation
                        float score = calculateScoreOfPosition(new AiGameboard(gameboard), activePiece, nextPiece, horizontalMove, dropVerticalMove, rotation);
                        if (score > bestScore) {
                            bestRotation = rotation;
                            bestHorizontalMove = horizontalMove;
                            bestScore = score;
                        }
                    }

                }
            }
        }

        Gdx.app.log("AI", "Move: h " + bestHorizontalMove + ", rotate " + bestRotation);

        // now we found the best position, add the necessary movements to the queue
        movementArrayList.clear();

        switch (bestRotation) {
            case 2:
                movementArrayList.addLast(Movement.ROTATE_RIGHT);
                movementArrayList.addLast(Movement.ROTATE_RIGHT);
                break;
            case 1:
                movementArrayList.addLast(Movement.ROTATE_RIGHT);
                break;
            case 3:
                movementArrayList.addLast(Movement.ROTATE_LEFT);
                break;
        }

        for (int i = 0; i < Math.abs(bestHorizontalMove); i++) {
            movementArrayList.addLast(bestHorizontalMove < 0 ? Movement.MOVE_LEFT : Movement.MOVE_RIGHT);
        }

        movementArrayList.addLast(Movement.DROP);
    }

    private float calculateScoreOfPosition(AiGameboard gameboard, Tetromino activePiece, Tetromino nextPiece,
                                           int horizontalMove, int verticalMove, int rotation) {

        // and pretend to pin the tetromino here
        for (Vector2 coord : activePiece.getRotationVectors(rotation)) {
            int x = (int) activePiece.getPosition().x + (int) coord.x + horizontalMove;
            int y = (int) activePiece.getPosition().y + (int) coord.y - verticalMove;
            gameboard.setPosition(x, y, true);
        }

        // calculate the different dimensions
        int completedLines = gameboard.clearFullLines();

        float completedLinesVal;
        if (completedLines == 1) {
            // we don't want to go for single lines, but they are rewared because of lower height
            // of 10. So counterbalance this here
            completedLinesVal = -10;
        } else {
            completedLinesVal = Math.max(0, (completedLines - 1) * (completedLines - 1));
        }

        int aggregatedHeight = aggregateHeight(gameboard);
        float countHoles = countHoles(gameboard);
        int bumpiness = computeBumpiness(gameboard);

        // 0.510066, 0.760666, 0.35663, 0.184483
        return -5.1f * aggregatedHeight + 7.6f * completedLinesVal - 3.66f * countHoles - 1.8f * bumpiness;
    }

    private int computeBumpiness(AiGameboard gameboard) {
        int bumpiness = 0;
        int lastHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            int y = Gameboard.GAMEBOARD_ALLROWS;

            while (y > 0 && !gameboard.isPositionFull(x, y - 1))
                y--;

            if (x > 0)
                bumpiness = bumpiness + Math.abs(y - lastHeight);

            lastHeight = y;
        }

        return bumpiness;
    }

    private float countHoles(AiGameboard gameboard) {
        float holes = 0;
        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            // multiply holes with the number of blocks above the holes
            // this takes into account that it is more difficult to fill them the more to clear there is
            int numFull = 0;

            for (int y = Gameboard.GAMEBOARD_ALLROWS - 1; y >= 0; y--) {
                if (gameboard.isPositionFull(x, y))
                    numFull++;

                if (!gameboard.isPositionFull(x, y) && numFull > 0)
                    holes = holes + 1 + (MathUtils.log2(numFull));
            }
        }

        return holes;
    }

    private int aggregateHeight(AiGameboard gameboard) {
        int aggregateHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            int y = Gameboard.GAMEBOARD_ALLROWS;

            while (y > 0 && !gameboard.isPositionFull(x, y - 1))
                y--;

            aggregateHeight = aggregateHeight + y;
        }

        return aggregateHeight;
    }

    public void update(float delta, Tetromino activePiece) {
        // process the queue, but slow down

        slowDown -= delta;

        if (slowDown < 0 && !movementArrayList.isEmpty()) {
            Movement movement = movementArrayList.removeFirst();

            switch (movement) {
                case MOVE_LEFT:
                    aiGameModel.inputDoOneHorizontalMove(null, true);
                    break;
                case MOVE_RIGHT:
                    aiGameModel.inputDoOneHorizontalMove(null, false);
                    break;
                case ROTATE_RIGHT:
                    aiGameModel.inputRotate(null, true);
                    break;
                case ROTATE_LEFT:
                    aiGameModel.inputRotate(null, false);
                    break;
                case DROP:
                    aiGameModel.inputSetSoftDropFactor(null, GameModel.FACTOR_HARD_DROP);
                    break;
            }

            slowDown = .2f;
        }
    }

    enum Movement {MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT, DROP}

    class AiGameboard {
        final boolean[][] fullSquares = new boolean[Gameboard.GAMEBOARD_ALLROWS][Gameboard.GAMEBOARD_COLUMNS];

        AiGameboard(Gameboard gameboard) {
            // we need to get the actual gameboard
            int[][] gameboardSquares = gameboard.getGameboardSquares();
            for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
                for (int j = 0; j < Gameboard.GAMEBOARD_COLUMNS; j++) {
                    fullSquares[i][j] = gameboardSquares[i][j] != Gameboard.SQUARE_EMPTY;
                }
            }
        }

        boolean isPositionFull(int x, int y) {
            return fullSquares[y][x];
        }

        void setPosition(int x, int y, boolean full) {
            fullSquares[y][x] = full;
        }

        int clearFullLines() {
            int fullLines = 0;
            for (int y = Gameboard.GAMEBOARD_ALLROWS - 1; y >= 0; y--) {
                boolean hasBlank = false;
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS && !hasBlank; x++) {
                    if (!isPositionFull(x, y))
                        hasBlank = true;
                }
                if (!hasBlank) {
                    fullLines++;

                    // move everything down
                    for (int y2 = y; y2 < Gameboard.GAMEBOARD_ALLROWS; y2++) {
                        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                            if (y2 < Gameboard.GAMEBOARD_ALLROWS - 1) {
                                fullSquares[y2][x] = fullSquares[y2 + 1][x];
                            } else {
                                fullSquares[y2][x] = false;

                            }
                        }
                    }
                }
            }

            return fullLines;
        }
    }
}
