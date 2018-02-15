package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FATextButton;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 20.02.2017.
 */
public abstract class AbstractScoreScreen extends AbstractMenuScreen {
    protected static final int SCORE_COUNTING_SPEED = 2000;
    protected float maxCountingTime;

    public AbstractScoreScreen(LightBlocksGame app) {
        super(app);
    }

    public void setMaxCountingTime(float maxCountingTime) {
        this.maxCountingTime = maxCountingTime;
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.COMMENT_STAR_TROPHY;
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        // Share Button
        Button share = new FATextButton(FontAwesome.NET_SHARE1, app.TEXTS.get("menuShare"), app.skin);
        share.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                app.share.shareText(getShareText(), null);
            }
        });

        buttons.add(share).fill().uniform();
    }

    protected abstract String getShareText();

    @Override
    protected void fillMenuTable(Table menuTable) {
        // must be subclassed
        menuTable.defaults().right();
        menuTable.defaults().space(15);
    }

    ;


    protected void addScoresLine(Table scoreTable, String label, int digits, long score) {
        Array<Long> array = new Array<Long>(1);
        array.add(score);
        addScoresLine(scoreTable, label, digits, array, 0);
    }

    protected void addScoresLine(Table scoreTable, String label, int digits, Array<Long> score, long bestScore) {

        scoreTable.row();
        scoreTable.add(new Label(app.TEXTS.get(label).toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_BIG))
                .left();

        for (int i = 0; i < score.size; i++) {
            ScoreLabel scoreLabel = new ScoreLabel(0, 0, app.skin, LightBlocksGame.SKIN_FONT_BIG);
            scoreLabel.setMaxCountingTime(maxCountingTime);
            scoreLabel.setCountingSpeed(digits == 0 ? SCORE_COUNTING_SPEED / 10 : SCORE_COUNTING_SPEED);
            float prefLabelWidth = scoreLabel.getPrefWidth() * digits;
            scoreLabel.setAlignment(Align.right);
            scoreLabel.setScore(score.get(i));

            if (bestScore > 0 && !isBestScore(i)) {
                scoreLabel.setEmphasizeScore(bestScore, Color.RED);
                scoreLabel.setEmphasizeSound(app.isPlaySounds() ? app.unlockedSound : null);
            }

            scoreTable.add(scoreLabel).minWidth(prefLabelWidth);
        }
    }

    protected boolean isBestScore(int i) {
        return false;
    }


}
