package com.example.calculator_vault_final.controller;

import com.example.calculator_vault_final.model.DatabaseManager;
import com.example.calculator_vault_final.model.VaultFile;
import com.example.calculator_vault_final.service.FileVaultService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class VaultController {
	@FXML private TableView<VaultFile> table;
	@FXML private TableColumn<VaultFile, String> colName;
	@FXML private TableColumn<VaultFile, String> colType;
	@FXML private TableColumn<VaultFile, String> colSize;
	@FXML private TableColumn<VaultFile, String> colDate;
	@FXML private Button btnUpload, btnOpen, btnDownload, btnDelete, btnChangePin, btnLock;

	private final DatabaseManager db = new DatabaseManager();
	private final FileVaultService vaultService = new FileVaultService(db);
	private final ObservableList<VaultFile> items = FXCollections.observableArrayList();
	private String currentPin;

	public void setCurrentPin(String pin) { this.currentPin = pin; }

	@FXML
	public void initialize() {
		if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("originalExtension"));
		if (colSize != null) colSize.setCellValueFactory(cell -> {
			long size = cell.getValue().getFileSize();
			return new javafx.beans.property.SimpleStringProperty(FileVaultService.formatFileSize(size));
		});
		if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("uploadedAt"));
		if (table != null) table.setItems(items);
		refreshTable();
		updateButtonStates();
		if (table != null) table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateButtonStates());
	}

	private void updateButtonStates() {
		boolean hasSel = table != null && table.getSelectionModel().getSelectedItem() != null;
		if (btnOpen != null) btnOpen.setDisable(!hasSel);
		if (btnDownload != null) btnDownload.setDisable(!hasSel);
		if (btnDelete != null) btnDelete.setDisable(!hasSel);
	}

	private void refreshTable() {
		try {
			items.setAll(db.getAllFiles());
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "Failed to load files").showAndWait();
		}
	}

	@FXML
	public void onUpload() {
		FileChooser fc = new FileChooser();
		File file = fc.showOpenDialog(getStage());
		if (file == null) return;
		try {
			vaultService.saveFileToVault(file, currentPin);
			refreshTable();
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Upload failed: " + e.getMessage()).showAndWait();
		}
	}

	@FXML
	public void onOpenSelected() {
		VaultFile sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) return;
		try {
			byte[] data = vaultService.retrieveFileFromVault(sel.getId(), currentPin);
			File temp = vaultService.createTempFile(sel, data);
			FileVaultService.openFileWithDesktop(temp);
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Open failed: " + e.getMessage()).showAndWait();
		}
	}

	@FXML
	public void onDownloadSelected() {
		VaultFile sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) return;
		try {
			byte[] data = vaultService.retrieveFileFromVault(sel.getId(), currentPin);
			FileChooser fc = new FileChooser();
			fc.setInitialFileName(sel.getFileName());
			File dest = fc.showSaveDialog(getStage());
			if (dest == null) return;
			java.nio.file.Files.write(dest.toPath(), data);
			new Alert(Alert.AlertType.INFORMATION, "Saved to: " + dest.getAbsolutePath()).showAndWait();
		} catch (Exception e) {
			new Alert(Alert.AlertType.ERROR, "Download failed: " + e.getMessage()).showAndWait();
		}
	}

	@FXML
	public void onDeleteSelected() {
		VaultFile sel = table.getSelectionModel().getSelectedItem();
		if (sel == null) return;
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected file?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> res = confirm.showAndWait();
		if (res.isPresent() && res.get() == ButtonType.OK) {
			try {
				db.deleteFile(sel.getId());
				refreshTable();
			} catch (SQLException e) {
				new Alert(Alert.AlertType.ERROR, "Delete failed").showAndWait();
			}
		}
	}

	@FXML
	public void onChangePin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/pin-setup.fxml"));
			Parent root = loader.load();
			PINSetupController ctrl = loader.getController();
			ctrl.initForChangePin(currentPin);
			Stage dlg = new Stage();
			dlg.setTitle("Change PIN");
			dlg.setScene(new Scene(root));
			dlg.initOwner(getStage());
			dlg.setResizable(false);
			dlg.showAndWait();
			// If changed, controller updates DB and re-encrypts; we refresh table and update currentPin
			String newPin = ctrl.getResultingPin();
			if (newPin != null) {
				this.currentPin = newPin;
				refreshTable();
			}
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "Failed to open PIN dialog").showAndWait();
		}
	}

	@FXML
	public void onLockVault() {
		FileVaultService.deleteTempFiles();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/calculator-view.fxml"));
			Parent root = loader.load();
			Stage stage = getStage();
			stage.setTitle("Calculator");
			stage.setScene(new Scene(root, 400, 600));
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "Failed to return to calculator").showAndWait();
		}
	}

	private Stage getStage() { return (Stage) table.getScene().getWindow(); }
}
