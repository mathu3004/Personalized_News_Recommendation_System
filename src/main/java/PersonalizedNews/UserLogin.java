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

    private boolean isPasswordVisible = false;

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
            String enteredPassword = viewPassword.getText().trim(); // Ensure password is encrypted in production

            // Query the database for the user
            Document query = new Document("email", enteredEmail).append("password", enteredPassword);
            Document user = collection.find(query).first();

            if (user != null) {
                // Login successful
                System.out.println("Login successful!");

                // Navigate to the dashboard or next page
                Parent root = FXMLLoader.load(getClass().getResource("ViewArticles.fxml")); // Replace with your next scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Dashboard");
                stage.show();
            } else {
                // Invalid credentials
                System.out.println("Invalid email or password!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
}
