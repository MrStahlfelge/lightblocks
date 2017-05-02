package de.golfgl.lightblocks.gpgs;

import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.model.TutorialModel;

/**
 * Helper class
 * <p>
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
    public static final String ACH_MISSION_10_ACCOMPLISHED = "CgkI4vHs17ETEAIQKw";
    public static final String ACH_MISSION_15_ACCOMPLISHED = "CgkI4vHs17ETEAIQOg";
    public static final String ACH_ALL_MISSIONS_PERFECT = "CgkI4vHs17ETEAIQOw";
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
    public static final String EVENT_TUTORIAL_STARTED = "CgkI4vHs17ETEAIQJQ";
    public static final String EVENT_MISSION_1_TYPEA_1A = "CgkI4vHs17ETEAIQJg";
    public static final String EVENT_MISSION_2_TYPEB_1A = "CgkI4vHs17ETEAIQJw";
    public static final String EVENT_MISSION_3_TYPEA_1B = "CgkI4vHs17ETEAIQKA";
    public static final String EVENT_MISSION_4_SPECIAL_1A = "CgkI4vHs17ETEAIQKQ";
    public static final String EVENT_MISSION_5_TYPEA_1C = "CgkI4vHs17ETEAIQKg";
    public static final String EVENT_MISSION_6_SPECIAL_1B = "CgkI4vHs17ETEAIQLA";
    public static final String EVENT_MISSION_8_SPECIAL_1C = "CgkI4vHs17ETEAIQLg";
    public static final String EVENT_MISSION_9_GARBAGE_1A = "CgkI4vHs17ETEAIQMQ";
    public static final String EVENT_MISSION_10_TYPEB_1C = "CgkI4vHs17ETEAIQMg";
    public static final String EVENT_MISSION_7_TYPEB_1B = "CgkI4vHs17ETEAIQMw";
    public static final String EVENT_MISSION_11_GRAVITYA_2A = "CgkI4vHs17ETEAIQNA";
    public static final String EVENT_INET_MULTIPLAYER_MATCH_STARTED = "CgkI4vHs17ETEAIQNQ";
    public static final String EVENT_MISSION_12_TYPEA_1D = "CgkI4vHs17ETEAIQNg";
    public static final String EVENT_MISSION_13_TYPEB_2A = "CgkI4vHs17ETEAIQNw";
    public static final String EVENT_MISSION_14_GARBAGE_1B = "CgkI4vHs17ETEAIQOA";
    public static final String EVENT_MISSION_15_TYPEB_1D = "CgkI4vHs17ETEAIQOQ";

    public static String getLeaderBoardIdByModelId(String gameModelId) {
        String retVal;

        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "1"))
            return LEAD_MARATHON_GESTURES;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "2"))
            return LEAD_MARATHON_GRAVITY;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "3"))
            return LEAD_MARATHON_GAMEPAD;

        return null;
    }

    public static String getNewGameEventByModelId(String gameModelId) {
        String retVal;

        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "1"))
            return EVENT_GESTURE_MARATHON_STARTED;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "2"))
            return EVENT_GRAVITY_MARATHON_STARTED;
        if (gameModelId.equalsIgnoreCase(MarathonModel.MODEL_MARATHON_ID + "3"))
            return EVENT_GAMEPAD_MARATHON_STARTED;

        if (gameModelId.equalsIgnoreCase(TutorialModel.MODEL_ID))
            return EVENT_TUTORIAL_STARTED;

        // Die Missionen
        // kein Switch wegen Java 6!
        if (gameModelId.equalsIgnoreCase("typeA_1A"))
            return EVENT_MISSION_1_TYPEA_1A;
        if (gameModelId.equalsIgnoreCase("typeB_1A"))
            return EVENT_MISSION_2_TYPEB_1A;
        if (gameModelId.equalsIgnoreCase("typeA_1B"))
            return EVENT_MISSION_3_TYPEA_1B;
        if (gameModelId.equalsIgnoreCase("special_1A"))
            return EVENT_MISSION_4_SPECIAL_1A;
        if (gameModelId.equalsIgnoreCase("typeA_1C"))
            return EVENT_MISSION_5_TYPEA_1C;

        if (gameModelId.equalsIgnoreCase("special_1B"))
            return EVENT_MISSION_6_SPECIAL_1B;
        if (gameModelId.equalsIgnoreCase("special_1C"))
            return EVENT_MISSION_8_SPECIAL_1C;
        if (gameModelId.equalsIgnoreCase("typeB_1B"))
            return EVENT_MISSION_7_TYPEB_1B;
        if (gameModelId.equalsIgnoreCase("garbage_1A"))
            return EVENT_MISSION_9_GARBAGE_1A;
        if (gameModelId.equalsIgnoreCase("typeB_1C"))
            return EVENT_MISSION_10_TYPEB_1C;
        if (gameModelId.equalsIgnoreCase("gravityA_2A"))
            return EVENT_MISSION_11_GRAVITYA_2A;
        if (gameModelId.equalsIgnoreCase("typeA_1D"))
            return EVENT_MISSION_12_TYPEA_1D;

        return null;
    }

}
