package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageArticles {

    // ExecutorService to manage concurrency
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void onClickToAdd(ActionEvent event) {
        executorService.execute(() -> navigateToPage(event, "AddArticles.fxml", "Add Articles", 633, 413));
    }

    public void onClickEditDelete(ActionEvent event) {
        executorService.execute(() -> navigateToPage(event, "EditDeleteArticles.fxml", "Edit/Delete Articles", 666, 478));
    }

    public void onClickLogout(ActionEvent event) {
        executorService.execute(() -> navigateToPage(event, "AdministratorLogin.fxml", "Admin Login", 434, 298));
    }

    public void onClickExit(ActionEvent event) {
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
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                javafx.application.Platform.runLater(stage::close);
            }
        });
    }

    public void onClickView(ActionEvent event) {
        executorService.execute(() -> navigateToPage(event, "AdminViewArticles.fxml", "View Articles", 875, 454));
    }

    private void navigateToPage(ActionEvent event, String fxmlFile, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            javafx.application.Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, width, height));
                    root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                    stage.setTitle(title);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Simulate a background task for processing articles
    public void processArticlesInBackground() {
        executorService.execute(() -> {
            System.out.println("Starting article processing...");
            try {
                // Simulate processing large datasets
                Thread.sleep(5000); // Simulate a time-consuming task
                System.out.println("Article processing completed.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    // Shutdown ExecutorService when the application exits
    public void shutdown() {
        executorService.shutdown();
    }
}