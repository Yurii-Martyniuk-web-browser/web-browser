package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.*;
import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.visitor.CssApplierVisitor;
import com.webbrowser.webbrowser.browser.rendering.visitor.DomTraverser;
import com.webbrowser.webbrowser.browser.rendering.visitor.ResourceFetcherVisitor;
import com.webbrowser.webbrowser.network.HttpProcessor;
import com.webbrowser.webbrowser.network.HttpResponse;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class BrowserPageLoader extends PageLoadTemplate {

    private final HttpProcessor httpProcessor;
    private final FxRenderer fxRenderer;
    private final HtmlParser htmlParser;

    private final VBox viewPort;
    private final StringProperty titleProperty;

    private PageContext pageContext;
    private final BaseUrlResolver baseUrlResolver = new BaseUrlResolver();

    private Map<String, String> loadedScripts = new HashMap<>();

    public BrowserPageLoader(VBox viewPort, StringProperty titleProperty) {
        this.viewPort = viewPort;
        this.titleProperty = titleProperty;

        this.httpProcessor = new HttpProcessor();
        this.htmlParser = new HtmlParser();
        this.fxRenderer = new FxRenderer();
    }

    @Override
    protected HttpResponse fetchHttpResponse(String url) {
        this.pageContext = new PageContext();
        this.loadedScripts.clear();

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

        ResourceFetcherVisitor visitor = new ResourceFetcherVisitor(
                httpProcessor,
                baseUrlResolver.getBaseUrl(),
                pageContext
        );

        DomTraverser.traverse(domDocument.getRoot(), visitor);

        visitor.awaitCompletion();

        this.loadedScripts = visitor.getLoadedScripts();
    }

    @Override
    protected void applyStyles(Document domDocument) {
        DomTraverser.traverse(domDocument.getRoot(), new CssApplierVisitor(pageContext));
    }

    @Override
    protected void buildFxNodes(Document domDocument) {
        RenderTreeBuilder renderTreeBuilder = new RenderTreeBuilder(pageContext);

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

    private static class JavaFxUpdater {
        static void update(Runnable action) {
            if (Platform.isFxApplicationThread()) {
                action.run();
            } else {
                Platform.runLater(action);
            }
        }
    }
}