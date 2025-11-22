package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.*;
import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.visitor.*;
import com.webbrowser.webbrowser.network.HttpProcessor;
import com.webbrowser.webbrowser.network.HttpResponse;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrowserPageLoader extends PageLoadTemplate {

    private final HttpProcessor httpProcessor;
    private final FxRenderer fxRenderer;
    private final HtmlParser htmlParser;
    private final VBox viewPort;
    private final StringProperty titleProperty;
    private final RenderTreeBuilder renderTreeBuilder;

    private final Map<String, String> loadedScripts = new HashMap<>();
    private String currentBaseUrl;

    public BrowserPageLoader(VBox viewPort, StringProperty titleProperty) {
        this.httpProcessor = new HttpProcessor();
        this.htmlParser = new HtmlParser();
        this.fxRenderer = new FxRenderer();
        this.renderTreeBuilder = new RenderTreeBuilder();
        this.viewPort = viewPort;
        this.titleProperty = titleProperty;
    }

    public void clearScripts() {
        loadedScripts.clear();
    }

    public Map<String, String> getLoadedScripts() {
        return loadedScripts;
    }

    @Override
    protected HttpResponse fetchHttpResponse(String url) {
        try {
            this.currentBaseUrl = new URL(url).toExternalForm();
        } catch (java.net.MalformedURLException e) {
            System.err.println("Invalid URL format for base URL: " + url);
        }
        return httpProcessor.loadUrl(url);
    }

    @Override
    protected Document parseHtml(String htmlContent) {
        return htmlParser.parse(htmlContent);
    }

    @Override
    protected void fetchResources(Document domDocument) {
        Platform.runLater(() -> viewPort.getChildren().addFirst(new Label("Fetching resources...")));

        ResourceFetcherVisitor resourceVisitor = new ResourceFetcherVisitor(httpProcessor, currentBaseUrl);

        DomTraverser.traverse(domDocument.getRoot(), resourceVisitor);

        loadedScripts.forEach((scriptName, script) -> {
            System.out.println("Script name" + scriptName);
            System.out.println("Script" + script);
        });

        this.loadedScripts.putAll(resourceVisitor.getLoadedScripts());
    }

    @Override
    protected void applyStyles(Document domDocument) {
        DomTraverser.traverse(domDocument.getRoot(), new CssApplierVisitor());
    }

    @Override
    protected void buildFxNodes(Document domDocument) {
        RenderNode renderRoot = renderTreeBuilder.build(domDocument);

        javafx.scene.Node fxRoot = fxRenderer.render(renderRoot);

        Platform.runLater(() -> {
            if (!domDocument.title().isEmpty()) {
                titleProperty.set(domDocument.title());
            } else {
                titleProperty.set(currentBaseUrl);
            }

            viewPort.getChildren().clear();
            viewPort.getChildren().add(fxRoot);
        });
    }

    @Override
    protected void displayError(HttpResponse response) {
        Platform.runLater(() -> {
            viewPort.getChildren().clear();
            viewPort.getChildren().add(new Label("Error: " + response.getStatusCode()));
        });
    }
}