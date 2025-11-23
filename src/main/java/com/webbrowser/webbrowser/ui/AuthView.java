package com.webbrowser.webbrowser.ui;

import com.webbrowser.webbrowser.service.RestApiClient;
import com.webbrowser.webbrowser.service.UserSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AuthView {
    private final RestApiClient apiClient = new RestApiClient();

    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Account Access");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            statusLabel.setText("Logging in...");
            apiClient.login(emailField.getText(), passField.getText())
                    .thenAccept(resp -> Platform.runLater(() -> {
                        if (resp.success()) {
                            UserSession.getInstance().login(resp.userId(), emailField.getText());
                            stage.close();
                        } else {
                            statusLabel.setText("Login failed: " + resp.message());
                        }
                    }));
        });

        registerBtn.setOnAction(e -> {
            statusLabel.setText("Registering...");
            apiClient.register(emailField.getText(), passField.getText())
                    .thenAccept(resp -> Platform.runLater(() -> {
                        if (resp.success()) {
                            statusLabel.setText("Registered! Now login.");
                            statusLabel.setStyle("-fx-text-fill: green;");
                        } else {
                            statusLabel.setText("Registration failed: " + resp.message());
                            statusLabel.setStyle("-fx-text-fill: red;");
                        }
                    }));
        });

        VBox root = new VBox(10, new Label("Browser Account"), emailField, passField, loginBtn, registerBtn, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }
}