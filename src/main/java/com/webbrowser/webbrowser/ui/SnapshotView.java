package com.webbrowser.webbrowser.ui;

import com.webbrowser.webbrowser.service.RestApiClient;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SnapshotView {
    private final RestApiClient apiClient = new RestApiClient();

    public void show(Long historyId) {
        Stage stage = new Stage();
        stage.setTitle("Snapshot Viewer (ID: " + historyId + ")");

        TabPane tabPane = new TabPane();

        apiClient.getSnapshot(historyId).thenAccept(snapshot -> Platform.runLater(() -> {
            if (snapshot == null) {
                stage.setTitle("Error: Snapshot not found");
                return;
            }

            // 1. HTML Tab
            addTab(tabPane, "HTML", snapshot.mainHtml());

            // 2. CSS Tabs
            if (snapshot.css() != null) {
                snapshot.css().forEach(css -> addTab(tabPane, "CSS: " + getFileName(css.url()), css.content()));
            }

            // 3. JS Tabs
            if (snapshot.js() != null) {
                snapshot.js().forEach(js -> addTab(tabPane, "JS: " + getFileName(js.url()), js.content()));
            }

            // (Можна додати вкладку для картинок, якщо потрібно)
        }));

        StackPane root = new StackPane(tabPane);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void addTab(TabPane pane, String title, String content) {
        TextArea area = new TextArea(content);
        area.setEditable(false);
        Tab tab = new Tab(title, area);
        pane.getTabs().add(tab);
    }

    private String getFileName(String url) {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) { return url; }
    }
}