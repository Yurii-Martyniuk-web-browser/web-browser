package com.webbrowser.webbrowser.browser.core;

class BaseUrlResolver {

    private String baseUrl;

    public void resolve(String url) {
        try {
            this.baseUrl = java.net.URI.create(url).toURL().toExternalForm();
        } catch (Exception e) {
            this.baseUrl = url;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
