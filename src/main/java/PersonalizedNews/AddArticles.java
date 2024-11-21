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

public class AddArticles {
    @FXML
    public TextField articleName;

    @FXML
    public TextField articleAuthor;

    @FXML
    public DatePicker publishedDate;

    @FXML
    public TextField articleID;

    @FXML
    public TextArea articleDescription;

    @FXML
    public TextField articleURL;

    @FXML
    public TextArea articleContent;

    @FXML
    public void onClickAddArticles(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Check if article ID is unique
            int enteredArticleID = Integer.parseInt(articleID.getText().trim());
            Document idQuery = new Document("articleId", enteredArticleID);
            if (collection.find(idQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must be unique!");
                return;
            }

            // Check if the same author and title combination already exists
            String enteredArticleName = articleName.getText().trim();
            String enteredAuthor = articleAuthor.getText().trim();
            Document nameAuthorQuery = new Document("title", enteredArticleName)
                    .append("author", enteredAuthor);
            if (collection.find(nameAuthorQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The same author and title combination already exists!");
                return;
            }

            // Check if the URL is unique
            String enteredArticleURL = articleURL.getText().trim();
            Document urlQuery = new Document("url", enteredArticleURL);
            if (collection.find(urlQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The URL must be unique!");
                return;
            }

            // Add the article to the database
            Document newArticle = new Document("articleId", enteredArticleID)
                    .append("title", enteredArticleName)
                    .append("author", enteredAuthor)
                    .append("publishedAt", publishedDate.getValue().toString())
                    .append("description", articleDescription.getText().trim())
                    .append("url", articleURL.getText().trim())
                    .append("content", articleContent.getText().trim());

            collection.insertOne(newArticle);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article added successfully!");

            // Clear all fields after successful addition
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (articleID.getText().trim().isEmpty() ||
                articleName.getText().trim().isEmpty() ||
                articleAuthor.getText().trim().isEmpty() ||
                publishedDate.getValue() == null ||
                articleDescription.getText().trim().isEmpty() ||
                articleURL.getText().trim().isEmpty() ||
                articleContent.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        articleID.clear();
        articleName.clear();
        articleAuthor.clear();
        publishedDate.setValue(null);
        articleDescription.clear();
        articleURL.clear();
        articleContent.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onClickReset(ActionEvent event) {
        clearFields();
    }
}
