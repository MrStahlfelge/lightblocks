package de.golfgl.lightblocks.state;

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

    public static Array<WelcomeButton.WelcomeText> fillWelcomes(final LightBlocksGame app) {
        Array<WelcomeButton.WelcomeText> welcomes = new Array<WelcomeButton.WelcomeText>();

        int lastUsedVersion = app.localPrefs.getLastUsedLbVersion();
        long clearedLines = app.savegame.getTotalScore().getClearedLines();
        int daysSinceLastStart = app.localPrefs.getDaysSinceLastStart();

        // bis einschl 1818 wurde Version nicht gespeichert
        if (lastUsedVersion == 0 && clearedLines > 10)
            lastUsedVersion = 1818;

        // ganz neuer User
        if (lastUsedVersion == 0) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeNew"), null));
        } else if (lastUsedVersion < LightBlocksGame.GAME_VERSIONNUMBER || LightBlocksGame.GAME_DEVMODE) {
            // Update-Hinweise
            listNewFeatures(welcomes, app, lastUsedVersion);
        }

        // Unter 50 Reihen und tutorial verfügbar und nicht gemacht: anbieten
        if (clearedLines < 100 && TutorialModel.tutorialAvailable() &&
                app.savegame.getBestScore(Mission.KEY_TUTORIAL).getRating() == 0)
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeTutorial"), new Runnable() {
                @Override
                public void run() {
                    SinglePlayerScreen sp = app.mainMenuScreen.showSinglePlayerScreen();
                    sp.showMissionPage();
                }
            }));

        // noch kein Text da? Dann eventuell allgemeine Begrüßung
        if (welcomes.size == 0 && daysSinceLastStart >= 5) {
            welcomes.add(new WelcomeButton.WelcomeText(app.TEXTS.get("welcomeAfterSomeTime"), null));
        }

        //welcomes.add(new WelcomeButton.WelcomeText("Have a good morning", null));
        //welcomes.add(new WelcomeButton.WelcomeText("Have a\ngood day", null));

        return welcomes;
    }

    protected static void listNewFeatures(Array<WelcomeButton.WelcomeText> welcomes,
                                          LightBlocksGame app, int listChangesSince) {
        // Neue Features in 1819: Shades of Grey, Hard Drop
        if (listChangesSince < 1819) {
            welcomes.add(new WelcomeButton.WelcomeText("Lightblocks can now perform hard drops. " +
                    (PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN) ?
                            "For gesture input, enable it on swipe up in the settings." : "Use the UP button or key.")
                    + "\nThere's also a new option for block shadings.", new ShowSettingsRunnable(app)));
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
}
