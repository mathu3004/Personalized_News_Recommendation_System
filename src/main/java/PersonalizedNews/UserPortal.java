package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    private void loadFXML(String fxmlFileName, String alertMessage) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource(fxmlFileName));
        // Clear the current content
        ContentArea.getChildren().clear();
        // Set the loaded FXML as the new content
        ContentArea.getChildren().add(fxml);

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
            loadFXML("UserHome.fxml", "Welcome to your personalized User Portal!");
        } catch (IOException ex) {
            // Print stack trace for debugging if the initial scene cannot be loaded
            ex.printStackTrace();
        }
    }

    @FXML
    public void onClickDashboard() throws IOException {
        loadFXML("UserHome.fxml", "Welcome to the Dashboard.");
    }

    @FXML
    public void onClickView() throws IOException {
        loadFXML("ViewArticles.fxml", "View the articles you are interested in.");
    }

    @FXML
    public void onClickRate() throws IOException {
        loadFXML("ViewArticles.fxml", "Rate the articles you have read.");
    }

    @FXML
    public void onClickManage() throws IOException {
        loadFXML("ManageProfile.fxml", "Manage your saved articles.");
    }

    @FXML
    public void onClickLogout() {
        try {
            // Navigate back to the AdministratorLogin.fxml page
            Parent root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));
            Stage stage = (Stage) ContentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Administrator Login");
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
