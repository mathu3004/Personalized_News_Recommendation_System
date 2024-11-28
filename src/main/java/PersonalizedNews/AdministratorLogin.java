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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
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
        executorService.execute(() -> handleLogin(event));
    }

    private void handleLogin(ActionEvent event) {
        // MongoDB connection setup
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            // Access the database and collection
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("AdminAccounts");

            String enteredEmail = email.getText().trim();
            String enteredAdminName = adminName.getText().trim();
            String enteredPassword = isPasswordVisible ? PasswordText.getText().trim() : Password.getText().trim();

            if (enteredPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Password cannot be empty!");
                return;
            }

            Document query = new Document();
            if (!enteredEmail.isEmpty()) {
                query.append("email", enteredEmail);
            }
            else if (!enteredAdminName.isEmpty()) {
                query.append("username", enteredAdminName);
            } else {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter either an email or an admin name!");
                return;
            }
            query.append("password", enteredPassword);

            // Query the database for the admin
            Document admin = collection.find(query).first();

            if (admin != null) {
                // Login successful
                showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome to the Admin Dashboard!");

                // Clear input fields
                clearFields();

                // Navigate to the admin dashboard
                javafx.application.Platform.runLater(() -> navigateToDashboard(event));
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email, admin name, or password!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ManageArticles.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 574, 400));
            root.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickBackMain(ActionEvent event) {
        executorService.execute(() -> navigateToMain(event));
    }

    private void navigateToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("WelcomePage.fxml"));
            javafx.application.Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 600, 450));
                    root.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());
                    stage.setTitle("Welcome to Mark's News");
                    stage.show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickReset(ActionEvent event) {
        clearFields();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void clearFields() {
        javafx.application.Platform.runLater(() -> {
            email.clear();
            adminName.clear();
            Password.clear();
            PasswordText.clear();
        });
    }

    // Shutdown ExecutorService when the application exits
    public void shutdown() {
        executorService.shutdown();
    }
}