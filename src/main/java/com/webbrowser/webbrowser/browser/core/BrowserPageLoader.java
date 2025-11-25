package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.*;
import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.visitor.CssApplierVisitor;
import com.webbrowser.webbrowser.browser.rendering.visitor.ResourceFetcherVisitor;
import com.webbrowser.webbrowser.network.HttpProcessor;
import com.webbrowser.webbrowser.network.HttpResponse;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserPageLoader extends PageLoadTemplate {

    private final HttpProcessor httpProcessor;
    private final FxRenderer fxRenderer;
    private final HtmlParser htmlParser;
    private final RenderTreeBuilder renderTreeBuilder;

    private final VBox viewPort;
    private final StringProperty titleProperty;

    private final BaseUrlResolver baseUrlResolver = new BaseUrlResolver();
    private final ResourceLoadingService resourceLoader = new ResourceLoadingService();

    private Map<String, String> loadedScripts;

    public BrowserPageLoader(
            VBox viewPort,
            StringProperty titleProperty,
            HttpProcessor httpProcessor,
            HtmlParser htmlParser,
            FxRenderer fxRenderer,
            RenderTreeBuilder renderTreeBuilder
    ) {
        this.viewPort = viewPort;
        this.titleProperty = titleProperty;
        this.httpProcessor = httpProcessor;
        this.htmlParser = htmlParser;
        this.fxRenderer = fxRenderer;
        this.renderTreeBuilder = renderTreeBuilder;
        this.loadedScripts = new HashMap<>();
    }

    public BrowserPageLoader(VBox viewPort, StringProperty titleProperty) {
        this(viewPort, titleProperty,
                new HttpProcessor(),
                new HtmlParser(),
                new FxRenderer(),
                new RenderTreeBuilder());
    }

    @Override
    protected HttpResponse fetchHttpResponse(String url) {
        baseUrlResolver.resolve(url);
        return httpProcessor.loadUrl(url);
    }

    @Override
    protected Document parseHtml(String html) {
        return htmlParser.parse(html);
    }

    @Override
    protected void fetchResources(Document domDocument) {

        JavaFxUpdater.update(() ->
                viewPort.getChildren().addFirst(new Label("Fetching resources..."))
        );

        ResourceFetcherVisitor visitor =
                resourceLoader.collectResources(domDocument, baseUrlResolver.getBaseUrl());

        this.loadedScripts = visitor.getLoadedScripts();
        renderTreeBuilder.setImages(visitor.getLoadedImages());
    }

    @Override
    protected void applyStyles(Document domDocument) {
        DomTraverser.traverse(domDocument.getRoot(), new CssApplierVisitor());
    }

    @Override
    protected void buildFxNodes(Document domDocument) {

        RenderNode renderRoot = renderTreeBuilder.build(domDocument);
        Node fxRoot = fxRenderer.render(renderRoot);

        JavaFxUpdater.update(() -> {
            String title = domDocument.title();
            if (title == null || title.isEmpty()) {
                title = baseUrlResolver.getBaseUrl();
            }
            titleProperty.set(title);

            viewPort.getChildren().clear();
            viewPort.getChildren().add(fxRoot);
        });
    }

    @Override
    protected void displayError(HttpResponse response) {
        JavaFxUpdater.update(() -> {
            viewPort.getChildren().clear();
            viewPort.getChildren().add(new Label("Error: " + response.getStatusCode()));
        });
    }

    public Map<String, String> getLoadedScripts() {
        return loadedScripts;
    }

    public void clearScripts() {
        loadedScripts.clear();
    }
}
