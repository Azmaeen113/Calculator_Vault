# Calculator Vault Final - Project Structure

## Overview
This is a JavaFX desktop application that combines a calculator with a secure file vault feature, built using Maven and following the MVC (Model-View-Controller) architecture pattern.

## Directory Structure

```
Calculator_Vault_Final/
│
├── .git/                                    # Git version control
├── .gitignore                               # Git ignore configuration
├── .idea/                                   # IntelliJ IDEA project settings
├── .mvn/                                    # Maven wrapper configuration
│
├── ANDROID_APP_PROMPT.md                    # Android app documentation
├── PROJECT_STRUCTURE.md                     # This file
├── mvnw                                     # Maven wrapper script (Unix/Linux)
├── mvnw.cmd                                 # Maven wrapper script (Windows)
├── pom.xml                                  # Maven project configuration & dependencies
├── run.bat                                  # Quick run script for Windows
│
├── src/                                     # Source code directory
│   └── main/
│       ├── java/                            # Java source files
│       │   └── com/
│       │       └── example/
│       │           └── calculator_vault_final/
│       │               │
│       │               ├── HelloApplication.java       # Main JavaFX application entry point
│       │               ├── Launcher.java              # Application launcher (bypasses module issues)
│       │               │
│       │               ├── controller/                # MVC Controllers
│       │               │   ├── CalculatorController.java    # Handles calculator UI logic
│       │               │   ├── HistoryController.java       # Manages calculation history
│       │               │   ├── PINSetupController.java      # Manages PIN setup/change
│       │               │   └── VaultController.java         # Manages vault file operations
│       │               │
│       │               ├── model/                     # Data Models
│       │               │   ├── ConfigManager.java           # Application configuration management
│       │               │   ├── DatabaseManager.java         # SQLite database operations
│       │               │   └── VaultFile.java              # File vault data structure
│       │               │
│       │               ├── service/                   # Business Logic Layer
│       │               │   ├── CalculatorService.java       # Calculator computation logic
│       │               │   └── FileVaultService.java        # File encryption/decryption logic
│       │               │
│       │               └── util/                      # Utility classes (currently empty)
│       │
│       └── resources/                       # Application resources
│           └── com/
│               └── example/
│                   └── calculator_vault_final/
│                       ├── calculator-view.fxml      # Calculator UI layout
│                       ├── history-view.fxml         # History page UI layout
│                       ├── pin-setup.fxml            # PIN setup page UI layout
│                       ├── vault-view.fxml           # Vault page UI layout
│                       └── styles.css                # Global CSS styles
│
├── target/                                  # Maven build output (generated)
│   ├── classes/                             # Compiled .class files
│   │   └── com/
│   │       └── example/
│   │           └── calculator_vault_final/
│   │               ├── *.class              # Compiled Java classes
│   │               ├── *.fxml               # Copied FXML files
│   │               └── styles.css           # Copied CSS file
│   │
│   ├── generated-sources/                   # Generated source files
│   │   └── annotations/
│   │
│   └── maven-status/                        # Maven build status
│       └── maven-compiler-plugin/
│           └── compile/
│               └── default-compile/
│                   ├── createdFiles.lst
│                   └── inputFiles.lst
│
└── out/                                     # Additional IDE output

```

## Architecture

### MVC Pattern
The application follows the Model-View-Controller design pattern:

- **Model** (`model/`): Data structures, database management, and configuration
- **View** (`resources/*.fxml`): FXML files defining the user interface layouts
- **Controller** (`controller/`): Java classes handling UI logic and user interactions

### Layer Structure

1. **Presentation Layer** (Controllers + FXML)
   - Handles user input and displays output
   - Binds UI elements to business logic

2. **Business Logic Layer** (Services)
   - Contains core application logic
   - Independent of UI implementation

3. **Data Layer** (Models)
   - Manages data persistence
   - Handles database and configuration operations

## Key Components

### Application Entry
- **Launcher.java**: Main entry point that launches the JavaFX application
- **HelloApplication.java**: JavaFX application class, initializes the UI

### Features

#### 1. Calculator
- **CalculatorController.java**: Manages calculator UI interactions
- **CalculatorService.java**: Performs mathematical calculations
- **calculator-view.fxml**: Calculator interface layout

#### 2. History
- **HistoryController.java**: Displays and manages calculation history
- **DatabaseManager.java**: Stores/retrieves history from SQLite database
- **history-view.fxml**: History page layout

#### 3. Secure Vault
- **VaultController.java**: Manages vault file operations (upload, download, delete)
- **FileVaultService.java**: Handles file encryption and decryption
- **VaultFile.java**: Represents a stored file in the vault
- **vault-view.fxml**: Vault interface layout

#### 4. Security
- **PINSetupController.java**: Handles PIN creation and modification
- **ConfigManager.java**: Stores encrypted PIN and settings

## Technologies Used

- **Language**: Java
- **UI Framework**: JavaFX
- **Build Tool**: Maven
- **Database**: SQLite (embedded)
- **Styling**: CSS

## Build & Run

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn javafx:run
```

Or on Windows:
```bash
run.bat
```

## Notes

- The `util/` package is currently empty and reserved for future utility classes
- The `target/` directory is generated during build and should not be version controlled
- FXML files and CSS are copied from `src/main/resources/` to `target/classes/` during compilation
