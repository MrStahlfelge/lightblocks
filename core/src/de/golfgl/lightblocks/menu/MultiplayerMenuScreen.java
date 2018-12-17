package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.backend.BackendMatchesMenuPage;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
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

        if (LightBlocksGame.GAME_DEVMODE)
            modePager.addPage(new BackendMatchesMenuPage(app, this));
    }

    public interface IMultiplayerModePage {
        Actor getDefaultActor();
    }
}
