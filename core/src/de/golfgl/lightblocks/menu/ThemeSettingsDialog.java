package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.Theme;

public class ThemeSettingsDialog extends ControllerMenuDialog {

    private final LightBlocksGame app;
    private final ScaledLabel labelThemeName;
    private final FaButton resetThemeButton;
    private final RoundedTextButton installThemeButton;
    private final FaButton closeButton;
    private final Cell themeFeatureCell;
    private String shownThemeName;
    private final Cell themeAdditionalInfoCell;

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
        contentTable.row();
        themeFeatureCell = contentTable.add();
        contentTable.row();
        themeAdditionalInfoCell = contentTable.add().expandX().fillX();

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

        // Additional info zusammenbauen
        themeAdditionalInfoCell.setActor(null).minHeight(50);

        String additionalInfo = app.theme.getThemeVersion() > 0 ?
                app.TEXTS.format("labelThemeVersion", String.valueOf(app.theme.getThemeVersion())) : "";

        if (app.theme.getThemeAuthor() != null) {
            if (!additionalInfo.isEmpty())
                additionalInfo = additionalInfo + "\n";

            additionalInfo = additionalInfo.concat(app.TEXTS.format("labelThemeAuthor", app.theme.getThemeAuthor()));
        }

        if (!additionalInfo.isEmpty()) {
            ScaledLabel addInfoLabel = new ScaledLabel(additionalInfo, app.skin, LightBlocksGame.SKIN_FONT_REG);
            addInfoLabel.setAlignment(Align.center);
            addInfoLabel.setEllipsis(true);

            themeAdditionalInfoCell.setActor(addInfoLabel);
        }

        // Vorschautabelle zusammenbauen
        Table featureTable = new Table() {
            @Override
            protected void drawChildren(Batch batch, float parentAlpha) {
                //BlockActor muss zweimal gezeichnet werden
                super.drawChildren(batch, parentAlpha);
                super.drawChildren(batch, parentAlpha);
            }
        };

        int tablePadding = 10;
        featureTable.defaults().padBottom(tablePadding).padTop(tablePadding);

        featureTable.add(new BlockActor(app, Tetromino.TETRO_IDX_L, true))
                .width(BlockActor.blockWidth).height(BlockActor.blockWidth).bottom().padLeft(tablePadding)
                .padRight(tablePadding);

        if (!app.theme.usesDefaultBlockPictures) {
            featureTable.add(new BlockActor(app, Tetromino.TETRO_IDX_O, true))
                    .width(BlockActor.blockWidth).height(BlockActor.blockWidth).bottom().padRight(tablePadding);

            featureTable.add(new BlockActor(app, Tetromino.TETRO_IDX_I, true))
                    .width(BlockActor.blockWidth).height(BlockActor.blockWidth).bottom().padRight(tablePadding);
        }

        if (!app.theme.usesDefaultSounds) {
            ScaledLabel soundLabel = new ScaledLabel(FontAwesome.SETTINGS_MUSIC, app.skin, FontAwesome.SKIN_FONT_FA, .5f);
            soundLabel.setColor(app.theme.titleColor);
            featureTable.add(soundLabel).padLeft(tablePadding * 2).padRight(tablePadding);
        }

        featureTable.setBackground(Theme.tintDrawableIfPossible(app.skin.getDrawable("white"), app.theme.bgColor));

        themeFeatureCell.setActor(featureTable);
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
