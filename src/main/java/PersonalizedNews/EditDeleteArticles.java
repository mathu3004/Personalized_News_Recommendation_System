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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class EditDeleteArticles {

    @FXML
    public TextField articleID;
    @FXML
    public TextField title;
    @FXML
    public TextField author;
    @FXML
    public TextArea description;
    @FXML
    public DatePicker publishedDate;
    @FXML
    public TextArea content;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);

    @FXML
    public void onClickCheck(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            String enteredArticleID = articleID.getText().trim();
            if (enteredArticleID.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must not be empty!");
                return;
            }

            Document query = new Document("articleId", Integer.parseInt(enteredArticleID));
            Document article = collection.find(query).first();

            if (article != null) {
                // Load article details into the fields
                title.setText(article.getString("title"));
                author.setText(article.getString("author"));
                description.setText(article.getString("description"));
                content.setText(article.getString("content"));

                // Parse publishedAt to LocalDate
                String publishedAtStr = article.getString("publishedAt");
                if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
                    publishedDate.setValue(DATE_FORMATTER.parse(publishedAtStr, LocalDate::from));
                }
                showAlert(Alert.AlertType.INFORMATION, "Load Success", "Article details loaded successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Not Found", "No article found with the given Article ID!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must be a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickUpdate(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            int enteredArticleID = Integer.parseInt(articleID.getText().trim());
            String enteredTitle = title.getText().trim();
            String enteredAuthor = author.getText().trim();

            // Check if title and author combination is unique
            Document titleAuthorQuery = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("articleId", new Document("$ne", enteredArticleID)); // Exclude the current article
            if (collection.find(titleAuthorQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The same title and author combination already exists!");
                return;
            }

            // Update the article in the database
            Document updatedArticle = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("description", description.getText().trim())
                    .append("content", content.getText().trim())
                    .append("publishedAt", publishedDate.getValue().format(DATE_FORMATTER));

            Document query = new Document("articleId", enteredArticleID);
            collection.updateOne(query, new Document("$set", updatedArticle));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully!");

            // Initialize FetchArticles in a separate thread to avoid UI blocking
            new Thread(() -> {
                System.out.println("Initializing FetchArticles...");
                FetchArticlesCategory.initialize();
            }).start();

            // Clear all fields after successful update
            clearFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must be a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickReset(ActionEvent event) {
        // Clear all fields when reset is clicked
        clearFields();
    }

    private boolean validateInputs() {
        if (articleID.getText().trim().isEmpty() ||
                title.getText().trim().isEmpty() ||
                author.getText().trim().isEmpty() ||
                description.getText().trim().isEmpty() ||
                content.getText().trim().isEmpty() ||
                publishedDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        articleID.clear();
        title.clear();
        author.clear();
        description.clear();
        content.clear();
        publishedDate.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onClickDelete(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            String enteredArticleID = articleID.getText().trim();

            // Validate input
            if (enteredArticleID.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must not be empty!");
                return;
            }

            Document query = new Document("articleId", Integer.parseInt(enteredArticleID));
            Document article = collection.find(query).first();

            if (article != null) {
                collection.deleteOne(query);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Article deleted successfully!");
                // Initialize FetchArticles in a separate thread to avoid UI blocking
                new Thread(() -> {
                    System.out.println("Initializing FetchArticles...");
                    FetchArticlesCategory.initialize();
                }).start();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Not Found", "No article found with the given Article ID!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must be a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onClickMain(ActionEvent event) {
        try {
            // Navigate to the signup page
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
}
