package de.golfgl.lightblocks.multiplayer.ai;

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
        // future: take next piece into account

        float bestScore = Float.MAX_VALUE * -1;
        int bestRotation = 0;
        int bestHorizontalMove = 0;

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
                        float score = calculateScoreOfPosition(gameboard, activePiece, horizontalMove, dropVerticalMove, rotation);
                        if (score > bestScore) {
                            bestRotation = rotation;
                            bestHorizontalMove = horizontalMove;
                            bestScore = score;
                        }
                    }

                }
            }
        }

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

    private float calculateScoreOfPosition(Gameboard gameboard, Tetromino activePiece, int horizontalMove, int verticalMove, int rotation) {
        // we need to get the actual gameboard
        int[][] gameboardSquares = gameboard.getGameboardSquares();
        boolean[][] fullSquares = new boolean[Gameboard.GAMEBOARD_ALLROWS][Gameboard.GAMEBOARD_COLUMNS];
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            for (int j = 0; j < Gameboard.GAMEBOARD_COLUMNS; j++) {
                fullSquares[i][j] = gameboardSquares[i][j] != Gameboard.SQUARE_EMPTY;
            }
        }

        // and pretend to pin the tetromino here
        for (Vector2 coord : activePiece.getRotationVectors(rotation)) {
            int x = (int) activePiece.getPosition().x + (int) coord.x + horizontalMove;
            int y = (int) activePiece.getPosition().y + (int) coord.y - verticalMove;
            fullSquares[y][x] = true;
        }

        // new calculate the different dimensions
        int aggregatedHeight = aggregateHeight(fullSquares);
        int completedLines = completedLines(fullSquares);
        int countHoles = countHoles(fullSquares);
        int bumpiness = computeBumpiness(fullSquares);

        return -5.1f * aggregatedHeight + 7.6f * completedLines - 3.66f * countHoles - 51f * bumpiness;
    }

    private int computeBumpiness(boolean[][] fullSquares) {
        int bumpiness = 0;
        int lastHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            int y = Gameboard.GAMEBOARD_ALLROWS;

            while (y > 0 && !fullSquares[y - 1][x])
                y--;

            if (x > 0)
                bumpiness = Math.abs(y - lastHeight);

            lastHeight = y;
        }

        return bumpiness;
    }

    private int countHoles(boolean[][] fullSquares) {
        int holes = 0;
        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            boolean hadFull = false;

            for (int y = Gameboard.GAMEBOARD_ALLROWS - 1; y >= 0; y--) {
                if (fullSquares[y][x])
                    hadFull = true;

                if (hadFull && !fullSquares[y][x])
                    holes++;
            }
        }

        return 0;
    }

    private int completedLines(boolean[][] fullSquares) {
        int fullLines = 0;
        for (int y = 0; y < Gameboard.GAMEBOARD_ALLROWS; y++) {
            boolean hasBlank = false;
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS && !hasBlank; x++) {
                if (!fullSquares[y][x])
                    hasBlank = true;
            }
            if (!hasBlank)
                fullLines++;
        }

        return fullLines;
    }

    private int aggregateHeight(boolean[][] fullSquares) {
        int aggregateHeight = 0;

        for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
            int y = Gameboard.GAMEBOARD_ALLROWS;

            while (y > 0 && !fullSquares[y - 1][x])
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
}
