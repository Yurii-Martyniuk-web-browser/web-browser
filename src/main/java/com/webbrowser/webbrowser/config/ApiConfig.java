package com.webbrowser.webbrowser.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {

    private static final Properties properties = new Properties();

    // Статичний блок ініціалізації завантажує файл при старті програми
    static {
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Вибачте, не вдалося знайти application.properties");
                // Встановлюємо дефолтне значення, якщо файл не знайдено
                properties.setProperty("api.base.url", "http://localhost:8180/rest-service-1.0-SNAPSHOT/api");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getApiUrl() {
        return properties.getProperty("api.base.url");
    }
}