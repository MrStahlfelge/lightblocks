package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.files.FileHandle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.server.model.ServerInfo;
import de.golfgl.lightblocks.state.InitGameParameters;

public class ServerConfiguration {
    public static final String KEY_XML_SERVER_NAME = "name";
    public static final String KEY_XML_SERVER_OWNER = "owner";
    public static final String KEY_XML_SERVER_DESC = "description";
    public static final String KEY_XML_PRIVATE_ROOMS = "privateRooms";
    public static final String KEY_XML_GAMEMODES = "gamemodes";

    private final String[] args;
    private final Logger logger;
    public int threadNum = 10;
    public int port = 8887;
    public int loglevel = Application.LOG_INFO;
    public int beginningLevel = 0;
    public int modeType;
    public boolean enableNsd;
    public boolean resetEmptyRooms = true;
    public int secondsInactivity = 25;
    public int secondsTimeout = 5;
    private ServerInfo serverInfo;

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
            this.threadNum = Math.max(2, threadNum);
            logger.info("Using " + this.threadNum + " thread(s).");
        } else {
            logger.info("Using " + this.threadNum + " threads. Configure with --server.threads=xxxx");
        }

        enableNsd = 0 != findInt("enableNsd", 1);

        readXml();
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

    private void readXml() {
        FileHandle file = new FileHandle(new File("server.xml"));
        boolean xmlExisted = file.exists();
        HeadlessPreferences prefs = new HeadlessPreferences(file);

        serverInfo = new ServerInfo();
        serverInfo.authRequired = false;
        serverInfo.name = prefs.getString(KEY_XML_SERVER_NAME, "Lightblocks Server");
        serverInfo.owner = prefs.getString(KEY_XML_SERVER_OWNER, "undefined");
        serverInfo.description = prefs.getString(KEY_XML_SERVER_DESC, "No server description given.");
        serverInfo.privateRooms = prefs.getBoolean(KEY_XML_PRIVATE_ROOMS, false);
        serverInfo.version = LightblocksServer.SERVER_VERSION;
        serverInfo.modes = new ArrayList<>();

        modeType = prefs.getInteger(KEY_XML_GAMEMODES, InitGameParameters.TYPE_MIX);

        if (modeType == InitGameParameters.TYPE_MIX)
            serverInfo.modes.add("random");
        if (modeType == InitGameParameters.TYPE_MIX || modeType == InitGameParameters.TYPE_CLASSIC)
            serverInfo.modes.add("classic");
        if (modeType == InitGameParameters.TYPE_MIX || modeType == InitGameParameters.TYPE_MODERN)
            serverInfo.modes.add("modern");

        secondsTimeout = Math.max(secondsTimeout, prefs.getInteger("disconnectTimeoutSeconds", secondsTimeout));
        secondsInactivity = Math.max(secondsInactivity, prefs.getInteger("disconnectInactivitySeconds", secondsInactivity));

        if (!xmlExisted) {
            logger.info("You can set server information by editing server.xml file in the current working directory.");
            prefs.putString(KEY_XML_SERVER_NAME, serverInfo.name);
            prefs.putString(KEY_XML_SERVER_OWNER, serverInfo.owner);
            prefs.putString(KEY_XML_SERVER_DESC, serverInfo.description);
            prefs.putInteger(KEY_XML_GAMEMODES, modeType);
            prefs.putBoolean(KEY_XML_PRIVATE_ROOMS, serverInfo.privateRooms);
            prefs.flush();
        }

    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }
}
