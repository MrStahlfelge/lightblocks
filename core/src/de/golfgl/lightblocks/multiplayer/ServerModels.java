package de.golfgl.lightblocks.multiplayer;

import java.util.List;

public class ServerModels {
    public static class ServerInfo {
        // sent on open message
        public int version;
        public String name;
        public String owner;
        public String description;
        public boolean authRequired;
        public List<String> modes;

        // sent on ping pong
        public int activePlayers = -1;
    }
}
