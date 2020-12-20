package de.golfgl.lightblocks.input;

import com.badlogic.gdx.controllers.Controller;

/**
 * Class helping to identify a certain input for device two player game
 */
public interface InputIdentifier {
    boolean isSameInput(InputIdentifier inputIdentifier);

    class GameControllerInput implements InputIdentifier {
        private final String gameControllerId;
        public Controller lastControllerRef;

        public GameControllerInput(Controller controller) {
            this.gameControllerId = controller.getUniqueId();
            this.lastControllerRef = controller;
        }

        public String getGameControllerId() {
            return gameControllerId;
        }

        @Override
        public boolean isSameInput(InputIdentifier inputIdentifier) {
            if (this == inputIdentifier) return true;
            if (!(inputIdentifier instanceof GameControllerInput)) {
                return false;
            }
            GameControllerInput that = (GameControllerInput) inputIdentifier;
            return gameControllerId.equals(that.gameControllerId);
        }
    }

    class KeyboardInput implements InputIdentifier {
        private final int playerIndex;

        public KeyboardInput(int playerIndex) {
            this.playerIndex = playerIndex;
        }

        @Override
        public boolean isSameInput(InputIdentifier inputIdentifier) {
            if (this == inputIdentifier) return true;
            if (!(inputIdentifier instanceof KeyboardInput)) {
                return false;
            }

            KeyboardInput that = (KeyboardInput) inputIdentifier;
            return this.playerIndex == that.playerIndex;
        }
    }

    class TouchscreenInput implements InputIdentifier {

        @Override
        public boolean isSameInput(InputIdentifier inputIdentifier) {
            if (this == inputIdentifier) return true;
            return (inputIdentifier instanceof TouchscreenInput);
        }
    }
}
