package com.webbrowser.webbrowser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class BrowserApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Custom JavaFX Browser (Coursework Edition)");

        TabPane tabPane = new TabPane();

        createNewTab(tabPane);

        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        tabPane.getTabs().add(addTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == addTab) {
                createNewTab(tabPane);
            }
        });

        Scene scene = new Scene(tabPane, 1024, 768);
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
        launch(args);
    }
}