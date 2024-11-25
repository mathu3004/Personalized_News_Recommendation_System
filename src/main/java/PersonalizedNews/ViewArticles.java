package PersonalizedNews;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewArticles {
    @FXML
    private TableColumn<Article, String> columnCategory;

    @FXML
    private TableColumn<Article, String> columnAuthor;

    @FXML
    private TableColumn<Article, String> columnPublishedDate;

    @FXML
    private TableColumn<Article, String> columnTitle;

    @FXML
    private TableColumn<Article, String> columnDescription;

    @FXML
    private TableColumn<Article, RadioButton> columnSelect;

    @FXML
    private TableView<Article> articlesTable;

    private String username;

    @FXML
    public void initialize() {
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        columnAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        columnPublishedDate.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        columnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnSelect.setCellValueFactory(new PropertyValueFactory<>("select"));
    }

    public void initializeUsername() {
        username = SessionManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            System.out.println("Error: Username is not set in session!");
            return;
        }
        System.out.println("Logged in as: " + username);
        loadRecommendedArticles();
    }

    public void loadRecommendedArticles() {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> categorizedArticles = database.getCollection("CategorizedArticles");
        List<Document> recommendedArticles = categorizedArticles.find().into(new ArrayList<>());

        // Populate TableView
        ObservableList<Article> observableArticles = FXCollections.observableArrayList();

        for (Document article : recommendedArticles) {
            Article articleObj = new Article(
                    article.getInteger("articleId"),
                    article.getString("category"),
                    article.getString("author"),
                    article.getString("publishedAt"),
                    article.getString("title"),
                    article.getString("description"),
                    new RadioButton()
            );
            observableArticles.add(articleObj);
        }

        articlesTable.setItems(observableArticles);
        articlesTable.refresh();
    }

    private void saveActionToDB(String action, int articleId) {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        // Fetch or initialize user document
        Document userDoc = ratedArticles.find(new Document("username", username)).first();
        if (userDoc == null) {
            userDoc = new Document("username", username)
                    .append("liked", new ArrayList<>())
                    .append("skipped", new ArrayList<>())
                    .append("saved", new ArrayList<>())
                    .append("read", new ArrayList<>());
            ratedArticles.insertOne(userDoc);
        }

        // Manage specific action
        switch (action) {
            case "read":
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet("read", articleId)
                );
                break;

            case "liked":
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.pull("skipped", articleId) // Remove from skipped if present
                );
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet("liked", articleId) // Add to liked
                );
                break;

            case "skipped":
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.pull("liked", articleId) // Remove from liked if present
                );
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet("skipped", articleId) // Add to skipped
                );
                break;

            case "saved":
                boolean isSaved = userDoc.getList("saved", Integer.class).contains(articleId);
                if (isSaved) {
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.pull("saved", articleId) // Remove from saved if already present
                    );
                } else {
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.addToSet("saved", articleId) // Add to saved
                    );
                }
                break;
        }
    }

    @FXML
    public void onClickRead(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            saveActionToDB("read", selectedArticle.getArticleId());
            showAlert("Success", "Read the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
            try {
                // Load the ReadArticles view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ReadArticles.fxml"));
                Parent root = loader.load();

                // Pass the article data to the ReadArticles controller
                ReadArticles controller = loader.getController();
                controller.setUsername(username);
                controller.loadArticleDetails(selectedArticle.getArticleId());

                // Set the scene and show the ReadArticles view
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 600, 628));
                root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                stage.setTitle("Read Article");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickLike(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            saveActionToDB("liked", selectedArticle.getArticleId());
            showAlert("Success", "Liked the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            saveActionToDB("skipped", selectedArticle.getArticleId());
            showAlert("Success", "Skipped the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            saveActionToDB("saved", selectedArticle.getArticleId());
            showAlert("Success", "Saved the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    private Article getSelectedArticle() {
        return articlesTable.getItems().stream()
                .filter(article -> article.getSelect().isSelected())
                .findFirst()
                .orElse(null);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}