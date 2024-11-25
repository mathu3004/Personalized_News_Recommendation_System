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

    private String username;
    private String articleID;

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
            this.articleID = String.valueOf(articleId);
        } else {
            showAlert("Error", "Article not found!", Alert.AlertType.ERROR);
        }
    }

    private void saveActionToDB(String action) {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        Document userDoc = ratedArticles.find(new Document("username", username)).first();

        if (userDoc == null) {
            // Initialize a new document for the user if it doesn't exist
            userDoc = new Document("username", username)
                    .append("liked", new ArrayList<>())
                    .append("skipped", new ArrayList<>())
                    .append("saved", new ArrayList<>())
                    .append("read", new ArrayList<>());
            ratedArticles.insertOne(userDoc);
        }

        // Remove the articleId from other arrays before adding it to the target array
        if (action.equals("liked") || action.equals("skipped")) {
            // Remove the articleId from the opposite action's array
            String oppositeAction = action.equals("liked") ? "skipped" : "liked";
            ratedArticles.updateOne(
                    new Document("username", username),
                    Updates.pull(oppositeAction, Integer.parseInt(articleID))
            );
            // Add the articleId to the target action's array
            ratedArticles.updateOne(
                    new Document("username", username),
                    Updates.addToSet(action, Integer.parseInt(articleID))
            );
        } else if (action.equals("saved")) {
            // Toggle the articleId in the "saved" array
            boolean isSaved = userDoc.getList("saved", Integer.class).contains(Integer.parseInt(articleID));
            if (isSaved) {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.pull("saved", Integer.parseInt(articleID))
                );
            } else {
                ratedArticles.updateOne(
                        new Document("username", username),
                        Updates.addToSet("saved", Integer.parseInt(articleID))
                );
            }
        }

        // Ensure the articleId is added to the 'read' array
        ratedArticles.updateOne(
                new Document("username", username),
                Updates.addToSet("read", Integer.parseInt(articleID))
        );
    }

    @FXML
    public void onClickLike(ActionEvent event) {
        saveActionToDB("liked");
        showAlert("Success", "Liked the article with ID " + articleID, Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        saveActionToDB("skipped");
        showAlert("Success", "Skipped the article with ID " + articleID, Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        saveActionToDB("saved");
        showAlert("Success", "Saved the article with ID " + articleID, Alert.AlertType.INFORMATION);
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
