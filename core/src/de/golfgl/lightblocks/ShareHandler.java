package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;

/**
 * Teilen einer Nachricht. Im Android-Projekt übersteuert
 *
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class ShareHandler {

    public void shareText(String message, String title) {

        String uri;

        uri = "mailto:?body=" + message;

        if (title != null)
            uri += "?subject=" + title;

        Gdx.net.openURI(uri);
    }

}
