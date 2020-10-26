package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.menu.backend.BackendMatchesMenuPage;
import de.golfgl.lightblocks.scene2d.FaRadioButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class MultiplayerMenuScreen extends AbstractMenuDialog {
    protected Cell mainCell;
    protected Button shareAppButton;
    protected PagedScrollPane modePager;
    protected PagedScrollPane.PageIndicator pageIndicator;
    private Cell<Button> shareButtonCell;

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

        shareButtonCell = buttons.add(shareAppButton);
        addFocusableActor(shareAppButton);

        validate();
        modePager.scrollToPage(app.localPrefs.getLastMultiPlayerMenuPage());
        setSecondButton((IMultiplayerModePage) modePager.getCurrentPage());
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        modePager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        modePager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == modePager && getStage() != null) {
                    IMultiplayerModePage currentPage = (IMultiplayerModePage) modePager.getCurrentPage();
                    ((MyStage) getStage()).setFocusedActor(currentPage.getDefaultActor());
                    setSecondButton(currentPage);
                    app.localPrefs.saveLastUsedMultiPlayerMenuPage(modePager.getCurrentPageIndex());
                }
            }
        });

        modePager.addPage(new BackendMatchesMenuPage(app, this));
        modePager.addPage(new LocalDeviceMultiplayerPage());

        mainCell = menuTable.add(modePager).fill().expand();
    }

    protected void setSecondButton(IMultiplayerModePage currentPage) {
        Actor secondButton = currentPage.getSecondMenuButton();
        shareButtonCell.setActor(secondButton != null ? secondButton : shareAppButton);
    }

    public void showPage(int idx) {
        modePager.scrollToPage(idx);
    }

    public interface IMultiplayerModePage {
        Actor getDefaultActor();

        @Nullable
        Actor getSecondMenuButton();
    }

    private class LocalDeviceMultiplayerPage extends Table implements IMultiplayerModePage {
        private final PlayButton playButton;
        private final BeginningLevelChooser beginningLevelSlider;
        private final FaRadioButton<Integer> modeType;

        public LocalDeviceMultiplayerPage() {
            Label playmodeInfo = new ScaledLabel(app.TEXTS.get("multiplayerDeviceHelp"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG, .75f);
            playmodeInfo.setWrap(true);
            playmodeInfo.setAlignment(Align.center);

            playButton = new PlayButton(app);
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    beginNewGame();
                }
            });
            addFocusableActor(playButton);

            Table paramsTable = new Table();
            paramsTable.add(new ScaledLabel(app.TEXTS.get("labelBeginningLevel"), app.skin, LightBlocksGame.SKIN_FONT_BIG)).left();
            beginningLevelSlider = new BeginningLevelChooser(app, 0, 9) {
                @Override
                protected void onControllerDefaultKeyDown() {
                    ((MyStage) getStage()).setFocusedActor(playButton);
                }
            };
            addFocusableActor(beginningLevelSlider.getSlider());
            paramsTable.row();
            paramsTable.add(beginningLevelSlider);

            Table typeTable = new Table();
            ScaledLabel typeLabel = new ScaledLabel(app.TEXTS.get("marathonChooseTypeLabel"),
                    app.skin, LightBlocksGame.SKIN_FONT_BIG);
            modeType = new FaRadioButton<>(app.skin, false);
            modeType.setShowIndicator(false);
            modeType.addEntry(InitGameParameters.TYPE_CLASSIC, "", app.TEXTS.get("modeTypeClassic"));
            modeType.addEntry(InitGameParameters.TYPE_MODERN, "", app.TEXTS.get("modeTypeModern"));
            modeType.setValue(app.localPrefs.getLastUsedModeType());
            addFocusableActor(modeType);
            typeTable.add(typeLabel);
            typeTable.row();
            typeTable.add(modeType);

            add(new ScaledLabel(app.TEXTS.get("labelMultiplayerDevice"), app.skin, LightBlocksGame
                    .SKIN_FONT_TITLE, .8f));
            row();
            add(playmodeInfo).fill().expandX().pad(10, 20, 10, 20);
            row();
            add(typeTable).expand().fill();
            row();
            add(paramsTable).expand();
            row();
            add(playButton).expandY();
        }

        @Override
        public Actor getDefaultActor() {
            return playButton;
        }

        @Override
        public Actor getSecondMenuButton() {
            return null;
        }

        private void beginNewGame() {
            InitGameParameters initGameParametersParams = new InitGameParameters();
            initGameParametersParams.setGameMode(InitGameParameters.GameMode.DeviceMultiplayer);
            initGameParametersParams.setBeginningLevel(beginningLevelSlider.getValue());
            initGameParametersParams.setInputKey(PlayScreenInput.KEY_KEYSORGAMEPAD);
            initGameParametersParams.setModeType(modeType.getValue());

            app.localPrefs.saveLastUsedModeType(modeType.getValue());

            try {
                PlayScreen.gotoPlayScreen(app, initGameParametersParams);
            } catch (VetoException e) {
                new VetoDialog(e.getMessage(), app.skin, LightBlocksGame.nativeGameWidth * .75f).show(getStage());
            }
        }
    }
}
