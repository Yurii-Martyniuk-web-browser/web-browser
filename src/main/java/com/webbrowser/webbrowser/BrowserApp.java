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

        // Створюємо першу вкладку за замовчуванням
        createNewTab(tabPane);

        // Кнопка для додавання нових вкладок (проста реалізація через меню або хоткей,
        // тут для простоти додамо пусту вкладку, яка слугує кнопкою "+")
        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        tabPane.getTabs().add(addTab);

        // Логіка перемикання (якщо вибрали "+", створюємо нову вкладку)
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

        // Вставляємо перед кнопкою "+"
        int size = tabPane.getTabs().size();
        int index = size > 0 ? size - 1 : 0;

        tabPane.getTabs().add(index, tab);
        tabPane.getSelectionModel().select(tab);

        // Слухач зміни заголовка сторінки
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