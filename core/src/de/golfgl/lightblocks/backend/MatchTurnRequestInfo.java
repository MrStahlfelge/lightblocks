package de.golfgl.lightblocks.backend;

/**
 * Information, die nach gemachter Runde an den Server gesendet werden
 * <p>
 * Created by Benjamin Schulte on 08.01.2019.
 */

public class MatchTurnRequestInfo {
    public String matchId;
    public String turnKey;
    public String replay;
    public int score1;
    public int score2;
    public int linesSent;
    public String drawyer;
    public String garbagePos;
    public boolean droppedOut1;
    public boolean droppedOut2;
}
