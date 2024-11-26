package PersonalizedNews;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadArticles {
    @FXML
    public Label readTitle;
    @FXML
    public Label viewAuthor;
    @FXML
    public Label viewPublishedDate;
    @FXML
    public Label viewContent;
    @FXML
    public Button likeButton;
    @FXML
    public Button skipButton;
    @FXML
    public Button saveButton;

    private String username;
    private int articleID;

    public void setUsername(String username) {
        this.username = username;
    }

    public void loadArticleDetails(int articleId) {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> articles = database.getCollection("Articles");
        Document article = articles.find(new Document("articleId", articleId)).first();

        if (article != null) {
            readTitle.setText(article.getString("title"));
            viewAuthor.setText(article.getString("author"));
            viewPublishedDate.setText(article.getString("publishedAt"));
            viewContent.setText(article.getString("content"));
            this.articleID = Integer.parseInt(String.valueOf(articleId));
            updateButtonStates();
        } else {
            showAlert("Error", "Article not found!", Alert.AlertType.ERROR);
        }
    }

    private void updateButtonStates() {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        Document userDoc = ratedArticles.find(new Document("username", username)).first();

        if (userDoc != null) {
            List<Integer> liked = userDoc.getList("liked", Integer.class);
            List<Integer> skipped = userDoc.getList("skipped", Integer.class);
            List<Integer> saved = userDoc.getList("saved", Integer.class);

            likeButton.setText(liked != null && liked.contains(Integer.parseInt(String.valueOf(articleID))) ? "Unlike" : "Like");
            skipButton.setText(skipped != null && skipped.contains(Integer.parseInt(String.valueOf(articleID))) ? "Unskip" : "Skip");
            saveButton.setText(saved != null && saved.contains(Integer.parseInt(String.valueOf(articleID))) ? "Unsave" : "Save");
        }
    }

    private void saveActionToDB(String action) {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        Document userDoc = ratedArticles.find(new Document("username", username)).first();

        if (userDoc == null) {
            userDoc = new Document("username", username)
                    .append("liked", new ArrayList<>())
                    .append("skipped", new ArrayList<>())
                    .append("saved", new ArrayList<>())
                    .append("read", new ArrayList<>());
            ratedArticles.insertOne(userDoc);
        }

        int articleId = Integer.parseInt(String.valueOf(articleID));
        if (action.equals("liked") || action.equals("skipped")) {
            String oppositeAction = action.equals("liked") ? "skipped" : "liked";
            ratedArticles.updateOne(
                    new Document("username", username),
                    Updates.pull(oppositeAction, articleId)
            );

            if (userDoc.getList(action, Integer.class).contains(articleId)) {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.pull(action, articleId)
                );
            } else {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet(action, articleId)
                );
            }
        } else if (action.equals("saved")) {
            if (userDoc.getList("saved", Integer.class).contains(articleId)) {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.pull("saved", articleId)
                );
            } else {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet("saved", articleId)
                );
            }
        }

        ratedArticles.updateOne(
                new Document("username", username),
                Updates.addToSet("read", articleId)
        );

        updateButtonStates();
    }

    @FXML
    public void onClickLike(ActionEvent event) {
        saveActionToDB("liked");
        showAlert("Success", likeButton.getText().equals("Unlike") ? "Liked the article." : "Unliked the article.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        saveActionToDB("skipped");
        showAlert("Success", skipButton.getText().equals("Unskip") ? "Skipped the article." : "Unskipped the article.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        saveActionToDB("saved");
        showAlert("Success", saveButton.getText().equals("Unsave") ? "Saved the article." : "Unsaved the article.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void initializeUser(String username) {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> userAccounts = database.getCollection("UserAccounts");

        Document userDoc = userAccounts.find(new Document("username", username)).first();

        if (userDoc != null) {
            List<String> preferences = userDoc.getList("preferences", String.class);
            System.out.println("User Preferences: " + preferences);
        } else {
            System.out.println("User not found!");
        }
    }

    public void onClickBackViewArticles(ActionEvent event) {
        try {
            // Navigate back to the Manage Articles page
            Parent root = FXMLLoader.load(getClass().getResource("UserPortal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 855, 525));
            stage.setTitle("User Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
