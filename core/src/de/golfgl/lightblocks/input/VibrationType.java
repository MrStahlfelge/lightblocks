package de.golfgl.lightblocks.input;

public enum VibrationType {
    HAPTIC_FEEDBACK,
    MOTIVATION,
    DROP,
    CLEAR,
    SPECIAL_CLEAR,
    GARBAGE;

    public int getVibrationLength() {
        switch (this) {
            case HAPTIC_FEEDBACK:
                return 20;
            case DROP:
                return 80;
            case CLEAR:
                return 150;
            case SPECIAL_CLEAR:
                return 300;
            case GARBAGE:
                return 100;
            case MOTIVATION:
                return 120;
            default:
                return 0;
        }
    }
}
