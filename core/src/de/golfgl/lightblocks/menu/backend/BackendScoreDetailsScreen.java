package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.IPlayerInfo;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayKeyboardInput;
import de.golfgl.lightblocks.state.Replay;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class BackendScoreDetailsScreen extends AbstractFullScreenDialog {
    private final ScoreListEntry score;
    private Replay replay;

    public BackendScoreDetailsScreen(final LightBlocksGame app, ScoreListEntry score) {
        this(app, score, null);
    }

    public BackendScoreDetailsScreen(final LightBlocksGame app, final ScoreListEntry score, IPlayerInfo playerInfo) {
        super(app);
        this.score = score;

        // Fill Content
        Table contentTable = getContentTable();

        contentTable.row();
        contentTable.add(new Label(FontAwesome.COMMENT_STAR_TROPHY, app.skin, FontAwesome.SKIN_FONT_FA));

        contentTable.row();
        final String gameModeLabel;
        gameModeLabel = getI18NIfExistant(score.gameMode, "labelModel_");
        contentTable.add(new Label(gameModeLabel, app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        contentTable.row();
        contentTable.add(new ScaledLabel(app.TEXTS.get("scoreGainedByLabel"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE, .5f));

        contentTable.row();
        final BackendUserLabel userLabel = new BackendUserLabel(playerInfo != null ? playerInfo : score, app,
                "default");
        contentTable.add(userLabel);
        // Wenn man schon aus dem User kommt, nicht nochmal hin
        if (playerInfo != null)
            userLabel.setToLabelMode();
        else
            addFocusableActor(userLabel);

        contentTable.row();
        contentTable.add(new ScaledLabel(String.valueOf(BackendScoreTable.formatTimePassedString(app, score
                .scoreGainedTime)), app.skin, LightBlocksGame.SKIN_FONT_BIG));

        contentTable.row();
        contentTable.add().height(20);

        contentTable.row();
        contentTable.add(new ScoreDetailsTable());

        if (score.replayUri != null || replay != null) {
            contentTable.row().pad(20);
            FaTextButton showReplayButton = new FaTextButton(FontAwesome.CIRCLE_PLAY, "Watch replay", app.skin,
                    "default");
            contentTable.add(showReplayButton);
            addFocusableActor(showReplayButton);
            showReplayButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                    if (replay != null)
                        showReplayDialog(gameModeLabel, userLabel);
                    else
                        app.backendManager.getBackendClient().fetchReplay(score.replayUri, new WaitForResponse<String>
                                (app, getStage()) {
                            @Override
                            public void onRequestSuccess(String retrievedData) {
                                Replay replay = new Replay();
                                replay.fromString(retrievedData);

                                if (!replay.isValid()) {
                                    onRequestFailed(500, "Replay is corrupt");
                                } else {
                                    super.onRequestSuccess(retrievedData);
                                    BackendScoreDetailsScreen.this.replay = replay;
                                    showReplayDialog(gameModeLabel, userLabel);
                                }
                            }
                        });
                }
            });
        }
    }

    protected static String findI18NIfExistant(I18NBundle texts, String key, String prefix) {
        String retVal;
        if (key == null)
            key = "";
        try {
            retVal = texts.get(prefix + key);
        } catch (Throwable t) {
            retVal = key;
        }
        return retVal;
    }

    public void setReplay(Replay replay) {
        if (replay.isValid())
            this.replay = replay;
    }

    public void showReplayDialog(String gameModeLabel, BackendUserLabel userLabel) {
        if (replay != null && replay.isValid()) {
            ReplayDialog dialog = new ReplayDialog(app, replay, gameModeLabel, userLabel.getNickName());
            dialog.show(getStage());
        }
    }

    protected String getI18NIfExistant(String key, String prefix) {
        return findI18NIfExistant(app.TEXTS, key, prefix);
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }

    private class ScoreDetailsTable extends Table {

        public ScoreDetailsTable() {
            addLine("labelScore", String.valueOf(score.score), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelLines", String.valueOf(score.lines), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelBlocks", String.valueOf(score.drawnBlocks), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelTime", ScoreTable.formatTimeString(score.timePlayedMs, 2),
                    LightBlocksGame.SKIN_FONT_TITLE);

            allPlatformLine();
            addInputTypeLine();

            if (score.params != null && !score.params.isEmpty())
                addScoreParamsLines();
        }

        protected void addScoreParamsLines() {
            try {
                JsonValue params = new JsonReader().parse(score.params);
                JsonValue current = params.child;
                while (current != null) {
                    try {
                        addRawLabelLine(getI18NIfExistant(current.name, "labelGameScoreParam_"), current.asString(),
                                LightBlocksGame.SKIN_FONT_BIG, 5);

                    } catch (Throwable t) {

                    }
                    current = current.next;
                }
            } catch (Throwable t) {

            }
        }

        protected void addInputTypeLine() {
            Cell inputTypeCell = addLine("menuTouchControlType", getI18NIfExistant(score.inputType, "labelInputType_"),
                    LightBlocksGame.SKIN_FONT_BIG, 5);

            String faInputLabel = "";
            if (PlayKeyboardInput.INPUT_KEY_CONTROLLER.equalsIgnoreCase(score.inputType))
                faInputLabel = FontAwesome.DEVICE_GAMEPAD;
            else if (PlayKeyboardInput.INPUT_KEY_KEYBOARD.equalsIgnoreCase(score.inputType))
                faInputLabel = FontAwesome.DEVICE_KEYBOARD;
            else if (PlayGesturesInput.INPUT_KEY_GESTURES.equalsIgnoreCase(score.inputType))
                faInputLabel = FontAwesome.DEVICE_GESTURE2;

            if (!faInputLabel.isEmpty()) {
                Label faLabel = new Label(faInputLabel, app.skin, FontAwesome.SKIN_FONT_FA);
                faLabel.setFontScale(.5f);
                inputTypeCell.setActor(faLabel);
            }
        }

        protected void allPlatformLine() {
            Cell platformCell = addLine("platformLabel", getI18NIfExistant(score.platform, "labelPlatform_"),
                    LightBlocksGame
                            .SKIN_FONT_BIG, 20);

            String faPlatformLabel = "";
            if (BackendManager.PLATFORM_DESKTOP.equalsIgnoreCase(score.platform))
                faPlatformLabel = FontAwesome.DEVICE_LAPTOP;
            else if (BackendManager.PLATFORM_MOBILE.equalsIgnoreCase(score.platform))
                faPlatformLabel = FontAwesome.DEVICE_MOBILEPHONE;

            if (!faPlatformLabel.isEmpty()) {
                Label faLabel = new Label(faPlatformLabel, app.skin, FontAwesome.SKIN_FONT_FA);
                faLabel.setFontScale(.5f);
                platformCell.setActor(faLabel);
            }
        }

        private Cell addLine(String label, String value, String style) {
            return addLine(label, value, style, 0);
        }

        private Cell addLine(String label, String value, String style, float padTop) {
            return addRawLabelLine(app.TEXTS.get(label), value, style, padTop);
        }

        private Cell addRawLabelLine(String label, String value, String style, float padTop) {
            row().padTop(padTop);
            add(new ScaledLabel(label.toUpperCase(), app.skin, style)).left().padRight(30);
            ScaledLabel scoreLabel = new ScaledLabel(value, app.skin, style);
            return add(scoreLabel).right();
        }
    }
}
