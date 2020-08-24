package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

import de.golfgl.lightblocks.input.PlayScreenInput;

/**
 * Helper class for the missions
 * <p>
 * Created by Benjamin Schulte on 18.04.2017.
 */

public class Mission {
    public static final String KEY_TUTORIAL = "tutorial";

    // GPGS Achievement "Mission 10 geschafft"
    public static final String MISSION10ACHIEVEMENT = "typeB_1C";
    // GPGS Achievement "Mission 15 geschafft"
    public static final String MISSION15ACHIEVEMENT = "typeB_1D";

    private static final String[] missions = {KEY_TUTORIAL, "typeA_1A", "typeB_1A", "typeA_1B", "special_1A",
            "typeA_1C", "special_1B", "typeB_1B", "special_1C", "garbage_1A", "typeB_1C",
            "gravityA_2A", "typeA_1D", "gravityB_2A", "garbage_1B", "typeB_1D"};

    private static final String[] needGesture = {"tutorial"};
    private static final String[] needGravity = {"gravityA_2A", "gravityB_2A"};

    private String uniqueId;
    private int index;

    public static List<Mission> getMissionList() {
        Array<String> needsGestures = new Array<String>(needGesture);
        Array<String> needsGravity = new Array<String>(needGravity);
        boolean touchAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN);
        boolean gravityAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_ACCELEROMETER);

        List<Mission> missionsList = new ArrayList<Mission>(missions.length);
        int added = 0;

        for (int i = 0; i < missions.length; i++) {
            String uid = missions[i];
            if (!(needsGestures.contains(uid, false) && !touchAvailable)
                    && !(needsGravity.contains(uid, false) && !gravityAvailable)) {

                Mission mission = new Mission();
                mission.setUniqueId(uid);
                mission.setIndex(added);
                missionsList.add(mission);
                added++;
            }
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
