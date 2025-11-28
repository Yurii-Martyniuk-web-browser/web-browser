package com.webbrowser.webbrowser;

import com.webbrowser.webbrowser.ui.AuthView;
import com.webbrowser.webbrowser.ui.BrowserTabContent;
import com.webbrowser.webbrowser.ui.HistoryView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

public class BrowserApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Custom JavaFX Browser (No WebView)");

        TabPane tabPane = new TabPane();

        createNewTab(tabPane);

        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        tabPane.getTabs().add(addTab);

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal == addTab) {
                        createNewTab(tabPane);
                    }
                });

        Button loginBtn = new Button("Login / Register");
        loginBtn.setOnAction(e -> new AuthView().show(primaryStage));

        Button historyBtn = new Button("History");
        historyBtn.setOnAction(e -> new HistoryView().show());

        HBox toolbar = new HBox(10, loginBtn, historyBtn);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #b0b0b0; -fx-border-width: 0 0 1 0;");

        VBox root = new VBox(toolbar, tabPane);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createNewTab(TabPane tabPane) {
        BrowserTabContent tabContent = new BrowserTabContent();

        Tab tab = new Tab("New Tab");
        tab.setContent(tabContent.getView());

        int size = tabPane.getTabs().size();
        int index = size > 0 ? size - 1 : 0;

        tabPane.getTabs().add(index, tab);
        tabPane.getSelectionModel().select(tab);

        tabContent.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                tab.setText(newTitle);
            }
        });
    }

    public static void main(String[] args) {
        try {
            Application.launch(BrowserApp.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                new FileWriter("error.log").append(e.toString()).close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}