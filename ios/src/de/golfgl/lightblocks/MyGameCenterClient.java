package de.golfgl.lightblocks;

import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.leaderboard.IFetchLeaderBoardEntriesResponseListener;
import de.golfgl.lightblocks.gpgs.GpgsHelper;

public class MyGameCenterClient extends GameCenterClient {
    private static final String LEAD_LB_MARATHON = "marathon";
    private static final String LEAD_RETRO_MARATHON = "lead_retromarathon";
    private static final String LEAD_GRAVITY_MARATHON = "lead_gravity";
    private static final String LEAD_PRACTICE = "lead_practice";
    private static final String LEAD_SPRINT = "lead_sprint";
    private static final String LEAD_MODERNFREEZE = "modernfreeze";
    private static final String ACH_FOUR_LINES = "fourlines";
    private static final String ACH_DOUBLE = "ach_double";
    private static final String ACH_TSPIN = "ach_tspin";
    private static final String ACH_ADDICTION1 = "ach_addiction1";
    private static final String ACH_CHAIN = "ach_specialchain";
    private static final String ACH_MISSION10 = "ach_mission10";
    private static final String ACH_LONGCLEANER = "ach_longcleaner";
    private static final String ACH_ADDICTION2 = "ach_addiction2";
    private static final String ACH_SCORE_1MIO = "ach_score1m";
    private static final String ACH_100_FOUR_LINES = "ach_fourlines100";
    private static final String ACH_MISSION15 = "ach_missions_done";
    private static final String ACH_CLEAN_COMPLETE = "ach_cleancomplete";
    private static final String ACH_ADDICTION3 = "ach_addiction3";
    private static final String ACH_MARATHON75 = "ach_marathon75";
    private static final String ACH_MARATHON100 = "ach_marathon100";
    private static final String ACH_MARATHON150 = "ach_marathon150";
    private static final String ACH_MARATHON200 = "ach_marathon200";
    private static final String ACH_MARATHON250 = "ach_marathon250";
    private static final String ACH_MARATHON300 = "ach_marathon300";
    private static final String ACH_GRAVITY50 = "ach_gravity50";
    private static final String ACH_RETRO120 = "ach_retro120k";
    private static final String ACH_SPRINT120 = "ach_sprint120";
    private static final String ACH_FREEZER = "ach_freezer";
    private static final String ACH_SUPERFREEZER = "ach_superfreezer";
    private static final String ACH_10TSPIN = "ach_10tspin";
    private static final String ACH_COMBINATOR = "ach_combinator";
    private static final String ACH_MARATHON_LVL16 = "ach_marathon_lvl16";
    private static final String ACH_MISSIONS_PERFECT = "ach_missions_perfect";

    public MyGameCenterClient(UIViewController viewController) {
        super(viewController);
    }

    @Override
    public boolean incrementAchievement(String achievementId, int incNum, float completionPercentage) {
        String gcAch = mapAchievements(achievementId);

        if (gcAch != null)
            return super.incrementAchievement(gcAch, incNum, completionPercentage);
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

        if (gcLeader != null) {
            // Im Sprint-Fall müssen die MS in 100stel-Sekunden umgerechnet werden...
            if (gcLeader.equals(LEAD_SPRINT))
                score = score / 10;

            return super.submitToLeaderboard(gcLeader, score, tag);
        } else
            return false;
    }

    @Override
    public void showLeaderboards(String leaderBoardId) throws GameServiceException {
        String gcLeader = mapLeaderboards(leaderBoardId);

        if (gcLeader != null || leaderBoardId == null)
            super.showLeaderboards(gcLeader);
    }

    private String mapLeaderboards(String leaderBoardId) {
        if (leaderBoardId == null)
            return null;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_MARATHON))
            return LEAD_LB_MARATHON;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_MARATHON_RETRO))
            return LEAD_RETRO_MARATHON;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_MARATHON_GRAVITY))
            return LEAD_GRAVITY_MARATHON;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_PRACTICE_MODE))
            return LEAD_PRACTICE;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_SPRINT_MODE))
            return LEAD_SPRINT;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_FREEZE_MODE))
            return LEAD_MODERNFREEZE;

        return null;
    }

    private String mapAchievements(String achievementId) {
        if (achievementId == null)
            return null;
        else if (achievementId.equals(GpgsHelper.ACH_FOUR_LINES))
            return ACH_FOUR_LINES;
        else if (achievementId.equals(GpgsHelper.ACH_DOUBLE_SPECIAL))
            return ACH_DOUBLE;
        else if (achievementId.equals(GpgsHelper.ACH_TSPIN))
            return ACH_TSPIN;
        else if (achievementId.equals(GpgsHelper.ACH_ADDICTION_LEVEL_1))
            return ACH_ADDICTION1;
        else if (achievementId.equals(GpgsHelper.ACH_SPECIAL_CHAIN))
            return ACH_CHAIN;
        else if (achievementId.equals(GpgsHelper.ACH_MISSION_10_ACCOMPLISHED))
            return ACH_MISSION10;
        else if (achievementId.equals(GpgsHelper.ACH_LONGCLEANER))
            return ACH_LONGCLEANER;
        else if (achievementId.equals(GpgsHelper.ACH_ADDICTION_LEVEL_2))
            return ACH_ADDICTION2;
        else if (achievementId.equals(GpgsHelper.ACH_SCORE_MILLIONAIRE))
            return ACH_SCORE_1MIO;
        else if (achievementId.equals(GpgsHelper.ACH_100_FOUR_LINES))
            return ACH_100_FOUR_LINES;
        else if (achievementId.equals(GpgsHelper.ACH_MISSION_15_ACCOMPLISHED))
            return ACH_MISSION15;
        else if (achievementId.equals(GpgsHelper.ACH_CLEAN_COMPLETE))
            return ACH_CLEAN_COMPLETE;
        else if (achievementId.equals(GpgsHelper.ACH_HIGH_LEVEL_ADDICTION))
            return ACH_ADDICTION3;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SCORE_75000))
            return ACH_MARATHON75;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SCORE_100000))
            return ACH_MARATHON100;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SCORE_150000))
            return ACH_MARATHON150;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SCORE_200000))
            return ACH_MARATHON200;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SCORE_250000))
            return ACH_MARATHON250;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_SUPER_CHECKER))
            return ACH_MARATHON300;
        else if (achievementId.equals(GpgsHelper.ACH_GRAVITY_KING))
            return ACH_GRAVITY50;
        else if (achievementId.equals(GpgsHelper.ACH_MARATHON_FLYING_BASILICA))
            return ACH_RETRO120;
        else if (achievementId.equals(GpgsHelper.ACH_SPRINTER))
            return ACH_SPRINT120;
        else if (achievementId.equals(GpgsHelper.ACH_10_TSPINS))
            return ACH_10TSPIN;
        else if (achievementId.equals(GpgsHelper.ACH_COMBINATOR))
            return ACH_COMBINATOR;
        else if (achievementId.equals(GpgsHelper.ACH_PLUMBOUS_TETROMINOS))
            return ACH_MARATHON_LVL16;
        else if (achievementId.equals(GpgsHelper.ACH_ALL_MISSIONS_PERFECT))
            return ACH_MISSIONS_PERFECT;
        else if (achievementId.equals(GpgsHelper.ACH_FREEZER))
            return ACH_FREEZER;
        else if (achievementId.equals(GpgsHelper.ACH_SUPER_FREEZER))
            return ACH_SUPERFREEZER;

        return null;
    }


}
