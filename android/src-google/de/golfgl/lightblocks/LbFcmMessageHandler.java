package de.golfgl.lightblocks;

import com.google.firebase.messaging.RemoteMessage;

import de.golfgl.gdxpushmessages.FcmMessageHandler;
import de.golfgl.lightblocks.backend.BackendManager;

public class LbFcmMessageHandler extends FcmMessageHandler {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // auf jeden Fall an den Listener weitergeben
        super.onMessageReceived(remoteMessage);

        if (!GeneralAndroidLauncher.isGameInForeground()) {
            if (remoteMessage.getData().containsKey(KEY_RM_PAYLOAD) &&
                    BackendManager.PUSH_PAYLOAD_MULTIPLAYER.equals(remoteMessage.getData().get(KEY_RM_PAYLOAD)))
                GeneralAndroidLauncher.makeMultiplayerNotification(this);
        }
    }
}
