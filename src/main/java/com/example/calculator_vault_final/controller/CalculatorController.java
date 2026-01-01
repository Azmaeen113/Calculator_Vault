package com.example.calculator_vault_final.controller;

import com.example.calculator_vault_final.model.ConfigManager;
import com.example.calculator_vault_final.model.DatabaseManager;
import com.example.calculator_vault_final.service.CalculatorService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CalculatorController {
	@FXML private TextField display;

	private String currentInput = "";
	private Double firstOperand = null;
	private String operator = null;
	private final DatabaseManager db = new DatabaseManager();

	@FXML
	public void initialize() {
		if (display != null) {
			display.setEditable(false);
		}
	}

	@FXML
	public void onNumber(ActionEvent e) {
		String value = ((Button) e.getSource()).getText();
		currentInput += value;
		updateDisplay();
		checkSecretTrigger();
	}

	@FXML
	public void onOperator(ActionEvent e) {
		try {
			if (currentInput.isEmpty() && firstOperand == null) return;
			// Capture first operand if we just entered it
			if (!currentInput.isEmpty()) {
				firstOperand = Double.parseDouble(currentInput);
			}
			operator = ((Button) e.getSource()).getText();
			currentInput = "";
			updateDisplay();
		} catch (NumberFormatException ignored) {}
	}

	@FXML
	public void onEquals() {
		try {
			if (firstOperand == null || operator == null || currentInput.isEmpty()) return;
			double second = Double.parseDouble(currentInput);
			double result;
			switch (operator) {
				case "+": result = CalculatorService.add(firstOperand, second); break;
				case "-": result = CalculatorService.subtract(firstOperand, second); break;
				case "×": case "*": result = CalculatorService.multiply(firstOperand, second); break;
				case "÷": case "/": result = CalculatorService.divide(firstOperand, second); break;
				default: return;
			}
			currentInput = formatNumber(result);
			display.setText(currentInput);
			firstOperand = null;
			operator = null;
		} catch (ArithmeticException ex) {
			Alert a = new Alert(Alert.AlertType.ERROR, "Division by zero");
			a.showAndWait();
			onClear();
		} catch (Exception ignored) {}
		checkSecretTrigger();
	}

	@FXML
	public void onClear() {
		currentInput = "";
		firstOperand = null;
		operator = null;
		display.setText("");
	}

	private void checkSecretTrigger() {
		try {
			String pinHash = db.getPinHash();
			if (pinHash != null && ConfigManager.hashPin(currentInput).equals(pinHash)) {
				openVault(currentInput);
				onClear();
			}
		} catch (Exception ignored) {}
	}

	private void openVault(String pin) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/vault-view.fxml"));
			Parent root = loader.load();
			VaultController controller = loader.getController();
			controller.setCurrentPin(pin);
			Stage stage = (Stage) display.getScene().getWindow();
			stage.setTitle("Secure Vault");
			stage.setScene(new Scene(root, 800, 600));
		} catch (IOException e) {
			Alert a = new Alert(Alert.AlertType.ERROR, "Failed to open vault view");
			a.showAndWait();
		}
	}

	private void updateDisplay() {
		if (operator == null) {
			display.setText(currentInput.isEmpty() && firstOperand != null ? formatNumber(firstOperand) : currentInput);
			return;
		}
		String left = firstOperand != null ? formatNumber(firstOperand) : "";
		String right = currentInput;
		display.setText((left + " " + operator + " " + right).trim());
	}

	private String formatNumber(double value) {
		if (Double.isFinite(value) && value % 1 == 0) {
			return String.valueOf((long) value);
		}
		return String.valueOf(value);
	}
}
