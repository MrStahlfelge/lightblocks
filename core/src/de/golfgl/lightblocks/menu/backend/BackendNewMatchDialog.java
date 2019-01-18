package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.menu.BeginningLevelChooser;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 18.01.2019.
 */

public class BackendNewMatchDialog extends ControllerMenuDialog {
    private final LightBlocksGame app;
    private final Button randomPlayerButton;
    private final BeginningLevelChooser beginningLevelSlider;
    private final Button leaveButton;

    public BackendNewMatchDialog(LightBlocksGame app) {
        super("", app.skin);
        this.app = app;

        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);

        // Back button
        button(leaveButton);

        beginningLevelSlider = new BeginningLevelChooser(app, app.localPrefs.getBattleBeginningLevel(), 9) {
            @Override
            protected void onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(randomPlayerButton);
            }
        };
        addFocusableActor(beginningLevelSlider.getSlider());
        Table levelSliderTable = new Table();
        levelSliderTable.add(new ScaledLabel(app.TEXTS.get("labelBeginningMaxLevel"), app.skin,
                LightBlocksGame.SKIN_FONT_REG)).left();
        levelSliderTable.row();
        levelSliderTable.add(beginningLevelSlider);


        randomPlayerButton = new RoundedTextButton(app.TEXTS.get("buttonRandomOpponent"), app.skin);
        randomPlayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openNewMatch();
            }
        });
        addFocusableActor(randomPlayerButton);

        Table contentTable = getContentTable();
        contentTable.add(new ScaledLabel(app.TEXTS.get("buttonNewBattle"), app.skin, LightBlocksGame.SKIN_FONT_TITLE)
        ).pad(10);

        contentTable.row().pad(15, 20, 15, 20);
        contentTable.add(levelSliderTable);

        contentTable.row().padBottom(15);
        contentTable.add(randomPlayerButton);
    }

    private void openNewMatch() {
        app.localPrefs.saveBattleBeginningLevel(beginningLevelSlider.getValue());
        app.backendManager.openNewMultiplayerMatch(null, beginningLevelSlider.getValue(),
                new WaitForResponse<MatchEntity>(app, getStage()) {
                    @Override
                    protected void onSuccess() {
                        hide();
                    }
                });
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return randomPlayerButton;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return leaveButton;
    }
}
