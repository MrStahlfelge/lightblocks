package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 22.03.2017.
 */

public class MusicButton extends TextButton {

    private final static int NO_SOUND = 0;
    private final static int NO_MUSIC = 1;
    private final static int SOUND_MUSIC = 2;

    private int state;
    private Label lblStateDesc;
    private LightBlocksGame app;

    public MusicButton(final LightBlocksGame app, Label stateDescriptionLabel) {
        super("", app.skin, FontAwesome.SKIN_FONT_FA);

        this.state = (app.isPlayMusic() ? SOUND_MUSIC : app.isPlaySounds() ? NO_MUSIC : NO_SOUND);
        this.lblStateDesc = stateDescriptionLabel;
        this.app = app;
        getLabelCell().minWidth(60);

        setIconAndLabelFromState();

        this.addListener(new ChangeListener() {
                             public void changed(ChangeEvent event, Actor actor) {
                                 state--;
                                 if (state < 0)
                                     state = SOUND_MUSIC;

                                 app.setPlayMusic(state == SOUND_MUSIC);
                                 app.setPlaySounds(state > NO_SOUND);
                                 setIconAndLabelFromState();
                             }
                         }
        );
    }

    protected void setIconAndLabelFromState() {
        switch (state) {
            case SOUND_MUSIC:
                setText(FontAwesome.SETTINGS_MUSIC);
                if (lblStateDesc != null)
                    lblStateDesc.setText(app.TEXTS.get("menuPlayMusic"));
                break;
            case NO_MUSIC:
                setText(FontAwesome.SETTINGS_SPEAKER_ON);
                if (lblStateDesc != null)
                    lblStateDesc.setText(app.TEXTS.get("menuPlaySounds"));
                break;
            default:
                setText(FontAwesome.SETTINGS_SPEAKER_OFF);
                if (lblStateDesc != null)
                    lblStateDesc.setText(app.TEXTS.get("menuNoSounds"));
        }

    }
}
