package de.golfgl.lightblocks;

import de.golfgl.gdxgamesvcs.GameCircleClient;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.leaderboard.IFetchLeaderBoardEntriesResponseListener;
import de.golfgl.lightblocks.gpgs.GpgsHelper;

/**
 * Created by Benjamin Schulte on 10.04.2018.
 */

public class MyGameCircleClient extends GameCircleClient {
    public static final String ACH_FOUR_LINES = "ACH_FOURLINES";
    public static final String ACH_DOUBLE_SPECIAL = "ACH_DOUBLE";
    public static final String ACH_TSPIN = "ACH_TSPIN";
    public static final String ACH_CLEAN_COMPLETE = "ACH_CLEAN_COMPLETE";
    public static final String ACH_MATCHMAKER = "ACH_MATCHMAKER";
    public static final String ACH_LONGCLEANER = "ACH_LONGCLEAN";
    public static final String ACH_ADDICTION_LEVEL_1 = "ACH_ADDICTION1";
    public static final String ACH_ADDICTION_LEVEL_2 = "ACH_ADDICTION2";
    public static final String ACH_HIGH_LEVEL_ADDICTION = "ACH_HIGH_LEVEL_ADDICTION";
    public static final String ACH_MARATHON_SCORE_75000 = "ACH_SCORE075K";
    public static final String ACH_MARATHON_SCORE_100000 = "ACH_SCORE100K";
    public static final String ACH_MARATHON_SCORE_150000 = "ACH_SCORE150K";
    public static final String ACH_MARATHON_SCORE_200000 = "ACH_SCORE200K";
    public static final String ACH_MARATHON_SCORE_250000 = "ACH_SCORE250K";
    public static final String ACH_MARATHON_SUPER_CHECKER = "ACH_SCORE300K";
    public static final String ACH_SCORE_MILLIONAIRE = "ACH_MILLIONAIRE";
    public static final String ACH_GRAVITY_KING = "ACH_GRAVITY_KING";
    public static final String ACH_MEGA_MULTI_PLAYER = "ACH_MEGAMULTIPLAYER";
    public static final String ACH_GAMEPAD_OWNER = "ACH_GAMEPADOWNER";
    public static final String ACH_SPECIAL_CHAIN = "ACH_SPCHAIN";
    public static final String ACH_100_FOUR_LINES = "ACH_100FOURS";
    public static final String ACH_10_TSPINS = "ACH_10_TSPINS";
    public static final String ACH_FRIENDLY_MULTIPLAYER = "ACH_FRIENDLYMULTIPLAYER";
    public static final String ACH_COMPLETE_TURNAROUND = "ACH_COMPLETETURNAROUND";
    public static final String ACH_PLUMBOUS_TETROMINOS = "ACH_PLUMBOUSTETROMINOS";
    public static final String ACH_MISSION_10_ACCOMPLISHED = "ACH_MISSION10";
    public static final String ACH_MISSION_15_ACCOMPLISHED = "ACH_MISSION15";
    public static final String ACH_ALL_MISSIONS_PERFECT = "ACH_MISSIONS_PERFECT";
    public static final String LEAD_MARATHON_GESTURES = "LB_MARA_GEST";
    public static final String LEAD_MARATHON_GRAVITY = "LB_MARA_GRAV";
    public static final String LEAD_MARATHON_GAMEPAD = "LB_MARA_GPAD";

    @Override
    public boolean incrementAchievement(String achievementId, int incNum, float completionPercentage) {
        String gcAch = mapAchievements(achievementId);

        if (gcAch != null)
            return super.incrementAchievement(gcAch, incNum, completionPercentage);
        else
            return false;
    }

    @Override
    public boolean unlockAchievement(String achievementId) {
        String gcAch = mapAchievements(achievementId);

        if (gcAch != null)
            return super.unlockAchievement(gcAch);
        else
            return false;
    }

    @Override
    public boolean fetchLeaderboardEntries(String leaderBoardId, int limit, boolean relatedToPlayer,
                                           IFetchLeaderBoardEntriesResponseListener callback) {
        String gcLeader = mapLeaderboards(leaderBoardId);

        if (gcLeader != null)
            return super.fetchLeaderboardEntries(gcLeader, limit, relatedToPlayer, callback);
        else
            return false;
    }

    @Override
    public boolean submitToLeaderboard(String leaderboardId, long score, String tag) {
        String gcLeader = mapLeaderboards(leaderboardId);

        if (gcLeader != null)
            return super.submitToLeaderboard(gcLeader, score, tag);
        else
            return false;
    }

