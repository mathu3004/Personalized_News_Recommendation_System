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
import java.util.List;

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

        // Check if user exists in RatedArticles
        Document userDoc = ratedArticles.find(new Document("username", username)).first();
        if (userDoc != null) {
            // Fetch liked and skipped articles for interactive users
            liked = userDoc.getList("liked", Integer.class);
            skipped = userDoc.getList("skipped", Integer.class);
            read = userDoc.getList("read", Integer.class);

            // Recommend articles based on liked categories
            for (int articleId : liked) {
                Document likedArticle = articles.find(new Document("articleId", articleId)).first();
                if (likedArticle != null) {
                    String category = likedArticle.getString("category");
                    List<Document> similarArticles = articles.find(new Document("category", category)).into(new ArrayList<>());
                    for (Document article : similarArticles) {
                        int id = article.getInteger("articleId");
                        if (!liked.contains(id) && !skipped.contains(id) && !containsArticle(recommendedArticles, id)) {
                            recommendedArticles.add(article);
                        }
                    }
                }
            }
        } else {
            // Fallback to preferences for non-interactive users
            Document userAccount = userAccounts.find(new Document("username", username)).first();
            if (userAccount != null) {
                preferences = userAccount.getList("preferences", String.class);
                for (String preference : preferences) {
                    List<Document> preferenceArticles = categorizedArticles.find(new Document("category", preference)).into(new ArrayList<>());
                    for (Document article : preferenceArticles) {
                        int id = article.getInteger("articleId");
                        if (!containsArticle(recommendedArticles, id)) {
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
