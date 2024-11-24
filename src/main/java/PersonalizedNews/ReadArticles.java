package PersonalizedNews;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.bson.Document;

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
            System.out.println("Article not found!");
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
        System.out.println("Article liked!");
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        saveActionToDB("skipped");
        System.out.println("Article skipped!");
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        saveActionToDB("saved");
        System.out.println("Article saved!");
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
}