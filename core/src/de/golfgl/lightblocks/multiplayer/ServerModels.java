package de.golfgl.lightblocks.multiplayer;

public class ServerModels {
    public static class ServerInfo {
        // sent on open message
        public int version;
        public String name;
        public String owner;
        public String description;
        public boolean authRequired;

        // sent on ping pong
        public int activePlayers = -1;
    }
}
