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
    private final RenderTreeBuilder renderTreeBuilder; // <-- ДОДАНО

    private Map<String, String> loadedScripts = new HashMap<>();
    private String currentBaseUrl; // Зберігаємо базовий URL для резолвінгу

    public BrowserPageLoader(VBox viewPort, StringProperty titleProperty) {
        this.httpProcessor = new HttpProcessor();
        this.htmlParser = new HtmlParser();
        this.fxRenderer = new FxRenderer();
        this.renderTreeBuilder = new RenderTreeBuilder(); // <-- Ініціалізація
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
            // Встановлюємо базовий URL для резолвінгу ресурсів
            this.currentBaseUrl = new URL(url).toExternalForm();
            // ... (обробка HTTP) ...
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
        // !!! ВИПРАВЛЕНО: Заміна fxRenderer.traverse на прямий обхід DOM !!!
        Platform.runLater(() -> viewPort.getChildren().addFirst(new Label("Fetching resources...")));

        ResourceFetcherVisitor resourceVisitor = new ResourceFetcherVisitor(httpProcessor, currentBaseUrl);

        // Ми припускаємо, що обхідник DOM (з патерну Visitor) був перенесений
        // до окремого статичного класу або методу.
        // Тут ми викликаємо статичний обхідник, передаючи йому root.
        DomTraverser.traverse(domDocument.getRoot(), resourceVisitor); // <-- ВИКЛИК ПЕРЕНЕСЕНОГО ОБХІДНИКА

        this.loadedScripts.putAll(resourceVisitor.getLoadedScripts());
    }

    @Override
    protected void applyStyles(Document domDocument) {
        // !!! ВИПРАВЛЕНО: Заміна fxRenderer.traverse на прямий обхід DOM !!!

        // Викликаємо обхідник для застосування стилів до DOM-дерева
        DomTraverser.traverse(domDocument.getRoot(), new CssApplierVisitor());
    }

    @Override
    protected void buildFxNodes(Document domDocument) {
        // !!! НОВА АРХІТЕКТУРА: BUILD RENDER TREE -> RENDER FX !!!

        // 1. Побудова Render Tree (включає стилі)
        RenderNode renderRoot = renderTreeBuilder.build(domDocument);

        // 2. Render FX Nodes
        javafx.scene.Node fxRoot = fxRenderer.render(renderRoot);

        Platform.runLater(() -> {
            // Оновлюємо заголовок
            if (!domDocument.title().isEmpty()) {
                titleProperty.set(domDocument.title());
            } else {
                titleProperty.set(currentBaseUrl);
            }

            viewPort.getChildren().clear();
            viewPort.getChildren().add(fxRoot); // Додаємо кореневий вузол рендера
        });
    }

    @Override
    protected void displayError(HttpResponse response) {
        // (Без змін, код з минулого прикладу)
        Platform.runLater(() -> {
            viewPort.getChildren().clear();
            viewPort.getChildren().add(new Label("Error: " + response.getStatusCode()));
        });
    }
}