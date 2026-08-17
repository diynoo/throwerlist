package com.dinob.throwerlist.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ThrowerListConfig {
    private static final String ENABLED_PROP = "enabled";
    private static final String KICK_MSG_PROP = "kickMessage";
    private static final String DELAY_PROP = "kickDelayTicks";
    private static final String AUTO_REFRESH_PROP = "autoRefreshRemote";
    private static final String REMOTE_URL_PROP = "remoteUrl";
    private static final String DEBUG_PROP = "debug";

    public static boolean enabled = true;
    public static String kickMessage = "autokicked <name>";
    public static int kickDelayTicks = 20;
    public static boolean autoRefreshRemote = true;
    public static boolean debug = false;
    public static final String DEFAULT_REMOTE_URL = "https://raw.githubusercontent.com/diynoo/throwerlist/main/uuids.txt";
    public static String remoteUrl = DEFAULT_REMOTE_URL;

    private static Path configFile;

    public static void load() {
        configFile = getConfigDir().resolve("throwerlist.properties");
        if (!Files.exists(configFile)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(configFile)) {
            Properties props = new Properties();
            props.load(reader);
            enabled = Boolean.parseBoolean(props.getProperty(ENABLED_PROP, "true"));
            kickMessage = props.getProperty(KICK_MSG_PROP, "autokicked <name>");
            kickDelayTicks = Integer.parseInt(props.getProperty(DELAY_PROP, "20"));
            autoRefreshRemote = Boolean.parseBoolean(props.getProperty(AUTO_REFRESH_PROP, "true"));
            remoteUrl = props.getProperty(REMOTE_URL_PROP, DEFAULT_REMOTE_URL);
            debug = Boolean.parseBoolean(props.getProperty(DEBUG_PROP, "false"));
            if (remoteUrl.contains("YOUR_USERNAME") || remoteUrl.contains("YOUR_")) {
                remoteUrl = DEFAULT_REMOTE_URL;
            }
        } catch (IOException ignored) {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configFile.getParent());
            Properties props = new Properties();
            props.setProperty(ENABLED_PROP, Boolean.toString(enabled));
            props.setProperty(KICK_MSG_PROP, kickMessage);
            props.setProperty(DELAY_PROP, Integer.toString(kickDelayTicks));
            props.setProperty(AUTO_REFRESH_PROP, Boolean.toString(autoRefreshRemote));
            props.setProperty(REMOTE_URL_PROP, remoteUrl);
            props.setProperty(DEBUG_PROP, Boolean.toString(debug));
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                props.store(writer, "ThrowerList Configuration");
            }
        } catch (IOException ignored) {}
    }

    public static Path getConfigDir() {
        return Path.of(System.getProperty("user.dir")).resolve("config");
    }
}