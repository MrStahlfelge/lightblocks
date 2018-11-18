package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class MultiplayerMenuScreen extends AbstractMenuDialog {
    protected Cell mainCell;
    protected Button shareAppButton;
    protected PagedScrollPane modePager;
    protected PagedScrollPane.PageIndicator pageIndicator;

    public MultiplayerMenuScreen(LightBlocksGame app, Group actorToHide) {
        super(app, actorToHide);
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PEOPLE;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuPlayMultiplayerButton");
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        shareAppButton = new ShareButton(app);

        pageIndicator = modePager.getPageIndicator();
        buttons.add(pageIndicator)
                .minWidth(modePager.getPageIndicator().getPrefWidth() * 2)
                .uniform(false, false);

        buttons.add(shareAppButton);
        addFocusableActor(shareAppButton);

        validate();
        modePager.scrollToPage(app.localPrefs.getLastMultiPlayerMenuPage());

    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        modePager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        modePager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == modePager && getStage() != null) {
                    ((MyStage) getStage()).setFocusedActor(((IMultiplayerModePage) modePager.getCurrentPage())
                            .getDefaultActor());
                    app.localPrefs.saveLastUsedMultiPlayerMenuPage(modePager.getCurrentPageIndex());
                }
            }
        });

        modePager.addPage(new GtbDuel());
    }

    protected interface IMultiplayerModePage {
        Actor getDefaultActor();
    }

    private class GtbDuel extends Table implements IMultiplayerModePage {
        private Button websiteButton;

        public GtbDuel() {
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

            row();
            add(competitionIntro).fill().expandX().pad(20);

            row().padTop(30);
            add(websiteButton);
            addFocusableActor(websiteButton);

        }

        @Override
        public Actor getDefaultActor() {
            return websiteButton;
        }
    }

}
