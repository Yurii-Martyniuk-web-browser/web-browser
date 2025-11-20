package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.network.HttpResponse;


public abstract class PageLoadTemplate {


    public final void loadAndRender(String url) {
        System.out.println("Starting page load for: " + url);

        // 1. Fetch
        HttpResponse response = fetchHttpResponse(url);

        if (response.isClientError() || response.isServerError()) {
            displayError(response);
            return;
        }

        // 2. Parse DOM
        Document domDocument = parseHtml(response.getBody());

        // 3. Fetch Resources (CSS/Images)
        fetchResources(domDocument);

        // 4. Apply Styles (Тепер це частина Build Render Tree або фінального рендерингу)
        applyStyles(domDocument);

        // 5. Build Render Tree & Render FX Nodes
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