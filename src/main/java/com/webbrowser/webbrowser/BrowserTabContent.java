package com.webbrowser.webbrowser;

import com.webbrowser.webbrowser.browser.core.BrowserPageLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BrowserTabContent {

    private final BorderPane root;
    private final TextField addressBar;
    private final VBox viewPort;
    private final BrowserPageLoader pageLoader;
    private final StringProperty titleProperty = new SimpleStringProperty("New Tab");

    public BrowserTabContent() {
        viewPort = new VBox(10);
        viewPort.setPadding(new Insets(10));
        viewPort.setStyle("-fx-background-color: white;");

        ScrollPane scrollPane = new ScrollPane(viewPort);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        addressBar = new TextField("https://en.wikipedia.org/wiki/Java_(programming_language)");
        Button goButton = new Button("Go");
        Button viewJsButton = new Button("View JS Code"); // Нова вимога

        pageLoader = new BrowserPageLoader(viewPort, titleProperty);

        HBox topBar = new HBox(5, addressBar, goButton, viewJsButton);
        HBox.setHgrow(addressBar, Priority.ALWAYS);
        topBar.setPadding(new Insets(5));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0; -fx-background-color: #f4f4f4;");

        goButton.setOnAction(e -> loadPage());
        addressBar.setOnAction(e -> loadPage());

        // Відкриття вікна з JS кодом
        viewJsButton.setOnAction(e -> {
            JsCodeViewer viewer = new JsCodeViewer(pageLoader.getLoadedScripts());
            viewer.show();
        });

        root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(scrollPane);
    }

    public BorderPane getView() {
        return root;
    }

    public StringProperty titleProperty() {
        return titleProperty;
    }

    private void loadPage() {
        String url = addressBar.getText().trim();
        if (url.isEmpty()) return;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            addressBar.setText(url);
        }

        // Очищаємо попередні скрипти перед новим завантаженням
        pageLoader.clearScripts();

        final String finalUrl = url;
        new Thread(() -> pageLoader.loadAndRender(finalUrl)).start();
    }
}