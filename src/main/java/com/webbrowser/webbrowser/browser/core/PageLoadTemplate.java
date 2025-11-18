package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.network.HttpResponse;
import org.jsoup.nodes.Document;


public abstract class PageLoadTemplate {


    public final void loadAndRender(String url) {
        System.out.println("Starting page load for: " + url);

        HttpResponse response = fetchHttpResponse(url);

        if (response.isClientError() || response.isServerError()) {
            displayError(response);
            return;
        }

        Document domDocument = parseHtml(response.getBody());

        fetchResources(domDocument);

        applyStyles(domDocument);

        buildFxNodes(domDocument);

        System.out.println("Page rendering finished.");
    }

    protected abstract HttpResponse fetchHttpResponse(String url);

    protected abstract Document parseHtml(String htmlContent);

    protected abstract void fetchResources(Document domDocument);

    protected abstract void applyStyles(Document domDocument);

    protected abstract void buildFxNodes(Document domDocument);

    protected abstract void displayError(HttpResponse response);
}