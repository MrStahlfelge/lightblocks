package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.SinglePlayerScreen;
import de.golfgl.lightblocks.menu.WelcomeButton;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;

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

        // Unter 50 Reihen und tutorial verfügbar und nicht gemacht: anbieten
        if (app.savegame.getTotalScore().getClearedLines() < 100 && TutorialModel.tutorialAvailable() &&
                app.savegame.getBestScore(Mission.KEY_TUTORIAL).getRating() == 0)
            welcomes.add(new WelcomeButton.WelcomeText("Welcome, new user! Play the tutorial to learn about " +
                    "Lightblock's gesture controls.", new Runnable() {
                @Override
                public void run() {
                    SinglePlayerScreen sp = app.mainMenuScreen.showSinglePlayerScreen();
                    sp.showMissionPage();
                }
            }));

        //welcomes.add(new WelcomeButton.WelcomeText("Have a good morning", null));
        //welcomes.add(new WelcomeButton.WelcomeText("Have a\ngood day", null));

        return welcomes;
    }
}
