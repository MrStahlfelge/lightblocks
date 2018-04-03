package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 01.01.2018.
 */

public class ShareButton extends FaButton {
    private String shareText;

    public ShareButton(final LightBlocksGame app) {
        super(FontAwesome.NET_SHARE1, app.skin);
        shareText = app.TEXTS.get("gameTitle") + ": " + LightBlocksGame.GAME_URL;

        addListener(new ChangeListener() {
                        public void changed(ChangeEvent event, Actor actor) {
                            app.share.shareText(shareText, null);
                        }
                    }
        );
    }

    public ShareButton(LightBlocksGame app, String shareMessage) {
        this(app);
        this.shareText = shareMessage;
    }
}
