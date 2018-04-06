package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 06.04.2018.
 */

public class CompetitionMenuScreen extends AbstractMenuDialog {

    private Button websiteButton;

    public CompetitionMenuScreen(LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PEOPLE;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMultiplayerButton");
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        Label competitionIntro = new ScaledLabel(app.TEXTS.get("competitionIntro"), app.skin, app.SKIN_FONT_TITLE);
        competitionIntro.setAlignment(Align.center);
        competitionIntro.setWrap(true);

        websiteButton = new RoundedTextButton(app.TEXTS.get("buttonWebsite"), app.skin);
        websiteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI(LightBlocksGame.GAME_URL);
            }
        });

        menuTable.row();
        menuTable.add(competitionIntro).fill().expandX().pad(20);

        menuTable.row().padTop(30);
        menuTable.add(websiteButton);
        addFocusableActor(websiteButton);
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return websiteButton;
    }
}
