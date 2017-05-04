package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Type B: Vorgegebene Garbage wegcleanen
 * <p>
 * Created by Benjamin Schulte on 22.04.2017.
 */

public class CleanModel extends MissionModel {
    // je 25 übige Blöcke den Bonusscore mit einem Faktor mehr multiplizieren
    final static int BONUS_LEVELUP_NUM = 25;
    final static int BONUS_PER_BLOCK = 100;

    @Override
    public int getRating() {

        final GameScore score = getScore();
        int factorForBonus = getScore().getCurrentLevel() + 1;
        int blocksLeft = getMaxBlocksToUse() - score.getDrawnTetrominos();

        int bonusScore = 0;

        while (blocksLeft > BONUS_LEVELUP_NUM) {
            bonusScore += BONUS_LEVELUP_NUM * BONUS_PER_BLOCK * (factorForBonus);
            factorForBonus++;
            blocksLeft -= BONUS_LEVELUP_NUM;
        }
        bonusScore += blocksLeft * BONUS_PER_BLOCK * (factorForBonus);

        addBonusScore(bonusScore);

        return super.getRating();
    }

    @Override
    public String getMissionModelGoalDescription() {
        return "goalClean";
    }

    @Override
    public String[] getGoalParams() {
        return new String[]{String.valueOf(getMaxBlocksToUse())};
    }

    @Override
    protected void activeTetrominoDropped() {
        if (getScore().getClearedLines() > 0 && !getGameboard().hasGarbage())
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        // Der Wert darf nicht wieder gespeichert werden, um die Garbage nicht bei jedem Laden
        // wieder einzufügen
        int insertGarbage = jsonData.getInt("initialGarbage", 0);
        getGameboard().initGarbage(insertGarbage);
    }
}
