package de.golfgl.lightblocks.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

import de.golfgl.lightblocks.LightBlocksGame;

public class HtmlLauncher extends GwtApplication {

    // padding is to avoid scrolling in iframes, set to 20 if you have problems
    private static final int PADDING = 100;
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
        return new LightBlocksGame();
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