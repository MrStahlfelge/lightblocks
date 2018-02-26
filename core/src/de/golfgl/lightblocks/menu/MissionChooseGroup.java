package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.gdx.controllers.IControllerScrollable;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Menu for missions
 * <p>
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class MissionChooseGroup extends Table implements SinglePlayerScreen.IGameModeGroup {

    private final MissionsTable missionsTable;
    boolean needsRefresh = false;
    private List<Mission> missions;
    private SinglePlayerScreen menuScreen;
    private Button playButton;
    private ScoresGroup scoresGroup;
    private LightBlocksGame app;

    public MissionChooseGroup(SinglePlayerScreen myParentScreen, LightBlocksGame app) {
        this.menuScreen = myParentScreen;
        this.app = app;

        missions = app.getMissionList();

        missionsTable = new MissionsTable();

        row();
        add(new ScaledLabel(app.TEXTS.get("menuPlayMissionButton"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        row();
        menuScreen.addFocusableActor(missionsTable);
        add(missionsTable).expand().fill().pad(20, 50, 20, 30);
        row();

        // Gleich wie bei Marathon, nochmal auslagern
        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        row();
        add(playButton).minHeight(playButton.getPrefHeight() * 2f).top();
        menuScreen.addFocusableActor(playButton);
        row();
        scoresGroup = new ScoresGroup(app);
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();

        needsRefresh = true;
    }

    private void beginNewGame() {
        try {
            String modelId = getGameModelId();

            PlayScreen ps;
            if (modelId.equals(TutorialModel.MODEL_ID)) {
                ps = PlayScreen.gotoPlayScreen((AbstractScreen) app.getScreen(), TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
            } else {
                InitGameParameters igp = new InitGameParameters();
                igp.setMissionId(modelId);
                ps = PlayScreen.gotoPlayScreen((AbstractScreen) app.getScreen(), igp);
            }

            needsRefresh = true;
            menuScreen.gameStarted(false);
        } catch (VetoException e) {
            new VetoDialog(e.getMessage(), app.skin, menuScreen.getAvailableContentWidth() * .75f).show(getStage());
        }

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (needsRefresh) {
            needsRefresh = false;
            missionsTable.refreshMenuTable(missionsTable.getSelectedIndex() < 0);
// hier wird nicht richtig gescrollt, und Tastaturbedienbarkeit geht auch gar nicht
            menuScreen.validate();
            missionsTable.scrollToSelected();
            missionsTable.updateVisualScroll();
        }
    }

    @Override
    public Actor getConfiguredDefaultActor() {
        return playButton;
    }

    @Override
    public String getGameModelId() {
        int selectedIndex = missionsTable.getSelectedIndex();
        return selectedIndex >= 0 ? missions.get(selectedIndex).getUniqueId() : null;
    }

    private class MissionsTable extends ScrollPane implements IControllerScrollable, IControllerActable,
            ITouchActionButton {
        private Label[] idxLabel;
        private Label[] titleLabel;
        private Label[] ratingLabel;
        private Table table;
        private int selectedIndex = -1;

        public MissionsTable() {
            super(null, app.skin);
            table = new Table();
            setActor(table);

            setFadeScrollBars(false);
            setScrollingDisabled(true, false);

            idxLabel = new Label[missions.size()];
            titleLabel = new Label[missions.size()];
            ratingLabel = new Label[missions.size()];

            table.defaults().space(0, 5, 0, 5);

            for (Mission mission : missions) {
                final String uid = mission.getUniqueId();
                final int idx = mission.getIndex();
                String lblUid = Mission.getLabelUid(uid);

                // In Scaled Label umbauen!
                idxLabel[idx] = new ScaledLabel(Integer.toString(idx), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
                titleLabel[idx] = new ScaledLabel(app.TEXTS.get(lblUid), app.skin, LightBlocksGame
                        .SKIN_FONT_TITLE);
                ratingLabel[idx] = new ScaledLabel("", app.skin, FontAwesome.SKIN_FONT_FA, .5f);
                ratingLabel[idx].setAlignment(Align.center);

                ClickListener idxEvent = new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        missionsTable.setSelectedIndex(idx);
                    }
                };

                idxLabel[idx].addListener(idxEvent);
                ratingLabel[idx].addListener(idxEvent);
                titleLabel[idx].addListener(idxEvent);

                table.row();
                table.add(idxLabel[idx]).align(Align.right);
                table.add(titleLabel[idx]).fill().expandX();
                table.add(ratingLabel[idx]).fill();
            }

        }

        public void refreshMenuTable(boolean selectLastPossible) {
            boolean isAncestorDone = true;
            int lastPossible = 0;

            for (int idx = 0; idx < getMissionNum(); idx++) {

                final String uid = missions.get(idx).getUniqueId();
                int rating = app.savegame.getBestScore(uid).getRating();
                boolean selectable = isAncestorDone || rating > 0;
                if (selectable)
                    lastPossible = idx;
                Touchable touchable = (selectable ? Touchable.enabled : Touchable.disabled);
                Color rowColor = (selectedIndex == idx ? Color.WHITE :
                        (selectable ? LightBlocksGame.COLOR_UNSELECTED : LightBlocksGame.COLOR_DISABLED));

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
            ratingLabel[idx].clearActions();
            ratingLabel[idx].setColor(rowColor);
            titleLabel[idx].clearActions();
            titleLabel[idx].setColor(rowColor);
            idxLabel[idx].clearActions();
            idxLabel[idx].setColor(rowColor);
        }

        public void scrollToSelected() {
            scrollTo(0, ratingLabel[selectedIndex].getY(), 1, ratingLabel[selectedIndex].getHeight());
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public void setSelectedIndex(int idx) {
            if (idx != selectedIndex) {
                if (selectedIndex >= 0 && selectedIndex < getMissionNum())
                    setRowColor(selectedIndex, LightBlocksGame.COLOR_UNSELECTED);
                selectedIndex = idx;
                setRowColor(selectedIndex, Color.WHITE);

                menuScreen.onGameModelIdChanged();
                scoresGroup.show(getGameModelId());
            }
        }

        protected int getMissionNum() {
            return idxLabel.length;
        }

        @Override
        public boolean onControllerDefaultKeyDown() {
            ((ControllerMenuStage) getStage()).setFocusedActor(playButton);
            return true;
        }

        @Override
        public boolean onControllerDefaultKeyUp() {
            return false;
        }

        @Override
        public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
            switch (direction) {
                case east:
                case west:
                    return false;
                case north:
                    if (getSelectedIndex() > 0) {
                        setSelectedIndex(getSelectedIndex() - 1);
                        scrollToSelected();
                        return true;
                    }
                    return false;
                case south:
                    if (getSelectedIndex() < getMissionNum() - 1) {
                        setSelectedIndex(getSelectedIndex() + 1);
                        scrollToSelected();
                        return true;
                    } else
                        return false;
            }
            return false;
        }

        @Override
        public void touchAction() {
            ratingLabel[selectedIndex].addAction(MyStage.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR,
                    ratingLabel[selectedIndex].getColor()));
            titleLabel[selectedIndex].addAction(MyStage.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR,
                    ratingLabel[selectedIndex].getColor()));
            idxLabel[selectedIndex].addAction(MyStage.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR,
                    ratingLabel[selectedIndex].getColor()));
        }
    }
}
