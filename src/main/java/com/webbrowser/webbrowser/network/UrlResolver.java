package com.webbrowser.webbrowser.network;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlResolver {

    public static String resolve(String baseUrl, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) return "";

        if (relativeUrl.startsWith("//")) {
            if (baseUrl.startsWith("https:")) return "https:" + relativeUrl;
            return "http:" + relativeUrl;
        }

        try {
            URI base = new URI(baseUrl);
            return base.resolve(relativeUrl).toString();
        } catch (URISyntaxException | IllegalArgumentException e) {
            System.err.println("URL Resolve Error: " + e.getMessage());
            return relativeUrl; // Повертаємо як є, якщо не вийшло
        }
    }
}