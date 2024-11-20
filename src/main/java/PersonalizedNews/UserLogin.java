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

public class UserLogin {
    @FXML
    public TextField email;
    @FXML
    public PasswordField password;
    @FXML
    public TextField viewPassword;
    @FXML
    public TextField username;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Ensure viewPassword is invisible by default
        viewPassword.setVisible(false);
    }


    @FXML
    public void onClickView(ActionEvent event) {
        if (isPasswordVisible) {
            // Hide the password
            password.setText(viewPassword.getText());
            password.setVisible(true);
            viewPassword.setVisible(false);
            isPasswordVisible = false;
        } else {
            // Show the password
            viewPassword.setText(password.getText());
            viewPassword.setVisible(true);
            password.setVisible(false);
            isPasswordVisible = true;
        }
    }

    @FXML
    public void onClickSubmit(ActionEvent event) {
        // MongoDB connection setup
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            // Access the database and collection
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("UserAccounts");

            // Get user input
            String enteredEmail = email.getText().trim();
            String enteredUsername = username.getText().trim();
            String enteredPassword = password.getText().trim(); // Ensure password is encrypted in production

            // Validate inputs
            if (enteredEmail.isEmpty() && enteredUsername.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter at least an email or username!");
                return;
            }

            if (enteredPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Password cannot be empty!");
                return;
            }

            // Query the database for the user
            Document query = new Document();
            if (!enteredEmail.isEmpty()) {
                query.append("email", enteredEmail);
            }
            if (!enteredUsername.isEmpty()) {
                query.append("username", enteredUsername);
            }
            query.append("password", enteredPassword);
            Document user = collection.find(query).first();

            if (user != null) {
                // Login successful
                showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome to your dashboard!");


                //Clear input fields
                email.clear();
                username.clear();
                viewPassword.clear();
                password.clear();

                // Navigate to the dashboard or next page
                Parent root = FXMLLoader.load(getClass().getResource("ManageProfile.fxml")); // Replace with your next scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Dashboard");
                stage.show();
            } else {
                // Invalid credentials
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email, username, or password!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    public void onClickBackMain(ActionEvent event) {
        try {
            // Return to the main welcome page
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

    @FXML
    public void onCLickSignup(ActionEvent event) {
        try {
            // Navigate to the signup page
            Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Signup Page");
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
}
