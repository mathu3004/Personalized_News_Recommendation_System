package PersonalizedNews;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.bson.Document;

import java.time.ZoneId;
import java.util.Date;

public class EditArticles {

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
    public TextField URL;
    @FXML
    public TextArea content;

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
                URL.setText(article.getString("url"));

                // Convert Date to LocalDate for DatePicker
                Date publishedAtDate = article.getDate("publishedAt");
                if (publishedAtDate != null) {
                    publishedDate.setValue(publishedAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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
            String enteredURL = URL.getText().trim();

            // Check if title and author combination is unique
            Document titleAuthorQuery = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("articleId", new Document("$ne", enteredArticleID)); // Exclude the current article
            if (collection.find(titleAuthorQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The same title and author combination already exists!");
                return;
            }

            // Check if the URL is unique
            Document urlQuery = new Document("url", enteredURL)
                    .append("articleId", new Document("$ne", enteredArticleID)); // Exclude the current article
            if (collection.find(urlQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The URL must be unique!");
                return;
            }

            // Update the article in the database
            Document updatedArticle = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("description", description.getText().trim())
                    .append("content", content.getText().trim())
                    .append("url", enteredURL)
                    .append("publishedAt", Date.from(publishedDate.getValue()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()));

            Document query = new Document("articleId", enteredArticleID);
            collection.updateOne(query, new Document("$set", updatedArticle));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully!");

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
                URL.getText().trim().isEmpty() ||
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
        URL.clear();
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
}