    @Override
    public void showLeaderboards(String leaderBoardId) throws GameServiceException {
        String gcLeader = mapLeaderboards(leaderBoardId);

        if (gcLeader != null || leaderBoardId == null)
            super.showLeaderboards(gcLeader);
    }

    public String mapAchievements(String independantId) {
        if (independantId == null)
            return null;
        else if (independantId.equals(GpgsHelper.ACH_FOUR_LINES))
            return ACH_FOUR_LINES;
        else if (independantId.equals(GpgsHelper.ACH_DOUBLE_SPECIAL))
            return ACH_DOUBLE_SPECIAL;
        else if (independantId.equals(GpgsHelper.ACH_TSPIN))
            return ACH_TSPIN;
        else if (independantId.equals(GpgsHelper.ACH_CLEAN_COMPLETE))
            return ACH_CLEAN_COMPLETE;
        else if (independantId.equals(GpgsHelper.ACH_MATCHMAKER))
            return ACH_MATCHMAKER;
        else if (independantId.equals(GpgsHelper.ACH_LONGCLEANER))
            return ACH_LONGCLEANER;
        else if (independantId.equals(GpgsHelper.ACH_ADDICTION_LEVEL_1))
            return ACH_ADDICTION_LEVEL_1;
        else if (independantId.equals(GpgsHelper.ACH_ADDICTION_LEVEL_2))
            return ACH_ADDICTION_LEVEL_2;
        else if (independantId.equals(GpgsHelper.ACH_HIGH_LEVEL_ADDICTION))
            return ACH_HIGH_LEVEL_ADDICTION;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SCORE_75000))
            return ACH_MARATHON_SCORE_75000;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SCORE_100000))
            return ACH_MARATHON_SCORE_100000;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SCORE_150000))
            return ACH_MARATHON_SCORE_150000;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SCORE_200000))
            return ACH_MARATHON_SCORE_200000;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SCORE_250000))
            return ACH_MARATHON_SCORE_250000;
        else if (independantId.equals(GpgsHelper.ACH_MARATHON_SUPER_CHECKER))
            return ACH_MARATHON_SUPER_CHECKER;
        else if (independantId.equals(GpgsHelper.ACH_SCORE_MILLIONAIRE))
            return ACH_SCORE_MILLIONAIRE;
        else if (independantId.equals(GpgsHelper.ACH_GRAVITY_KING))
            return ACH_GRAVITY_KING;
        else if (independantId.equals(GpgsHelper.ACH_MEGA_MULTI_PLAYER))
            return ACH_MEGA_MULTI_PLAYER;
        else if (independantId.equals(GpgsHelper.ACH_GAMEPAD_OWNER))
            return ACH_GAMEPAD_OWNER;
        else if (independantId.equals(GpgsHelper.ACH_SPECIAL_CHAIN))
            return ACH_SPECIAL_CHAIN;
        else if (independantId.equals(GpgsHelper.ACH_100_FOUR_LINES))
            return ACH_100_FOUR_LINES;
        else if (independantId.equals(GpgsHelper.ACH_10_TSPINS))
            return ACH_10_TSPINS;
        else if (independantId.equals(GpgsHelper.ACH_FRIENDLY_MULTIPLAYER))
            return ACH_FRIENDLY_MULTIPLAYER;
        else if (independantId.equals(GpgsHelper.ACH_COMPLETE_TURNAROUND))
            return ACH_COMPLETE_TURNAROUND;
        else if (independantId.equals(GpgsHelper.ACH_PLUMBOUS_TETROMINOS))
            return ACH_PLUMBOUS_TETROMINOS;
        else if (independantId.equals(GpgsHelper.ACH_MISSION_10_ACCOMPLISHED))
            return ACH_MISSION_10_ACCOMPLISHED;
        else if (independantId.equals(GpgsHelper.ACH_MISSION_15_ACCOMPLISHED))
            return ACH_MISSION_15_ACCOMPLISHED;
        else if (independantId.equals(GpgsHelper.ACH_ALL_MISSIONS_PERFECT))
            return ACH_ALL_MISSIONS_PERFECT;

        return null;
    }

    public String mapLeaderboards(String independantId) {
        if (independantId == null)
            return null;
        else if (independantId.equals(GpgsHelper.LEAD_MARATHON_GESTURES))
            return LEAD_MARATHON_GESTURES;
        else if (independantId.equals(GpgsHelper.LEAD_MARATHON_GRAVITY))
            return LEAD_MARATHON_GRAVITY;
        else if (independantId.equals(GpgsHelper.LEAD_MARATHON_GAMEPAD))
            return LEAD_MARATHON_GAMEPAD;

        return null;
    }
}
