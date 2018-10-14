package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.backend.BackendUserDetailsScreen;
import de.golfgl.lightblocks.menu.backend.CreateNewAccountDialog;
import de.golfgl.lightblocks.scene2d.InfoButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.TotalScore;

/**
 * Anzeige von Total Scores
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class TotalScoreScreen extends AbstractMenuDialog {

    private static final int MAX_COUNTING_TIME = 1;
    private TotalScore total;
    private Cell publicProfileButtonCell;
    private Actor showPublicProfileButton;
    private Actor createPublicProfileButton;

    public TotalScoreScreen(LightBlocksGame app, Group actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected String getSubtitle() {
        return app.TEXTS.get("labelAllGames");
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("labelTotalScores");
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.COMMENT_STAR_TROPHY;
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        total = app.savegame.getTotalScore();

        ScoreTable scoreTable = new ScoreTable(app);
        scoreTable.setMaxCountingTime(MAX_COUNTING_TIME);
        scoreTable.addScoresLine("labelScore", 10, total.getScore());
        scoreTable.addScoresLine("labelLines", 0, total.getClearedLines());
        scoreTable.addScoresLine("labelBlocks", 0, total.getDrawnTetrominos());
        scoreTable.addScoresLine("labelFourLines", 0, total.getFourLineCount());
        scoreTable.addScoresLine("labelTSpin", 0, total.getTSpins());
        scoreTable.addScoresLine("labelMaxCombo", 0, total.getMaxComboCount());
        scoreTable.addScoresLine("labelMultiPlayerWon", 0, total.getMultiPlayerMatchesWon());

        menuTable.add(scoreTable).fill().expandY();

        showPublicProfileButton = new RoundedTextButton(app.TEXTS.get("showProfileLabel"), app.skin);
        showPublicProfileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new BackendUserDetailsScreen(app, app.backendManager.ownUserId()).show(getStage());
            }
        });
        addFocusableActor(showPublicProfileButton);

        createPublicProfileButton = new CreatePublicProfileButton(app);
        addFocusableActor(createPublicProfileButton);

        menuTable.row().padTop(20);
        publicProfileButtonCell = menuTable.add();

        fillPublicProfileButtonCell();
    }

    private void fillPublicProfileButtonCell() {
        if (app.backendManager.hasUserId() && showPublicProfileButton.getParent() == null) {
            publicProfileButtonCell.setActor(showPublicProfileButton).fill(false);
            if (getStage() != null && getStage() instanceof ControllerMenuStage)
                ((ControllerMenuStage) getStage()).setFocusedActor(showPublicProfileButton);
        }

        if (!app.backendManager.hasUserId() && createPublicProfileButton.getParent() == null) {
            publicProfileButtonCell.setActor(createPublicProfileButton).fill();
        }
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        ShareButton shareButton = new ShareButton(app, app.TEXTS.format("shareTotalText", total.getDrawnTetrominos
                (), LightBlocksGame.GAME_URL_SHORT));
        buttons.add(shareButton);
        addFocusableActor(shareButton);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        fillPublicProfileButtonCell();
    }

    public static class CreatePublicProfileButton extends InfoButton {
        public CreatePublicProfileButton(final LightBlocksGame app) {
            super(app.TEXTS.get("createPublicProfileLabel"), app.TEXTS.get("publicProfileIntro"), app.skin);
            getLabel().setFontScale(.5f);
            getDescLabel().setAlignment(Align.center);

            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new CreateNewAccountDialog(app).show(getStage());
                }
            });
        }
    }
}
