package com.example.calculator_vault_final.controller;

import com.example.calculator_vault_final.model.ConfigManager;
import com.example.calculator_vault_final.model.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PINSetupController {
	@FXML private PasswordField txtPin;
	@FXML private PasswordField txtConfirm;
	@FXML private PasswordField txtCurrent;

	private boolean isChangeFlow = false;
	private String currentPin;
	private String resultingPin;
	private final DatabaseManager db = new DatabaseManager();

	public void initForChangePin(String currentPin) {
		this.isChangeFlow = true;
		this.currentPin = currentPin;
		if (txtCurrent != null) txtCurrent.setVisible(true);
	}

	public String getResultingPin() { return resultingPin; }

	@FXML
	public void onSubmit() {
		try {
			if (isChangeFlow) {
				String cur = txtCurrent.getText();
				String pin = txtPin.getText();
				String confirm = txtConfirm.getText();
				if (!validPin(cur) || !validPin(pin) || !pin.equals(confirm)) {
					showError("Invalid PINs or mismatch");
					return;
				}
				// verify current matches stored
				String storedHash = db.getPinHash();
				if (storedHash == null || !ConfigManager.verifyPin(cur, storedHash)) {
					showError("Current PIN incorrect");
					return;
				}
				if (cur.equals(pin)) {
					showError("New PIN must differ from current");
					return;
				}
				// re-encrypt all files
				db.updateAllFilesEncryption(cur, pin);
				// update hash
				db.updatePinHash(ConfigManager.hashPin(cur), ConfigManager.hashPin(pin));
				resultingPin = pin;
				close();
			} else {
				String pin = txtPin.getText();
				String confirm = txtConfirm.getText();
				if (!validPin(pin) || !pin.equals(confirm)) {
					showError("PIN must be 5 digits and match");
					return;
				}
				System.out.println("Setting PIN hash...");
				db.setPinHash(ConfigManager.hashPin(pin));
				System.out.println("PIN set successfully");
				resultingPin = pin;
				close();
			}
		} catch (Exception e) {
			System.err.println("PIN setup error: " + e.getMessage());
			e.printStackTrace();
			showError("PIN setup failed: " + e.getMessage());
		}
	}

	private boolean validPin(String s) { return s != null && s.matches("\\d{5}"); }
	private void showError(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
	private void close() { ((Stage) txtPin.getScene().getWindow()).close(); }
}
