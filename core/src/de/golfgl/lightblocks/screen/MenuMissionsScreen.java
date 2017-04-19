package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scenes.FATextButton;

/**
 * Menu for missions
 * <p>
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class MenuMissionsScreen extends AbstractMenuScreen {

    private HashMap<String, Label> scoreLabels;

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
        List<Mission> missions = Mission.getMissionList();

        scoreLabels = new HashMap<String, Label>(missions.size());

        int index = 0;
        menuTable.defaults().space(7, 15, 7, 15);

        for (Mission mission : missions) {
            final String uid = mission.getUniqueId();
            final int usIdx = uid.indexOf("_");

            String lblUid = (usIdx >= 0 ? uid.substring(0, usIdx) : uid);

            menuTable.row();
            menuTable.add(new Label(Integer.toString(index), app.skin, LightBlocksGame.SKIN_FONT_BIG)).align(Align
                    .right);

            menuTable.add(new Label(app.TEXTS.get("labelModel_" + lblUid), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG)).align(Align.left).expandX();

            String scoreLabelString = "";
            int rating = app.savegame.getBestScore(uid).getRating();

            if (rating >= 1) {
                rating--;

                for (int i = 0; i < 3; i++) {
                    if (rating >= 2)
                        scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_FULL;
                    else if (rating >= 1)
                        scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_HALF;
                    else
                        scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_EMPTY;

                    rating = rating - 2;
                }
            } else
                scoreLabelString = FontAwesome.CIRCLE_CROSS;

            final Label scoreLabel = new Label(scoreLabelString, app.skin, FontAwesome.SKIN_FONT_FA);
            scoreLabel.setFontScale(.5f);
            scoreLabels.put(uid, scoreLabel);
            menuTable.add(scoreLabel).padRight(30);

            index++;
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

        buttons.defaults().fill();
        buttons.add(playButton).prefWidth(playButton.getPrefWidth() * 1.2f);

    }

    private void beginNewGame() {
        try {
            PlayScreen ps = PlayScreen.gotoPlayScreen(this, TutorialModel.getTutorialInitParams());
            ps.setShowScoresWhenGameOver(false);
            ps.setBackScreen(this);
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }

    }
}
