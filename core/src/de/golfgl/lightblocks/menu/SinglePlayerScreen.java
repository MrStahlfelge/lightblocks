package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.InfoButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class SinglePlayerScreen extends AbstractMenuDialog {

    private static final String PREF_KEY_ACTIVEPAGE = "singlePlayerPage";
    private PagedScrollPane modePager;
    private Button leaderboardButton;

    public SinglePlayerScreen(final LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        leaderboardButton.setDisabled(app.gpgsClient == null || !app.gpgsClient.isSessionActive());
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        leaderboardButton = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
        leaderboardButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showLeaderboards(GpgsHelper.getLeaderBoardIdByModelId(getGameModelId()));
                } catch (GameServiceException e) {
                    new VetoDialog("Error showing leaderboard.", app.skin, getStage().getWidth()).show(getStage());
                }
            }
        });
        addFocusableActor(leaderboardButton);
        buttons.add(leaderboardButton);

        validate();
        modePager.scrollToPage(app.prefs.getInteger(PREF_KEY_ACTIVEPAGE, 0));
        onGameModeChanged();
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        modePager = new PagedScrollPane();
        modePager.addPage(new IntroGroup());
        modePager.addPage(new MissionChooseGroup(this, app));
        modePager.addPage(new MarathonGroup(this, app));
        modePager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == modePager)
                    onGameModeChanged();
            }
        });

        menuTable.add(modePager).fill().expand();
    }

    /**
     * called when game mode was changed
     */
    protected void onGameModeChanged() {
        if (getStage() != null)
            ((MyStage) getStage()).setFocusedActor(((IGameModeGroup) modePager.getCurrentPage())
                    .getConfiguredDefaultActor());
        onGameModelIdChanged();
    }

    protected void onGameModelIdChanged() {
        leaderboardButton.setVisible(GpgsHelper.getLeaderBoardIdByModelId(getGameModelId()) != null);
    }

    private String getGameModelId() {
        return ((IGameModeGroup) modePager.getCurrentPage()).getGameModelId();
    }

    @Override
    protected String getTitleIcon() {
        return null;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuSinglePlayer");
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return ((IGameModeGroup) modePager.getCurrentPage()).getConfiguredDefaultActor();
    }

    /**
     * called by game mode groups when screen switched to play screen
     *
     * @param backToMainMenu if true, SingplePlayerScreen will get closed
     */
    public void gameStarted(boolean backToMainMenu) {
        app.prefs.putInteger(PREF_KEY_ACTIVEPAGE, modePager.getCurrentPageIndex());
        app.prefs.flush();

        if (backToMainMenu)
            hideImmediately();
    }

    public interface IGameModeGroup {
        Actor getConfiguredDefaultActor();

        String getGameModelId();
    }

    private class IntroGroup extends Table implements IGameModeGroup {
        private final Button missionsButton;
        private final Button marathonButton;

        public IntroGroup() {
            missionsButton = new InfoButton(app.TEXTS.get("menuPlayMissionButton"),
                    app.TEXTS.get("introModelMissions"), app.skin);
            missionsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    modePager.scrollToPage(1);
                }
            });
            addFocusableActor(missionsButton);
            marathonButton = new InfoButton(app.TEXTS.get("labelMarathon"),
                    app.TEXTS.get("introModelMarathon") + " " + app.TEXTS.get("goalModelMarathon"),
                    app.skin);
            marathonButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    modePager.scrollToPage(2);
                }
            });
            addFocusableActor(marathonButton);

            pad(0, 20, 0, 20);
            row();
            Label label1 = new ScaledLabel(app.TEXTS.get("introGameModels"), app.skin,
                    app.SKIN_FONT_BIG, .85f);
            label1.setWrap(true);
            label1.setAlignment(Align.center);
            add(label1).fill().expandX();

            row().padTop(30);
            add(missionsButton).fill();

            row().padTop(30);
            add(marathonButton).fill();
        }

        @Override
        public Actor getConfiguredDefaultActor() {
            return missionsButton;
        }

        @Override
        public String getGameModelId() {
            return null;
        }
    }
}
