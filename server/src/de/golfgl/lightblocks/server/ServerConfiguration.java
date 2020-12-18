package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.server.model.ServerInfo;
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
    private final Logger logger;

    public ServerConfiguration(String[] arg) {
        this.args = arg;

        int loglevel = findInt("verbosity", -1);
        if (loglevel >= 0) {
            this.loglevel = Math.min(loglevel, Application.LOG_DEBUG);
            String sl4jLevel;
            switch (loglevel) {
                case 0:
                    sl4jLevel = "off";
                    break;
                case 1:
                    sl4jLevel = "error";
                    break;
                case 2:
                    sl4jLevel = "info";
                    break;
                case 3:
                    sl4jLevel = "debug";
                    break;
                default:
                    sl4jLevel = "trace";
                    break;
            }
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, sl4jLevel);
        } else {
            // logger not initialized
            System.out.println("Set log verbosity with --verbosity={0,1,2,3,4}");
        }

        logger = LoggerFactory.getLogger(ServerConfiguration.class);
        int configPort = findInt("server.port", 0);

        if (configPort > 0) {
            port = configPort;
        } else {
            logger.info("No port configured, using default port " + port + ". Set a port with --server.port=xxxx");
        }

        int threadNum = findInt("server.threads", 0);
        if (threadNum > 0) {
            this.threadNum = threadNum;
            logger.info("Using " + this.threadNum + " thread(s).");
        } else {
            logger.info("Using " + this.threadNum + " threads. Configure with --server.threads=xxxx");
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
        String retVal = System.getProperty(name, defaultVal);

        // and check for command line argument, too
        for (String arg : args) {
            if (arg.startsWith("--" + name + "=")) {
                retVal = arg.substring(name.length() + 3);
            }
        }

        return retVal;
    }

    public ServerInfo getServerInfo() {
        FileHandle file = new FileHandle(new File( "server.xml"));

        HeadlessPreferences prefs = new HeadlessPreferences(file);

        ServerInfo serverInfo = new ServerInfo();
        serverInfo.authRequired = false;
        serverInfo.name = prefs.getString("name", "Lightblocks Server");
        serverInfo.owner = prefs.getString("owner", "undefined");
        serverInfo.description = prefs.getString("description", "No server description given.");
        if (!file.exists()) {
            logger.info("You can set server information by editing server.xml file in the current working directory.");
            prefs.putString("name", serverInfo.name);
            prefs.putString("owner", "");
            prefs.putString("description", "");
            prefs.flush();
        }
        serverInfo.version = LightblocksServer.SERVER_VERSION;

        return serverInfo;
    }
}
