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
import de.golfgl.lightblocks.scene2d.GlowLabel;
import de.golfgl.lightblocks.scene2d.MyActions;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
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
    private final Label missionTitle;
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
        row().padTop(20);
        add(new ScaledLabel(app.TEXTS.get("labelChooseMission"), app.skin, LightBlocksGame.SKIN_FONT_BIG, .75f))
                .bottom();
        row();
        menuScreen.addFocusableActor(missionsTable);
        add(missionsTable).expand().fill().maxWidth(LightBlocksGame.nativeGameWidth);
        row();
        missionTitle = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE, .95f);
        add(missionTitle).expand().top();

        // Gleich wie bei Marathon, nochmal auslagern
        playButton = new PlayButton(app);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        row();
        add(playButton).minHeight(playButton.getPrefHeight() * 2f).top().fillX().expand();
        menuScreen.addFocusableActor(playButton);
        row();
        scoresGroup = new ScoresGroup(app, false);
        add(scoresGroup).height(scoresGroup.getPrefHeight()).fill();

        needsRefresh = true;
    }

    private void beginNewGame() {
        try {
            String modelId = getGameModelId();

            PlayScreen ps;
            if (modelId.equals(TutorialModel.MODEL_ID)) {
                ps = PlayScreen.gotoPlayScreen(app, TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
            } else {
                InitGameParameters igp = new InitGameParameters();
                igp.setMissionId(modelId);
                ps = PlayScreen.gotoPlayScreen(app, igp);
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
            int currentSelected = missionsTable.getSelectedIndex();
            missionsTable.refreshMenuTable(currentSelected < 0 ||
                    currentSelected == missionsTable.getLastPossible());
            menuScreen.validate();
            missionsTable.scrollToSelected();
            missionsTable.updateVisualScroll();
            scoresGroup.show(getGameModelId());
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
        private GlowLabel[] idxLabel;
        private String[] titles;
        private Label[] ratingLabel;
        private boolean[] selectable;
        private Table table;
        private int selectedIndex = -1;
        private int lastPossible;

        public MissionsTable() {
            super(null);
            table = new Table();
            setActor(table);

            //setFadeScrollBars(false);
            setScrollingDisabled(false, true);

            idxLabel = new GlowLabel[missions.size()];
            titles = new String[missions.size()];
            ratingLabel = new Label[missions.size()];
            selectable = new boolean[missions.size()];

            for (Mission mission : missions) {
                final String uid = mission.getUniqueId();
                final int idx = mission.getIndex();
                String lblUid = Mission.getLabelUid(uid);

                idxLabel[idx] = new GlowLabel(Integer.toString(idx), app.skin, 1f);
                idxLabel[idx].setAlignment(Align.center);
                titles[idx] = app.TEXTS.get(lblUid);
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
            }

            table.add().uniform().width(LightBlocksGame.nativeGameWidth / 5);
            for (int idx = 0; idx < missions.size(); idx++) {
                table.add(idxLabel[idx]).uniform().fill();
            }
            table.add().uniform();
            table.row();
            table.add();
            for (int idx = 0; idx < missions.size(); idx++) {
                table.add(ratingLabel[idx]).center();
            }
            table.add();
        }

        @Override
        public float getMinHeight() {
            return getPrefHeight();
        }

        public void refreshMenuTable(boolean selectLastPossible) {
            boolean isAncestorDone = true;
            lastPossible = 0;

            for (int idx = 0; idx < getMissionNum(); idx++) {

                final String uid = missions.get(idx).getUniqueId();
                int rating = app.savegame.getBestScore(uid).getRating();
                selectable[idx] = isAncestorDone || rating > 0;
                if (selectable[idx])
                    lastPossible = idx;
                Touchable touchable = (selectable[idx] ? Touchable.enabled : Touchable.disabled);
                Color rowColor = (selectedIndex == idx ? Color.WHITE :
                        (selectable[idx] ? LightBlocksGame.COLOR_UNSELECTED : LightBlocksGame.COLOR_DISABLED));

                isAncestorDone = (rating > 0);

                String scoreLabelString;
                if (rating >= 1) {
                    scoreLabelString = RoundOverScoreScreen.getFARatingString(rating);
                } else
                    scoreLabelString = "";

                ratingLabel[idx].setText(scoreLabelString);
                setRowColor(idx, rowColor);
                ratingLabel[idx].setTouchable(touchable);
                idxLabel[idx].setTouchable(touchable);
            }

            if (selectLastPossible)
                setSelectedIndex(lastPossible);
        }

        protected void setRowColor(int idx, Color rowColor) {
            ratingLabel[idx].clearActions();
            ratingLabel[idx].setColor(rowColor);
            idxLabel[idx].clearActions();
            idxLabel[idx].setColor(rowColor);
            idxLabel[idx].setGlowing(rowColor == Color.WHITE);
        }

        public void scrollToSelected() {
            scrollTo(idxLabel[selectedIndex].getX(), ratingLabel[selectedIndex].getY(),
                    idxLabel[selectedIndex].getWidth(), ratingLabel[selectedIndex].getHeight(),
                    true, false);
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public boolean setSelectedIndex(int idx) {
            if (idx != selectedIndex && selectable[idx]) {
                if (selectedIndex >= 0 && selectedIndex < getMissionNum())
                    setRowColor(selectedIndex, LightBlocksGame.COLOR_UNSELECTED);
                selectedIndex = idx;
                setRowColor(selectedIndex, Color.WHITE);

                menuScreen.onGameModelIdChanged();
                scoresGroup.show(getGameModelId());
                missionTitle.setText(titles[idx]);
                scrollToSelected();
            }
            return (selectedIndex == idx);
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
            int idxToChange = 1;
            switch (direction) {
                case north:
                case south:
                    return false;
                case west:
                    while (getSelectedIndex() - idxToChange >= 0) {
                        if (setSelectedIndex(getSelectedIndex() - idxToChange))
                            return true;
                        else
                            idxToChange++;
                    }
                    return false;
                case east:
                    while (getSelectedIndex() + idxToChange < getMissionNum()) {
                        if (setSelectedIndex(getSelectedIndex() + idxToChange))
                            return true;
                        else
                            idxToChange++;
                    }
                    return false;
            }
            return false;
        }

        @Override
        public void touchAction() {
            ratingLabel[selectedIndex].addAction(MyActions.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR,
                    ratingLabel[selectedIndex].getColor()));
            idxLabel[selectedIndex].addAction(MyActions.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR,
                    ratingLabel[selectedIndex].getColor()));
        }

        public int getLastPossible() {
            return lastPossible;
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            if (ratingLabel != null && selectedIndex >= 0)
                scrollToSelected();
        }
    }
}
