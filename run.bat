@echo off
REM Run the JavaFX application with proper module paths and all dependencies
setlocal enabledelayedexpansion

set JAVAFX_HOME=%USERPROFILE%\.m2\repository\org\openjfx
set M2_HOME=%USERPROFILE%\.m2\repository

set MODULE_PATH=!JAVAFX_HOME!\javafx-controls\21.0.6;!JAVAFX_HOME!\javafx-fxml\21.0.6;!JAVAFX_HOME!\javafx-graphics\21.0.6;!JAVAFX_HOME!\javafx-base\21.0.6

set CLASSPATH=target\classes;!M2_HOME!\org\xerial\sqlite-jdbc\3.43.0.0\sqlite-jdbc-3.43.0.0.jar;!M2_HOME!\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar

java --module-path "!MODULE_PATH!" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "!CLASSPATH!" com.example.calculator_vault_final.HelloApplication

pause

