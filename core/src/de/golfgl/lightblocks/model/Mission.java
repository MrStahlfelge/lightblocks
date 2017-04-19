package de.golfgl.lightblocks.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for the missions
 * <p>
 * Created by Benjamin Schulte on 18.04.2017.
 */

public class Mission {

    private static final String[] missions = {"tutorial", "typeA_1A", "typeB_1A", "typeA_1B", "special_1A", "typeA_1C",
            "special_1B", "typeA_1D", "special_1C", "typeA_1E", "minTetroSet_1A",
            "garbage_1A", "typeB_1B", "blocks_1A", "typeA_2A", "typeB_2A",
            "typeA_2B", "special_2A", "typeA_1F", "garbage_1B", "typeB_2B"};

    private String uniqueId;
    private boolean done;
    private int rating;

    public static List<Mission> getMissionList() {

        List<Mission> missionsList = new ArrayList<Mission>(missions.length);

        for (int i = 0; i < missions.length; i++) {
            Mission mission = new Mission();
            mission.setUniqueId(missions[i]);
            missionsList.add(mission);
        }

        return missionsList;
    }

    public static boolean isMission(String modelId) {
        return Arrays.asList(missions).contains(modelId);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    protected void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean isDone() {
        return done;
    }

    protected void setDone(boolean done) {
        this.done = done;
    }

    public int getRating() {
        return rating;
    }

    protected void setRating(int rating) {
        this.rating = rating;
    }

}
