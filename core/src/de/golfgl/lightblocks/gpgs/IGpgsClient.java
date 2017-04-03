package de.golfgl.lightblocks.gpgs;

/**
 * Core Interface for GpgsClient
 * <p>
 * Created by Benjamin Schulte on 26.03.2017.
 */

public interface IGpgsClient {

    /**
     * Connects to Gpgs service
     *
     * @param autoStart if true, no error messages or log in prompts will be shown
     */
    void connect(boolean autoStart);

    /**
     * Disconnects from Gpgs service
     *
     * @param autoEnd if false, an expiclit signOut is performed
     */
    void disconnect(boolean autoEnd);

    public String getPlayerDisplayName();

    boolean isConnected();

    public void showLeaderboards(String leaderBoardId) throws GpgsException;

    public void showAchievements() throws GpgsException;

    public void submitToLeaderboard(String leaderboardId, long score, String tag) throws GpgsException;

    public void submitEvent(String eventId, int increment);

    void unlockAchievement(String achievementId);

    void incrementAchievement(String achievementId, int incNum);
}
