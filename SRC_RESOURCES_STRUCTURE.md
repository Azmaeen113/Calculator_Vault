# Source and Resources Structure

## Complete src/ Directory Structure

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── example/
    │           └── calculator_vault_final/
    │               ├── HelloApplication.java
    │               ├── Launcher.java
    │               │
    │               ├── controller/
    │               │   ├── CalculatorController.java
    │               │   ├── HistoryController.java
    │               │   ├── PINSetupController.java
    │               │   └── VaultController.java
    │               │
    │               ├── model/
    │               │   ├── ConfigManager.java
    │               │   ├── DatabaseManager.java
    │               │   └── VaultFile.java
    │               │
    │               ├── service/
    │               │   ├── CalculatorService.java
    │               │   └── FileVaultService.java
    │               │
    │               └── util/
    │                   (empty - reserved for future utilities)
    │
    └── resources/
        └── com/
            └── example/
                └── calculator_vault_final/
                    ├── calculator-view.fxml
                    ├── history-view.fxml
                    ├── pin-setup.fxml
                    ├── styles.css
                    └── vault-view.fxml
```

## File Descriptions

### Java Source Files (`src/main/java/`)

#### Root Package
- **HelloApplication.java** - Main JavaFX application class
- **Launcher.java** - Application entry point

#### controller/
- **CalculatorController.java** - Calculator UI controller
- **HistoryController.java** - History page controller
- **PINSetupController.java** - PIN setup/change controller
- **VaultController.java** - Vault page controller

#### model/
- **ConfigManager.java** - Application configuration management
- **DatabaseManager.java** - SQLite database operations
- **VaultFile.java** - Vault file data model

#### service/
- **CalculatorService.java** - Calculator business logic
- **FileVaultService.java** - File encryption/decryption service

#### util/
- Currently empty - reserved for utility classes

### Resource Files (`src/main/resources/`)

#### FXML Views
- **calculator-view.fxml** - Calculator interface layout
- **history-view.fxml** - History page layout
- **pin-setup.fxml** - PIN setup page layout
- **vault-view.fxml** - Vault page layout

#### Styles
- **styles.css** - Global application styles
