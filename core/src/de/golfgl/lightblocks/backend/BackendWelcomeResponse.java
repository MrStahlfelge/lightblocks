package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class BackendWelcomeResponse {
    public final long responseTime;
    public final long timeDelta;
    public final boolean isBeta;
    public final boolean serverMultiplayerUnlocked;
    public final boolean authenticated;
    public final String token;
    public final String warningMsg;
    public final List<BackendMessage> messageList;
    public final boolean competitionActionRequired;
    public final boolean competitionNewsAvailable;

    /**
     * sets competitionNewsFlag to read, if necessary
     */
    public BackendWelcomeResponse(BackendWelcomeResponse template, boolean competitionNewsAvailable) {
        responseTime = template.responseTime;
        timeDelta = template.timeDelta;
        isBeta = template.isBeta;
        serverMultiplayerUnlocked = template.serverMultiplayerUnlocked;
        authenticated = template.authenticated;
        token = template.token;
        warningMsg = template.warningMsg;
        messageList = template.messageList;
        this.competitionNewsAvailable = competitionNewsAvailable;

        // competitionActionRequired is never true if competitionNewsAvailable is false
        // competitionNewsAvailable is always trtue if competitionActionRequired is true
        if (competitionNewsAvailable) {
            competitionActionRequired = template.competitionActionRequired;
        } else {
            competitionActionRequired = false;
        }
    }

    public BackendWelcomeResponse(JsonValue fromJson) {
        responseTime = fromJson.getLong("responseTime");
        timeDelta = responseTime - TimeUtils.millis();
        isBeta = fromJson.getBoolean("yoursIsBeta");
        serverMultiplayerUnlocked = fromJson.getBoolean("serverMultiplayerUnlocked", false);
        authenticated = fromJson.getBoolean("authenticated");
        token = fromJson.getString("authToken", null);
        warningMsg = fromJson.getString("warningMsg", null);
        competitionActionRequired = fromJson.getBoolean("competitionActionRequired", false);
        competitionNewsAvailable = competitionActionRequired;

        messageList = new ArrayList<>();
        JsonValue messagesJson = fromJson.get("messages");
        if (messagesJson != null)
            for (JsonValue msgJson = messagesJson.child; msgJson != null; msgJson = msgJson.next) {
                messageList.add(new BackendMessage(msgJson));
            }


    }
}
