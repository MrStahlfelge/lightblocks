package de.golfgl.lightblocks.screen;

/**
 * Exception for showing error dialog
 *
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class VetoException extends Exception {

    VetoException(String message) {
        super(message);
    }
}
