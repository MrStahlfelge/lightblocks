package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

public class ThemeSettingsDialog extends ControllerMenuDialog {

    private final LightBlocksGame app;
    private final ScaledLabel labelThemeName;
    private final FaButton resetThemeButton;
    private final RoundedTextButton installThemeButton;
    private final FaButton closeButton;
    private String shownThemeName;

    public ThemeSettingsDialog(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        getButtonTable().defaults().pad(0, 40, 0, 40).uniform();
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);
        GlowLabelButton moreInfoButton = new GlowLabelButton("", "?", app.skin, GlowLabelButton.SMALL_SCALE_MENU);
        moreInfoButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.openOrShowUri(LightBlocksGame.GAME_URL + "themes.html");
            }
        });
        getButtonTable().add(moreInfoButton);

        installThemeButton = new RoundedTextButton(app.TEXTS.get("buttonInstallTheme"), app.skin);
        installThemeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.doInstallTheme(ThemeSettingsDialog.this);
            }
        });

        resetThemeButton = new FaButton(FontAwesome.MISC_CROSS, app.skin);
        resetThemeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.theme.resetTheme();
                refreshContentTable();
            }
        });
        getButtonTable().add(resetThemeButton);

        Table contentTable = getContentTable();

        contentTable.clear();

        contentTable.pad(15);
        contentTable.row();
        contentTable.add(new ScaledLabel(app.TEXTS.get("menuThemeConfig"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        contentTable.row().padTop(20);
        ScaledLabel labelThemeIntro = new ScaledLabel(app.TEXTS.get("labelThemeIntro"), app.skin, LightBlocksGame.SKIN_FONT_REG);
        labelThemeIntro.setWrap(true);
        contentTable.add(labelThemeIntro).fillX().width(LightBlocksGame.nativeGameWidth * .85f);

        contentTable.row().padTop(20);
        contentTable.add(new ScaledLabel(app.TEXTS.get("labelThemeInstalled"), app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        contentTable.row();
        labelThemeName = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        labelThemeName.setEllipsis(true);
        labelThemeName.setAlignment(Align.center);
        contentTable.add(labelThemeName).fillX();

        contentTable.row().padTop(10);
        contentTable.add(installThemeButton);

        addFocusableActor(resetThemeButton);
        addFocusableActor(installThemeButton);
        addFocusableActor(moreInfoButton);

        refreshContentTable();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (shownThemeName != app.theme.getThemeName())
            refreshContentTable();
    }

    protected void refreshContentTable() {
        shownThemeName = app.theme.getThemeName();
        labelThemeName.setText(app.theme.isThemePresent() ? shownThemeName : app.TEXTS.get("labelNoTheme"));
        resetThemeButton.setDisabled(!app.theme.isThemePresent());
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return installThemeButton;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }
}
