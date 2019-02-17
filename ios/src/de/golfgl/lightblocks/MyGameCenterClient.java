package de.golfgl.lightblocks;

import org.robovm.apple.uikit.UIViewController;

import de.golfgl.gdxgamesvcs.GameCenterClient;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.leaderboard.IFetchLeaderBoardEntriesResponseListener;
import de.golfgl.lightblocks.gpgs.GpgsHelper;

public class MyGameCenterClient extends GameCenterClient {
    private static final String LEAD_MARATHON = "marathon";
    private static final String ACH_FOUR_LINES = "fourlines";

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

    private String mapLeaderboards(String leaderBoardId) {
        if (leaderBoardId == null)
            return null;
        else if (leaderBoardId.equals(GpgsHelper.LEAD_MARATHON))
            return LEAD_MARATHON;

        return null;
    }

    private String mapAchievements(String achievementId) {
        if (achievementId == null)
            return null;
        else if (achievementId.equals(GpgsHelper.ACH_FOUR_LINES))
            return ACH_FOUR_LINES;

        return null;
    }


}
