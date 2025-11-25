package com.webbrowser.webbrowser.network;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class UrlResolver {

    private static final Logger log = Logger.getLogger(UrlResolver.class.getName());

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
            log.info("sdsd");
            return relativeUrl;
        }
    }
}