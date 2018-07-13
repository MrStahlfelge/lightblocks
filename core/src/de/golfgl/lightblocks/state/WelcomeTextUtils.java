package de.golfgl.lightblocks.state;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.SinglePlayerScreen;
import de.golfgl.lightblocks.menu.WelcomeButton;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.screen.PlayScreenInput;

/**
 * Created by Benjamin Schulte on 01.05.2018.
 */

public class WelcomeTextUtils {
    // So kann auch die Farbe verändert werden:
    //Color.WHITE.set(0.2f, 1, 0.2f, 1);
    // Aber durch Klick auf das Label wieder zurücksetzen
    // 17.3. St Patrick's Day - Lightblocks Hintergrundmusik!

    // Hier kann "Welcome back :-)", "Have a good morning" usw. stehen, "Hi MrStahlfelge"
    private static boolean alreadyShownDaysSinceLastStart = false;

    public static Array<WelcomeButton.WelcomeText> fillWelcomes(final LightBlocksGame app) {
        Array<WelcomeButton.WelcomeText> welcomes = new Array<WelcomeButton.WelcomeText>();

        int lastUsedVersion = app.localPrefs.getLastUsedLbVersion();
        long clearedLines = app.savegame.getTotalScore().getClearedLines();
        int daysSinceLastStart = app.localPrefs.getDaysSinceLastStart();
        boolean isLongTimePlayer = app.savegame.getTotalScore().getDrawnTetrominos() >= 1000;

        // bis einschl 1818 wurde Version nicht gespeichert
        if (lastUsedVersion == 0 && isLongTimePlayer)
            lastUsedVersion = 1818;

        // AB HIER DIE EINBLENDUNGEN

        // 1. UPDATE HINWEISE
        // ganz neuer User
        if (lastUsedVersion == 0) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeNew"), null));
        } else if (lastUsedVersion < LightBlocksGame.GAME_VERSIONNUMBER || LightBlocksGame.GAME_DEVMODE) {
            // Update-Hinweise
            listNewFeatures(welcomes, app, lastUsedVersion);
        }

        // 2. EINBLENDUNGEN ZU NOCH NICHT GESPIELTEN MODIS ODER UNGENUTZTEN FEATURES

        // Unter 50 Reihen und tutorial verfügbar und nicht gemacht: anbieten
        if (clearedLines < 100 && TutorialModel.tutorialAvailable() &&
                app.savegame.getBestScore(Mission.KEY_TUTORIAL).getRating() == 0)
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeTutorial"),
                    new ShowSinglePlayerPageRunnable(app, SinglePlayerScreen.PAGEIDX_MISSION)));

        // 3. BESONDERE TAGE ODER SOWAS (WEIHNACHTEN ETC)


        // 4. WENN NOCH KEIN TEXT DA, BEGRÜSSEN WIR SPORADISCHE NUTZER
        if (welcomes.size == 0 && daysSinceLastStart >= 5 && !alreadyShownDaysSinceLastStart) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeAfterSomeTime"), null));
            alreadyShownDaysSinceLastStart = true;
        }

        // 5. WERBUNG UND ÄHNLICHES, DASS NACH ZUFALLSPRINZIP EINGEBLENDET WIRD
        // wird aufgrund von 4. nur für wiederkehrende Nutzer angezeigt
        if (welcomes.size == 0) {
            if (isLongTimePlayer && MathUtils.randomBoolean(.05f))
                welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeOtherPlatforms"),
                        new OpenWebsiteRunnable(app)));

            if (isLongTimePlayer && app.localPrefs.getSupportLevel() == 0 && app.canDonate() &&
                    MathUtils.randomBoolean(.05f))
                welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeDonations"),
                        new Runnable() {
                            @Override
                            public void run() {
                                app.mainMenuScreen.showDonationDialog();
                            }
                        }));
        }

        // ENDE

        //welcomes.add(new WelcomeButton.WelcomeText("Have a good morning", null));
        //welcomes.add(new WelcomeButton.WelcomeText("Have a\ngood day", null));

        return welcomes;
    }

    protected static void listNewFeatures(Array<WelcomeButton.WelcomeText> welcomes,
                                          LightBlocksGame app, int listChangesSince) {
        boolean touchAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN);

        if (listChangesSince < 1828)
            welcomes.add(new WelcomeButton.WelcomeText("There is a new option to show a ghost piece in game.",
                    new ShowSettingsRunnable(app)));

        // 1825
        if (listChangesSince < 1825) {
            welcomes.add(new WelcomeButton.WelcomeText("There are new game modes:\nPractice and Sprint 40L. Have fun!",
                    new ShowSinglePlayerPageRunnable(app, SinglePlayerScreen.PAGEIDX_OVERVIEW)));
            if (app.isOnAndroidTV())
                welcomes.add(new WelcomeButton.WelcomeText("You can now configure which buttons you " +
                        "want to use on your remote control.", new ShowSettingsRunnable(app)));

        }

        // Neue Features in 1819: Shades of Grey, Hard Drop
        if (listChangesSince < 1819) {
            welcomes.add(new WelcomeButton.WelcomeText("Lightblocks can now perform hard drops. " +
                    (touchAvailable ?
                            "For gesture input, enable it on swipe up in the settings." : "Use the UP button or key.")
                    + "\nThere's also a new option for block shadings.", new ShowSettingsRunnable(app)));
        }
        if (listChangesSince < 1822 && touchAvailable) {
            welcomes.add(new WelcomeButton.WelcomeText("There is a new option to play with On Screen Controls instead" +
                    " of gestures in landscape mode.", new ShowSettingsRunnable(app)));
        }
    }

    private static class ShowSinglePlayerPageRunnable implements Runnable {
        private final LightBlocksGame app;
        private final int pageidx;

        public ShowSinglePlayerPageRunnable(LightBlocksGame app, int pageIdx) {
            this.app = app;
            this.pageidx = pageIdx;
        }

        @Override
        public void run() {
            SinglePlayerScreen sp = app.mainMenuScreen.showSinglePlayerScreen();
            sp.showPage(pageidx);
        }
    }

    private static class ShowSettingsRunnable implements Runnable {
        private final LightBlocksGame app;

        public ShowSettingsRunnable(LightBlocksGame app) {
            this.app = app;
        }

        @Override
        public void run() {
            app.mainMenuScreen.showSettings();
        }
    }

    private static class OpenWebsiteRunnable implements Runnable {
        private final LightBlocksGame app;

        public OpenWebsiteRunnable(LightBlocksGame app) {
            this.app = app;
        }

        @Override
        public void run() {
            app.openOrShowUri(LightBlocksGame.GAME_URL);
        }
    }
}
