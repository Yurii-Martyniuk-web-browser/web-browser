package com.webbrowser.webbrowser;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.Map;

public class JsCodeViewer {
    private final Map<String, String> scripts;

    public JsCodeViewer(Map<String, String> scripts) {
        this.scripts = scripts;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Loaded JavaScript Source Code");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        StringBuilder content = new StringBuilder();
        if (scripts.isEmpty()) {
            content.append("// No external JavaScript files found on this page.");
        } else {
            scripts.forEach((url, code) -> {
                content.append("/* --- SOURCE: ").append(url).append(" --- */\n\n");
                content.append(code).append("\n\n");
                content.append("/* ------------------------------------------------- */\n\n");
            });
        }

        textArea.setText(content.toString());

        BorderPane root = new BorderPane(textArea);
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
}