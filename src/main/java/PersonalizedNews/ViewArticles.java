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
import org.apache.commons.text.similarity.CosineSimilarity;
import org.bson.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static PersonalizedNews.FetchArticlesCategory.preprocessText;

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

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

        executorService.submit(() -> {
            MongoDatabase database = DatabaseConnector.getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();
            if (userDoc == null) {
                promptForPreferences();
            }
            loadRecommendedArticles();
        });
    }

    private void promptForPreferences() {
        List<String> categories = Arrays.asList("Sports", "Travel", "Health", "Entertainment", "Politics", "Business", "AI", "Technology");
        List<String> selectedPreferences = new ArrayList<>();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(categories.get(0), categories);
        dialog.setTitle("Preference Selection");
        dialog.setHeaderText("Select Your Preferences");
        dialog.setContentText("Choose a category you like (add more later):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedPreferences::add);

        if (!selectedPreferences.isEmpty()) {
            MongoDatabase database = DatabaseConnector.getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            ratedArticles.insertOne(new Document("username", username)
                    .append("preferences", selectedPreferences)
                    .append("liked", new ArrayList<>())
                    .append("skipped", new ArrayList<>())
                    .append("read", new ArrayList<>()));
        }
    }

    private void displayArticles(List<Document> articles) {
        ObservableList<Article> observableArticles = FXCollections.observableArrayList();

        for (Document article : articles) {
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

    public void loadRecommendedArticles() {
        MongoDatabase database = DatabaseConnector.getDatabase();
        MongoCollection<Document> categorizedArticles = database.getCollection("CategorizedArticles");
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        // Fetch user interaction data
        executorService.submit(() -> {
            Document userDoc = ratedArticles.find(new Document("username", username)).first();

            if (userDoc == null || (userDoc.getList("liked", Integer.class).isEmpty() &&
                    userDoc.getList("read", Integer.class).isEmpty() &&
                    userDoc.getList("skipped", Integer.class).isEmpty())) {
                recommendBasedOnPreferences(categorizedArticles);
            } else {
                recommendBasedOnInteractions(categorizedArticles, userDoc);
            }
        });
    }

    private void recommendBasedOnInteractions(MongoCollection<Document> categorizedArticles, Document userDoc) {
        // Fetch interaction data
        List<Integer> likedArticles = userDoc.getList("liked", Integer.class);
        List<Integer> skippedArticles = userDoc.getList("skipped", Integer.class);
        List<Integer> savedArticles = userDoc.getList("saved", Integer.class);
        List<Integer> readArticles = userDoc.getList("read", Integer.class);

        // Count skipped and liked articles by category
        Map<String, Long> skippedCategoryCounts = skippedArticles.stream()
                .map(articleId -> getArticleCategory(categorizedArticles, articleId))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        Map<String, Long> likedCategoryCounts = likedArticles.stream()
                .map(articleId -> getArticleCategory(categorizedArticles, articleId))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        // Exclude categories skipped more than three times
        Set<String> excludedCategories = skippedCategoryCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Fetch available articles excluding skipped and read-only articles
        List<Document> availableArticles = categorizedArticles.find(new Document("articleId",
                new Document("$nin", Stream.concat(
                        Stream.concat(skippedArticles.stream(), readArticles.stream().filter(id -> !savedArticles.contains(id))),
                        likedArticles.stream()
                ).collect(Collectors.toList()))
        )).into(new ArrayList<>());

        // Include one random saved article
        List<Document> savedArticleList = categorizedArticles.find(new Document("articleId",
                new Document("$in", savedArticles)
        )).into(new ArrayList<>());
        Collections.shuffle(savedArticleList);
        List<Document> prioritizedSavedArticles = savedArticleList.stream().limit(1).collect(Collectors.toList());

        // Apply content-based filtering to rank liked articles
        List<Document> rankedLikedArticles = recommendArticlesUsingContent(availableArticles.stream()
                .filter(article -> likedCategoryCounts.containsKey(article.getString("category")) &&
                        !excludedCategories.contains(article.getString("category")))
                .collect(Collectors.toList()), likedArticles, categorizedArticles);

        // Distribute recommendations across liked categories proportionally
        Map<String, List<Document>> articlesByCategory = rankedLikedArticles.stream()
                .collect(Collectors.groupingBy(article -> article.getString("category")));

        List<Document> sortedRecommendations = new ArrayList<>();
        likedCategoryCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // Descending order by count
                .forEach(entry -> {
                    String category = entry.getKey();
                    if (articlesByCategory.containsKey(category)) {
                        sortedRecommendations.addAll(articlesByCategory.get(category));
                    }
                });

        // Combine saved and liked articles (limit to 10)
        List<Document> combinedRecommendations = Stream.concat(
                prioritizedSavedArticles.stream(),
                sortedRecommendations.stream()
        ).limit(10).collect(Collectors.toList());

        // Fallback to ensure 10 recommendations
        if (combinedRecommendations.size() < 10) {
            List<Document> fallbackArticles = availableArticles.stream()
                    .filter(article -> !excludedCategories.contains(article.getString("category")))
                    .limit(10 - combinedRecommendations.size())
                    .collect(Collectors.toList());
            combinedRecommendations.addAll(fallbackArticles);
        }

        // Display final recommendations
        displayArticles(combinedRecommendations);
    }

    private void recommendBasedOnPreferences(MongoCollection<Document> categorizedArticles) {
        MongoCollection<Document> ratedArticles = DatabaseConnector.getDatabase().getCollection("RatedArticles");
        Document userDoc = ratedArticles.find(new Document("username", username)).first();
        List<String> preferredCategories = userDoc.getList("preferences", String.class);

        // Fetch articles matching preferred categories
        List<Document> preferredArticles = categorizedArticles.find(
                new Document("category", new Document("$in", preferredCategories))
        ).into(new ArrayList<>());

        // Apply content-based filtering
        List<Document> rankedArticles = recommendArticlesUsingContent(preferredArticles, new ArrayList<>(), categorizedArticles);

        // Distribute recommendations evenly across preferences
        Map<String, List<Document>> articlesByCategory = rankedArticles.stream()
                .collect(Collectors.groupingBy(article -> article.getString("category")));

        List<Document> balancedRecommendations = new ArrayList<>();
        int limitPerCategory = Math.max(1, 20 / preferredCategories.size());

        for (String category : preferredCategories) {
            if (articlesByCategory.containsKey(category)) {
                balancedRecommendations.addAll(articlesByCategory.get(category).stream()
                        .limit(limitPerCategory)
                        .collect(Collectors.toList()));
            }
        }

        // Ensure exactly 20 recommendations
        balancedRecommendations = balancedRecommendations.stream().limit(20).collect(Collectors.toList());

        displayArticles(balancedRecommendations);
    }

    private String getArticleCategory(MongoCollection<Document> collection, int articleId) {
            Document article = collection.find(new Document("articleId", articleId)).first();
            return article != null ? article.getString("category") : "Uncategorized";
        }

    private List<Document> recommendArticlesUsingContent(List<Document> availableArticles, List<Integer> likedArticles, MongoCollection<Document> collection) {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        Map<CharSequence, Integer> userProfileVector = new ConcurrentHashMap<>();

        // Build profile vector from liked articles
        for (int articleId : likedArticles) {
            Document article = collection.find(new Document("articleId", articleId)).first();
            if (article != null) {
                Map<CharSequence, Integer> articleVector = vectorizeContent(article.getString("content"));
                articleVector.forEach((key, value) -> userProfileVector.merge(key, value, Integer::sum));
            }
        }

        // Rank available articles by similarity
        return availableArticles.stream()
                .sorted((a1, a2) -> {
                    Map<CharSequence, Integer> vec1 = vectorizeContent(a1.getString("content"));
                    Map<CharSequence, Integer> vec2 = vectorizeContent(a2.getString("content"));
                    double score1 = cosineSimilarity.cosineSimilarity(userProfileVector, vec1);
                    double score2 = cosineSimilarity.cosineSimilarity(userProfileVector, vec2);
                    return Double.compare(score2, score1); // Descending order
                })
                .collect(Collectors.toList());
    }


    private Map<CharSequence, Integer> vectorizeContent(String content) {
            Map<CharSequence, Integer> vector = new HashMap<>();
            String[] words = preprocessText(content).split("\\s+");
            for (String word : words) {
                vector.put(word, vector.getOrDefault(word, 0) + 1);
            }
            return vector;
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