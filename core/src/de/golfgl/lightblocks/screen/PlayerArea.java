package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.VibrationType;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.model.BackendBattleModel;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.MotivationLabel;
import de.golfgl.lightblocks.scene2d.ParticleEffectActor;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.state.Theme;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Player's area on the PlayScreen, including score labels
 * <p>
 * In most cases, there is only a single Play Area per PlayScreen, but multiplayer games might
 * have more
 */
public class PlayerArea extends Group implements IGameModelListener {
    public static final float DURATION_TETRO_MOVE = 1 / 30f;
    public static final float DURATION_REMOVE_DELAY = .15f;
    public static final float DURATION_REMOVE_FADEOUT = .2f;
    private static final int NINE_PATCH_BORDER_SIZE = 5;
    protected final Image imGarbageIndicator;
    protected final BlockGroup blockGroup;
    private final ParticleEffectActor weldEffect;
    private final Image imComboIndicator;
    final Group labelGroup;
    private final BlockActor[][] blockMatrix;
    private final BlockActor[] nextTetro;
    private final BlockActor[] holdTetro;
    private final MotivationLabel motivatorLabel;
    final PlayScreen.PlayScoreTable scoreTable;
    final Label gameType;
    private final LightBlocksGame app;
    private final PlayScreen playScreen;
    GameModel gameModel;
    private ScaledLabel timeLabel;
    private boolean noLineClearAnimation;
    private ScoreLabel blocksLeft;
    private Label timeLabelDesc;
    private int currentShownTime;
    private boolean isLandscapeArrangement;

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

    void gameModelInitialized() {
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
                    return gameModel.onTimeLabelTouchedByPlayer();
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

    @Override
    public void insertNewBlock(int x, int y, int blockType) {
        BlockActor block = new BlockActor(app, blockType, true);
        insertBlock(x, y, block);
    }

    private void insertBlock(int x, int y, BlockActor block) {
        block.setX(x * BlockActor.blockWidth);
        block.setY(y * BlockActor.blockWidth);
        blockGroup.addActor(block);
        blockMatrix[x][y] = block;
    }

    @Override
    public void moveTetro(Integer[][] v, int dx, int dy, int ghostPieceDistance) {
        if (dx != 0 && app.localPrefs.isPlaySounds() && app.theme.horizontalMoveSound != null)
            app.theme.horizontalMoveSound.play();

        if (dx != 0 || dy != 0) {
            // remove every block from gameboard at first...
            Array<BlockActor> blocks = removeBlockActorsFromMatrix(v);

            //... and then put them back
            for (int i = 0; i < v.length; i++) {
                BlockActor block = blocks.get(i);
                int x = v[i][0];
                int y = v[i][1];
                block.setMoveAction(Actions.moveTo((x + dx) * BlockActor.blockWidth, (y + dy) * BlockActor
                        .blockWidth, DURATION_TETRO_MOVE));
                blockMatrix[x + dx][y + dy] = block;
                blockGroup.setGhostPiecePosition(i, x + dx, y - ghostPieceDistance, ghostPieceDistance);
            }
        }
    }

    private Array<BlockActor> removeBlockActorsFromMatrix(Integer[][] v) {
        Array<BlockActor> blocks = new Array<>(v.length);

        for (Integer[] xy : v) {
            if (blockMatrix[xy[0]][xy[1]] == null)
                Gdx.app.error("BLOCKS", "Block null at " + xy[0].toString() + " " + xy[1].toString());

            blocks.add(blockMatrix[xy[0]][xy[1]]);
            blockMatrix[xy[0]][xy[1]] = null;
        }
        return blocks;
    }

    @Override
    public void rotateTetro(Integer[][] vOld, Integer[][] vNew, int ghostPieceDistance) {
        if (app.localPrefs.isPlaySounds() && app.theme.rotateSound != null)
            app.theme.rotateSound.play();

        // remove every block from gameboard at first...
        Array<BlockActor> blocks = removeBlockActorsFromMatrix(vOld);

        //... and then put them back
        for (int i = 0; i < vOld.length; i++) {
            BlockActor block = blocks.get(i);
            int newx = vNew[i][0];
            int newy = vNew[i][1];
            block.setMoveAction(Actions.moveTo((newx) * BlockActor.blockWidth, (newy) * BlockActor.blockWidth, 1 /
                    20f));
            blockMatrix[newx][newy] = block;
            blockGroup.setGhostPiecePosition(i, newx, newy - ghostPieceDistance, ghostPieceDistance);
        }


    }

    @Override
    public void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {
        final float moveActorsTime = .1f;

        int linesToInsert = (garbageHolePosition == null ? 0 : garbageHolePosition.length);

        if (linesToRemove.size <= 0 && linesToInsert <= 0)
            return;

        if (noLineClearAnimation) {
            // enlighten the full row and you are done
            for (int i = linesToRemove.size - 1; i >= 0; i--) {
                int y = linesToRemove.get(i);

                // enlighten and remove all blocks from the line to be removed
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][y];
                    blockMatrix[x][y] = null;
                    block.setEnlightened(true);

                    block.addAction(sequence(Actions.delay(5), Actions.fadeOut(2),
                            Actions.removeActor()));
                    //block.addAction(Actions.moveBy((BlockActor.blockWidth / 2) * (x - 5), 0, 2 + 5));
                }
            }

            return;
        }

