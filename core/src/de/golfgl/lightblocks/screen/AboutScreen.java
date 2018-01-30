package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.scenes.ShareButton;

/**
 * Created by Benjamin Schulte on 01.01.2018.
 */

public class AboutScreen extends AbstractMenuDialog {
    public static final String TWITTER_URL = "https://twitter.com/MrStahlfelge";
    public static final String WEBSITE_URL = "https://www.golfgl.de";
    private Cell widthDefiningCell;

    public AboutScreen(LightBlocksGame app, Actor toHide) {
        super(app, toHide);
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.COMMENT_STAR_HEART;
    }

    @Override
    protected String getSubtitle() {
        return app.TEXTS.get("gameTitle") + " " + LightBlocksGame.GAME_VERSIONSTRING;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("menuAbout");
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        menuTable.row();

        menuTable.add(new Label(app.TEXTS.get("labelCopyright") + " " + app.TEXTS.get("gameAuthor"), app.skin));
        menuTable.row().padTop(20);
        Label labelAbout1 = new Label(app.TEXTS.get("labelAbout1"), app.skin, app.SKIN_FONT_BIG);
        labelAbout1.setWrap(true);
        labelAbout1.setAlignment(Align.center);
        widthDefiningCell = menuTable.add(labelAbout1).fill().minWidth(getAvailableContentWidth());
        menuTable.row().padTop(20);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelAbout2"))).fill();

        FATextButton websiteButton = new FATextButton(FontAwesome.CIRCLE_EMPTY, app.TEXTS.get("buttonWebsite"),
                app.skin);
        websiteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI(WEBSITE_URL);
            }
        });
        FATextButton twitterButton = new FATextButton(FontAwesome.NET_TWITTER, app.TEXTS.get("buttonTwitter"),
                app.skin);
        twitterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI(TWITTER_URL);
            }
        });
        Table myButtons = new Table();
        myButtons.defaults().uniform().fill();
        myButtons.add(websiteButton);
        myButtons.add(twitterButton);

        menuTable.row().padTop(20);
        menuTable.add(myButtons);

        menuTable.row().padTop(40);
        menuTable.add(new Label(app.TEXTS.get("labelContributors1"), app.skin, app.SKIN_FONT_BIG));
        menuTable.row().padTop(10);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelContributors2"))).fill();
        menuTable.row().padTop(5);
        menuTable.add(new InfoButton("libGDX", app.TEXTS.get("labelLibgdx"), "http://github.com/libgdx/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new InfoButton("Ray3k", app.TEXTS.get("labelRay3k"), "https://ray3k.wordpress.com/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new InfoButton("Sounds", app.TEXTS.get("labelSounds"), "http://freesound.org/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new InfoButton(app.TEXTS.get("labelMusic"), app.TEXTS.get("labelMusicDesc"),
                "https://www.youtube.com/watch?v=QDNHYF0Hp4U")).fill();
    }

    @Override
    protected boolean isScrolling() {
        return true;
    }

    private Label getWrapLabel(String text) {
        Label labelContribute2 = new Label(text, app.skin);
        labelContribute2.setWrap(true);
        return labelContribute2;
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        ShareButton shareAppButton = new ShareButton(app);

        FATextButton storeButton = new FATextButton(FontAwesome.DEVICE_ANDROID, app.TEXTS.get("buttonStore"),
                app.skin);
        storeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI(LightBlocksGame.GAME_STOREURL);
            }
        });

        buttons.add(shareAppButton);
        buttonsToAdd.add(shareAppButton);
        buttons.add(storeButton);
        buttonsToAdd.add(storeButton);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (widthDefiningCell != null) {
            widthDefiningCell.minWidth(getAvailableContentWidth());
            widthDefiningCell.getTable().invalidate();
            //widthDefiningCell.getTable().validate();
        }
    }

    private class InfoButton extends Button {
        InfoButton(String title, String description, final String url) {

            super(app.skin);
            Label titleLabel = new Label(title, app.skin, app.SKIN_FONT_BIG);
            titleLabel.setFontScale(.8f);
            titleLabel.pack();

            add(titleLabel).padTop(10);
            row();
            add(getWrapLabel(description)).fill().expandX().pad(10);

            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.net.openURI(url);
                }
            });
        }
    }
}
