package PersonalizedNews.UserMaintainance;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import org.bson.Document;

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
                            fxml.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());
                        }
                        fxml.getStylesheets().add(getClass().getResource("/PersonalizedNews/Button.css").toExternalForm());

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
                Platform.runLater(() -> loadFXML("/PersonalizedNews/UserHome.fxml", null, 855, 455, false));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    public void onClickDashboard() {
        loadFXML("/PersonalizedNews/UserHome.fxml", "Welcome to the Dashboard.", 855, 455, false);
    }

    @FXML
    public void onClickView() {
        executorService.execute(() -> {
            try {
                // Load the ViewArticles FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PersonalizedNews/ViewArticles.fxml"));
                Parent fxml = loader.load();

                // Retrieve the controller and initialize the username
                ViewCustomArticles controller = loader.getController();
                String username = SessionManager.getInstance().getUsername();

                Platform.runLater(() -> {
                    if (username != null) {
                        controller.initializeUsername(); // Set the username and load articles
                        fxml.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());

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
        loadFXML("/PersonalizedNews/ManageProfile.fxml", "Manage your saved articles.", 550, 679, true);
    }

    @FXML
    public void onClickLogout() {
        executorService.execute(() -> {
            // Use Platform.runLater to ensure UI interactions happen on the JavaFX Application Thread
            Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Logout Confirmation");
                alert.setHeaderText("Are you sure you want to logout?");
                alert.setContentText("Press OK to logout or Cancel to stay.");

                // Customizing the buttons in the alert dialog
                ButtonType okButton = new ButtonType("OK");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, cancelButton);

                // Handling user's choice
                alert.showAndWait().ifPresent(response -> {
                    if (response == okButton) {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/PersonalizedNews/UserLogin.fxml"));

                            Platform.runLater(() -> {
                                Stage stage = (Stage) ContentArea.getScene().getWindow();
                                root.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());
                                stage.setScene(new Scene(root, 440, 280));
                                stage.setTitle("User Login");
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            });
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

    @FXML
    public void onClickDeactivate(ActionEvent event) {
            // Confirm the action with the user
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Deactivate Account");
            alert.setHeaderText("Are you sure you want to deactivate your account?");
            alert.setContentText("This action cannot be undone. Press OK to proceed or Cancel to abort.");

            ButtonType okButton = new ButtonType("OK");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == okButton) {
                    String username = SessionManager.getInstance().getUsername();

                    if (username != null) {
                        executorService.execute(() -> {
                            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://mathu0404:Janu3004@cluster3004.bmusn.mongodb.net/?retryWrites=true&w=majority&appName=Cluster3004")) {
                                MongoDatabase database = mongoClient.getDatabase("News");

                                // Collections
                                MongoCollection<Document> userAccounts = database.getCollection("UserAccounts");
                                MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

                                // Start Deletion Process
                                try {
                                    // Delete articles rated by the user
                                    ratedArticles.deleteMany(new Document("username", username));

                                    // Delete user account
                                    userAccounts.deleteOne(new Document("username", username));

                                    Platform.runLater(() -> {
                                        // Show success message and redirect to login
                                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                        successAlert.setTitle("Account Deactivated");
                                        successAlert.setHeaderText(null);
                                        successAlert.setContentText("Your account has been successfully deactivated.");
                                        successAlert.showAndWait();

                                        // Redirect to login
                                        try {
                                            Parent root = FXMLLoader.load(getClass().getResource("/PersonalizedNews/CreateAccount.fxml"));
                                            Stage stage = (Stage) ContentArea.getScene().getWindow();
                                            root.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());
                                            stage.setScene(new Scene(root, 440, 280));
                                            stage.setTitle("User Signup");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Platform.runLater(() -> {
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setTitle("Error");
                                        errorAlert.setHeaderText("Account Deactivation Failed");
                                        errorAlert.setContentText("An error occurred while deactivating your account. Please try again later.");
                                        errorAlert.showAndWait();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // Show warning if username is null
                        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                        warningAlert.setTitle("No Username Found");
                        warningAlert.setHeaderText(null);
                        warningAlert.setContentText("No username found in session. Please log in again.");
                        warningAlert.showAndWait();
                    }
                }
            });
    }
}
