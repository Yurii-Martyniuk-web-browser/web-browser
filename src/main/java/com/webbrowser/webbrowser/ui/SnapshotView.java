package com.webbrowser.webbrowser.ui;

import com.webbrowser.webbrowser.dto.SnapshotResponse;
import com.webbrowser.webbrowser.service.RestApiClient;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;

public class SnapshotView {
    private final RestApiClient apiClient = new RestApiClient();

    public void show(Long historyId) {
        Stage stage = new Stage();
        stage.setTitle("Snapshot Viewer (ID: " + historyId + ")");

        TabPane tabPane = new TabPane();

        SnapshotResponse snapshotSout = apiClient.getSnapshot(historyId).join();
        System.out.println(snapshotSout);

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
            if (snapshot.images() != null) {
                snapshot.images().forEach(image -> addImageTab(tabPane, "IMAGES: " + getFileName(image.url()), image.content()));
            }
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

    private void addImageTab(TabPane pane, String title, byte[] imageBytes) {
        // 1. Створюємо потік з масиву байтів
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

        // 2. Створюємо об'єкт Image
        Image image = new Image(inputStream);

        // 3. Створюємо ImageView для відображення
        ScrollPane scrollPane = getScrollPane(image);

        // 5. Створюємо нову вкладку і додаємо її до панелі
        Tab tab = new Tab(title);
        tab.setContent(scrollPane);

        pane.getTabs().add(tab);
    }

    private static ScrollPane getScrollPane(Image image) {
        ImageView imageView = new ImageView(image);

        // Опціонально: налаштування збереження пропорцій при зміні розміру вікна
        imageView.setPreserveRatio(true);

        // 4. Огортаємо ImageView у ScrollPane, щоб можна було прокручувати великі зображення
        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Опціонально: центрування зображення
        StackPane centerPane = new StackPane(imageView);
        centerPane.setStyle("-fx-background-color: #f4f4f4;"); // Світло-сірий фон
        scrollPane.setContent(centerPane);
        return scrollPane;
    }

    private String getFileName(String url) {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) { return url; }
    }
}