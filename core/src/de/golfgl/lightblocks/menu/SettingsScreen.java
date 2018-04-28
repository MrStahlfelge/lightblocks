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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.FaCheckbox;
import de.golfgl.lightblocks.scene2d.RadioButtonTable;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
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

        groupPager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        groupPager.addPage(generalGroup);
        gesturesGroup = new GestureSettings();
        if (PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN))
            groupPager.addPage(gesturesGroup);
        groupPager.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor == groupPager)
                    ((MyStage) getStage()).setFocusedActor(((ISettingsGroup) groupPager.getCurrentPage())
                            .getDefaultActor());

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
        private final GlowLabelButton menuMusicButton;
        private Slider gridIntensitySlider;
        private Image gridPreview;

        private GeneralSettings() {
            menuMusicButton = new GlowLabelButton(".", ".", app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
            menuMusicButton.addListener(new MusicButtonListener(app, true, menuMusicButton));

            gridIntensitySlider = new TouchableSlider(0, 1, .1f, false, app.skin);
            gridIntensitySlider.setValue(app.getGridIntensity());
            gridIntensitySlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gridIntensityChanged();
                }
            });

            final Button colorModeCheck = new FaCheckbox(app.TEXTS.get("menuBlockColorShades"), app.skin);
            colorModeCheck.setChecked(app.getBlockColorMode() != BlockActor.COLOR_MODE_NONE);
            colorModeCheck.addListener(new ChangeListener() {
                                           public void changed(ChangeEvent event, Actor actor) {
                                               app.setBlockColorMode(colorModeCheck.isChecked() ? BlockActor
                                                       .COLOR_MODE_SHADEOFGREY : BlockActor.COLOR_MODE_NONE);
                                           }
                                       }
            );

            Button gamePadButton = new GlowLabelButton(FontAwesome.DEVICE_GAMEPAD, app.TEXTS.get
                    ("menuGamepadConfig"), app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
            gamePadButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new GamepadSettingsDialog(app).show(getStage());
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
            add(new ScaledLabel(app.TEXTS.get("menuGeneral"), app.skin, app.SKIN_FONT_TITLE, .8f)).top();

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

            row();
            add(colorModeCheck);

            row();
            add(gamePadButton);

            addFocusableActor(menuMusicButton);
            addFocusableActor(gridIntensitySlider);
            addFocusableActor(colorModeCheck);
            addFocusableActor(gamePadButton);
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
        private final Button touchPanelButton;
        PlayGesturesInput pgi;
        Group touchPanel;
        private Slider touchPanelSizeSlider;

        public GestureSettings() {
            touchPanelButton = new FaCheckbox(app.TEXTS.get("menuShowTouchPanel"), app.skin);
            touchPanelButton.setChecked(app.getShowTouchPanel());
            touchPanelButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 app.setShowTouchPanel(touchPanelButton.isChecked());
                                             }
                                         }
            );
            touchPanelSizeSlider = new TouchableSlider(25, Gdx.graphics.getWidth() * .33f, 1, false, app.skin);
            touchPanelSizeSlider.setValue(app.getTouchPanelSize());
            touchPanelSizeSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    touchPanelSizeChanged();
                }
            });
            final RadioButtonTable<Integer> swipeUpButtons = new RadioButtonTable(app);
            swipeUpButtons.addEntry(app.TEXTS.get("menuSwipeUpToNothing"), PlayGesturesInput.SWIPEUP_DONOTHING);
            swipeUpButtons.addEntry(app.TEXTS.get("menuSwipeUpToPause"), PlayGesturesInput.SWIPEUP_PAUSE);
            swipeUpButtons.addEntry(app.TEXTS.get("menuSwipeUpToHardDrop"), PlayGesturesInput.SWIPEUP_HARDDROP);

            swipeUpButtons.setValue(app.getSwipeUpType());
            swipeUpButtons.addListener(new ChangeListener() {
                                           public void changed(ChangeEvent event, Actor actor) {
                                               if (actor == swipeUpButtons)
                                                   app.setSwipeUpType(swipeUpButtons.getValue());
                                           }
                                       }
            );

            pad(0, 20, 0, 20);
            defaults().expand().fillX();

            row();
            add(new ScaledLabel(app.TEXTS.get("menuInputGestures"), app.skin, app.SKIN_FONT_TITLE, .8f))
                    .top().fill(false);

            row();
            add(touchPanelButton).bottom();

            Table touchPanelTable = new Table();
            touchPanelTable.add(new ScaledLabel(app.TEXTS.get("menuSizeOfTouchPanel"), app.skin, app.SKIN_FONT_REG,
                    .8f));
            touchPanelTable.row();
            touchPanelTable.add(touchPanelSizeSlider).minHeight(40).fill();

            row().padTop(20);
            add(touchPanelTable).top();

            Table swipeUp = new Table();
            swipeUp.add(new ScaledLabel(app.TEXTS.get("menuSwipeUpTo"), app.skin, app.SKIN_FONT_TITLE)).left();
            swipeUp.row();
            swipeUp.add(swipeUpButtons).fill();
            row();
            add(swipeUp);

            addFocusableActor(touchPanelButton);
            addFocusableActor(touchPanelSizeSlider);
            buttonsToAdd.addAll(swipeUpButtons.getButtons());
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
            return touchPanelButton;
        }
    }
}