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
            "special_1B", "typeB_1B", "special_1C", "garbage_1A", "typeB_1C"}; //

    private String uniqueId;
    private int index;

    public static List<Mission> getMissionList() {

        List<Mission> missionsList = new ArrayList<Mission>(missions.length);

        for (int i = 0; i < missions.length; i++) {
            Mission mission = new Mission();
            mission.setUniqueId(missions[i]);
            mission.setIndex(i);
            missionsList.add(mission);
        }

        return missionsList;
    }

    /**
     * returns the label used in i18n
     *
     * @param uid
     * @return
     */
    public static String getLabelUid(String uid) {
        final int usIdx = uid.indexOf("_");
        return "labelModel_" + (usIdx >= 0 ? uid.substring(0, usIdx) : uid);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    protected void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

}
