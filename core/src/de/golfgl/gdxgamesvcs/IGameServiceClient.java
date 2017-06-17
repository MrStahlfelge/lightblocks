package de.golfgl.gdxgamesvcs;

/**
 * Created by Benjamin Schulte on 16.06.2017.
 */

public interface IGameServiceClient {

    /**
     * Gets an id for this game service.
     *
     * @return Game Service ID
     */
    String getGameServiceId();

    /**
     * Connects to Gameservice, opens the session.
     *
     * @param silent if true, no error messages or log in prompts will be shown for. Use this at application start
     *               or after resuming the application in Android.
     */
    void connect(boolean silent);

    /**
     * Disconnects from Gameservice, closes the session
     *
     */
    void disconnect();

    /**
     * Signs explicitely out and disconnects from Gameservice. Use only when explicitely wanted by user.
     */
    void logOff();

    /**
     * Gets Players display name
     * @return Display name
     */
    String getPlayerDisplayName();

    /**
     * Checks the connection status of the game service. See also isConnectionPending.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Opens user interface for leader boards, if available.
     *
     * @param leaderBoardId if null, then overview is opened (when supported)
     * @throws GameServiceException
     */
    void showLeaderboards(String leaderBoardId) throws GameServiceException;

    /**
     * Opens user interface for achievements, if available.
     *
     * @throws GameServiceException
     */
    void showAchievements() throws GameServiceException;

    /**
     * Submits to given leaderboard
     *
     * @param leaderboardId
     * @param score
     * @param tag
     * @throws GameServiceException when no connection is open
     */
    void submitToLeaderboard(String leaderboardId, long score, String tag) throws GameServiceException;

    void submitEvent(String eventId, int increment);

    void unlockAchievement(String achievementId);

    void incrementAchievement(String achievementId, int incNum);

    void saveGameState(byte[] gameState, long progressValue);

    void loadGameState();
}
