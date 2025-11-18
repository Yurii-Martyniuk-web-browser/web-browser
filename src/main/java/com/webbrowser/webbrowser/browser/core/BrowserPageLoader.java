package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.FxRenderer;
import com.webbrowser.webbrowser.browser.rendering.HtmlParser;
import com.webbrowser.webbrowser.network.HttpProcessor;
import com.webbrowser.webbrowser.network.HttpResponse;
import com.webbrowser.webbrowser.browser.rendering.visitor.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jsoup.nodes.Document;


public class BrowserPageLoader extends PageLoadTemplate {

    private final HttpProcessor httpProcessor;
    private final FxRenderer fxRenderer;
    private final HtmlParser htmlParser;
    private final VBox viewPort;

    public BrowserPageLoader(VBox viewPort) {
        this.httpProcessor = new HttpProcessor();
        this.htmlParser = new HtmlParser();
        this.fxRenderer = new FxRenderer();
        this.viewPort = viewPort;
    }

    @Override
    protected HttpResponse fetchHttpResponse(String url) {
        return httpProcessor.loadUrl(url);
    }

    @Override
    protected Document parseHtml(String htmlContent) {
        return htmlParser.parse(htmlContent);
    }

    @Override
    protected void fetchResources(Document domDocument) {
        System.out.println("Step 3: Fetching external resources (CSS, images)...");
        fxRenderer.traverse(domDocument, new ResourceFetcherVisitor());
    }

    @Override
    protected void applyStyles(Document domDocument) {
        System.out.println("Step 4: Applying styles to DOM...");
        fxRenderer.traverse(domDocument, new CssApplierVisitor());
    }

    @Override
    protected void buildFxNodes(Document domDocument) {
        System.out.println("Step 5: Building JavaFX nodes...");

        Platform.runLater(() -> {
            viewPort.getChildren().clear();

            FxNodeBuilderVisitor fxBuilder = new FxNodeBuilderVisitor(viewPort);
            fxRenderer.traverse(domDocument, fxBuilder);

            viewPort.getChildren().addFirst(new Label("--- DOCUMENT TITLE: " + domDocument.title() + " ---"));
        });
    }

    @Override
    protected void displayError(HttpResponse response) {
        System.err.println("Load Error: " + response.getStatusCode());

        Document errorDom = htmlParser.parse(response.getBody());

        Platform.runLater(() -> {
            viewPort.getChildren().clear();

            viewPort.getChildren().add(new Label("--- HTTP Load Error ---"));
            viewPort.getChildren().add(new Label("Status: " + response.getStatusCode() + " " + response.getStatusText()));

            FxNodeBuilderVisitor fxBuilder = new FxNodeBuilderVisitor(viewPort);
            fxRenderer.traverse(errorDom, fxBuilder);
        });
    }
}