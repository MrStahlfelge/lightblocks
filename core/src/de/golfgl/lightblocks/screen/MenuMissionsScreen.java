package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Menu for missions
 *
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class MenuMissionsScreen extends AbstractMenuScreen {
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
