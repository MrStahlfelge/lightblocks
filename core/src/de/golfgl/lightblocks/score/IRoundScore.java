package de.golfgl.lightblocks.score;

/**
 * Created by Benjamin Schulte on 20.02.2017.
 */
public interface IRoundScore {
    int getScore();

    int getClearedLines();

    int getDrawnTetrominos();
}
