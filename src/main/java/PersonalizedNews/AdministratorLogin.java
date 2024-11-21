package PersonalizedNews;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class AdministratorLogin {
    @FXML
    public TextField adminName;
    @FXML
    public TextField email;
    @FXML
    public PasswordField Password;
    @FXML
    public TextField PasswordText;
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Ensure PasswordText is invisible by default
        PasswordText.setVisible(false);
    }

    @FXML
    public void onClickView(ActionEvent event) {
        if (isPasswordVisible) {
            // Switch back to hiding the password
            Password.setText(PasswordText.getText());
            Password.setVisible(true);
            PasswordText.setVisible(false);
            isPasswordVisible = false;
        } else {
            // Switch to showing the password
            PasswordText.setText(Password.getText());
            PasswordText.setVisible(true);
            Password.setVisible(false);
            isPasswordVisible = true;
        }
    }

    @FXML
    public void onClickLogin(ActionEvent event) {
        // MongoDB connection setup
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            // Access the database and collection
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("AdminAccounts");

            // Get user input
            String enteredEmail = email.getText().trim();
            String enteredAdminName = adminName.getText().trim();
            String enteredPassword = isPasswordVisible ? PasswordText.getText().trim() : Password.getText().trim();

            // Validate inputs
            if (enteredEmail.isEmpty() && enteredAdminName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter at least an email or admin name!");
                return;
            }

            if (enteredPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Password cannot be empty!");
                return;
            }

            // Build the query
            Document query = new Document();
            if (!enteredEmail.isEmpty()) {
                query.append("email", enteredEmail);
            }
            if (!enteredAdminName.isEmpty()) {
                query.append("adminName", enteredAdminName);
            }
            query.append("password", enteredPassword);

            // Query the database for the admin
            Document admin = collection.find(query).first();

            if (admin != null) {
                // Login successful
                showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome to the Admin Dashboard!");

                // Clear input fields
                clearFields();

                // Navigate to the admin dashboard or next page
                Parent root = FXMLLoader.load(getClass().getResource("ManageArticles.fxml")); // Replace with your admin dashboard scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Admin Dashboard");
                stage.show();
            } else {
                // Invalid credentials
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email, admin name, or password!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onClickBackMain(ActionEvent event) {
        try {
            // Navigate to the signup page
            Parent root = FXMLLoader.load(getClass().getResource("WelcomePage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Welcome Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        // Clear input fields
        email.clear();
        adminName.clear();
        Password.clear();
        PasswordText.clear();
    }

    public void onClickReset(ActionEvent event) {
        clearFields();
    }
}
