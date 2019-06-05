package de.golfgl.lightblocks.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

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
        Window.enableScrolling(false);
        Window.setMargin("0");
        Window.addResizeHandler(new ResizeListener());
        cfg.preferFlash = false;
        return cfg;
    }

    @Override
    public ApplicationListener createApplicationListener() {
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
        final Canvas canvas = Canvas.createIfSupported();
        canvas.setWidth("" + (int) (LightBlocksGame.nativeGameWidth * 0.7f) + "px");
        canvas.setHeight("70px");
        getRootPanel().add(canvas);
        final Context2d context = canvas.getContext2d();
        context.setTextAlign(Context2d.TextAlign.CENTER);
        context.setTextBaseline(Context2d.TextBaseline.MIDDLE);
        context.setFont("18pt Calibri");

        return new Preloader.PreloaderCallback() {
            @Override
            public void update(Preloader.PreloaderState state) {
                if (state.hasEnded()) {
                    context.fillRect(0, 0, 300, 40);
                } else {
                    System.out.println("loaded " + state.getProgress());
                    CssColor color = CssColor.make(30, 30, 30);
                    context.setFillStyle(color);
                    context.setStrokeStyle(color);
                    context.fillRect(0, 0, 300, 70);
                    color = CssColor.make(200, 200, 200); //, (((TimeUtils.nanoTime() - loadStart) % 1000000000) /
                    // 1000000000f));
                    context.setFillStyle(color);
                    context.setStrokeStyle(color);
                    context.fillRect(0, 0, 300 * (state.getDownloadedSize() / (float) state.getTotalSize()) * 0.97f,
                            70);

                    context.setFillStyle(CssColor.make(50, 50, 50));
                    context.fillText("loading", 300 / 2, 70 / 2);

                }
            }

            @Override
            public void error(String file) {
                System.out.println("error: " + file);
            }
        };
    }

    class ResizeListener implements ResizeHandler {
        @Override
        public void onResize(ResizeEvent event) {
            int width = event.getWidth() - PADDING;
            int height = event.getHeight() - PADDING;
            getRootPanel().setWidth("" + width + "px");
            getRootPanel().setHeight("" + height + "px");
            getApplicationListener().resize(width, height);
            Gdx.graphics.setWindowedMode(width, height);
        }
    }

}