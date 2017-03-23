package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.I18NBundle;

/**
 * A gameblocker prevents the game to play.
 *
 * Created by Benjamin Schulte on 22.03.2017.
 */

public abstract class GameBlocker {

    public abstract String getDescription(I18NBundle bundle);

    public static class InputGameBlocker extends GameBlocker {
        @Override
        public String getDescription(I18NBundle bundle) {
            return bundle.get("labelCalibration");
        }
    }

    public static class OtherPlayerPausedGameBlocker extends GameBlocker {
        public String playerId;

        @Override
        public String getDescription(I18NBundle bundle) {
            return bundle.format("labelMultiplayerOtherPaused", playerId);
        }
    }

    public static class WaitForOthersInitializedBlocker extends GameBlocker {
        @Override
        public String getDescription(I18NBundle bundle) {
            return bundle.get("labelMultiplayerInitializing");
        }
    }
}
