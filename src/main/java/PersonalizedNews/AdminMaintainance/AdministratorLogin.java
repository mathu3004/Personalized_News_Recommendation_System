package PersonalizedNews.AdminMaintainance;

import PersonalizedNews.MainClass.Admin;
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
            Password.setText(PasswordText.getText());
            Password.setVisible(true);
            PasswordText.setVisible(false);
            isPasswordVisible = false;
        } else {
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
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("AdminAccounts");

            // Create Admin object from inputs
            Admin admin = new Admin(
                    adminName.getText().trim(),
                    email.getText().trim(),
                    isPasswordVisible ? PasswordText.getText().trim() : Password.getText().trim()
            );

            if (!admin.isValid()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required!");
                return;
            }

            // Build a query using the Admin class
            Document query = admin.toQueryDocument();
            Document adminDoc = collection.find(query).first();

            if (adminDoc != null) {
                showAlert(Alert.AlertType.INFORMATION, "Login Success", "Welcome to the Admin Dashboard!");
                clearFields();
                javafx.application.Platform.runLater(() -> navigateToDashboard(event));
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials!");
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

    public void shutdown() {
        executorService.shutdown();
    }
}
