package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class BackendWelcomeResponse {
    public final long responseTime;
    public final long timeDelta;
    public final boolean isBeta;
    public final boolean authenticated;
    public final String warningMsg;
    public final List<BackendMessage> messageList;
    public final boolean competitionActionRequired;

    /**
     * kopiert, setzt jedoch competitionActionRequired auf false
     */
    public BackendWelcomeResponse(BackendWelcomeResponse template, boolean competitionActionRequired) {
        responseTime = template.responseTime;
        timeDelta = template.timeDelta;
        isBeta = template.isBeta;
        authenticated = template.authenticated;
        warningMsg = template.warningMsg;
        messageList = template.messageList;
        this.competitionActionRequired = competitionActionRequired;
    }

    public BackendWelcomeResponse(JsonValue fromJson) {
        responseTime = fromJson.getLong("responseTime");
        timeDelta = responseTime - TimeUtils.millis();
        isBeta = fromJson.getBoolean("yoursIsBeta");
        authenticated = fromJson.getBoolean("authenticated");
        warningMsg = fromJson.getString("warningMsg", null);
        competitionActionRequired = fromJson.getBoolean("competitionActionRequired", false);

        messageList = new ArrayList<>();
        JsonValue messagesJson = fromJson.get("messages");
        if (messagesJson != null)
            for (JsonValue msgJson = messagesJson.child; msgJson != null; msgJson = msgJson.next) {
                messageList.add(new BackendMessage(msgJson));
            }


    }
}
