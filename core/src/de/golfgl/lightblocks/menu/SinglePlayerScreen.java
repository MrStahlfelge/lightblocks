package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
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
    public static final int PAGEIDX_OVERVIEW = 0;
    public static final int PAGEIDX_MISSION = 1;
    public static final int PAGEIDX_MARATHON = 2;
    public static final int PAGEIDX_PRACTICE = 3;
    public static final int PAGEIDX_SPRINT = 4;
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

        buttons.add(modePager.getPageIndicator()
                .setTransitionStyle(PagedScrollPane.PageIndicator.transitionFade)).fill();

        leaderboardButton = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
        leaderboardButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showLeaderboards(GpgsHelper.getLeaderBoardIdByModelId(getGameModelId()));
                } catch (GameServiceException e) {
                    new VetoDialog("Error showing leaderboard.", app.skin, getStage().getWidth() * .8f)
                            .show(getStage());
                }
            }
        });
        addFocusableActor(leaderboardButton);
        buttons.add(leaderboardButton);

        validate();
        modePager.scrollToPage(app.localPrefs.getLastSinglePlayerMenuPage());
        onGameModeChanged();
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        modePager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        modePager.addPage(new IntroGroup());
        modePager.addPage(new MissionChooseGroup(this, app));
        modePager.addPage(new SimpleGameModeGroup.MarathonGroup(this, app));
        modePager.addPage(new SimpleGameModeGroup.PracticeModeGroup(this, app));
        modePager.addPage(new SimpleGameModeGroup.SprintModeGroup(this, app));
        modePager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == modePager)
                    onGameModeChanged();
            }
        });

        menuTable.add(modePager).fill().expand();
    }

    public void showPage(int idx) {
        modePager.scrollToPage(idx);
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
        leaderboardButton.setVisible(GpgsHelper.getLeaderBoardIdByModelId(getGameModelId()) != null
                && app.gpgsClient != null &&
                app.gpgsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.ShowLeaderboardUI));
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
        app.localPrefs.saveLastUsedSinglePlayerMenuPage(modePager.getCurrentPageIndex());

        if (backToMainMenu)
            hideImmediately();
    }

    public interface IGameModeGroup {
        Actor getConfiguredDefaultActor();

        String getGameModelId();
    }

    private class IntroGroup extends ControllerScrollPane implements IGameModeGroup {
        private final Button missionsButton;
        private final Button marathonButton;
        private Table table;
        private Actor lastFocused;

        public IntroGroup() {
            super(null);
            table = new Table();
            setActor(table);
            setScrollingDisabled(true, false);

            missionsButton = addPageScrollInfoButton(app.TEXTS.get("menuPlayMissionButton"),
                    app.TEXTS.get("introModelMissions"), PAGEIDX_MISSION);

            marathonButton = addPageScrollInfoButton(app.TEXTS.get("labelMarathon"),
                    app.TEXTS.get("introModelMarathon"), PAGEIDX_MARATHON);

            Button practiceButton = addPageScrollInfoButton(app.TEXTS.get("labelModel_practice"),
                    app.TEXTS.get("introModelPractice"), PAGEIDX_PRACTICE);

            Button sprintButton = addPageScrollInfoButton(app.TEXTS.get("labelModel_sprint40"),
                    app.TEXTS.get("goalModelSprint"), PAGEIDX_SPRINT);

            table.pad(0, 20, 0, 20);
            table.row();
            Label label1 = new ScaledLabel(app.TEXTS.get("introGameModels"), app.skin,
                    app.SKIN_FONT_BIG, .85f);
            label1.setWrap(true);
            label1.setAlignment(Align.center);
            table.add(label1).fill().expandX();

            table.row().padTop(10);
            table.add(missionsButton).fill();

            table.row().padTop(10);
            table.add(marathonButton).fill();

            table.row().padTop(10);
            table.add(practiceButton).fill();

            table.row().padTop(10);
            table.add(sprintButton).fill();
        }

        public void scrollToActor(Actor focused) {
            if (focused != null && focused.isDescendantOf(this))
                scrollTo(focused.getX(), focused.getY(), focused.getWidth(), focused.getHeight(), false, true);
        }

        private InfoButton addPageScrollInfoButton(String title, String description, final int pageToScrollTo) {
            InfoButton button = new InfoButton(title, description, app.skin);
            button.getDescLabel().setAlignment(Align.center);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    modePager.scrollToPage(pageToScrollTo);
                }
            });
            addFocusableActor(button);
            return button;
        }

        @Override
        public Actor getConfiguredDefaultActor() {
            if (lastFocused == null)
                lastFocused = missionsButton;
            return lastFocused;
        }

        @Override
        public String getGameModelId() {
            return null;
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            if (getStage() != null && getStage() instanceof MyStage) {
                Actor focusedActor = ((MyStage) getStage()).getFocusedActor();
                if (focusedActor != null && lastFocused != focusedActor && focusedActor.isDescendantOf(this)
                        && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    lastFocused = focusedActor;
                    scrollToActor(focusedActor);
                }
            }
        }
    }
}
