package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.ScoreLabel;

/**
 * Created by Benjamin Schulte on 20.02.2017.
 */
public abstract class AbstractScoreScreen extends AbstractScreen {
    protected static final int SCORE_COUNTING_SPEED = 2000;
    protected float maxCountingTime;

    public AbstractScoreScreen(LightBlocksGame app) {
        super(app);
    }

    public void initializeUI(float maxCountingTime) {

        this.maxCountingTime = maxCountingTime;

        // SCORES
        Table scoreTable = new Table();
        scoreTable.defaults().right();
        scoreTable.defaults().space(15);
        fillScoreTable(scoreTable);

        //Titel
        // Der Titel wird nach den Scores gefüllt, denn dort wird eventuell Highscore gesetzt
        Label title = new Label(getTitle().toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        Label subTitle = new Label(getSubtitle(), app.skin, LightBlocksGame.SKIN_FONT_BIG);

        // Back button
        Button leave = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        setBackButton(leave);
        // Share Button
        Button share = new de.golfgl.lightblocks.scenes.FATextButton(FontAwesome.NET_SHARE1, app.TEXTS.get
                ("menuShare"), app.skin);
        share.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                app.share.shareText(getShareText(), null);
            }
        });

        // Buttons
        Table buttons = new Table();
        buttons.defaults().fill().uniform();
        buttons.add(leave).fill(false);
        buttons.add(share);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.row();
        mainTable.add(new Label(FontAwesome.COMMENT_STAR_TROPHY, app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(title);
        mainTable.row();
        mainTable.add(subTitle).spaceBottom(50);
        mainTable.row();
        mainTable.add(scoreTable);
        mainTable.row();
        mainTable.add(buttons).spaceTop(50);

        stage.addActor(mainTable);

    }

    protected abstract String getSubtitle();

    protected abstract String getTitle();

    protected abstract String getShareText();

    protected abstract void fillScoreTable(Table scoreTable);


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
                scoreLabel.setEmphasizeSound(app.unlockedSound);
            }

            scoreTable.add(scoreLabel).minWidth(prefLabelWidth);
        }
    }

    protected boolean isBestScore(int i) {
        return false;
    }


}
