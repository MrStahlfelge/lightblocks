package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableSlider;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayScreenInput;

/**
 * Einstellungen
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class SettingsScreen extends AbstractMenuDialog {

    private PagedScrollPane groupPager;
    private GeneralSettings generalGroup;
    private GestureSettings gesturesGroup;

    public SettingsScreen(final LightBlocksGame app, Actor toHide) {
        super(app, toHide);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        generalGroup = new GeneralSettings();

        groupPager = new PagedScrollPane(app.skin, LightBlocksGame.STYLE_PAGER);
        groupPager.addPage(generalGroup);
        gesturesGroup = new GestureSettings();
        groupPager.addPage(gesturesGroup);
        groupPager.addPage(new GamepadSettings());
        groupPager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == groupPager)
                    ((ISettingsGroup) groupPager.getCurrentPage()).getDefaultActor();

            }
        });


        menuTable.add(groupPager).fill().expand();
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        buttons.add(groupPager.getPageIndicator()
                .setTransitionStyle(PagedScrollPane.PageIndicator.transitionFade)).fill();

        buttons.add();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.SETTINGS_GEARS;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuSettings");
    }

    /**
     * speichert die Einstellungen die nicht sofort gespeichert werden
     */
    private void flushChanges() {
        app.setTouchPanelSize(gesturesGroup.getTouchPanelSize());
        app.setGridIntensity(generalGroup.getGridIntensity());
    }

    @Override
    public void hide(Action action) {
        flushChanges();
        super.hide(action);
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return generalGroup.getDefaultActor();
    }

    private interface ISettingsGroup {
        Actor getDefaultActor();
    }

    private class GeneralSettings extends Table implements ISettingsGroup {
        private final RoundedTextButton menuMusicButton;
        private Slider gridIntensitySlider;
        private Image gridPreview;

        private GeneralSettings() {
            menuMusicButton = new RoundedTextButton("", "", app.skin);
            menuMusicButton.addListener(new MusicButtonListener(app, true, menuMusicButton));

            gridIntensitySlider = new TouchableSlider(0, 1, .1f, false, app.skin);
            gridIntensitySlider.setValue(app.getGridIntensity());
            gridIntensitySlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gridIntensityChanged();
                }
            });

            gridPreview = new Image(app.trBlock) {
                @Override
                public float getPrefHeight() {
                    return 0;
                }
            };
            gridPreview.setScaling(Scaling.none);

            gridIntensityChanged();

            //Settings Table
            pad(0, 20, 0, 20);
            defaults().expandY();

            row();
            add(menuMusicButton);

            row();

            Table gridIntensity = new Table();
            gridIntensity.defaults().fill();
            gridIntensity.add(new ScaledLabel(app.TEXTS.get("menuGridIntensity"), app.skin, app.SKIN_FONT_TITLE))
                    .colspan(2);
            gridIntensity.row();
            gridIntensity.add(gridPreview);
            gridIntensity.add(gridIntensitySlider).minHeight(40).fill(false, true)
                    .width(.5f * LightBlocksGame.nativeGameWidth).left();
            add(gridIntensity);

            addFocusableActor(menuMusicButton);
            addFocusableActor(gridIntensitySlider);

        }

        protected void gridIntensityChanged() {
            float color = BlockGroup.GRIDINTENSITYFACTOR * gridIntensitySlider.getValue();
            gridPreview.setColor(color, color, color, 1f);
        }

        public float getGridIntensity() {
            return gridIntensitySlider.getValue();
        }

        public Actor getDefaultActor() {
            return menuMusicButton;
        }
    }

    private class GestureSettings extends Table implements ISettingsGroup {
        PlayGesturesInput pgi;
        Group touchPanel;
        private Slider touchPanelSizeSlider;

        public GestureSettings() {
            final Button touchPanelButton = new TextButton(PlayScreenInput.getInputFAIcon(1), app.skin, FontAwesome
                    .SKIN_FONT_FA + "-checked");
            touchPanelButton.setChecked(app.getShowTouchPanel());
            touchPanelButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 app.setShowTouchPanel(touchPanelButton.isChecked());
                                             }
                                         }
            );
            touchPanelSizeSlider = new Slider(25, Gdx.graphics.getWidth() * .33f, 1, false, app.skin);
            touchPanelSizeSlider.setValue(app.getTouchPanelSize());
            touchPanelSizeSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    touchPanelSizeChanged();
                }
            });
            final Button pauseSwipeButton = new TextButton(FontAwesome.UP_ARROW, app.skin, FontAwesome
                    .SKIN_FONT_FA + "-checked");
            pauseSwipeButton.setChecked(app.getPauseSwipeEnabled());
            pauseSwipeButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 app.setPauseSwipeEnabled(pauseSwipeButton.isChecked());
                                             }
                                         }
            );

            defaults().fill();
            row().spaceTop(30);
            add(new Label(app.TEXTS.get("menuInputGestures"), app.skin, app.SKIN_FONT_BIG)).colspan(2);
            row();
            add(touchPanelButton).uniform();
            Label tg = new Label(app.TEXTS.get("menuShowTouchPanel"), app.skin);
            tg.setWrap(true);
            add(tg).prefWidth(LightBlocksGame.nativeGameWidth * 0.5f);
            row();
            add();
            add(new Label(app.TEXTS.get("menuSizeOfTouchPanel"), app.skin));
            row();
            add();
            add(touchPanelSizeSlider).minHeight(40).fill(false, true)
                    .width(.5f * LightBlocksGame.nativeGameWidth).left();
            row();
            add(pauseSwipeButton).uniform();
            add(new Label(app.TEXTS.get("menuPauseSwipeEnabled"), app.skin));
        }

        protected void touchPanelSizeChanged() {
            if (pgi == null)
                pgi = new PlayGesturesInput();

            if (touchPanel != null && touchPanel.hasParent())
                touchPanel.remove();
            touchPanel = pgi.initializeTouchPanel((AbstractScreen) app.getScreen(),
                    (int) touchPanelSizeSlider.getValue());
            touchPanel.setVisible(true);
            touchPanel.setTouchable(Touchable.disabled);

            Vector2 position = new Vector2(0, 0);
            position = touchPanelSizeSlider.localToStageCoordinates(position);

            touchPanel.setPosition(getWidth() / 2, position.y - 30);
            Color c = pgi.rotateRightColor;
            c.a = 1;
            pgi.setTouchPanelColor(c);
            touchPanel.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeOut(1f, Interpolation.fade),
                    Actions.removeActor()));
        }

        public int getTouchPanelSize() {
            return (int) touchPanelSizeSlider.getValue();
        }

        @Override
        public Actor getDefaultActor() {
            return touchPanelSizeSlider;
        }
    }

    private class GamepadSettings extends Table implements ISettingsGroup {
        private final Button gamePadButton;

        public GamepadSettings() {
            gamePadButton = new TextButton(PlayScreenInput.getInputFAIcon(3), app.skin, FontAwesome
                    .SKIN_FONT_FA);
            gamePadButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    GamepadConfigDialog gpc = new GamepadConfigDialog(app);
                    gpc.show(getStage());
                }
            });

            defaults().fill();
            row().spaceTop(30);
            add(gamePadButton).uniform();
            add(new Label(app.TEXTS.get("menuGamepadConfig"), app.skin, app.SKIN_FONT_BIG));

        }

        @Override
        public Actor getDefaultActor() {
            return gamePadButton;
        }
    }
}