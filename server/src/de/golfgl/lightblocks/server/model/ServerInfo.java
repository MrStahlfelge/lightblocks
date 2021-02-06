package de.golfgl.lightblocks.server.model;

import java.util.List;

public class ServerInfo {
    public int version;
    public String name;
    public String owner;
    public String description;
    public boolean authRequired;
    public List<String> modes;
    public boolean privateRooms;
}
