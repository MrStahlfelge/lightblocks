package de.golfgl.lightblocks.multiplayer.ai;

import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.model.Tetromino;

public interface AiAcessibleGameModel {
    Tetromino getNextTetromino();

    boolean isHoldMoveAllowedByModel();

    boolean isComboScoreAllowedByModel();

    boolean inputHoldActiveTetromino(InputIdentifier inputId);

    boolean isGameOver();

    void inputSetSoftDropFactor(InputIdentifier inputId, float newVal);

    void inputRotate(InputIdentifier inputId, boolean clockwise);

    boolean inputTimelabelTouched(InputIdentifier inputId);

    void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft);

    void inputDoOneHorizontalMove(InputIdentifier inputId, boolean isLeft);

    void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft);

    boolean isModernRotation();

    int getLinesToClear();

    int getMaxBlocksToUse();

    boolean hasSecondGameboard();
}
