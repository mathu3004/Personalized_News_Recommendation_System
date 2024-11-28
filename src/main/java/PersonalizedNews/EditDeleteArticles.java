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
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool for concurrency

    @FXML
    public void onClickCheck(ActionEvent event) {
        executorService.execute(() -> fetchArticle(event));
    }

    private void fetchArticle(ActionEvent event) {
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
                javafx.application.Platform.runLater(() -> {
                    title.setText(article.getString("title"));
                    author.setText(article.getString("author"));
                    description.setText(article.getString("description"));
                    content.setText(article.getString("content"));

                    String publishedAtStr = article.getString("publishedAt");
                    if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
                        publishedDate.setValue(DATE_FORMATTER.parse(publishedAtStr, LocalDate::from));
                    }
                });

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
        executorService.execute(() -> updateArticle(event));
    }

    private void updateArticle(ActionEvent event) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017")) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> collection = database.getCollection("Articles");

            if (!validateInputs()) {
                return;
            }

            int enteredArticleID = Integer.parseInt(articleID.getText().trim());
            String enteredTitle = title.getText().trim();
            String enteredAuthor = author.getText().trim();

            Document titleAuthorQuery = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("articleId", new Document("$ne", enteredArticleID));

            if (collection.find(titleAuthorQuery).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The same title and author combination already exists!");
                return;
            }

            Document updatedArticle = new Document("title", enteredTitle)
                    .append("author", enteredAuthor)
                    .append("description", description.getText().trim())
                    .append("content", content.getText().trim())
                    .append("publishedAt", publishedDate.getValue().format(DATE_FORMATTER));

            Document query = new Document("articleId", enteredArticleID);
            collection.updateOne(query, new Document("$set", updatedArticle));
            FetchArticlesCategory.initialize();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully!");
            clearFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Article ID must be a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickDelete(ActionEvent event) {
        executorService.execute(() -> deleteArticle(event));
    }

    private void deleteArticle(ActionEvent event) {
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
                collection.deleteOne(query);
                FetchArticlesCategory.initialize();
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
        javafx.application.Platform.runLater(() -> {
            articleID.clear();
            title.clear();
            author.clear();
            description.clear();
            content.clear();
            publishedDate.setValue(null);
        });
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

    public void shutdown() {
        executorService.shutdown();
    }

    public void onClickMain(ActionEvent event) {
        executorService.execute(() -> navigateToMain(event));
    }

    private void navigateToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ManageArticles.fxml"));
            javafx.application.Platform.runLater(() -> {
                try {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 574, 400));
                    root.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());
                    stage.setTitle("Admin Dashboard");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
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
}