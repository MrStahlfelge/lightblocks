package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 22.03.2017.
 */

public class MusicButton extends TextButton {

    public MusicButton(final LightBlocksGame app) {
        super(FontAwesome.SETTINGS_MUSIC, app.skin, FontAwesome.SKIN_FONT_FA
                + "-checked");
        this.setChecked(app.isPlayMusic());
        this.addListener(new ChangeListener() {
                             public void changed(ChangeEvent event, Actor actor) {
                                 app.setPlayMusic(isChecked());
                             }
                         }
        );
    }
}
