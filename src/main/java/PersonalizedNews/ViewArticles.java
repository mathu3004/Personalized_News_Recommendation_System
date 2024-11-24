package PersonalizedNews;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;

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
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");
        MongoCollection<Document> articles = database.getCollection("Articles");
        MongoCollection<Document> categorizedArticles = database.getCollection("CategorizedArticles");
        MongoCollection<Document> userAccounts = database.getCollection("UserAccounts");

        List<Document> recommendedArticles = new ArrayList<>();
        List<Integer> liked = new ArrayList<>();
        List<Integer> read = new ArrayList<>();
        List<Integer> skipped = new ArrayList<>();
        List<String> preferences = new ArrayList<>();
        List<String> prioritizedCategories = new ArrayList<>();

        // Check if user exists in RatedArticles
        Document userDoc = ratedArticles.find(new Document("username", username)).first();
        if (userDoc != null) {
            // Fetch liked and skipped articles for interactive users
            liked = userDoc.getList("liked", Integer.class);
            skipped = userDoc.getList("skipped", Integer.class);
            read = userDoc.getList("read", Integer.class);

            // Recommend articles based on liked categories
            Map<String, Integer> categoryWeights = new HashMap<>();
            for (int articleId : liked) {
                Document likedArticle = articles.find(new Document("articleId", articleId)).first();
                if (likedArticle != null) {
                    String category = likedArticle.getString("category");
                    categoryWeights.put(category, categoryWeights.getOrDefault(category, 0) + 2); // Higher weight for likes
                }
            }
            for (int articleId : read) {
                Document readArticle = articles.find(new Document("articleId", articleId)).first();
                if (readArticle != null) {
                    String category = readArticle.getString("category");
                    categoryWeights.put(category, categoryWeights.getOrDefault(category, 0) + 1); // Lower weight for reads
                }
            }
            for (int articleId : skipped) {
                Document skippedArticle = articles.find(new Document("articleId", articleId)).first();
                if (skippedArticle != null) {
                    String category = skippedArticle.getString("category");
                    categoryWeights.put(category, categoryWeights.getOrDefault(category, 0) - 2); // Negative weight for skips
                }
            }

            // Sort categories by weight (descending order)
            prioritizedCategories = categoryWeights.entrySet().stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();

            // Recommend articles from prioritized categories
            for (String category : prioritizedCategories) {
                List<Document> categoryArticles = articles.find(new Document("category", category)).into(new ArrayList<>());
                for (Document article : categoryArticles) {
                    int articleId = article.getInteger("articleId");
                    if (!liked.contains(articleId) && !read.contains(articleId) && !skipped.contains(articleId) &&
                            !containsArticle(recommendedArticles, articleId)) {
                        recommendedArticles.add(article);
                    }
                }
            }
        } else {
            // Fallback to preferences
            Document userAccount = userAccounts.find(new Document("username", username)).first();
            if (userAccount != null) {
                preferences = userAccount.getList("preferences", String.class);
                for (String preference : preferences) {
                    List<Document> preferenceArticles = articles.find(new Document("category", preference)).into(new ArrayList<>());
                    for (Document article : preferenceArticles) {
                        int articleId = article.getInteger("articleId");
                        if (!containsArticle(recommendedArticles, articleId)) {
                            recommendedArticles.add(article);
                        }
                    }
                }
            } else {
                System.out.println("No preferences found for user!");
            }
        }

        // Populate TableView
        ObservableList<Article> observableArticles = FXCollections.observableArrayList();
        ToggleGroup toggleGroup = new ToggleGroup();

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
            articleObj.getSelect().setToggleGroup(toggleGroup);
            observableArticles.add(articleObj);
        }

        articlesTable.setItems(observableArticles);
        articlesTable.refresh();
    }

    private boolean containsArticle(List<Document> articles, int articleId) {
        for (Document article : articles) {
            if (article.getInteger("articleId") == articleId) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void onClickRead(ActionEvent event) {
        Article selectedArticle = articlesTable.getItems().stream()
                .filter(article -> article.getSelect().isSelected())
                .findFirst()
                .orElse(null);

        if (selectedArticle != null) {
            MongoDatabase database = DatabaseConnector.getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            ratedArticles.updateOne(
                    new Document("username", username),
                    new Document("$addToSet", new Document("read", selectedArticle.getArticleId()))
            );

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ReadArticles.fxml"));
                Parent root = loader.load();

                ReadArticles controller = loader.getController();
                controller.setUsername(username);
                controller.loadArticleDetails(selectedArticle.getArticleId());

                Stage stage = (Stage) articlesTable.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No article selected!");
        }
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        Article selectedArticle = articlesTable.getItems().stream()
                .filter(article -> article.getSelect().isSelected())
                .findFirst()
                .orElse(null);

        if (selectedArticle != null) {
            MongoDatabase database = DatabaseConnector.getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            ratedArticles.updateOne(
                    new Document("username", username),
                    new Document("$addToSet", new Document("skipped", selectedArticle.getArticleId()))
            );

            System.out.println("Skipped article: " + selectedArticle.getTitle());
            loadRecommendedArticles();
        } else {
            System.out.println("No article selected!");
        }
    }
}
