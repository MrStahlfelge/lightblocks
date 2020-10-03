package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.MotivationLabel;
import de.golfgl.lightblocks.scene2d.ParticleEffectActor;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.state.Theme;

/**
 * Player's area on the PlayScreen, including score labels
 * <p>
 * In most cases, there is only a single Play Area per PlayScreen, but multiplayer games might
 * have more
 */
public class PlayerArea extends Group {
    private static final int NINE_PATCH_BORDER_SIZE = 5;
    protected final Image imGarbageIndicator;
    protected final BlockGroup blockGroup;
    final ParticleEffectActor weldEffect;
    final Image imComboIndicator;
    final Group labelGroup;
    final BlockActor[][] blockMatrix;
    final BlockActor[] nextTetro;
    final BlockActor[] holdTetro;
    final MotivationLabel motivatorLabel;
    final PlayScreen.PlayScoreTable scoreTable;
    final Label gameType;
    private final LightBlocksGame app;
    private final PlayScreen playScreen;
    ScaledLabel timeLabel;
    private ScoreLabel blocksLeft;
    private Label timeLabelDesc;
    private int currentShownTime;

    public PlayerArea(LightBlocksGame app, PlayScreen playScreen) {
        this.app = app;
        this.playScreen = playScreen;
        setTransform(false);
        setSize(LightBlocksGame.nativeGameWidth, LightBlocksGame.nativeGameHeight);

        // set the gameboard background picture from the theme
        if (app.theme.gameboardPic != null) {
            Image bgGb = new Image(app.theme.gameboardPic);
            bgGb.setScaling(Scaling.none);
            bgGb.pack();
            bgGb.setPosition(getWidth() / 2f - 1, getHeight() / 2f - 1, Align.center);
            addActor(bgGb);
        }

        ParticleEffect pweldEffect = app.theme.getParticleEffect();
        weldEffect = new ParticleEffectActor(pweldEffect, app.theme.particleEffectReset);

        if (!app.theme.particleEffectOnTop)
            addActor(weldEffect);

        switch (app.theme.particleEffectPosition) {
            case clear:
                // nothing to do here, positioned at a clear. no break here on purpose to center it
            case center:
                weldEffect.setPosition(getWidth() / 2f - app.theme.particleEffectWidth / 2f,
                        getHeight() / 2f - app.theme.particleEffectHeight / 2f);
                break;
            case top:
                weldEffect.setPosition(getWidth() / 2f - app.theme.particleEffectWidth / 2f,
                        getHeight() - app.theme.particleEffectHeight);
                break;
            case bottom:
                weldEffect.setPosition(getWidth() / 2f - app.theme.particleEffectWidth / 2f, 0);
                break;
        }


        if (app.theme.particleEffectTrigger == Theme.EffectTrigger.always)
            weldEffect.start();

        blockMatrix = new BlockActor[Gameboard.GAMEBOARD_COLUMNS][Gameboard.GAMEBOARD_ALLROWS];
        nextTetro = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];
        holdTetro = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];

        // Block group holds all blocks, it is the main gameboard
        blockGroup = new BlockGroup(app, true);
        blockGroup.setTransform(false);
        blockGroup.getColor().a = .4f;

        blockGroup.setPosition(getWidth() / 2, getHeight() / 2, Align.center);
        addActor(blockGroup);

        // borders around main gameboard area
        NinePatch line = new NinePatch(app.trGlowingLine, NINE_PATCH_BORDER_SIZE, NINE_PATCH_BORDER_SIZE,
                NINE_PATCH_BORDER_SIZE, NINE_PATCH_BORDER_SIZE);
        Image imLine = new Image(line);

        imLine.setX(blockGroup.getX() + blockGroup.getWidth());
        imLine.setY(blockGroup.getY() - NINE_PATCH_BORDER_SIZE);
        imLine.addAction(Actions.sizeTo(imLine.getWidth(), blockGroup.getGridHeight() +
                2 * NINE_PATCH_BORDER_SIZE, 1f, Interpolation.circleOut));
        imLine.setColor(app.theme.wallColor);
        addActor(imLine);

        imGarbageIndicator = new Image(line);
        imGarbageIndicator.setX(imLine.getX());
        imGarbageIndicator.setY(imLine.getY());
        imGarbageIndicator.setHeight(NINE_PATCH_BORDER_SIZE * 2);
        imGarbageIndicator.setColor(app.theme.emphasizeColor);
        imGarbageIndicator.setVisible(false);
        addActor(imGarbageIndicator);

        imLine = new Image(line);
        imLine.setY(blockGroup.getY() - NINE_PATCH_BORDER_SIZE);
        imLine.setX(blockGroup.getX() - imLine.getWidth() - 2);
        imLine.addAction(Actions.sizeTo(imLine.getWidth(), blockGroup.getGridHeight() +
                2 * NINE_PATCH_BORDER_SIZE, 1f, Interpolation.circleOut));
        imLine.setColor(app.theme.wallColor);
        addActor(imLine);

        imComboIndicator = new Image(line);
        imComboIndicator.setPosition(imLine.getX(), imLine.getY());
        imComboIndicator.setColor(new Color(app.theme.focussedColor));
        imComboIndicator.getColor().a = 0;
        addActor(imComboIndicator);
        showComboHeight(0);

        // show level name (in mosr cases). Wrapped in a group for rotation to work
        Group gameTypeLabels = new Group();
        gameType = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        gameType.setColor(app.theme.titleColor);
        gameTypeLabels.setPosition(imLine.getX(), blockGroup.getY());
        gameTypeLabels.addActor(gameType);
        gameTypeLabels.setRotation(90);

        addActor(gameTypeLabels);

        // Score Labels
        scoreTable = new PlayScreen.PlayScoreTable(app);
        // is initialized no matter what, so that it can get filled when game model is initialized
        blocksLeft = new ScoreLabel(3, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        app.theme.setScoreColor(blocksLeft);
        playScreen.populateScoreTable(scoreTable);
        // don't add the score table now, it is not complete yet

        if (!weldEffect.hasParent())
            addActor(weldEffect);

        labelGroup = new Group();
        labelGroup.setTransform(false);
        labelGroup.setWidth(blockGroup.getWidth());
        labelGroup.setHeight(blockGroup.getGridHeight() - 2);
        labelGroup.setPosition(blockGroup.getX(), blockGroup.getY());
        addActor(labelGroup);

        motivatorLabel = new MotivationLabel(app.skin, labelGroup,
                app.theme.achievementColor, app.theme.achievementShadowColor);

    }

    void gameModelInitialized(GameModel gameModel) {
        // this has to be set after game model is loaded, so that any preset scores don't
        // trigger any animations
        scoreTable.setEmphasizeTresholds();

        if (!gameModel.isGhostPieceAllowedByGameModel())
            blockGroup.setGhostPieceVisibility(false);

        // complete the Score Table
        if (gameModel.showBlocksScore()) {
            scoreTable.row();
            final Label labelBlocks = new ScaledLabel(app.TEXTS.get("labelBlocksScore").toUpperCase(), app.skin);
            app.theme.setScoreColor(labelBlocks);
            scoreTable.add(labelBlocks).right().bottom().padBottom(-2).spaceRight(3);
            scoreTable.add(blocksLeft).left().colspan(3);
        } else if (gameModel.showTime()) {
            scoreTable.row();
            String timeLabelDescString = gameModel.getShownTimeDescription();
            if (timeLabelDescString == null)
                timeLabelDescString = app.TEXTS.get("labelTime").toUpperCase();
            timeLabelDesc = new ScaledLabel(timeLabelDescString, app.skin);
            app.theme.setScoreColor(timeLabelDesc);
            scoreTable.add(timeLabelDesc).right().bottom().padBottom(-2).spaceRight(3);
            timeLabel = new ScaledLabel(ScoreTable.formatTimeString(0, 1), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            app.theme.setScoreColor(timeLabel);
            scoreTable.add(timeLabel).left().colspan(3);
            timeLabel.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return playScreen.gameModel.onTimeLabelTouchedByPlayer();
                }
            });
        }
        scoreTable.setLinesToClear(gameModel.getLinesToClear());
        scoreTable.validate();

        // now add the very last Actors
        addActor(scoreTable);

    }

    public void dispose() {
        weldEffect.dispose();
    }

    void insertBlock(int x, int y, BlockActor block) {
        block.setX(x * BlockActor.blockWidth);
        block.setY(y * BlockActor.blockWidth);
        blockGroup.addActor(block);
        blockMatrix[x][y] = block;
    }

    public void showGarbageAmount(int lines) {
        imGarbageIndicator.setVisible(true);
        imGarbageIndicator.clearActions();
        imGarbageIndicator.addAction(Actions.sizeTo(imGarbageIndicator.getWidth(),
                BlockActor.blockWidth * lines + NINE_PATCH_BORDER_SIZE * 2, .2f, Interpolation.fade));
    }

    public void showComboHeight(int comboHeight) {
        comboHeight = Math.max(0, comboHeight);

        imComboIndicator.clearActions();
        imComboIndicator.addAction(Actions.sizeTo(imGarbageIndicator.getWidth(),
                BlockActor.blockWidth * comboHeight + NINE_PATCH_BORDER_SIZE * 2, .2f, Interpolation.fade));
        imComboIndicator.addAction(comboHeight > 0 ? Actions.fadeIn(.1f, Interpolation.fade)
                : Actions.fadeOut(.2f, Interpolation.fade));
    }

    public void showNextTetro(Integer[][] relativeBlockPositions, int blockType) {
        // the next tetromino is set. It will be displayed on top right of the gameboard.
        // For the incoming animation it will get added to the group at lowest position, so that it does
        // not overlays the game board blocks

        final float offsetX = getNextPieceXPos();
        final float offsetY = getNextPieceYPos();

        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            nextTetro[i] = new BlockActor(app, blockType, true);
            nextTetro[i].setPosition((i == 0 || i == 2) ? -BlockActor.blockWidth : LightBlocksGame.nativeGameWidth +
                            BlockActor.blockWidth,
                    (i >= 2) ? 0 : LightBlocksGame.nativeGameHeight);
            nextTetro[i].setMoveAction(Actions.moveTo(offsetX + relativeBlockPositions[i][0] * BlockActor.blockWidth,
                    offsetY + relativeBlockPositions[i][1] * BlockActor.blockWidth, .5f, Interpolation.fade));
            nextTetro[i].addAction(Actions.alpha(app.theme.nextPieceAlpha, .5f, Interpolation.fade));
            nextTetro[i].getColor().a = 0;

            blockGroup.addBlockAtBottom(nextTetro[i]);
        }
    }

    protected float getNextPieceYPos() {
        return (Gameboard.GAMEBOARD_NORMALROWS - (playScreen.gameModel.isModernRotation() ? 1 : 0) + .3f) * BlockActor.blockWidth;
    }

    protected float getNextPieceXPos() {
        return getWidth() - blockGroup.getX() - (Tetromino.TETROMINO_BLOCKCOUNT -
                .3f) * BlockActor.blockWidth + Math.min(getX() / 2, BlockActor.blockWidth * 2.5f);
    }

    public void activateNextTetro(Integer[][] boardBlockPositions, int blockType, int ghostPieceDistance) {
        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            // move the already instantiated block to the correct position
            BlockActor block = nextTetro[i];

            final int x = boardBlockPositions[i][0];
            final int y = boardBlockPositions[i][1];

            if (block == null) {
                // null at game start, animations make no sense then. So just insert right at the
                // correct position.
                block = new BlockActor(app, blockType, true);
                insertBlock(x, y, block);
            } else {
                nextTetro[i] = null;
                blockMatrix[x][y] = block;
                block.clearActions();
                block.addAction(Actions.fadeIn(.1f));
                block.setMoveAction(Actions.moveTo(x * BlockActor.blockWidth, y * BlockActor.blockWidth, .1f,
                        Interpolation.fade));
            }
            block.setEnlightened(true);

            blockGroup.setGhostPiecePosition(i, x, y - ghostPieceDistance, ghostPieceDistance);
        }
    }

    void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions,
                                Integer[][] newActivePiecePositions, int ghostPieceDistance, int holdBlockType) {
        float offsetX;
        float offsetY = getNextPieceYPos();

        if (playScreen.isLandscape())
            offsetX = blockGroup.getX() - (Tetromino.TETROMINO_BLOCKCOUNT + 1.5f) * BlockActor.blockWidth;
        else
            offsetX = getNextPieceXPos() - (Tetromino.TETROMINO_BLOCKCOUNT + .5f) * BlockActor.blockWidth;

        final BlockActor[] oldHoldTetro = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];

        // move the active tetromino to hold pos
        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            oldHoldTetro[i] = holdTetro[i];

            if (oldActivePiecePositions != null) {
                final int oldX = oldActivePiecePositions[i][0];
                final int oldY = oldActivePiecePositions[i][1];

                holdTetro[i] = blockMatrix[oldX][oldY];
                blockMatrix[oldX][oldY] = null;
            } else {
                // add when game state is loaded
                holdTetro[i] = new BlockActor(app, holdBlockType, true);
                blockGroup.addActor(holdTetro[i]);
            }

            holdTetro[i].setMoveAction(Actions.moveTo(offsetX + newHoldPiecePositions[i][0] * BlockActor.blockWidth,
                    offsetY + newHoldPiecePositions[i][1] * BlockActor.blockWidth, .1f, Interpolation.fade));
            holdTetro[i].addAction(Actions.alpha(app.theme.nextPieceAlpha, .5f, Interpolation.fade));
            holdTetro[i].setEnlightened(false);
        }

        // pull the hold tetromino
        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            if (newActivePiecePositions != null) {
                final int newX = newActivePiecePositions[i][0];
                final int newY = newActivePiecePositions[i][1];
                blockMatrix[newX][newY] = oldHoldTetro[i];
                oldHoldTetro[i].addAction(Actions.fadeIn(.1f));
                oldHoldTetro[i].setMoveAction(Actions.moveTo(newX * BlockActor.blockWidth, newY * BlockActor.blockWidth,
                        .1f, Interpolation.fade));

                blockGroup.setGhostPiecePosition(i, newX, newY - ghostPieceDistance, ghostPieceDistance);
            }
        }
    }

    public void addMotivationText(String text, float duration) {
        motivatorLabel.addMotivationText(text, duration);
    }

    public void updateScore(GameScore score) {
        scoreTable.setClearedLines(score.getClearedLines());
        scoreTable.setCurrentLevel(score.getCurrentLevel());
        scoreTable.setScore(score.getScore());

        GameModel gameModel = playScreen.gameModel;
        if (gameModel.showBlocksScore())
            blocksLeft.setScore(gameModel.getMaxBlocksToUse() > 0 ?
                    gameModel.getMaxBlocksToUse() - score.getDrawnTetrominos() : score.getDrawnTetrominos());
    }

    protected void updateTimeLabel() {
        GameModel gameModel = playScreen.gameModel;
        if (gameModel.showTime() && timeLabel != null) {
            int timeMs = gameModel.getShownTimeMs();

            if (Math.abs(timeMs - currentShownTime) >= 100) {
                timeLabel.clearActions();

                String timeDesc = gameModel.getShownTimeDescription();
                Color timeLabelColor = gameModel.getShownTimeColor();

                String formattedTime = ScoreTable.formatTimeString(timeMs, 1);

                if (timeDesc != null)
                    timeLabelDesc.setText(timeDesc);

                if (timeLabelColor != null && !timeLabel.getColor().equals(timeLabelColor))
                    timeLabel.setColor(timeLabelColor);

                timeLabel.setText(formattedTime);
                currentShownTime = timeMs;
            }
        }
    }

    public void setGameOver() {
        blockGroup.setGhostPieceVisibility(false);
        // forces a last refresh on next render() call
        currentShownTime = currentShownTime - 100;
    }
}
