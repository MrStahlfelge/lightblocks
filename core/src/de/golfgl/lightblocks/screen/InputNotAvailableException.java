package de.golfgl.lightblocks.screen;

/**
 * Exception die geworfen wird wenn ein nicht vorhandener InputController genutzt werden soll
 * <p>
 * Created by Benjamin Schulte on 06.02.2017.
 */
public class InputNotAvailableException extends Throwable {
    private final int inputKey;

    public InputNotAvailableException(int inputKey) {
        this.inputKey = inputKey;
    }

    public int getInputKey() {
        return inputKey;
    }
}
