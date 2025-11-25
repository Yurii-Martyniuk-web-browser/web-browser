package com.webbrowser.webbrowser.ui;

import com.webbrowser.webbrowser.dto.HistoryItemDto;
import com.webbrowser.webbrowser.service.RestApiClient;
import com.webbrowser.webbrowser.service.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class HistoryView {
    private final RestApiClient apiClient = new RestApiClient();

    public void show() {
        if (UserSession.getInstance().isLoggedIn()) {
            new Alert(Alert.AlertType.WARNING, "Please login first!").show();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Browsing History");

        TableView<HistoryItemDto> table = new TableView<>();

        TableColumn<HistoryItemDto, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().id()))
        );

        TableColumn<HistoryItemDto, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().visitTime())
        );

        TableColumn<HistoryItemDto, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().title())
        );

        TableColumn<HistoryItemDto, String> urlCol = new TableColumn<>("URL");
        urlCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().url())
        );

        table.getColumns().addAll(List.of(idCol, timeCol, titleCol, urlCol));

        Button viewSnapshotBtn = new Button("View Saved Resources (Snapshot)");
        viewSnapshotBtn.setOnAction(e -> {
            HistoryItemDto selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                new SnapshotView().show(selected.id());
            }
        });

        apiClient.getHistory().thenAccept(list -> Platform.runLater(() ->
                table.getItems().setAll(list)
        ));

        List<?> data = apiClient.getHistory().join();
        System.out.println(data);

        VBox root = new VBox(10, table, viewSnapshotBtn);
        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }
}