        // Prepare to identify the rows that should get replaced
        IntArray lineMove = new IntArray(Gameboard.GAMEBOARD_ALLROWS);
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++)
            lineMove.add(i);


        if (linesToRemove.size > 0) {
            if (app.localPrefs.isPlaySounds() && app.theme.removeSound != null) {
                if (!special || app.theme.cleanSpecialSound == null)
                    app.theme.removeSound.play(.4f + linesToRemove.size * .2f);
                else
                    app.theme.cleanSpecialSound.play(.8f);
            }
            playScreen.inputAdapter.vibrate(special ? VibrationType.SPECIAL_CLEAR : VibrationType.CLEAR);

            for (int i = linesToRemove.size - 1; i >= 0; i--) {
                int y = linesToRemove.get(i);

                // enlighten and remove all blocks from the line to be removed
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][y];
                    blockMatrix[x][y] = null;
                    block.setEnlightened(true);


                    if (special)
                        // Special effect: move all blocks to a single point
                        block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(0) - .5f +
                                linesToRemove.size / 2f) * BlockActor.blockWidth, DURATION_REMOVE_DELAY, Interpolation.fade));
                    else if (linesToRemove.size >= 3)
                        // for 3 lines removed, the blocks are moved on a single point per line
                        block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(i)) *
                                BlockActor.blockWidth, DURATION_REMOVE_DELAY, Interpolation.fade));

                    block.addAction(sequence(Actions.delay(DURATION_REMOVE_DELAY), Actions.fadeOut(DURATION_REMOVE_FADEOUT),
                            Actions.removeActor()));
                }

                // identify the line that is got to replace the current one
                for (int higherY = y; higherY < Gameboard.GAMEBOARD_ALLROWS; higherY++)
                    if (higherY < Gameboard.GAMEBOARD_ALLROWS - 1)
                        lineMove.set(higherY, lineMove.get(higherY + 1));
                    else
                        lineMove.set(higherY, -1);
            }

            // add the particle effect
            if (app.theme.usesParticleEffect && (special && app.theme.particleEffectTrigger == Theme.EffectTrigger.specialClear
                    || app.theme.particleEffectTrigger == Theme.EffectTrigger.lineClear)) {

                if (app.theme.particleEffectPosition == Theme.EffectSpawnPosition.clear)
                    weldEffect.setPosition(blockGroup.getX() + 5f * BlockActor.blockWidth - app.theme.particleEffectWidth / 2f,
                            blockGroup.getY() + (linesToRemove.size / 2f + linesToRemove.get(0)) * BlockActor.blockWidth - app.theme.particleEffectHeight / 2f);

                weldEffect.start();
            }
        }

        // until now we had i = line. From now on lineMove.get(i) is the line getting replaced by i, or -1 if not replaced
        // Garbage is not considered yet
        for (int i = 0; i < lineMove.size; i++) {
            int replaceLineIwith = lineMove.get(i);
            int destinationY = i + linesToInsert;
            if (replaceLineIwith >= 0) {
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    // garbage still not considered here. the whole matrix will moved up for it on the
                    // next step
                    BlockActor block = blockMatrix[x][replaceLineIwith];
                    blockMatrix[x][replaceLineIwith] = null;
                    blockMatrix[x][i] = block;

                    // now move the actor, and consider the garbage
                    // if there's no movement needed, nothing is done
                    if (block != null && destinationY != replaceLineIwith) {
                        float delay = 0;

                        // check if the line is moved done or up. dely is only added for a down movement
                        final SequenceAction moveSequence = Actions.action(SequenceAction.class);

                        if (destinationY <= replaceLineIwith)
                            delay = DURATION_REMOVE_DELAY;

                        moveSequence.addAction(Actions.moveTo((x) * BlockActor.blockWidth, (destinationY) *
                                BlockActor.blockWidth, moveActorsTime));

                        // if the block moves out of the gameboard by the insert, it will get removed
                        // done here is not 100% correct, but the case happens very rarely and the block will
                        // never get touched again
                        if (destinationY >= Gameboard.GAMEBOARD_ALLROWS) {
                            moveSequence.addAction(Actions.fadeOut(DURATION_REMOVE_FADEOUT));
                            moveSequence.addAction(Actions.removeActor());
                        }

                        if (delay > 0) {
                            final BlockActor timedBlock = block;

                            Timer.schedule(new Timer.Task() {
                                @Override
                                public void run() {
                                    timedBlock.setMoveAction(moveSequence);
                                }
                            }, delay);

                        } else
                            block.setMoveAction(moveSequence);
                    }

                }

            }
        }

        if (linesToInsert > 0) {
            if (app.localPrefs.isPlaySounds() && app.theme.garbageSound != null)
                app.theme.garbageSound.play(.4f + linesToInsert * .2f);
            playScreen.inputAdapter.vibrate(VibrationType.GARBAGE);
            // move up the reference
            for (int i = Gameboard.GAMEBOARD_ALLROWS - 1; i >= linesToInsert; i--)
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][i - linesToInsert];
                    blockMatrix[x][i - linesToInsert] = null;
                    blockMatrix[x][i] = block;
                }

            // and add the new blocks at last
            for (int y = linesToInsert - 1; y >= 0; y--) {
                int holePos = garbageHolePosition[linesToInsert - 1 - y];

                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    if (x != holePos) {
                        BlockActor block = new BlockActor(app, Gameboard.SQUARE_GARBAGE, true);
                        block.setX(x * BlockActor.blockWidth);
                        block.setY((y - linesToInsert) * BlockActor.blockWidth);
                        block.setEnlightened(true, true);
                        block.setMoveAction(Actions.sequence(Actions.moveTo((x) * BlockActor.blockWidth, y *
                                BlockActor.blockWidth, moveActorsTime), Actions.run(block.getDislightenAction())));
                        blockGroup.addActor(block);
                        blockMatrix[x][y] = block;
                    }
                }
            }

        }
    }

    @Override
    public void markAndMoveFreezedLines(boolean playSoundAndMove, IntArray movedLines, IntArray fullLines) {
        if (movedLines.size == 0 && fullLines.size == 0)
            return;

        if (playSoundAndMove && app.localPrefs.isPlaySounds() && app.theme.cleanFreezedSound != null)
            app.theme.cleanFreezedSound.play();

        // enlighten full rows
        for (int i = 0; i < fullLines.size; i++) {
            int y = fullLines.get(i);
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                BlockActor block = blockMatrix[x][y];
                block.setEnlightened(true);
            }

        }
        // move rows to be moved
        BlockActor[] lineBlocks = new BlockActor[Gameboard.GAMEBOARD_COLUMNS];
        for (int movedLineNum = 0; movedLineNum < movedLines.size; movedLineNum++) {

            int y = movedLines.get(movedLineNum);

            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                lineBlocks[x] = blockMatrix[x][y];
                lineBlocks[x].setEnlightened(true);
            }

            // move the references downwards
            for (int i = y; i > movedLineNum; i--)
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    BlockActor block = blockMatrix[x][i - 1];
                    blockMatrix[x][i - 1] = null;
                    blockMatrix[x][i] = block;
                }

            // and put the whole row to the board
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++)
                blockMatrix[x][movedLineNum] = lineBlocks[x];
        }

        // blockmatrix is now correct, now we are moving the ui
        if (movedLines.size > 0 && playSoundAndMove)
            for (int y = 0; y <= movedLines.get(movedLines.size - 1); y++) {
                for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                    final BlockActor block = blockMatrix[x][y];
                    final int xf = x;
                    final int yf = y;

                    if (block != null) {
                        // little delay needed, because when a hard drop is done the active actor
                        // might not be in place yet
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                block.setMoveAction(Actions.moveTo(xf * BlockActor.blockWidth, yf *
                                        BlockActor.blockWidth, .3f));
                            }
                        }, .05f);
                    }
                }
            }

    }

    @Override
    public void emphasizeTimeLabel() {
        if (!timeLabel.hasActions()) {
            Color oldColor = new Color(timeLabel.getColor());
            timeLabel.setColor(app.theme.emphasizeColor);
            timeLabel.addAction(Actions.color(oldColor, 1f));
        }
    }

    @Override
    public void showGarbageAmount(int lines) {
        imGarbageIndicator.setVisible(true);
        imGarbageIndicator.clearActions();
        imGarbageIndicator.addAction(Actions.sizeTo(imGarbageIndicator.getWidth(),
                BlockActor.blockWidth * lines + NINE_PATCH_BORDER_SIZE * 2, .2f, Interpolation.fade));
    }

    @Override
    public void showComboHeight(int comboHeight) {
        comboHeight = Math.max(0, comboHeight);

        imComboIndicator.clearActions();
        imComboIndicator.addAction(Actions.sizeTo(imGarbageIndicator.getWidth(),
                BlockActor.blockWidth * comboHeight + NINE_PATCH_BORDER_SIZE * 2, .2f, Interpolation.fade));
        imComboIndicator.addAction(comboHeight > 0 ? Actions.fadeIn(.1f, Interpolation.fade)
                : Actions.fadeOut(.2f, Interpolation.fade));
    }

    @Override
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
        return (Gameboard.GAMEBOARD_NORMALROWS - (gameModel.isModernRotation() ? 1 : 0) + .3f) * BlockActor.blockWidth;
    }

    protected float getNextPieceXPos() {
        return getWidth() - blockGroup.getX() - (Tetromino.TETROMINO_BLOCKCOUNT -
                .3f) * BlockActor.blockWidth + Math.min(getX() / 2, BlockActor.blockWidth * 2.5f);
    }

    @Override
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

    @Override
    public void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions,
                                       Integer[][] newActivePiecePositions, int ghostPieceDistance, int holdBlockType) {
        float offsetX;
        float offsetY = getNextPieceYPos();

        if (isLandscapeArrangement)
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

    @Override
    public void updateScore(GameScore score, int gainedScore) {
        scoreTable.setClearedLines(score.getClearedLines());
        scoreTable.setCurrentLevel(score.getCurrentLevel());
        scoreTable.setScore(score.getScore());

        if (gameModel.showBlocksScore())
            blocksLeft.setScore(gameModel.getMaxBlocksToUse() > 0 ?
                    gameModel.getMaxBlocksToUse() - score.getDrawnTetrominos() : score.getDrawnTetrominos());
    }

    @Override
    public void pinTetromino(Integer[][] currentBlockPositions) {
        if (app.localPrefs.isPlaySounds() && app.theme.dropSound != null)
            app.theme.dropSound.play();

        playScreen.inputAdapter.vibrate(VibrationType.DROP);

        for (Integer[] vAfterMove : currentBlockPositions) {
            BlockActor activePieceBlock = blockMatrix[vAfterMove[0]][vAfterMove[1]];
            activePieceBlock.setEnlightened(false);
        }
    }

    @Override
    public void markConflict(int x, int y) {
        BlockActor block = blockMatrix[x][y];
        block.showConflictTouch();
    }

    @Override
    public void showMotivation(MotivationTypes achievement, @Nullable String extraMsg) {

        boolean playSound = true;
        boolean vibrate = true;
        String text = "";
        float duration = 2;

        switch (achievement) {
            case newLevel:
                text = app.TEXTS.get("labelLevel") + " " + extraMsg;
                break;
            case tSpin:
                text = app.TEXTS.get("motivationTSpin");
                break;
            case doubleSpecial:
                text = app.TEXTS.get("motivationDoubleSpecial");
                vibrate = false;
                break;
            case tenLinesCleared:
                text = extraMsg + " " + app.TEXTS.get("labelLines");
                playSound = false;
                vibrate = false;
                break;
            case boardCleared:
                text = app.TEXTS.get("motivationCleanComplete");
                vibrate = false;
                break;
            case newHighscore:
                text = app.TEXTS.get("motivationNewHighscore");
                break;
            case hundredBlocksDropped:
                text = app.TEXTS.format("motivationHundredBlocks", extraMsg);
                vibrate = false;
                playSound = false;
                break;
            case playerOver:
                if (extraMsg == null)
                    extraMsg = "Other player";
                else if (extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationPlayerOver", extraMsg);
                break;
            case gameOver:
                text = app.TEXTS.format("motivationGameOver");
                duration = 10;
                playSound = false;
                break;
            case gameWon:
                text = app.TEXTS.format("motivationGameWon");
                duration = 10;
                playSound = false;
                break;
            case gameSuccess:
                text = app.TEXTS.format("motivationGameSuccess");
                duration = 10;
                playSound = false;

                // keine weiteren Animationen zu abgebauten Reihen
                noLineClearAnimation = true;
                break;
            case bonusScore:
                text = app.TEXTS.format("motivationBonusScore", extraMsg);
                duration = 3;
                vibrate = false;
                break;
            case comboCount:
                text = app.TEXTS.format("motivationComboCount", extraMsg);
                duration = .5f;
                vibrate = false;
                break;
            case turnGarbage:
                playSound = true;
                if (extraMsg != null && extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationTurnGarbage", extraMsg);
                break;
            case turnOver:
                text = app.TEXTS.format("motivationTurnOver");
                break;
            case turnSurvive:
                if (extraMsg != null && extraMsg.length() >= 12)
                    extraMsg = extraMsg.substring(0, 10) + "...";
                text = app.TEXTS.format("motivationSurvive", extraMsg);
                duration = .75f;
                break;
            case prepare:
                text = app.TEXTS.format("motivationPrepare");
                duration = BackendBattleModel.PREPARE_TIME_SECONDS;
                break;
            case endFreezeMode:
                playSound = false;
                vibrate = false;
                duration = 1.5f;
                text = extraMsg + " " + app.TEXTS.get("labelLines");
                break;
        }

        if (playSound && app.localPrefs.isPlaySounds() && app.theme.unlockedSound != null)
            app.theme.unlockedSound.play();

        if (vibrate && playScreen.inputAdapter != null)
            playScreen.inputAdapter.vibrate(VibrationType.MOTIVATION);

        if (!text.isEmpty())
            addMotivationText(text.toUpperCase(), duration);
    }

    protected void updateTimeLabel() {
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

    public void setScoreTablePosition(int gameboardAlignment, float areaMaxWidth) {
        // if space next to the gameboard is wider than the scoretable, position the table centered
        // and move it down, but not more than two lines
        float xPosInArea = (areaMaxWidth - getWidth()) / 2;

        scoreTable.validate();
        scoreTable.setX(Math.max(10 - xPosInArea + scoreTable.getPrefWidth() / 2, -xPosInArea / 2));
        scoreTable.setY((gameboardAlignment == Align.top ? LightBlocksGame.nativeGameHeight : getStage().getHeight() - getY() * 1.2f)
                - MathUtils.clamp(xPosInArea / 2 - scoreTable.getPrefWidth() / 2,
                scoreTable.getPrefHeight() / 2 + 5, scoreTable.getLinePrefHeight() * 2 + scoreTable.getPrefHeight() /
                        2));

        isLandscapeArrangement = areaMaxWidth > getHeight();
    }

    public void switchedPause(boolean immediately, boolean isPaused) {
        final float fadingInterval = immediately ? 0 : .2f;

        blockGroup.clearActions();

        if (!isPaused) {
            if (blockGroup.getColor().a < 1) {
                blockGroup.addAction(Actions.fadeIn(fadingInterval));
                gameModel.setFreezeInterval(fadingInterval);
            }
        } else {
            blockGroup.addAction(Actions.fadeOut(fadingInterval));
        }
    }
}
