package de.golfgl.lightblocks.gpgs;

import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.MultiplayerModel;

/**
 * Helper class
 *
 * Created by Benjamin Schulte on 26.03.2017.
 */

public class GpgsHelper {

    public static final String ACH_FOUR_LINES = "CgkI4vHs17ETEAIQCw";
    public static final String ACH_DOUBLE_SPECIAL = "CgkI4vHs17ETEAIQDA";
    public static final String ACH_TSPIN = "CgkI4vHs17ETEAIQCg";
    public static final String ACH_CLEAN_COMPLETE = "CgkI4vHs17ETEAIQDQ";
    public static final String ACH_MATCHMAKER = "CgkI4vHs17ETEAIQDg";
    public static final String ACH_LONGCLEANER = "CgkI4vHs17ETEAIQDw";
    public static final String ACH_ADDICTION_LEVEL_1 = "CgkI4vHs17ETEAIQEA";
    public static final String ACH_ADDICTION_LEVEL_2 = "CgkI4vHs17ETEAIQEQ";
    public static final String ACH_HIGH_LEVEL_ADDICTION = "CgkI4vHs17ETEAIQEg";
    public static final String ACH_MARATHON_SCORE_75000 = "CgkI4vHs17ETEAIQEw";
    public static final String ACH_MARATHON_SCORE_100000 = "CgkI4vHs17ETEAIQFA";
    public static final String ACH_MARATHON_SCORE_150000 = "CgkI4vHs17ETEAIQFQ";
    public static final String ACH_MARATHON_SCORE_200000 = "CgkI4vHs17ETEAIQFg";
    public static final String ACH_MARATHON_SCORE_250000 = "CgkI4vHs17ETEAIQFw";
    public static final String ACH_MARATHON_SUPER_CHECKER = "CgkI4vHs17ETEAIQGA";
    public static final String ACH_SCORE_MILLIONAIRE = "CgkI4vHs17ETEAIQGQ";
    public static final String ACH_GRAVITY_KING = "CgkI4vHs17ETEAIQGg";
    public static final String ACH_MEGA_MULTI_PLAYER = "CgkI4vHs17ETEAIQGw";
    public static final String ACH_GAMEPAD_OWNER = "CgkI4vHs17ETEAIQHA";
    public static final String ACH_SPECIAL_CHAIN = "CgkI4vHs17ETEAIQHQ";
    public static final String ACH_100_FOUR_LINES = "CgkI4vHs17ETEAIQHg";
    public static final String ACH_10_TSPINS = "CgkI4vHs17ETEAIQHw";
    public static final String ACH_FRIENDLY_MULTIPLAYER = "CgkI4vHs17ETEAIQIA";
    public static final String ACH_COMPLETE_TURNAROUND = "CgkI4vHs17ETEAIQIQ";
    public static final String ACH_PLUMBOUS_TETROMINOS = "CgkI4vHs17ETEAIQJA";
    public static final String LEAD_MARATHON_GESTURES = "CgkI4vHs17ETEAIQAA";
    public static final String LEAD_MARATHON_GRAVITY = "CgkI4vHs17ETEAIQCA";
    public static final String LEAD_MARATHON_GAMEPAD = "CgkI4vHs17ETEAIQCQ";
    public static final String EVENT_LOCAL_MULTIPLAYER_MATCH_STARTED = "CgkI4vHs17ETEAIQAg";
    public static final String EVENT_MULTIPLAYER_MATCH_WON = "CgkI4vHs17ETEAIQAQ";
    public static final String EVENT_GESTURE_MARATHON_STARTED = "CgkI4vHs17ETEAIQAw";
    public static final String EVENT_BLOCK_DROP = "CgkI4vHs17ETEAIQBA";
    public static final String EVENT_LINES_CLEARED = "CgkI4vHs17ETEAIQBQ";
    public static final String EVENT_GRAVITY_MARATHON_STARTED = "CgkI4vHs17ETEAIQBg";
    public static final String EVENT_GAMEPAD_MARATHON_STARTED = "CgkI4vHs17ETEAIQBw";    

    public static String getLeaderBoardIdByModelId(String gameModelId) {
        String retVal;

        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "1"))
            return LEAD_MARATHON_GESTURES;
        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "2"))
            return LEAD_MARATHON_GRAVITY;
        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "3"))
            return LEAD_MARATHON_GAMEPAD;

        return null;
    }

    public static String getNewGameEventByModelId(String gameModelId) {
        String retVal;

        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "1"))
            return EVENT_GESTURE_MARATHON_STARTED;
        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "2"))
            return EVENT_GRAVITY_MARATHON_STARTED;
        if (gameModelId.equalsIgnoreCase (MarathonModel.MODEL_MARATHON_ID + "3"))
            return EVENT_GAMEPAD_MARATHON_STARTED;
        if (gameModelId.equalsIgnoreCase(MultiplayerModel.MODEL_ID))
            return EVENT_LOCAL_MULTIPLAYER_MATCH_STARTED;

        return null;
    }

}
