package PersonalizedNews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserPortal implements Initializable {

    @FXML
    public StackPane ContentArea;

    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for concurrency

    // Utility method to load an FXML file into ContentArea
    private void loadFXML(String fxmlFileName, String alertMessage, double width, double height, boolean applyCSS) {
        executorService.execute(() -> {
            try {
                Parent fxml = FXMLLoader.load(getClass().getResource(fxmlFileName));

                // Update UI on the JavaFX Application thread
                Platform.runLater(() -> {
                    try {
                        // Clear the current content
                        ContentArea.getChildren().clear();
                        // Set the loaded FXML as the new content
                        ContentArea.getChildren().add(fxml);

                        // Adjust stage dimensions
                        ContentArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
                            if (newScene != null) {
                                Stage stage = (Stage) newScene.getWindow();
                                if (stage != null) {
                                    stage.setWidth(width);
                                    stage.setHeight(height);
                                }
                            }
                        });

                        // Conditionally apply the CSS stylesheet
                        if (applyCSS) {
                            fxml.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                        }
                        fxml.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());

                        // Optional: Show an alert message after loading the FXML
                        if (alertMessage != null && !alertMessage.isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Information");
                            alert.setHeaderText(null);
                            alert.setContentText(alertMessage);
                            alert.showAndWait();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService.execute(() -> {
            try {
                // Load the initial view (UserHome) when the portal opens
                Platform.runLater(() -> loadFXML("UserHome.fxml", null, 855, 455, false));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    public void onClickDashboard() {
        loadFXML("UserHome.fxml", "Welcome to the Dashboard.", 855, 455, false);
    }

    @FXML
    public void onClickView() {
        executorService.execute(() -> {
            try {
                // Load the ViewArticles FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewArticles.fxml"));
                Parent fxml = loader.load();

                // Retrieve the controller and initialize the username
                ViewArticles controller = loader.getController();
                String username = SessionManager.getInstance().getUsername();

                Platform.runLater(() -> {
                    if (username != null) {
                        controller.initializeUsername(); // Set the username and load articles
                        fxml.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());

                        // Display the ViewArticles page
                        ContentArea.getChildren().clear();
                        ContentArea.getChildren().add(fxml);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("No Username Found");
                        alert.setHeaderText(null);
                        alert.setContentText("No username found in session. Please log in again.");
                        alert.showAndWait();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onClickManage() {
        loadFXML("ManageProfile.fxml", "Manage your saved articles.", 550, 679, true);
    }

    @FXML
    public void onClickLogout() {
        executorService.execute(() -> {
            try {
                // Navigate back to the UserLogin.fxml page
                Parent root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));

                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) ContentArea.getScene().getWindow();
                        root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                        stage.setScene(new Scene(root, 440, 280));
                        stage.setTitle("User Login");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onClickExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Press OK to exit or Cancel to stay.");

        // Customizing the buttons in the alert dialog
        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        // Handling user's choice
        alert.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                Platform.exit(); // Close the application
            }
        });
    }

    // Shutdown ExecutorService when the application exits
    public void shutdown() {
        executorService.shutdown();
    }
}
