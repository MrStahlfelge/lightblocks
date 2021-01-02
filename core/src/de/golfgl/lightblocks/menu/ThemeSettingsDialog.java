package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.EditableLabel;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.Theme;

public class ThemeSettingsDialog extends ControllerMenuDialog {

    private final LightBlocksGame app;
    private final ScaledLabel labelThemeName;
    private final FaButton resetThemeButton;
    private final RoundedTextButton installThemeButton;
    private final FaButton closeButton;
    private final Cell themeFeatureCell;
    private final Cell themeAdditionalInfoCell;
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
                app.doInstallTheme(null);
            }
        });

        RoundedTextButton downloadThemeButton = new RoundedTextButton(app.TEXTS.get("buttonDownloadTheme"), app.skin);
        downloadThemeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new DownloadThemeDialog().show(getStage());
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
        contentTable.row();
        contentTable.add(downloadThemeButton);

        addFocusableActor(resetThemeButton);
        addFocusableActor(installThemeButton);
        addFocusableActor(downloadThemeButton);
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

    public class DownloadThemeDialog extends ControllerMenuDialog {
        private final Button downloadButton;
        private final ProgressBar progressBar;
        private final Button closeButton;

        public DownloadThemeDialog() {
            super("", app.skin);

            getButtonTable().defaults().pad(0, 40, 0, 40);
            closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
            button(closeButton);

            ScaledLabel helplabel = new ScaledLabel("Paste download URL here:", app.skin, LightBlocksGame.SKIN_FONT_REG);

            ScaledLabel label = new ScaledLabel(Gdx.app.getClipboard().getContents(), app.skin, LightBlocksGame.SKIN_EDIT_BIG);
            FaButton editButton = new FaButton(FontAwesome.SETTING_PENCIL, app.skin);
            final EditableLabel nickNameEditTable = new EditableLabel(label, editButton, app.skin,
                    "URL", Input.OnscreenKeyboardType.Default) {
                @Override
                protected void onNewTextSet(String newText) {
                    super.onNewTextSet(newText);
                    ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
                }
            };
            nickNameEditTable.getLabel().setEllipsis(true);
            nickNameEditTable.setWidth(LightBlocksGame.nativeGameWidth - 100);

            progressBar = new ProgressBar(0, 100, 1, false, app.skin);
            progressBar.setVisible(false);

            downloadButton = new RoundedTextButton("Download", app.skin);
            downloadButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setDownloading(true);

                    Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
                    httpGet.setUrl(nickNameEditTable.getLabel().getText().toString());

                    Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
                        @Override
                        public void handleHttpResponse(Net.HttpResponse httpResponse) {

                            if (httpResponse.getStatus().getStatusCode() != HttpStatus.SC_OK) {
                                failed(new RuntimeException(httpResponse.getResultAsString()));
                                return;
                            }

                            // Determine how much we have to download
                            int length = Integer.parseInt(httpResponse.getHeader("Content-Length"));

                            // We're going to download the file to external storage, create the streams
                            InputStream is = httpResponse.getResultAsStream();
                            ByteArrayOutputStream os = new ByteArrayOutputStream(length);

                            byte[] bytes = new byte[1024];
                            int count = -1;
                            long read = 0;
                            try {
                                // Keep reading bytes and storing them until there are no more.
                                while ((count = is.read(bytes, 0, bytes.length)) != -1) {
                                    os.write(bytes, 0, count);
                                    read += count;

                                    // Update the UI with the download progress
                                    final int progress = ((int) (((double) read / (double) length) * 100));

                                    // Since we are downloading on a background thread, post a runnable to touch ui
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setValue(progress);
                                        }
                                    });
                                }

                                os.close();

                                final byte[] theme = os.toByteArray();

                                // Done
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        hide();
                                        app.doInstallTheme(new ByteArrayInputStream(theme));
                                    }
                                });

                            } catch (IOException e) {
                                failed(e);
                            }
                        }

                        public void failed(final Throwable t) {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    new VetoDialog("Error downloading file: " + t.getMessage(), app.skin,
                                            LightBlocksGame.nativeGameWidth * .9f).show(getStage());
                                    setDownloading(false);
                                }
                            });
                        }

                        @Override
                        public void cancelled() {
                            setDownloading(false);
                        }
                    });

                }
            });

            Table contentTable = getContentTable();

            contentTable.pad(20);

            addFocusableActor(nickNameEditTable);
            addFocusableActor(downloadButton);

            contentTable.add(helplabel);
            contentTable.row();
            contentTable.add(nickNameEditTable);
            contentTable.row().pad(20);
            contentTable.add(progressBar).fillX().expandX();
            contentTable.row();
            contentTable.add(downloadButton);

        }

        protected void setDownloading(boolean downloading) {
            downloadButton.setDisabled(downloading);
            closeButton.setDisabled(downloading);
            progressBar.setVisible(downloading);
        }

        @Override
        protected Actor getConfiguredDefaultActor() {
            return downloadButton;
        }

        @Override
        protected Actor getConfiguredEscapeActor() {
            return closeButton;
        }
    }
}
