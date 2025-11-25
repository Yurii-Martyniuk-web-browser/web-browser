package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.network.HttpResponse;

import java.util.logging.Logger;


public abstract class PageLoadTemplate {

    private static final Logger log = Logger.getLogger(PageLoadTemplate.class.getName());

    public final void loadAndRender(String url) {
        log.info("Loading " + url);

        HttpResponse response = fetchHttpResponse(url);

        if (response.isClientError() || response.isServerError()) {
            displayError(response);
            return;
        }

        Document domDocument = parseHtml(response.getBodyString());

        fetchResources(domDocument);

        applyStyles(domDocument);

        buildFxNodes(domDocument);

        log.info("Done loading " + url);
    }

    protected abstract HttpResponse fetchHttpResponse(String url);
    protected abstract Document parseHtml(String htmlContent);
    protected abstract void fetchResources(Document domDocument);
    protected abstract void applyStyles(Document domDocument);
    protected abstract void buildFxNodes(Document domDocument);
    protected abstract void displayError(HttpResponse response);
}