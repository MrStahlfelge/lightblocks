package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

import de.golfgl.gdx.controllers.ControllerScrollPane;

/**
 * ScrollPane with no problems when a Slider is inside
 * <p>
 * Created by Benjamin Schulte on 15.02.2018.
 */

public class BetterScrollPane extends ControllerScrollPane {
    public BetterScrollPane(Actor widget) {
        super(widget);
        setup();
    }

    public BetterScrollPane(Actor widget, Skin skin) {
        super(widget, skin);
        setup();
    }

    public BetterScrollPane(Actor widget, Skin skin, String styleName) {
        super(widget, skin, styleName);
        setup();
    }

    public BetterScrollPane(Actor widget, ScrollPaneStyle style) {
        super(widget, style);
        setup();
    }

    private void setup() {
        addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Actor actor = BetterScrollPane.this.hit(x, y, true);
                if (actor instanceof Slider) {
                    BetterScrollPane.this.setFlickScroll(false);
                    return true;
                }

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BetterScrollPane.this.setFlickScroll(true);
                super.touchUp(event, x, y, pointer, button);
            }
        });

    }
}
