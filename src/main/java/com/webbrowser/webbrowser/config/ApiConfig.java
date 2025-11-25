package com.webbrowser.webbrowser.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiConfig {

    private static final Logger log = Logger.getLogger(ApiConfig.class.getName());
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.warning("Application properties file not found");
                properties.setProperty("api.base.url", "http://localhost:8180/rest-service-1.0-SNAPSHOT/api");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            log.log(Level.WARNING, "Error loading properties file: " + ex.getMessage(), ex);
        }
    }

    public static String getApiUrl() {
        return properties.getProperty("api.base.url");
    }
}