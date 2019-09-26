package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaCheckbox;
import de.golfgl.lightblocks.scene2d.FaRadioButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableSlider;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.OnScreenGamepadConfigscreen;
import de.golfgl.lightblocks.screen.PlayGesturesInput;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Einstellungen
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class SettingsScreen extends AbstractMenuDialog {
    public static final int TOUCHPANELSIZE_MIN = 25;

    private PagedScrollPane groupPager;
    private GeneralSettings generalGroup;
    private TouchInputSettings gesturesGroup;

    public SettingsScreen(final LightBlocksGame app, Group toHide) {
        super(app, toHide);

        app.localPrefs.setScreenShownInThisVersion(LocalPrefs.KEY_SETTINGS_SCREEN);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        generalGroup = new GeneralSettings();

        groupPager = new PagedScrollPane(app.skin, LightBlocksGame.SKIN_STYLE_PAGER);
        groupPager.addPage(generalGroup);
        gesturesGroup = new TouchInputSettings();
        if (PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN))
            groupPager.addPage(gesturesGroup);
        if (LightBlocksGame.isOnAndroidTV() || Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
            groupPager.addPage(new TvRemoteSettings());

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
        app.localPrefs.setTouchPanelSize(gesturesGroup.getTouchPanelSize());
        app.localPrefs.setGridIntensity(generalGroup.getGridIntensity());
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
            gridIntensitySlider.setValue(app.localPrefs.getGridIntensity());
            gridIntensitySlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    gridIntensityChanged();
                }
            });

            final Button colorModeCheck = new FaCheckbox(app.TEXTS.get("menuBlockColorShades"), app.skin);
            colorModeCheck.setChecked(app.localPrefs.getBlockColorMode() != BlockActor.COLOR_MODE_NONE);
            colorModeCheck.addListener(new ChangeListener() {
                                           public void changed(ChangeEvent event, Actor actor) {
                                               app.localPrefs.setBlockColorMode(colorModeCheck.isChecked() ? BlockActor
                                                       .COLOR_MODE_SHADEOFGREY : BlockActor.COLOR_MODE_NONE);
                                           }
                                       }
            );

            final Button showGhostpiece = new FaCheckbox(app.TEXTS.get("menuGhostpiece"), app.skin);
            showGhostpiece.setChecked(app.localPrefs.getShowGhostpiece());
            showGhostpiece.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.localPrefs.setShowGhostpiece(showGhostpiece.isChecked());
                }
            });

            Button gamePadButton = new GlowLabelButton(FontAwesome.DEVICE_GAMEPAD, app.TEXTS.get
                    ("menuGamepadConfig"), app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
            gamePadButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new GamepadSettingsDialog(app).show(getStage());
                }
            });

            GlowLabelButton themeButton = new GlowLabelButton(FontAwesome.MISC_PIECE, app.TEXTS.get("menuThemeConfig"), app.skin,
                    GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
            themeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new ThemeSettingsDialog(app).show(getStage());
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
            add(showGhostpiece);

            row();
            add(colorModeCheck);

            row();
            add(gamePadButton);

            if (app.canInstallTheme()) {
                row();
                add(themeButton);
            }

            addFocusableActor(menuMusicButton);
            addFocusableActor(gridIntensitySlider);
            addFocusableActor(colorModeCheck);
            addFocusableActor(showGhostpiece);
            addFocusableActor(gamePadButton);
            addFocusableActor(themeButton);
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

    private class TouchInputSettings extends Table implements ISettingsGroup {
        private final Button defaultFocusedButton;
        private final Cell settingsTableCell;
        PlayGesturesInput pgi;
        Group touchPanel;
        private Slider touchPanelSizeSlider;
        private Table gestureSettings;
        private Table onScreenGamepadSettings;
        private Table onScreenButtonSettings;

        public TouchInputSettings() {
            final FaRadioButton<LocalPrefs.TouchControlType> onScreenControlsButton = new FaRadioButton<>(app.skin,
                    GlowLabelButton.FONT_SCALE_SUBMENU * 1.1f, false);
            onScreenControlsButton.addEntry(LocalPrefs.TouchControlType.gestures, "", app.TEXTS.get("menuUseGestureControls"));
            onScreenControlsButton.addEntry(LocalPrefs.TouchControlType.onScreenButtonsGamepad, "", app.TEXTS.get("menuUseOnScreenGamepad"));
            onScreenControlsButton.addEntry(LocalPrefs.TouchControlType.onScreenButtonsPortrait, "", app.TEXTS.get("menuUseOnScreenButtons"));
            onScreenControlsButton.setValue(app.localPrefs.getUsedTouchControls());
            onScreenControlsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.localPrefs.setUsedTouchControls(onScreenControlsButton.getValue());
                    setSettingsTableActor();
                }
            });

            final Button touchPanelButton = new FaCheckbox(app.TEXTS.get("menuShowTouchPanel"), app.skin);
            touchPanelButton.setChecked(app.localPrefs.getShowTouchPanel());
            touchPanelButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 app.localPrefs.setShowTouchPanel(touchPanelButton.isChecked());
                                             }
                                         }
            );
            int screenWidth = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            touchPanelSizeSlider = new TouchableSlider(TOUCHPANELSIZE_MIN, screenWidth * .33f, 1, false, app.skin);
            touchPanelSizeSlider.setValue(app.localPrefs.getTouchPanelSize(app.getDisplayDensityRatio()));
            touchPanelSizeSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    touchPanelSizeChanged();
                }
            });
            final FaRadioButton<Integer> swipeUpButtons = new FaRadioButton<Integer>(app.skin, false);
            swipeUpButtons.addEntry(PlayGesturesInput.SWIPEUP_DONOTHING, "", app.TEXTS.get("menuSwipeUpToNothing"));
            swipeUpButtons.addEntry(PlayGesturesInput.SWIPEUP_PAUSE, "", app.TEXTS.get("menuSwipeUpToPause"));
            swipeUpButtons.addEntry(PlayGesturesInput.SWIPEUP_HARDDROP, "", app.TEXTS.get("menuSwipeUpToHardDrop"));

            swipeUpButtons.setValue(app.localPrefs.getSwipeUpType());
            swipeUpButtons.addListener(new ChangeListener() {
                                           public void changed(ChangeEvent event, Actor actor) {
                                               if (actor == swipeUpButtons)
                                                   app.localPrefs.setSwipeUpType(swipeUpButtons.getValue());
                                           }
                                       }
            );

            pad(0, 20, 0, 20);
            defaults().expand().fillX();

            row();
            add(new ScaledLabel(app.TEXTS.get("menuInputGestures"), app.skin, app.SKIN_FONT_TITLE, .8f))
                    .top().fill(false);

            row();
            Table touchControlTypeTable = new Table();
            ScaledLabel menuControlTypeLabel = new ScaledLabel(app.TEXTS.get("menuTouchControlType") + ":", app.skin,
                    app.SKIN_FONT_TITLE);
            menuControlTypeLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onScreenControlsButton.changeValue();
                }
            });
            touchControlTypeTable.add(menuControlTypeLabel);
            touchControlTypeTable.row();

            touchControlTypeTable.add(onScreenControlsButton);
            add(touchControlTypeTable);

            gestureSettings = new Table();
            gestureSettings.add(touchPanelButton).bottom();

            Table touchPanelTable = new Table();
            touchPanelTable.add(new ScaledLabel(app.TEXTS.get("menuSizeOfTouchPanel"), app.skin, app.SKIN_FONT_REG,
                    .8f));
            touchPanelTable.row();
            touchPanelTable.add(touchPanelSizeSlider).minHeight(40).fill();

            gestureSettings.row().padTop(20);
            gestureSettings.add(touchPanelTable).top();

            Table swipeUp = new Table();
            ScaledLabel menuSwipeUpToLabel = new ScaledLabel(app.TEXTS.get("menuSwipeUpTo"), app.skin, app
                    .SKIN_FONT_TITLE);
            menuSwipeUpToLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    swipeUpButtons.changeValue();
                }
            });
            swipeUp.add(menuSwipeUpToLabel);
            swipeUp.row();
            swipeUp.add(swipeUpButtons);
            gestureSettings.row();
            gestureSettings.add(swipeUp);

            gestureSettings.row().padTop(20);
            final Button hideHoldCheckbox = new FaCheckbox(app.TEXTS.get("menuHideHoldButton"), app.skin);
            hideHoldCheckbox.setChecked(!app.localPrefs.isShowTouchHoldButton());
            hideHoldCheckbox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.localPrefs.setShowTouchHoldButton(!hideHoldCheckbox.isChecked());
                }
            });
            gestureSettings.add(hideHoldCheckbox);

            Button osbHelp = new RoundedTextButton(app.TEXTS.get("buttonHowToPlay"), app.skin);
            addFocusableActor(osbHelp);
            osbHelp.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new VetoDialog(app.TEXTS.get("inputOnScreenButtonHelp"), app.skin,
                            .8f * LightBlocksGame.nativeGameWidth).show(getStage());
                }
            });
            onScreenButtonSettings = new Table();
            onScreenButtonSettings.add(new Image(app.trPreviewOsb));
            onScreenButtonSettings.row();
            onScreenButtonSettings.add(osbHelp).expandY().top();
            onScreenButtonSettings.validate();

            Button adjustGamepad = new RoundedTextButton(app.TEXTS.get("buttonAdjust"), app.skin);
            addFocusableActor(adjustGamepad);
            adjustGamepad.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.setScreen(new OnScreenGamepadConfigscreen(app));
                }
            });
            onScreenGamepadSettings = new Table();
            onScreenGamepadSettings.add(new Image(app.trPreviewOsg));
            onScreenGamepadSettings.row().padTop(20);
            onScreenGamepadSettings.add(adjustGamepad).expandY().top();
            onScreenGamepadSettings.validate();

            row();
            settingsTableCell = add().height(gestureSettings.getPrefHeight()).fillX();
            setSettingsTableActor();

            addFocusableActor(onScreenControlsButton);
            addFocusableActor(touchPanelButton);
            addFocusableActor(touchPanelSizeSlider);
            addFocusableActor(swipeUpButtons);
            addFocusableActor(hideHoldCheckbox);
            defaultFocusedButton = onScreenControlsButton;
        }

        protected void setSettingsTableActor() {
            Actor actor;
            switch (app.localPrefs.getUsedTouchControls()) {
                case onScreenButtonsGamepad:
                    actor = onScreenGamepadSettings;
                    break;
                case onScreenButtonsPortrait:
                    actor = onScreenButtonSettings;
                    break;
                default:
                    actor = gestureSettings;
            }
            settingsTableCell.setActor(actor);
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
            return defaultFocusedButton;
        }
    }

    private class TvRemoteSettings extends Table implements ISettingsGroup {
        private final Button defaultFocusedButton;
        private LocalPrefs.TvRemoteKeyConfig tvRemoteKeyConfig;
        private int[] configKey;


        public TvRemoteSettings() {
            tvRemoteKeyConfig = app.localPrefs.getTvRemoteKeyConfig();
            convertConfigToArray();

            pad(0, 20, 0, 20);
            defaults().expand().fillX();

            row();
            add(new ScaledLabel(app.TEXTS.get(Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)
                    ? "menuSettingsKeyboard" : "menuSettingsTvRemote"),
                    app.skin, app.SKIN_FONT_TITLE, .8f)).top().fill(false);

            row();
            ScaledLabel menuSettingsHelpTvRemote = new ScaledLabel(app.TEXTS.get("menuSettingsHelpTvRemote"), app
                    .skin, LightBlocksGame.SKIN_FONT_REG);
            menuSettingsHelpTvRemote.setAlignment(Align.center);
            menuSettingsHelpTvRemote.setWrap(true);
            add(menuSettingsHelpTvRemote).fillX().expand();

            Table buttonConfig = new Table();
            buttonConfig.defaults().pad(3, 10, 3, 10);
            defaultFocusedButton = addButtonRow(buttonConfig, 0, "configTvRemoteRight");
            addButtonRow(buttonConfig, 1, "configTvRemoteLeft");
            addButtonRow(buttonConfig, 2, "configTvRemoteRotateCw");
            addButtonRow(buttonConfig, 3, "configTvRemoteRotateCc");
            addButtonRow(buttonConfig, 4, "configTvRemoteSoftDrop");
            addButtonRow(buttonConfig, 5, "configTvRemoteHardDrop");
            addButtonRow(buttonConfig, 6, "configTvRemoteHold");
            addButtonRow(buttonConfig, 7, "configTvRemoteFreeze");

            row();
            add(buttonConfig).fillX().expand();
        }

        protected GlowLabelButton addButtonRow(Table buttonConfig, final int index, String description) {
            buttonConfig.row();
            final String descriptionText = app.TEXTS.get(description);
            buttonConfig.add(new ScaledLabel(descriptionText, app.skin, LightBlocksGame.SKIN_FONT_TITLE)).right();
            final GlowLabelButton changeButton = new GlowLabelButton(Input.Keys.toString(configKey[index]), app.skin,
                    GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
            changeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new ChangeButtonDialog(descriptionText, index, changeButton).show(getStage());
                }
            });
            addFocusableActor(changeButton);
            buttonConfig.add(changeButton);
            return changeButton;
        }

        protected void convertConfigToArray() {
            configKey = new int[8];
            configKey[0] = tvRemoteKeyConfig.keyCodeRight;
            configKey[1] = tvRemoteKeyConfig.keyCodeLeft;
            configKey[2] = tvRemoteKeyConfig.keyCodeRotateClockwise;
            configKey[3] = tvRemoteKeyConfig.keyCodeRotateCounterclock;
            configKey[4] = tvRemoteKeyConfig.keyCodeSoftDrop;
            configKey[5] = tvRemoteKeyConfig.keyCodeHarddrop;
            configKey[6] = tvRemoteKeyConfig.keyCodeHold;
            configKey[7] = tvRemoteKeyConfig.keyCodeFreeze;
        }

        protected void convertArrayToConfigAndSave() {
            tvRemoteKeyConfig.keyCodeRight = configKey[0];
            tvRemoteKeyConfig.keyCodeLeft = configKey[1];
            tvRemoteKeyConfig.keyCodeRotateClockwise = configKey[2];
            tvRemoteKeyConfig.keyCodeRotateCounterclock = configKey[3];
            tvRemoteKeyConfig.keyCodeSoftDrop = configKey[4];
            tvRemoteKeyConfig.keyCodeHarddrop = configKey[5];
            tvRemoteKeyConfig.keyCodeHold = configKey[6];
            tvRemoteKeyConfig.keyCodeFreeze = configKey[7];
            app.localPrefs.saveTvRemoteConfig();
        }

        @Override
        public Actor getDefaultActor() {
            return defaultFocusedButton;
        }

        private class ChangeButtonDialog extends Dialog {

            private final GlowLabelButton changeButton;
            private final int index;
            private InputProcessor oldInputProcessor;

            public ChangeButtonDialog(String description, int index, GlowLabelButton changeButton) {
                super("", app.skin);
                Button cancelButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
                button(cancelButton);

                this.changeButton = changeButton;
                this.index = index;

                Table contentTable = getContentTable();

                contentTable.row();
                ScaledLabel helpLabel = new ScaledLabel(app.TEXTS.format("configTvRemoteRecord",
                        description.toUpperCase(), LightBlocksGame.isOnAndroidTV() ? "BACK" : "ESC"),
                        getSkin(), LightBlocksGame.SKIN_FONT_TITLE);
                helpLabel.setAlignment(Align.center);
                contentTable.add(helpLabel).pad(20);
            }

            @Override
            public Dialog show(Stage stage, Action action) {
                oldInputProcessor = Gdx.input.getInputProcessor();

                InputMultiplexer myMultiPlexer = new InputMultiplexer();
                myMultiPlexer.addProcessor(new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        if (((MyStage) getStage()).isEscapeActionKeyCode(keycode))
                            keycode = 0;

                        configKey[index] = keycode;
                        changeButton.setText(Input.Keys.toString(keycode));
                        convertArrayToConfigAndSave();

                        ChangeButtonDialog.this.hide();

                        return true;
                    }

                    @Override
                    public boolean keyUp(int keycode) {
                        return true;
                    }
                });
                myMultiPlexer.addProcessor(oldInputProcessor);
                Gdx.input.setInputProcessor(myMultiPlexer);
                return super.show(stage, action);
            }

            @Override
            public void hide(Action action) {
                Gdx.input.setInputProcessor(oldInputProcessor);
                super.hide(action);
            }
        }
    }

}