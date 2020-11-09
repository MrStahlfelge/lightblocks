package de.golfgl.lightblocks.multiplayer.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
    private final Queue<Movement> holdArrayList = new Queue<>();
    private final float heightFactor;
    private final float completeLinesFactor;
    private final float holesFactor;
    private final float bumpinessFactor;
    private float slowDown;

    public ArtificialPlayer(AiAcessibleGameModel aiGameModel, AiAcessibleGameModel opponentGameModel) {
        this.aiGameModel = aiGameModel;
        this.opponentGameModel = opponentGameModel;

        // 0.510066, 0.760666, 0.35663, 0.184483
        heightFactor = 5.1f;
        completeLinesFactor = 7.6f;
        holesFactor = 3.66f;
        bumpinessFactor = 1.8f;
    }

    public void onNextPiece(Gameboard gameboard, Tetromino activePiece) {
        // we have a new active piece. check how to place it best and add the needed movements to
        // the movement queue

        // future AI improvement possibilities:
        // - take waiting garbage into account by adding it to board for next piece, but not for clears (when modern)
        // - go for singles after a certain height is reached
        // - at the moment, line clears are not considered on their own but for all next pieces together - improve

        float bestScore = Float.NEGATIVE_INFINITY;

        Tetromino nextPiece = aiGameModel.getNextTetromino();

        Array<Tetromino> nextPieces = new Array<>();
        nextPieces.add(activePiece);
        nextPieces.add(nextPiece);

        bestScore = checkAllRotationsAndDropPlaces(new AiGameboard(gameboard), nextPieces, 0, bestScore, movementArrayList);

        if (aiGameModel.isHoldMoveAllowedByModel()) {
            // compare found best movement with hold piece
            Tetromino holdTetromino = aiGameModel.getHoldTetromino();

            nextPieces.clear();
            if (holdTetromino != null) {
                nextPieces.add(holdTetromino);
            }
            nextPieces.add(nextPiece);

            float holdMoveScore = checkAllRotationsAndDropPlaces(new AiGameboard(gameboard), nextPieces, 0, bestScore, holdArrayList);

            if (holdMoveScore > bestScore) {
                Gdx.app.debug("AI", "Hold the piece");
                movementArrayList.clear();
                movementArrayList.addFirst(Movement.HOLD);
                while (holdArrayList.notEmpty())
                    movementArrayList.addLast(holdArrayList.removeFirst());
            }
        }
        slowDown = .2f;
    }

    private float checkAllRotationsAndDropPlaces(AiGameboard gameboard, Array<Tetromino> nextPieces, int depth, float overallBestScore, Queue<Movement> movementArrayList) {
        Tetromino activePiece = nextPieces.get(depth);
        int bestRotation = 0;
        int bestHorizontalMove = 0;
        float bestScore = Float.NEGATIVE_INFINITY;

        // check all rotations and all drop places
        for (int rotation = 0; rotation < 4; rotation++) {
            if (activePiece.hasRotation(rotation)) {
                for (int horizontalMove = -Gameboard.GAMEBOARD_COLUMNS; horizontalMove <= Gameboard.GAMEBOARD_COLUMNS; horizontalMove++) {
                    AiGameboard aiGameboard = new AiGameboard(gameboard);
                    int dropVerticalMove = -1;

                    for (int verticalMove = 0; verticalMove <= Gameboard.GAMEBOARD_ALLROWS &&
                            dropVerticalMove + 1 == verticalMove; verticalMove++) {
                        tempPos.set(activePiece.getPosition().x + horizontalMove,
                                activePiece.getPosition().y - verticalMove);
                        if (aiGameboard.isValidPosition(activePiece, tempPos, rotation)) {
                            dropVerticalMove = verticalMove;
                        }
                    }

                    if (dropVerticalMove >= 0) {
                        // we have found the drop position for the current rotation
                        // and pretend to pin the tetromino here
                        for (Vector2 coord : activePiece.getRotationVectors(rotation)) {
                            int x = (int) activePiece.getPosition().x + (int) coord.x + horizontalMove;
                            int y = (int) activePiece.getPosition().y + (int) coord.y - dropVerticalMove;
                            aiGameboard.setPosition(x, y, true);
                        }

                        float score;
                        if (depth == nextPieces.size - 1) {
                            score = calculateScoreOfPosition(aiGameboard);
                        } else {
                            score = checkAllRotationsAndDropPlaces(aiGameboard, nextPieces, depth + 1, 0, null);
                        }

                        if (score > bestScore ||
                                // if we found an equal score, use it to prevent a left-hang
                                MathUtils.isEqual(score, bestScore) && MathUtils.randomBoolean()
                                        // but avoid unnecessary rotations
                                        && bestHorizontalMove != horizontalMove) {
                            bestRotation = rotation;
                            bestHorizontalMove = horizontalMove;
                            bestScore = score;
                        }
                    }

                }
            }
        }


        // now we found the best position, add the necessary movements to the queue
        if (movementArrayList != null && bestScore > overallBestScore) {
            Gdx.app.debug("AI", "Move: h " + bestHorizontalMove + ", rotate " + bestRotation);
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

        return bestScore;
    }

    private float calculateScoreOfPosition(AiGameboard gameboard) {

        // calculate the different dimensions
        int completedLines = gameboard.clearFullLines();

        float completedLinesVal;
        if (completedLines == 1) {
            // we don't want to go for single lines, but they are rewarded because of lower height
            // of 10. So counterbalance this here
            completedLinesVal = -10;
        } else {
            completedLinesVal = Math.max(0, (completedLines - 1) * (completedLines - 1));
        }

        float aggregatedHeight = aggregateWeightedHeight(gameboard);
        float countHoles = countHoles(gameboard);
        int bumpiness = computeBumpiness(gameboard);

        return -heightFactor * aggregatedHeight + completeLinesFactor * completedLinesVal - holesFactor * countHoles - bumpinessFactor * bumpiness;
    }

    private int computeBumpiness(AiGameboard gameboard) {
        int bumpiness = 0;
        int lastHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            int y = gameboard.getColumnHeight(x);

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
            int y = gameboard.getColumnHeight(x);
            aggregateHeight = aggregateHeight + y;
        }

        return aggregateHeight;
    }

    private float aggregateWeightedHeight(AiGameboard gameboard) {
        float aggregateHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            // pos weight 0123454321 => 1.0, 1.07, 1.14, ...
            // the more the column is centered, the more it is avoided drop pieces
            float posWeight = 1f + Math.abs(Math.abs(x - (Gameboard.GAMEBOARD_COLUMNS / 2f)) - Gameboard.GAMEBOARD_COLUMNS / 2f) / 15f;
            int y = gameboard.getColumnHeight(x);

            aggregateHeight = aggregateHeight + y * posWeight;
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
                    // wait for the human
                    if (opponentGameModel.getScore().getDrawnTetrominos() + 10 >= aiGameModel.getScore().getDrawnTetrominos())
                        aiGameModel.inputSetSoftDropFactor(null, GameModel.FACTOR_HARD_DROP);
                    else
                        movementArrayList.addFirst(movement);
                    break;
                case HOLD:
                    aiGameModel.inputHoldActiveTetromino(null);
                    break;
            }

            slowDown = .1f;
        }
    }

    enum Movement {MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT, DROP, HOLD}

    static class AiGameboard {
        final boolean[][] fullSquares = new boolean[Gameboard.GAMEBOARD_ALLROWS][Gameboard.GAMEBOARD_COLUMNS];

        AiGameboard(Gameboard gameboard) {
            int[][] gameboardSquares = gameboard.getGameboardSquares();
            for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
                for (int j = 0; j < Gameboard.GAMEBOARD_COLUMNS; j++) {
                    fullSquares[i][j] = gameboardSquares[i][j] != Gameboard.SQUARE_EMPTY;
                }
            }
        }

        AiGameboard(AiGameboard gameboard) {
            for (int y = 0; y < Gameboard.GAMEBOARD_ALLROWS; y++) {
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    fullSquares[y][x] = gameboard.isPositionFull(x, y);
                }
            }
        }

        boolean isPositionFull(int x, int y) {
            return fullSquares[y][x];
        }

        boolean isValidCoordinate(int x, int y) {
            if (x < 0 || x >= Gameboard.GAMEBOARD_COLUMNS) {
                return false;
            }

            if (y < 0 || y >= Gameboard.GAMEBOARD_ALLROWS) {
                return false;
            }

            return (!isPositionFull(x, y));
        }

        public boolean isValidPosition(Tetromino tetromino, Vector2 testPosition, int testRotation) {
            for (Vector2 v : tetromino.getRotationVectors(testRotation)) {
                int posX = ((int) v.x + (int) testPosition.x);
                int posY = ((int) v.y + (int) testPosition.y);

                if (!isValidCoordinate(posX, posY)) {
                    return false;
                }
            }

            return true;
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

        public int getColumnHeight(int x) {
            int y = Gameboard.GAMEBOARD_ALLROWS;
            while (y > 0 && !isPositionFull(x, y - 1))
                y--;
            return y;
        }
    }
}
