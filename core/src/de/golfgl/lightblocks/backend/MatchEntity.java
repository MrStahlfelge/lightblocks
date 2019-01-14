package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin Schulte on 17.11.2018.
 */

public class MatchEntity implements IPlayerInfo {
    public static final String PLAYER_STATE_WAIT = "WAIT";
    public static final String PLAYER_STATE_CHALLENGED = "CHALLENGED";
    public static final String PLAYER_STATE_YOURTURN = "YOURTURN";
    public static final String PLAYER_STATE_GAVEUP = "GAVEUP";
    public static final String PLAYER_STATE_TIMESUP = "TIMESUP";
    public static final String PLAYER_STATE_WON = "WON";
    public static final String PLAYER_STATE_LOST = "LOST";

    public final String uuid;
    public final String opponentId;
    public final long lastChangeTime;
    // abgelaufen/warten/gewonnen/verloren
    public final String matchState;
    public final int beginningLevel;
    public final boolean myTurn;
    public final String opponentNick;
    public final String opponentDecoration;
    public final int turnBlockCount;
    public final List<MatchTurn> turns;
    public final String opponentReplay;
    public final String garbageGap;
    public final String drawyer;
    public final String yourReplay;
    public final boolean isFullMatchInfo;

    public MatchEntity(JsonValue fromJson) {
        uuid = fromJson.getString("uuid");
        myTurn = fromJson.getBoolean("yourTurn");
        opponentId = fromJson.getString("opponentId", null);
        opponentNick = fromJson.getString("opponentNick", null);
        lastChangeTime = fromJson.getLong("lastChangeTime");
        matchState = fromJson.getString("yourMatchState").toLowerCase();
        beginningLevel = fromJson.getInt("beginningLevel");
        turnBlockCount = fromJson.getInt("turnBlockCount");
        opponentDecoration = fromJson.getString("opponentDecoration", null);
        garbageGap = fromJson.getString("garbageGap", "");
        drawyer = fromJson.getString("drawyer", "");
        yourReplay = fromJson.getString("yourReplay", null);
        opponentReplay = fromJson.getString("opponentReplay", null);

        turns = new ArrayList<>();
        JsonValue turnJson = fromJson.get("turns");
        if (turnJson != null) {
            for (JsonValue turn = turnJson.child; turn != null; turn = turn.next) {
                turns.add(new MatchTurn(turn));
            }
            isFullMatchInfo = true;
        } else
            isFullMatchInfo = yourReplay != null || opponentReplay != null;
    }

    @Override
    public String getUserId() {
        return opponentId;
    }

    @Override
    public String getUserNickName() {
        return opponentNick;
    }

    @Override
    public String getUserDecoration() {
        return opponentDecoration;
    }

    public static class MatchTurn {
        public final String matchId;
        public final int turnNum;
        public final int yourScore;
        public final int opponentScore;
        public final boolean youDroppedOut;
        public final boolean opponentDroppedOut;
        public final int linesSent;
        public final boolean youPlayed;
        public final boolean opponentPlayed;

        public MatchTurn(JsonValue turn) {
            matchId = turn.getString("matchId");
            turnNum = turn.getInt("turnNum");
            yourScore = turn.getInt("yourScore");
            opponentScore = turn.getInt("opponentScore");
            youDroppedOut = turn.getBoolean("youDroppedOut");
            opponentDroppedOut = turn.getBoolean("opponentDroppedOut");
            linesSent = turn.getInt("linesSent");
            youPlayed = turn.getBoolean("youPlayed");
            opponentPlayed = turn.getBoolean("opponentPlayed");
        }
    }
}
