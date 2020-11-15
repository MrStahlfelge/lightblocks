package de.golfgl.lightblocks.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.GwtGraphics;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.github.czyzby.websocket.GwtWebSockets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;

import de.golfgl.gdxgameanalytics.GwtGameAnalytics;
import de.golfgl.gdxgamesvcs.GameJoltClient;
import de.golfgl.gdxgamesvcs.NoGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.menu.backend.OnlyReplayScreen;

public class HtmlLauncher extends GwtApplication {

    // padding is to avoid scrolling in iframes, set to 20 if you have problems
    private static final int PADDING = LightBlocksGame.GAME_DEVMODE ? 100 : 10;
    private GwtApplicationConfiguration cfg;

    @Override
    public GwtApplicationConfiguration getConfig() {
        int w = Window.getClientWidth() - PADDING;
        int h = Window.getClientHeight() - PADDING;
        cfg = new GwtApplicationConfiguration(w, h);

        cfg.usePhysicalPixels = true;
        double density = GwtGraphics.getNativeScreenDensity();
        cfg.width = (int) (cfg.width * density);
        cfg.height = (int) (cfg.height * density);

        Window.enableScrolling(false);
        Window.setMargin("0");
        Window.addResizeHandler(new ResizeListener());
        return cfg;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        GwtWebSockets.initiate();

        LightBlocksGame lightBlocksGame = new LightBlocksGame() {
            @Override
            protected boolean shouldGoToReplay() {
                final String replayNickname = Window.Location.getParameter(OnlyReplayScreen.PARAM_NICKNAME);
                final String replayMode = Window.Location.getParameter(OnlyReplayScreen.PARAM_GAMEMODE);

                if (replayNickname != null && !replayNickname.isEmpty()) {
                    this.setScreen(new OnlyReplayScreen(this, replayNickname, replayMode));
                    return true;
                } else
                    return false;
            }
        };

        String hostName = Window.Location.getHostName();
        final String gjUsername = Window.Location.
                getParameter(GameJoltClient.GJ_USERNAME_PARAM);

        final boolean isOnGamejolt = gjUsername != null && !gjUsername.isEmpty() || hostName.contains("gamejolt");

        GameJoltClient gjClient = new GameJoltClient() {
            @Override
            public String getGameServiceId() {
                return isOnGamejolt ? super.getGameServiceId() : NoGameServiceClient.GAMESERVICE_ID;
            }
        };
        gjClient.initialize(GpgsHelper.GJ_APP_ID, GpgsHelper.GJ_PRIVATE_KEY);
        gjClient.setUserName(gjUsername)
                .setUserToken(com.google.gwt.user.client.Window.Location.
                        getParameter(GameJoltClient.GJ_USERTOKEN_PARAM))
                .setGuestName("Guest user")
                .setGjScoreTableMapper(new GpgsHelper.GamejoltScoreboardMapper())
                .setGjTrophyMapper(new GpgsHelper.GamejoltTrophyMapper());

        lightBlocksGame.gpgsClient = gjClient;
        lightBlocksGame.gameAnalytics = new GwtGameAnalytics();

        return lightBlocksGame;
    }

    @Override
    public Preloader.PreloaderCallback getPreloaderCallback() {
        return createPreloaderPanel(GWT.getHostPageBaseURL() + "preloadlogo.png");
    }

    @Override
    protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
        meterPanel.setStyleName("gdx-meter");
        meterPanel.addStyleName("nostripes");
        Style meterPanelStyle = meterPanel.getElement().getStyle();
        meterPanelStyle.setProperty("padding", "0px");
        meterPanelStyle.setProperty("WebkitBoxShadow", "none");
        meterPanelStyle.setProperty("boxShadow", "none");
        meterStyle.setProperty("backgroundColor", "#ffffff");
        meterStyle.setProperty("backgroundImage", "none");
    }

    class ResizeListener implements ResizeHandler {
        @Override
        public void onResize(ResizeEvent event) {
            int width = event.getWidth() - PADDING;
            int height = event.getHeight() - PADDING;
            getRootPanel().setWidth("" + width + "px");
            getRootPanel().setHeight("" + height + "px");
            if (cfg.usePhysicalPixels) {
                double density = GwtGraphics.getNativeScreenDensity();
                width = (int) (width * density);
                height = (int) (height * density);
            }
            getApplicationListener().resize(width, height);
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

}