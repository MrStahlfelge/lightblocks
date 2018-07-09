package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.InfoButton;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 01.01.2018.
 */

public class AboutScreen extends AbstractMenuDialog {
    public static final String TWITTER_URL = "https://twitter.com/MrStahlfelge";
    private Cell widthDefiningCell;
    private Actor defaultActor;

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

        menuTable.add(new ScaledLabel(app.TEXTS.get("labelCopyright") + " " + app.TEXTS.get("gameAuthor"), app.skin));
        menuTable.row().padTop(20);
        Label labelAbout1 = new ScaledLabel(app.TEXTS.get("labelAbout1"), app.skin, app.SKIN_FONT_TITLE);
        labelAbout1.setWrap(true);
        labelAbout1.setAlignment(Align.center);
        widthDefiningCell = menuTable.add(labelAbout1).fill().minWidth(getAvailableContentWidth());
        menuTable.row().padTop(20);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelAbout2"))).fill();

        if (app.canDonate()) {
            RoundedTextButton donateButton = new RoundedTextButton(app.TEXTS.get("buttonDonation"), app.skin);
            donateButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    new DonationDialog(app).show(getStage());
                }
            });
            addFocusableActor(donateButton);

            menuTable.row().padTop(20);
            menuTable.add(donateButton);

            defaultActor = donateButton;
        }

        menuTable.row().padTop(20);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelAbout2b"))).fill();

        Button websiteButton = new RoundedTextButton(app.TEXTS.get("buttonWebsite"), app.skin);
        websiteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.openOrShowUri(LightBlocksGame.GAME_URL);
            }
        });
        addFocusableActor(websiteButton);

        Table myButtons = new Table();
        myButtons.defaults().uniform().fill();

        if (LightBlocksGame.gameStoreUrl != null) {
            Button storeButton = new RoundedTextButton(app.TEXTS.get("buttonStore"), app.skin);
            storeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.net.openURI(LightBlocksGame.gameStoreUrl);
                }
            });
            addFocusableActor(storeButton);
            myButtons.add(storeButton);
            myButtons.row().padTop(10);
        }

        myButtons.add(websiteButton);

        menuTable.row().padTop(20);
        menuTable.add(myButtons);

        menuTable.row().padTop(20);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelAbout3"))).fill();

        if (!LightBlocksGame.isOnAndroidTV()) {
            Button mailButton = new RoundedTextButton(app.TEXTS.get("buttonMail"), app.skin);
            mailButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.net.openURI("mailto:" + LightBlocksGame.GAME_EMAIL);
                }
            });
            addFocusableActor(mailButton);

            menuTable.row().padTop(10);
            menuTable.add(mailButton);
        }

        menuTable.row().padTop(40);
        menuTable.add(new ScaledLabel(app.TEXTS.get("labelContributors1"), app.skin, app.SKIN_FONT_TITLE));
        menuTable.row().padTop(10);
        menuTable.add(getWrapLabel(app.TEXTS.get("labelContributors2"))).fill();
        menuTable.row().padTop(5);
        menuTable.add(new ThisInfoButton("libGDX", app.TEXTS.get("labelLibgdx"), "http://github.com/libgdx/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new ThisInfoButton("Ray3k", app.TEXTS.get("labelRay3k"), "https://ray3k.wordpress.com/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new ThisInfoButton("Sounds", app.TEXTS.get("labelSounds"), "http://freesound.org/")).fill();
        menuTable.row().padTop(5);
        menuTable.add(new ThisInfoButton(app.TEXTS.get("labelMusic"), app.TEXTS.get("labelMusicDesc"),
                "https://www.youtube.com/watch?v=QDNHYF0Hp4U")).fill();
    }

    @Override
    protected boolean isScrolling() {
        return true;
    }

    private Label getWrapLabel(String text) {
        Label labelContribute2 = new ScaledLabel(text, app.skin, LightBlocksGame.SKIN_FONT_REG, .7f);
        labelContribute2.setWrap(true);
        return labelContribute2;
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        Button twitterButton = new FaButton(FontAwesome.NET_TWITTER, app.skin);
        twitterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.openOrShowUri(TWITTER_URL);
            }
        });
        twitterButton.addListener(scrollOnKeyDownListener);
        buttons.add(twitterButton);
        addFocusableActor(twitterButton);

        ShareButton shareAppButton = new ShareButton(app);
        shareAppButton.addListener(scrollOnKeyDownListener);

        buttons.add(shareAppButton);
        addFocusableActor(shareAppButton);

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

    @Override
    protected Actor getConfiguredDefaultActor() {
        return defaultActor != null ? defaultActor : super.getConfiguredDefaultActor();
    }

    private class ThisInfoButton extends InfoButton {
        public ThisInfoButton(String title, String description, final String url) {
            super(title, description, app.skin);
            addFocusableActor(this);
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.openOrShowUri(url);
                }
            });
        }
    }
}
