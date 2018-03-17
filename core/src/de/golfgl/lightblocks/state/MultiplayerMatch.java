package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Set;

import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;

/**
 * Multiplayermatch: Wer hat wie oft gewonnen und welche Punkte gemacht?
 * <p>
 * Created by Benjamin Schulte on 07.03.2017.
 */

public class MultiplayerMatch {

    private HashMap<String, PlayerStat> players = new HashMap<String, PlayerStat>();

    /**
     * gets multiplayer statistics for a given player. Creates it if not available
     *
     * @param playerId
     * @return the player stats
     */
    public PlayerStat getPlayerStat(String playerId) {
        PlayerStat playerStat;

        synchronized (players) {
            playerStat = players.get(playerId);

            if (playerStat == null) {
                playerStat = new PlayerStat();
                playerStat.playerId = playerId;
                players.put(playerId, playerStat);
            }
        }

        return playerStat;
    }

    /**
     * Returns number of players currently in the statistics
     *
     * @return
     */
    public int getNumberOfPlayers() {
        synchronized (players) {
            return players.size();
        }
    }

    public Set<String> getPlayers() {
        synchronized (players) {
            return players.keySet();
        }
    }

    public void clearStats() {
        synchronized (players) {
            players.clear();
        }
    }

    public static class PlayerStat {
        private String playerId;
        private int numberOutplays;
        private boolean isPresent;
        private int totalScore;

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public int getNumberOutplays() {
            return numberOutplays;
        }

        public void incNumberOutplays() {
            this.numberOutplays++;
        }

        public boolean isPresent() {
            return isPresent;
        }

        public void setPresent(boolean present) {
            isPresent = present;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public void addTotalScore(int totalScore) {
            this.totalScore += totalScore;
        }

        public MultiPlayerObjects.PlayerInMatch toPlayerInMatch() {
            MultiPlayerObjects.PlayerInMatch retVal = new MultiPlayerObjects.PlayerInMatch();
            retVal.numOutplayed = numberOutplays;
            retVal.playerId = playerId;
            retVal.totalScore = totalScore;

            return retVal;
        }

        public void setFromPlayerInMatch(MultiPlayerObjects.PlayerInMatch pim) {
            if (!pim.playerId.equals(playerId))
                Gdx.app.error("Multiplayer", "Wrong player in match stat received - ignored");
            else {
                numberOutplays = pim.numOutplayed;
                totalScore = pim.totalScore;
            }
        }
    }
}
