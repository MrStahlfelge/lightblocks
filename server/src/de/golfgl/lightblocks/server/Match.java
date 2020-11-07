package de.golfgl.lightblocks.server;

public class Match {
    private Player player1;
    private Player player2;

    public Match() {
    }

    public void update(float delta) {
        if (getConnectedPlayerNum() == 0) {
            return;
        }

        // update game model
    }

    public boolean connectPlayer(Player player) {
        synchronized (this) {
            if (player1 == null) {
                player1 = player;
                return true;
            }
            if (player2 == null) {
                player2 = player;
                return true;
            }
            return false;
        }
    }

    public void playerDisconnected(Player player) {
        synchronized (this) {
            // TODO
        }
    }

    public int getConnectedPlayerNum() {
        return (player1 != null ? 1 : 0) + (player2 != null ? 1 : 0);
    }
}
