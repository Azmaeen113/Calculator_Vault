package com.example.calculator_vault_final.controller;

import com.example.calculator_vault_final.model.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HistoryController {
	@FXML private TableView<HistoryEntry> historyTable;
	@FXML private TableColumn<HistoryEntry, String> expressionColumn;
	@FXML private TableColumn<HistoryEntry, String> resultColumn;
	@FXML private TableColumn<HistoryEntry, String> timeColumn;
	@FXML private TableColumn<HistoryEntry, Void> actionColumn;

	private final DatabaseManager db = new DatabaseManager();
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@FXML
	public void initialize() {
		setupTableColumns();
		loadHistory();
	}

	private void setupTableColumns() {
		expressionColumn.setCellValueFactory(new PropertyValueFactory<>("expression"));
		resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
		timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

		// Add delete button to action column
		actionColumn.setCellFactory(param -> new TableCell<>() {
			private final Button deleteBtn = new Button("Delete");

			{
				deleteBtn.setOnAction(event -> {
					HistoryEntry entry = getTableView().getItems().get(getIndex());
					deleteEntry(entry);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(deleteBtn);
				}
			}
		});
	}

	private void loadHistory() {
		try {
			List<String[]> historyData = db.getCalculationHistory();
			ObservableList<HistoryEntry> entries = FXCollections.observableArrayList();

			for (String[] data : historyData) {
				entries.add(new HistoryEntry(
						Integer.parseInt(data[0]),
						data[1],
						data[2],
						data[3]
				));
			}

			historyTable.setItems(entries);
		} catch (SQLException e) {
			showError("Failed to load history: " + e.getMessage());
		}
	}

	private void deleteEntry(HistoryEntry entry) {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Delete Confirmation");
		confirmation.setHeaderText("Delete this calculation?");
		confirmation.setContentText(entry.getExpression() + " = " + entry.getResult());

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				db.deleteCalculation(entry.getId());
				loadHistory();
			} catch (SQLException e) {
				showError("Failed to delete entry: " + e.getMessage());
			}
		}
	}

	@FXML
	public void onClearAll() {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Clear All");
		confirmation.setHeaderText("Clear all calculation history?");
		confirmation.setContentText("This action cannot be undone.");

		Optional<ButtonType> result = confirmation.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				db.clearAllHistory();
				loadHistory();
			} catch (SQLException e) {
				showError("Failed to clear history: " + e.getMessage());
			}
		}
	}

	@FXML
	public void onBack() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/calculator-view.fxml"));
			Parent root = loader.load();
			Stage stage = (Stage) historyTable.getScene().getWindow();
			stage.setTitle("Calculator");
			stage.setScene(new Scene(root, 400, 500));
		} catch (IOException e) {
			showError("Failed to return to calculator");
		}
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	// Inner class to represent a history entry
	public static class HistoryEntry {
		private final int id;
		private final SimpleStringProperty expression;
		private final SimpleStringProperty result;
		private final SimpleStringProperty time;

		public HistoryEntry(int id, String expression, String result, String time) {
			this.id = id;
			this.expression = new SimpleStringProperty(expression);
			this.result = new SimpleStringProperty(result);
			this.time = new SimpleStringProperty(time);
		}

		public int getId() {
			return id;
		}

		public String getExpression() {
			return expression.get();
		}

		public String getResult() {
			return result.get();
		}

		public String getTime() {
			return time.get();
		}
	}
}
