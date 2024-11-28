package PersonalizedNews;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
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

public class UserLogin {
    @FXML
    public PasswordField password;
    @FXML
    public TextField viewPassword;
    @FXML
    public TextField username;

    private boolean isPasswordVisible = false;
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for concurrency

    @FXML
    public void initialize() {
        // Ensure viewPassword is invisible by default
        viewPassword.setVisible(false);
        viewPassword.setPromptText(password.getPromptText());
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
        executorService.execute(() -> handleLogin(event));
    }

    private void handleLogin(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            // Access the database and collection
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("UserAccounts");

            // Get user input
            String enteredUsername = username.getText().trim();
            String enteredPassword = isPasswordVisible ? viewPassword.getText().trim() : password.getText().trim();

            // Validate inputs
            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Username and password cannot be empty!");
                return;
            }

            // Query the database for the user
            Document query = new Document("username", enteredUsername).append("password", enteredPassword);
            Document user = collection.find(query).first();

            if (user != null) {
                Platform.runLater(() -> {
                    try {
                        // Login successful
                        ReadArticles.initializeUser(enteredUsername);
                        SessionManager.getInstance().setUsername(enteredUsername);

                        // Navigate to the dashboard or next page
                        Parent root = FXMLLoader.load(getClass().getResource("UserPortal.fxml"));
                        showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome to your dashboard!");
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 855, 525));
                        root.getStylesheets().add(getClass().getResource("GlowButton.css").toExternalForm());
                        stage.setTitle("User Dashboard");

                        // Clear input fields
                        clearFields();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickBackMain(ActionEvent event) {
        executorService.execute(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("WelcomePage.fxml"));
                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 600, 450));
                        root.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());
                        stage.setTitle("Welcome to Mark's News");
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onCLickSignup(ActionEvent event) {
        executorService.execute(() -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("CreateAccount.fxml"));
                Platform.runLater(() -> {
                    try {
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root, 651, 400));
                        root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                        stage.setTitle("User Signup");
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void clearFields() {
        Platform.runLater(() -> {
            username.clear();
            viewPassword.clear();
            password.clear();
        });
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void onClickReset(ActionEvent event) {
        clearFields();
    }

    // Shutdown ExecutorService to clean up threads when the application exits
    public void shutdown() {
        executorService.shutdown();
    }
}
