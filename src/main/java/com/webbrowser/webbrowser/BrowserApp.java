package com.webbrowser.webbrowser;

import com.webbrowser.webbrowser.browser.core.BrowserPageLoader;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BrowserApp extends Application {

    private TextField addressBar;
    private VBox viewPort;
    private BrowserPageLoader pageLoader;

    @Override
    public void start(Stage primaryStage) {
        System.out.println(System.getProperty("java.home"));

        primaryStage.setTitle("Simple FX Web Browser");

        viewPort = new VBox(10);
        viewPort.setPadding(new Insets(10));
        viewPort.setStyle("-fx-background-color: white;");

        ScrollPane scrollPane = new ScrollPane(viewPort);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        addressBar = new TextField("https://example.com");
        Button goButton = new Button("Go");

        pageLoader = new BrowserPageLoader(viewPort);

        HBox topBar = new HBox(5, addressBar, goButton);
        HBox.setHgrow(addressBar, javafx.scene.layout.Priority.ALWAYS);
        topBar.setPadding(new Insets(5));
        topBar.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 0 1 0;");

        goButton.setOnAction(e -> loadPageFromAddressBar());
        addressBar.setOnAction(e -> loadPageFromAddressBar());

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadPageFromAddressBar();
    }

    private void loadPageFromAddressBar() {
        String url = addressBar.getText().trim();
        if (url.isEmpty()) {
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            addressBar.setText(url);
        }

        viewPort.getChildren().clear();
        viewPort.getChildren().add(new Label("Loading: " + url + "..."));

        final String finalUrl = url;
        new Thread(() -> {
            pageLoader.loadAndRender(finalUrl);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}