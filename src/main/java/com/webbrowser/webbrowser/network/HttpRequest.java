package com.webbrowser.webbrowser.network;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String host;
    private final String path;
    private final Map<String, String> headers;
    private final boolean isSecure;

    private HttpRequest(String method, String host, String path, boolean isSecure) {
        this.method = method;
        this.host = host;
        this.path = path;
        this.headers = new HashMap<>();
        this.headers.put("Host", host);
        this.headers.put("Connection", "close");
        this.headers.put("User-Agent", "SimpleBrowserFX/1.0 (Java)");
        this.isSecure = isSecure;
    }

    public static HttpRequest createGet(String url) throws IllegalArgumentException {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String protocol = urlObj.getProtocol();
            boolean secure = protocol.equalsIgnoreCase("https");

            String host = urlObj.getHost();
            String path = urlObj.getPath().isEmpty() ? "/" : urlObj.getPath();
            if (urlObj.getQuery() != null) {
                path += "?" + urlObj.getQuery();
            }
            return new HttpRequest("GET", host, path, secure);
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public String getHost() { return host; }

    public int getPort() {
        return isSecure ? 443 : 80;
    }

    public boolean isSecure() { return isSecure; }

    public String toRequestString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        sb.append("\r\n");
        return sb.toString();
    }
}