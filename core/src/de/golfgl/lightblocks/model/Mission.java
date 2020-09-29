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

    // GPGS Achievement "Mission 10 done"
    public static final String MISSION10ACHIEVEMENT = "typeB_1C";
    // GPGS Achievement "Mission 15 done"
    public static final String MISSION15ACHIEVEMENT = "typeB_1D";

    private static final String[] missions = {KEY_TUTORIAL, "typeA_1A", "typeB_1A", "typeA_1B", "special_1A",
            "typeA_1C", "special_1B", "typeB_1B", "special_1C", "garbage_1A", "typeB_1C",
            "gravityA_2A", "typeA_1D", "gravityB_2A", "garbage_1B", "typeB_1D"};

    private static final String[] needGesture = {"tutorial"};
    private static final String[] needGravity = {"gravityA_2A", "gravityB_2A"};

    private String uniqueId;
    private int index;
    private int displayIndex;

    public static List<Mission> getMissionList() {
        Array<String> needsGestures = new Array<>(needGesture);
        Array<String> needsGravity = new Array<>(needGravity);
        boolean touchAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN);
        boolean gravityAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_ACCELEROMETER);

        List<Mission> missionsList = new ArrayList<>(missions.length);
        int added = 0;
        // tutorial will be displayed as index 0. if it is not available, start with index 1
        int displayIdx = touchAvailable ? 0 : 1;

        for (int i = 0; i < missions.length; i++) {
            String uid = missions[i];
            if (!(needsGestures.contains(uid, false) && !touchAvailable)
                    && !(needsGravity.contains(uid, false) && !gravityAvailable)) {

                Mission mission = new Mission();
                mission.uniqueId = uid;
                mission.index = added;
                mission.displayIndex = displayIdx;
                missionsList.add(mission);

                added++;
                displayIdx++;
            }
        }

        return missionsList;
    }

    /**
     * @return the label used in i18n
     */
    public static String getLabelUid(String uid) {
        final int usIdx = uid.indexOf("_");
        return "labelModel_" + (usIdx >= 0 ? uid.substring(0, usIdx) : uid);
    }

    public int getIndex() {
        return index;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public String getUniqueId() {
        return uniqueId;
    }

}
