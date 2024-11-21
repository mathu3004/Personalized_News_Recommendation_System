package PersonalizedNews;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.bson.Document;

public class DeleteArticles {

    @FXML
    public TextField ArticleID;

    @FXML
    public void onClickDelete(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            String enteredArticleID = ArticleID.getText().trim();

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

    @FXML
    public void onClickReset(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        ArticleID.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
