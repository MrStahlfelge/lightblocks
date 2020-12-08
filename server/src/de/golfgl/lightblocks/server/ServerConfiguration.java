package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Application;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.state.InitGameParameters;

public class ServerConfiguration {
    private final String[] args;
    public int threadNum = 10;
    public int port = 8887;
    public int loglevel = Application.LOG_INFO;
    public int beginningLevel = 0;
    public int modeType = InitGameParameters.TYPE_MIX;
    public String name = "Lighblocks Server";
    public boolean enableNsd;
    public boolean resetEmptyRooms = true;

    public ServerConfiguration(String[] arg) {
        this.args = arg;
        int configPort = findInt("server.port", 0);

        if (configPort > 0) {
            port = configPort;
        } else {
            System.out.println("No port configured, using default port " + port + ". Set a port with --server.port=xxxx");
        }

        int threadNum = findInt("server.threads", 0);
        if (threadNum > 0) {
            this.threadNum = threadNum;
            System.out.println("Using " + this.threadNum + " thread(s).");
        } else {
            System.out.println("Using " + this.threadNum + " threads. Configure with --server.threads=xxxx");
        }

        int loglevel = findInt("verbosity", -1);
        if (loglevel >= 0) {
            this.loglevel = loglevel;
        } else {
            System.out.println("Set log verbosity with --verbosity={0,1,2,3}");
        }

        enableNsd = 0 != findInt("enableNsd", 1);
    }

    protected int findInt(String name, int defaultVal) {
        // first check for system property
        int retVal = defaultVal;
        try {
            retVal = Integer.parseInt(findString(name, String.valueOf(defaultVal)));
        } catch (Throwable ignored) {
        }
        return retVal;
    }

    protected String findString(String name, @Nullable String defaultVal) {
        // first check for system property
        String retVal = defaultVal;
        retVal = System.getProperty(name, defaultVal);

        // and check for command line argument, too
        for (String arg : args) {
            if (arg.startsWith("--" + name + "=")) {
                retVal = arg.substring(name.length() + 3);
            }
        }

        return retVal;
    }
}
