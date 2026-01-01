package com.example.calculator_vault_final;
import com.example.calculator_vault_final.model.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            DatabaseManager db = new DatabaseManager();
            boolean first;
            try {
                first = db.isFirstTime() || db.getPinHash() == null;
            } catch (Exception e) {
                System.err.println("Error checking PIN status: " + e.getMessage());
                first = true;
            }

            if (first) {
                System.out.println("First time setup: showing PIN dialog");
                FXMLLoader pinLoader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/pin-setup.fxml"));
                Parent pinRoot = pinLoader.load();
                Stage dlg = new Stage();
                dlg.initOwner(stage);
                dlg.initModality(Modality.APPLICATION_MODAL);
                dlg.setTitle("Set PIN");
                dlg.setScene(new Scene(pinRoot, 300, 250));
                dlg.setResizable(false);
                dlg.showAndWait();
                System.out.println("PIN dialog closed");
            } else {
                System.out.println("PIN already set, skipping setup");
            }

            FXMLLoader calcLoader = new FXMLLoader(getClass().getResource("/com/example/calculator_vault_final/calculator-view.fxml"));
            Parent calc = calcLoader.load();
            Scene scene = new Scene(calc, 400, 600);
            stage.setTitle("Calculator");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Fatal error in start: " + e.getMessage());
            throw e;
        }
    }
}

