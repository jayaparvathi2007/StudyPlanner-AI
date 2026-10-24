package com.studyplanner.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global application configuration manager for AI API keys and model parameters.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties properties = new Properties();

    private static String runtimeApiKey = null;
    private static String runtimeModel = null;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("ai.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load ai.properties", e);
        }
    }

    public static synchronized String getApiKey() {
        // 1. Runtime override from UI settings
        if (runtimeApiKey != null && !runtimeApiKey.trim().isEmpty()) {
            return runtimeApiKey.trim();
        }

        // 2. Environment variables: GEMINI_API_KEY, AI_API_KEY
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        envKey = System.getenv("AI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        // 3. Properties file
        String propKey = properties.getProperty("ai.api.key", "");
        return propKey != null ? propKey.trim() : "";
    }

    public static synchronized void setApiKey(String apiKey) {
        runtimeApiKey = (apiKey != null) ? apiKey.trim() : "";
    }

    public static synchronized boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    public static synchronized String getModel() {
        if (runtimeModel != null && !runtimeModel.trim().isEmpty()) {
            return runtimeModel.trim();
        }
        String envModel = System.getenv("GEMINI_MODEL");
        if (envModel != null && !envModel.trim().isEmpty()) {
            return envModel.trim();
        }
        return properties.getProperty("ai.model", "gemini-1.5-flash");
    }

    public static synchronized void setModel(String model) {
        runtimeModel = (model != null) ? model.trim() : null;
    }
}
