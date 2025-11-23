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

public class BrowserApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Custom JavaFX Browser (No WebView)");

        // 1. Панель вкладок
        TabPane tabPane = new TabPane();

        // Створюємо першу вкладку
        createNewTab(tabPane);

        // 2. Вкладка-кнопка "+" для додавання нових
        Tab addTab = new Tab("+");
        addTab.setClosable(false); // Її не можна закрити
        tabPane.getTabs().add(addTab);

        // Логіка перемикання: якщо натиснули "+", створюємо нову вкладку і вибираємо її
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == addTab) {
                createNewTab(tabPane);
            }
        });

        // 3. Верхня панель інструментів (Toolbar)
        Button loginBtn = new Button("Login / Register");
        loginBtn.setOnAction(e -> new AuthView().show(primaryStage));

        Button historyBtn = new Button("History");
        historyBtn.setOnAction(e -> new HistoryView().show());

        HBox toolbar = new HBox(10, loginBtn, historyBtn);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #b0b0b0; -fx-border-width: 0 0 1 0;");

        // 4. Головний контейнер
        VBox root = new VBox(toolbar, tabPane);

        // ВАЖЛИВО: Кажемо TabPane займати весь доступний простір по вертикалі.
        // Оскільки всередині TabPane лежить ваш кастомний VBox (viewPort),
        // це дозволить вашому рендереру займати весь екран.
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createNewTab(TabPane tabPane) {
        // Тут ми використовуємо ВАШ клас BrowserTabContent,
        // який містить кастомний рендеринг (VBox замість WebView)
        BrowserTabContent tabContent = new BrowserTabContent();

        Tab tab = new Tab("New Tab");
        tab.setContent(tabContent.getView()); // getView() повертає BorderPane з вашим VBox всередині

        // Вставляємо нову вкладку ПЕРЕД вкладкою "+"
        int size = tabPane.getTabs().size();
        int index = size > 0 ? size - 1 : 0;

        tabPane.getTabs().add(index, tab);
        tabPane.getSelectionModel().select(tab);

        // Слухаємо зміну заголовка (Title) вашого кастомного Document
        // і оновлюємо назву вкладки
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