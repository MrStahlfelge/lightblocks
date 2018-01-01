package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 01.01.2018.
 */

public class ShareButton extends FATextButton {
    public ShareButton(final LightBlocksGame app) {
        super(FontAwesome.NET_SHARE1, app.TEXTS.get("menuShareApp"), app.skin);

        addListener(new ChangeListener() {
                        public void changed(ChangeEvent event, Actor actor) {
                            app.share.shareText(app.TEXTS.get("gameTitle") + ": " +
                                    LightBlocksGame.GAME_STOREURL, null);
                        }
                    }
        );
    }
}
