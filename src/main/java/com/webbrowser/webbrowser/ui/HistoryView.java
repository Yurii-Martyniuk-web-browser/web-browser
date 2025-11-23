package com.webbrowser.webbrowser.ui;

import com.webbrowser.webbrowser.dto.HistoryItemDto;
import com.webbrowser.webbrowser.service.RestApiClient;
import com.webbrowser.webbrowser.service.UserSession;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HistoryView {
    private final RestApiClient apiClient = new RestApiClient();

    public void show() {
        if (!UserSession.getInstance().isLoggedIn()) {
            new Alert(Alert.AlertType.WARNING, "Please login first!").show();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Browsing History");

        TableView<HistoryItemDto> table = new TableView<>();

        TableColumn<HistoryItemDto, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<HistoryItemDto, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("visitTime"));

        TableColumn<HistoryItemDto, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<HistoryItemDto, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));

        table.getColumns().addAll(idCol, timeCol, titleCol, urlCol);

        // Кнопка для перегляду снапшоту
        Button viewSnapshotBtn = new Button("View Saved Resources (Snapshot)");
        viewSnapshotBtn.setOnAction(e -> {
            HistoryItemDto selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new SnapshotView().show(selected.id());
            }
        });

        // Завантаження даних
        apiClient.getHistory().thenAccept(list -> Platform.runLater(() ->
                table.getItems().setAll(list)
        ));

        VBox root = new VBox(10, table, viewSnapshotBtn);
        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }
}