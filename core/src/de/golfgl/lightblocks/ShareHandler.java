package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Teilen einer Nachricht. Im Android-Projekt übersteuert
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class ShareHandler {

    public void shareText(String message, String title) {

        String uri = LightBlocksGame.GAME_URL + "share.php?u=";
        try {

            uri += URLEncoder.encode(message, "utf-8");

            if (title != null)
                uri += "&subject=" + URLEncoder.encode(title);

        } catch (UnsupportedEncodingException e) {
        }

        Gdx.net.openURI(uri);
    }

}
