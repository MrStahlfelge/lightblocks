package de.golfgl.lightblocks.state;

/**
 * Created by Benjamin Schulte on 20.02.2017.
 */
public interface IRoundScore {
    int getScore();

    int getClearedLines();

    int getDrawnTetrominos();

    int getRating();
}
