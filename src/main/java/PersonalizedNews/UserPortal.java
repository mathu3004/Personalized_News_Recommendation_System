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

public class UserPortal implements Initializable {

    @FXML
    public StackPane ContentArea;

    // Utility method to load an FXML file into ContentArea
    private void loadFXML(String fxmlFileName, String alertMessage, double width, double height, boolean applyCSS) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource(fxmlFileName));

        // Clear the current content
        ContentArea.getChildren().clear();
        // Set the loaded FXML as the new content
        ContentArea.getChildren().add(fxml);

        // Ensure scene is initialized before modifying the stage
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Load the initial view (UserHome) when the portal opens
            loadFXML("UserHome.fxml", null, 855, 455, false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void onClickDashboard() throws IOException {
        loadFXML("UserHome.fxml", "Welcome to the Dashboard.", 855, 455, false);
    }

    @FXML
    public void onClickView() throws IOException {
        try {
            // Load the ViewArticles FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewArticles.fxml"));
            Parent fxml = loader.load();

            // Retrieve the controller and initialize the username
            ViewArticles controller = loader.getController();
            String username = SessionManager.getInstance().getUsername();
            fxml.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
            if (username != null) {
                controller.initializeUsername(); // Set the username and load articles
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Username Found");
                alert.setHeaderText(null);
                alert.setContentText("No username found in session. Please log in again.");
                alert.showAndWait();
                return;
            }
            // Display the ViewArticles page
            ContentArea.getChildren().clear();
            ContentArea.getChildren().add(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickManage() throws IOException {
        loadFXML("ManageProfile.fxml", "Manage your saved articles.", 550, 679, true);
    }

    @FXML
    public void onClickLogout() {
        try {
            // Navigate back to the AdministratorLogin.fxml page
            Parent root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));
            Stage stage = (Stage) ContentArea.getScene().getWindow();
            root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
            stage.setScene(new Scene(root, 440, 280));
            stage.setTitle("User Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                // Close the application
                Stage stage = (Stage) ContentArea.getScene().getWindow();
                stage.close();
            }
        });
    }
}