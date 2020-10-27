package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendMessage;
import de.golfgl.lightblocks.backend.BackendWelcomeResponse;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.menu.SinglePlayerScreen;
import de.golfgl.lightblocks.menu.WelcomeButton;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.input.PlayScreenInput;

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
    private static Array<WelcomeButton.WelcomeText> randomWelcomes;

    public static Array<WelcomeButton.WelcomeText> fillWelcomes(final LightBlocksGame app, boolean refreshRandoms) {
        Array<WelcomeButton.WelcomeText> welcomes = new Array<WelcomeButton.WelcomeText>();

        int lastUsedVersion = app.localPrefs.getLastUsedLbVersion();
        long clearedLines = app.savegame.getTotalScore().getClearedLines();
        int daysSinceLastStart = app.localPrefs.getDaysSinceLastStart();
        boolean isLongTimePlayer = app.savegame.getTotalScore().getDrawnTetrominos() >= 1000;

        // AB HIER DIE EINBLENDUNGEN

        // 1. UPDATE HINWEISE
        // ganz neuer User
        if (lastUsedVersion == 0) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeNew"), null));
        } else if (lastUsedVersion < LightBlocksGame.GAME_VERSIONNUMBER || LightBlocksGame.GAME_DEVMODE) {
            // Update-Hinweise
            listNewFeatures(welcomes, app, lastUsedVersion);
        }

        // 2. EINBLENDUNGEN VOM SERVER / BESONDERE TAGE ODER SOWAS (WEIHNACHTEN ETC)
        if (app.backendManager.hasLastWelcomeResponse()) {
            listBackendMessages(app, welcomes, app.backendManager.getLastWelcomeResponse());
        }

        // 3. EINBLENDUNGEN ZU NOCH NICHT GESPIELTEN MODIS ODER UNGENUTZTEN FEATURES

        // Unter 50 Reihen und tutorial verfügbar und nicht gemacht: anbieten
        if (clearedLines < 100 && TutorialModel.tutorialAvailable() &&
                app.savegame.getBestScore(Mission.KEY_TUTORIAL).getRating() == 0)
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeTutorial"),
                    new ShowSinglePlayerPageRunnable(app, SinglePlayerScreen.PAGEIDX_MISSION)));

        // 4. WENN NOCH KEIN TEXT DA, BEGRÜSSEN WIR SPORADISCHE NUTZER
        if (welcomes.size == 0 && daysSinceLastStart >= 5 && !alreadyShownDaysSinceLastStart) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeAfterSomeTime"), null));
            alreadyShownDaysSinceLastStart = true;
        }

        // 5. WERBUNG UND ÄHNLICHES, DASS NACH ZUFALLSPRINZIP EINGEBLENDET WIRD
        // wird aufgrund von 4. nur für wiederkehrende Nutzer angezeigt
        if (welcomes.size == 0) {
            addRandomMessages(app, welcomes, isLongTimePlayer, refreshRandoms);
        }

        // ENDE

        //welcomes.add(new WelcomeButton.WelcomeText("Have a good morning", null));
        //welcomes.add(new WelcomeButton.WelcomeText("Have a\ngood day", null));

        return welcomes;
    }

    public static void addRandomMessages(final LightBlocksGame app, Array<WelcomeButton.WelcomeText> welcomes,
                                         boolean isLongTimePlayer, boolean refreshRandoms) {

        if (refreshRandoms || randomWelcomes == null) {
            randomWelcomes = new Array<>();

            if (isLongTimePlayer && MathUtils.randomBoolean(.01f))
                randomWelcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeOtherPlatforms"),
                        new OpenWebsiteRunnable(app)));

            if (isLongTimePlayer && app.localPrefs.getSupportLevel() == 0 && app.canDonate() &&
                    MathUtils.randomBoolean(.05f))
                randomWelcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeDonations"),
                        new Runnable() {
                            @Override
                            public void run() {
                                app.mainMenuScreen.showDonationDialog();
                            }
                        }));
        }

        welcomes.addAll(randomWelcomes);
    }

    private static void listBackendMessages(final LightBlocksGame app, Array<WelcomeButton.WelcomeText> welcomes,
                                            BackendWelcomeResponse welcomeResponse) {
        // Warnungen als erstes
        //TODO der sollte rot sein oder sowas
        if (welcomeResponse.warningMsg != null && !welcomeResponse.warningMsg.isEmpty())
            welcomes.add(new WelcomeButton.WelcomeText(welcomeResponse.warningMsg, null));

        if (welcomeResponse.competitionActionRequired)
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("labelCompetitionActions"),
                    new ShowMultiPlayerPageRunnable(app, 0)));
        else if (welcomeResponse.competitionNewsAvailable)
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("labelCompetitionNews"),
                    new ShowMultiPlayerPageRunnable(app, 0)));

        for (final BackendMessage message : welcomeResponse.messageList) {
            if (BackendMessage.TYPE_WELCOME.equals(message.type)) {
                Runnable run = null;
                if (message.infoUrl != null && !message.infoUrl.isEmpty())
                    run = new OpenWebsiteRunnable(app) {
                        @Override
                        public void run() {
                            app.openOrShowUri(message.infoUrl);
                        }
                    };

                welcomes.add(new WelcomeButton.WelcomeText(message.content, run));
            }
        }
    }

    protected static void listNewFeatures(Array<WelcomeButton.WelcomeText> welcomes,
                                          LightBlocksGame app, int listChangesSince) {
        boolean touchAvailable = PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN);

        if (listChangesSince < 2009)
            welcomes.add(new WelcomeButton.WelcomeText("New: On device split screen multiplayer mode.",
                    new ShowMultiPlayerPageRunnable(app, 0)));

        if (listChangesSince < 2005 && Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator))
            welcomes.add(new WelcomeButton.WelcomeText("New: You can enable vibration and haptic feedback.",
                    new ShowSettingsRunnable(app)));

        if (listChangesSince < 2004 && app.backendManager.hasUserId())
            welcomes.add(new WelcomeButton.WelcomeText("New: Global TB Battle leaderboard can switch to center your rank.",
                    new ShowMultiPlayerPageRunnable(app, 0)));

        if (listChangesSince < 2003 && touchAvailable &&
                app.localPrefs.getUsedTouchControls() == LocalPrefs.TouchControlType.onScreenButtonsGamepad)
            welcomes.add(new WelcomeButton.WelcomeText("New: On Screen Gamepad has new options for opacity and d-pad.",
                    new ShowSettingsRunnable(app)));

        if (listChangesSince < 1928 && touchAvailable)
            welcomes.add(new WelcomeButton.WelcomeText("New: Touch control settings extended with adjustable on screen buttons.",
                    new ShowSettingsRunnable(app)));

        if (listChangesSince < 1923)
            welcomes.add(new WelcomeButton.WelcomeText("New game mode: Play the modern way, with freeze!",
                    new ShowSinglePlayerPageRunnable(app, SinglePlayerScreen.PAGEIDX_FREEZE)));

        if (listChangesSince < 1910 && Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)) {
            welcomes.add(new WelcomeButton.WelcomeText("New: You can change the default key mapping " +
                    "in the settings screen.", new ShowSettingsRunnable(app)));
        }

        if (listChangesSince < 1909 && Gdx.app.getType() == Application.ApplicationType.iOS
                && app.supportsRealTimeMultiplayer()) {
            welcomes.add(new WelcomeButton.WelcomeText("New: Real time multiplayer mode!\n" +
                    "Play on a local network or via Apple Game Center.",
                    new ShowMultiPlayerPageRunnable(app, 1)));
        }

        if (listChangesSince < 1901) {
            welcomes.add(new WelcomeButton.WelcomeText("New: Global turn based multiplayer mode! " +
                    "With a turn 90 seconds play time at max, you can play in every break you have!",
                    new ShowMultiPlayerPageRunnable(app, 0)));
        }

        if (listChangesSince < 1837) {
            welcomes.add(new WelcomeButton.WelcomeText("New: REPLAYS! You can watch your and all other players' " +
                    "best performances! Check the score details to see if a replay is available.", null));
        }

        if (listChangesSince < 1836) {
            welcomes.add(new WelcomeButton.WelcomeText("The new Full Retro Marathon type brings you back to the year " +
                    "1989!", new ShowSinglePlayerPageRunnable(app, SinglePlayerScreen.PAGEIDX_MARATHON)));
        }

        if (listChangesSince < 1832) {
            welcomes.add(new WelcomeButton.WelcomeText("Long-awaited, now supported: HOLD pieces in most game modes!",
                    null));
            welcomes.add(new WelcomeButton.WelcomeText("New scoring mechanism: Consecutive line clears grant you " +
                    "bonus score.", null));
        }

        if (listChangesSince < 1830 && touchAvailable)
            welcomes.add(new WelcomeButton.WelcomeText("There is a new option to play with On Screen Controls instead" +
                    " of gestures in landscape and portrait mode.", new ShowSettingsRunnable(app)));

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

    private static class ShowMultiPlayerPageRunnable implements Runnable {
        private final LightBlocksGame app;
        private final int pageidx;

        public ShowMultiPlayerPageRunnable(LightBlocksGame app, int pageIdx) {
            this.app = app;
            this.pageidx = pageIdx;
        }

        @Override
        public void run() {
            MultiplayerMenuScreen sp = app.mainMenuScreen.showMultiplayerScreen();
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
