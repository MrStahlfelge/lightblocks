package de.golfgl.lightblocks.state;

import com.badlogic.gdx.math.MathUtils;

import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;

/**
 * This class represents the player
 *
 * Created by Benjamin Schulte on 26.02.2017.
 */

public class Player {

    public String getName() {
        return Integer.toString(MathUtils.random(100, 999));
    }

    public MultiPlayerObjects.Player toKryoPlayer() {
        MultiPlayerObjects.Player kp = new MultiPlayerObjects.Player();
        kp.name = getName();

        return kp;
    }
}
