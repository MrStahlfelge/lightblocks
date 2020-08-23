package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaCheckbox;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

class VibrationSettingsDialog extends ControllerMenuDialog {
    private final LightBlocksGame app;
    private final FaButton closeButton;
    private final FaCheckbox enableVibrationButton;
    private final FaCheckbox onlyOnController;
    private final FaCheckbox hapticFeedback;

    public VibrationSettingsDialog(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        getButtonTable().defaults().pad(0, 40, 0, 40).uniform();
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        enableVibrationButton = new FaCheckbox(app.TEXTS.get("labelEnableVibration"), app.skin);
        enableVibrationButton.setChecked(app.localPrefs.getVibrationEnabled());
        enableVibrationButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.localPrefs.setVibrationEnabled(enableVibrationButton.isChecked());
                setButtonsEnabled();
            }
        });

        onlyOnController = new FaCheckbox(app.TEXTS.get("labelOnlyGameController"), app.skin);
        onlyOnController.setChecked(app.localPrefs.getVibrationOnlyController());
        onlyOnController.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.localPrefs.setVibrationOnlyController(onlyOnController.isChecked());
                setButtonsEnabled();
            }
        });

        hapticFeedback = new FaCheckbox(app.TEXTS.get("labelEnableHapticFeedback"), app.skin);
        hapticFeedback.setChecked(app.localPrefs.getVibrationHaptic());
        hapticFeedback.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.localPrefs.setVibrationHaptic(hapticFeedback.isChecked());
                setButtonsEnabled();
            }
        });

        Table contentTable = getContentTable();

        contentTable.clear();
        contentTable.defaults().left();

        contentTable.pad(15);
        contentTable.row();
        contentTable.add(new ScaledLabel(app.TEXTS.get("menuVibrationConfig"), app.skin, LightBlocksGame.SKIN_FONT_TITLE)).center();

        contentTable.row().padTop(20);
        contentTable.add(enableVibrationButton);

        contentTable.row();
        contentTable.add(onlyOnController);

        contentTable.row();
        contentTable.add(hapticFeedback);

        addFocusableActor(enableVibrationButton);
        addFocusableActor(hapticFeedback);
        addFocusableActor(onlyOnController);

        setButtonsEnabled();
    }

    private void setButtonsEnabled() {
        onlyOnController.setDisabled(!enableVibrationButton.isChecked());
        hapticFeedback.setDisabled(!enableVibrationButton.isChecked() || onlyOnController.isChecked());
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return enableVibrationButton;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }

}
