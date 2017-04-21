package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Menu for missions
 * <p>
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class MenuMissionsScreen extends AbstractMenuScreen {

    private Label[] idxLabel;
    private Label[] titleLabel;
    private Label[] ratingLabel;
    private List<Mission> missions;
    private int selectedIndex = -1;

    public MenuMissionsScreen(LightBlocksGame app) {
        super(app);

        initializeUI();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.COMMENT_STAR_FLAG;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMissionButton");
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        missions = app.getMissionList();

        idxLabel = new Label[missions.size()];
        titleLabel = new Label[missions.size()];
        ratingLabel = new Label[missions.size()];

        menuTable.defaults().space(7, 15, 7, 15);

        for (Mission mission : missions) {
            final String uid = mission.getUniqueId();
            int idx = mission.getIndex();
            String lblUid = Mission.getLabelUid(uid);

            idxLabel[idx] = new Label(Integer.toString(idx), app.skin, LightBlocksGame.SKIN_FONT_BIG);
            titleLabel[idx] = new Label(app.TEXTS.get(lblUid), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG);
            ratingLabel[idx] = new Label("", app.skin, FontAwesome.SKIN_FONT_FA);
            ratingLabel[idx].setFontScale(.5f);
            ratingLabel[idx].setAlignment(Align.center);

            EventListener idxEvent = getEventListener(idx);

            idxLabel[idx].addListener(idxEvent);
            ratingLabel[idx].addListener(idxEvent);
            titleLabel[idx].addListener(idxEvent);

            menuTable.row();
            menuTable.add(idxLabel[idx]).align(Align.right);
            menuTable.add(titleLabel[idx]).fill().expandX();
            menuTable.add(ratingLabel[idx]).fill().padRight(30);
        }

    }

    public void refreshMenuTable(boolean selectLastPossible) {
        boolean isAncestorDone = true;
        int lastPossible = 0;

        for (int idx = 0; idx < idxLabel.length; idx++) {

            final String uid = missions.get(idx).getUniqueId();
            int rating = app.savegame.getBestScore(uid).getRating();
            boolean selectable = isAncestorDone || rating > 0;
            if (selectable)
                lastPossible = idx;
            Touchable touchable = (selectable ? Touchable.enabled : Touchable.disabled);
            Color rowColor = (selectedIndex == idx ? AbstractMenuScreen.COLOR_TABLE_HIGHLIGHTED :
                    (selectable ? AbstractMenuScreen.COLOR_TABLE_NORMAL : AbstractMenuScreen.COLOR_TABLE_DEACTIVATED));

            isAncestorDone = (rating > 0);

            String scoreLabelString;
            if (rating >= 1) {
                scoreLabelString = ScoreScreen.getFARatingString(rating);
            } else
                scoreLabelString = (selectable ? FontAwesome.CIRCLE_PLAY : FontAwesome.CIRCLE_CROSS);

            ratingLabel[idx].setText(scoreLabelString);
            setRowColor(idx, rowColor);
            ratingLabel[idx].setTouchable(touchable);
            titleLabel[idx].setTouchable(touchable);
            idxLabel[idx].setTouchable(touchable);
        }

        if (selectLastPossible)
            setSelectedIndex(lastPossible);
    }

    protected void setRowColor(int idx, Color rowColor) {
        ratingLabel[idx].setColor(rowColor);
        titleLabel[idx].setColor(rowColor);
        idxLabel[idx].setColor(rowColor);
    }

    public void setSelectedIndex(int idx) {
        if (idx != selectedIndex) {
            if (selectedIndex >= 0 && selectedIndex < idxLabel.length)
                setRowColor(selectedIndex, AbstractMenuScreen.COLOR_TABLE_NORMAL);
            selectedIndex = idx;
            setRowColor(selectedIndex, AbstractMenuScreen.COLOR_TABLE_HIGHLIGHTED);
        }
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        TextButton playButton = new FATextButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );

        TextButton highScoreButton = new FATextButton(FontAwesome.COMMENT_STAR_TROPHY, app.TEXTS.get("labelScores"),
                app.skin);
        highScoreButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            showHighscores();
                                        }
                                    }
        );

        buttons.defaults().fill();
        buttons.add(highScoreButton).uniform();
        buttons.add(playButton).prefWidth(playButton.getPrefWidth() * 1.2f);

    }

    private void showHighscores() {
        ScoreScreen scoreScreen = new ScoreScreen(app);
        final String uniqueId = missions.get(selectedIndex).getUniqueId();
        scoreScreen.setGameModelId(uniqueId);
        scoreScreen.addScoreToShow(app.savegame.getBestScore(uniqueId),
                app.TEXTS.get("labelBestScores"));
        scoreScreen.setBackScreen(this);
        scoreScreen.setMaxCountingTime(1);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);
    }

    private void beginNewGame() {
        try {
            String modelId = missions.get(selectedIndex).getUniqueId();

            PlayScreen ps;
            if (modelId.equals(TutorialModel.MODEL_ID)) {
                ps = PlayScreen.gotoPlayScreen(this, TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
            } else {
                InitGameParameters igp = new InitGameParameters();
                igp.setMissionId(modelId);
                ps = PlayScreen.gotoPlayScreen(this, igp);
            }

            ps.setBackScreen(this);
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }

    }

    @Override
    public void show() {
        super.show();
        refreshMenuTable(selectedIndex < 0);
    }

    private EventListener getEventListener(final int idx) {
        return new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    switch (((InputEvent) event).getType()) {
                        case touchUp:
                            setSelectedIndex(idx);
                        case touchDown:
                            // damit touchUp gesendet wird auch touchDown annehmen
                            return true;
                        default:
                            return false;
                    }
                }

                return false;
            }
        };
    }

    @Override
    public void dispose() {
        // kein dispose wie super, da dieser Screen wieder verwendet wird.
    }
}
