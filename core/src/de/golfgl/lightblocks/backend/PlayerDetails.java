package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailinformationen zu einem Spieler
 */

public class PlayerDetails implements IPlayerInfo {
    public final String uuid;
    public final String nickName;
    public final long lastActivity;
    public final long memberSince;
    public final long countTotalBlocks;
    public final int experience;
    public final String publicContact;
    public final List<ScoreListEntry> highscores;
    public final String passwordEmail;
    public final int donator;
    public final String country;
    public final int multiplayerMatchesCount;
    public final int multiplayerWinCount;
    public final int multiplayerLinesSent;
    public final int multiplayerTurns;

    PlayerDetails(JsonValue fromJson) {
        uuid = fromJson.getString("id");
        nickName = fromJson.getString("nickName");

        // diese in Übersichtsliste nicht enthalten und daher mit Defaults vorzubelegen
        lastActivity = fromJson.getLong("lastActivity", 0);
        memberSince = fromJson.getLong("memberSince", 0);
        countTotalBlocks = fromJson.getLong("countTotalBlocks", 0);
        experience = fromJson.getInt("experience", 0);
        publicContact = fromJson.getString("publicContact", null);
        passwordEmail = fromJson.getString("passwordEmail", null);
        donator = fromJson.getInt("donator", 0);
        country = fromJson.getString("country", null);
        multiplayerMatchesCount = fromJson.getInt("multiplayerMatchesCount", 0);
        multiplayerWinCount = fromJson.getInt("multiplayerWinCount", 0);
        multiplayerLinesSent = fromJson.getInt("multiplayerLinesSent", 0);
        multiplayerTurns = fromJson.getInt("multiplayerTurns", 0);

        highscores = new ArrayList<ScoreListEntry>();
        JsonValue highscorejson = fromJson.get("highscores");
        if (highscorejson != null)
            for (JsonValue score = highscorejson.child; score != null; score = score.next) {
                highscores.add(new ScoreListEntry(score, null, ScoreListEntry.ScoreType.best));
            }

    }

    @Override
    public String getUserId() {
        return uuid;
    }

    @Override
    public String getUserNickName() {
        return nickName;
    }

    @Override
    public String getUserDecoration() {
        //TODO
        return null;
    }
}
