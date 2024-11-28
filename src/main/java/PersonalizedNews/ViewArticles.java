package PersonalizedNews;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import javafx.application.Platform;
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
    public Button saveButton;
    @FXML
    public Button skipButton;
    @FXML
    public Button likeButton;
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
    private static final String CONNECTION_STRING = "mongodb://127.0.0.1:27017";
    private static final String DATABASE_NAME = "News";
    private static MongoClient mongoClient = null;

    @FXML
    public void initialize() {
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        columnAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        columnPublishedDate.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        columnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnSelect.setCellValueFactory(new PropertyValueFactory<>("select"));
        // Listener to select radio button when a row is clicked
        articlesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Article selectedArticle = (Article) newValue;
                if (selectedArticle.getSelect() != null) {
                    selectedArticle.getSelect().setSelected(true); // Select the radio button
                }
                resetButtonStates(selectedArticle); // Reset buttons based on the selected article
            }
        });
    }

    public void initializeUsername() {
        username = SessionManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            System.out.println("Error: Username is not set in session!");
            return;
        }
        System.out.println("Logged in as: " + username);

        executorService.submit(() -> {
            MongoDatabase database = getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();
            loadRecommendedArticles();
        });
    }

    private void displayArticles(List<Document> articles) {
        ToggleGroup toggleGroup = new ToggleGroup(); // Create a new ToggleGroup
        ObservableList<Article> observableArticles = FXCollections.observableArrayList();

        for (Document article : articles) {
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(toggleGroup);
            Article articleObj = new Article(
                    article.getInteger("articleId"),
                    article.getString("category"),
                    article.getString("author"),
                    article.getString("publishedAt"),
                    article.getString("title"),
                    article.getString("description"),
                    radioButton
            );
            observableArticles.add(articleObj);
        }

        Platform.runLater(() -> {
            articlesTable.setItems(observableArticles);
            articlesTable.refresh();
        });
    }

    private void resetButtonStates(Article selectedArticle) {
        executorService.submit(() -> {
        if (selectedArticle == null) return;

        MongoDatabase database = getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        // Fetch user document
        Document userDoc = ratedArticles.find(new Document("username", username)).first();

        Platform.runLater(() -> {
        if (userDoc != null) {
            List<Integer> likedArticles = userDoc.getList("liked", Integer.class);
            List<Integer> savedArticles = userDoc.getList("saved", Integer.class);

            // Reset or update button text for Like/Unlike
            if (likedArticles != null && likedArticles.contains(selectedArticle.getArticleId())) {
                likeButton.setText("Unlike");
            } else {
                likeButton.setText("Like");
            }

            // Reset or update button text for Save/Unsave
            if (savedArticles != null && savedArticles.contains(selectedArticle.getArticleId())) {
                saveButton.setText("Unsave");
            } else {
                saveButton.setText("Save");
            }
        } else {
            // Default state if no user document is found
            likeButton.setText("Like");
            saveButton.setText("Save");
        }
        });
        });
    }


    public void loadRecommendedArticles() {
        executorService.submit(() -> {
        MongoDatabase database = getDatabase();
        MongoCollection<Document> categorizedArticles = database.getCollection("CategorizedArticles");
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");
        MongoCollection<Document> userAccounts = database.getCollection("UserAccounts");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();

            if (userDoc == null || (userDoc.getList("liked", Integer.class).isEmpty() &&
                    userDoc.getList("read", Integer.class).isEmpty() &&
                    userDoc.getList("skipped", Integer.class).isEmpty())) {
                // No interactions; use preferences from UserAccounts
                recommendUsingStoredPreferences(userAccounts, categorizedArticles);
            } else {
                // Use interaction-based recommendations
                recommendBasedOnInteractions(categorizedArticles, userDoc);
            }
        });
    }

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    private void recommendBasedOnInteractions(MongoCollection<Document> categorizedArticles, Document userDoc) {
        executorService.submit(() -> {
        // Fetch interaction data
        List<Integer> likedArticles = userDoc.getList("liked", Integer.class);
        List<Integer> skippedArticles = userDoc.getList("skipped", Integer.class);
        List<Integer> savedArticles = userDoc.getList("saved", Integer.class);
        List<Integer> readArticles = userDoc.getList("read", Integer.class);

        // Count skipped categories and exclude those skipped more than 3 times
        Map<String, Long> skippedCategoryCounts = skippedArticles.stream()
                .map(articleId -> getArticleCategory(categorizedArticles, articleId))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        Set<String> excludedCategories = skippedCategoryCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        System.out.println("Excluded categories: " + excludedCategories);

        // Fetch available articles excluding liked, skipped, read-only, and excluded categories
        List<Document> availableArticles = categorizedArticles.find(new Document("articleId",
                        new Document("$nin", Stream.concat(
                                Stream.concat(skippedArticles.stream(), readArticles.stream()),
                                likedArticles.stream()
                        ).collect(Collectors.toList()))
                )).into(new ArrayList<>())
                .stream()
                .filter(article -> !excludedCategories.contains(article.getString("category"))) // Exclude skipped categories
                .collect(Collectors.toList());

        System.out.println("Available articles: " + availableArticles.size());

        // Include saved articles excluding those already liked, read or skipped
        List<Document> savedArticleList = categorizedArticles.find(
                        new Document("articleId",
                                new Document("$in", savedArticles)
                        )
                ).into(new ArrayList<>())
                .stream()
                .filter(article -> !likedArticles.contains(article.getInteger("articleId"))) // Exclude liked articles
                .filter(article -> !skippedArticles.contains(article.getInteger("articleId"))) // Exclude skipped articles
                .filter(article -> !readArticles.contains(article.getInteger("articleId"))) // Exclude read articles
                .collect(Collectors.toList());

        System.out.println("Saved articles selected: " + savedArticleList.size());

        // Recommend articles from liked categories using content-based filtering
        List<Document> rankedLikedArticles = recommendArticlesUsingContent(availableArticles.stream()
                .filter(article -> !excludedCategories.contains(article.getString("category")))
                .collect(Collectors.toList()), likedArticles, categorizedArticles);

        System.out.println("Ranked liked articles: " + rankedLikedArticles.size());

        // Combine prioritized saved articles with ranked liked articles
        List<Document> combinedRecommendations = Stream.concat(
                        savedArticleList.stream(), // Include filtered saved articles
                        rankedLikedArticles.stream()
                ).distinct() // Ensure no duplicate articles
                .limit(20)
                .collect(Collectors.toList());

        // Fallback to ensure 20 recommendations
        if (combinedRecommendations.size() < 20) {
            List<Document> fallbackArticles = availableArticles.stream()
                    .filter(article -> !combinedRecommendations.contains(article)) // Avoid duplicates
                    .limit(20 - combinedRecommendations.size())
                    .collect(Collectors.toList());
            combinedRecommendations.addAll(fallbackArticles);
        }

        System.out.println("Final recommendations: " + combinedRecommendations.size());
        displayArticles(combinedRecommendations);
        });
    }


    private void recommendUsingStoredPreferences(MongoCollection<Document> userAccounts, MongoCollection<Document> categorizedArticles) {
        // Fetch user preferences from UserAccounts
        executorService.submit(() -> {
        Document userAccount = userAccounts.find(new Document("username", username)).first();
        if (userAccount == null || !userAccount.containsKey("preferences")) {
            System.out.println("Error: No preferences found for user in UserAccounts!");
            displayArticles(Collections.emptyList()); // Show no recommendations
            return;
        }

        List<String> preferences = userAccount.getList("preferences", String.class);
        if (preferences.isEmpty()) {
            System.out.println("Preferences list is empty for user: " + username);
            displayArticles(Collections.emptyList());
            return;
        }

        System.out.println("User preferences retrieved: " + preferences);

        // Fetch articles matching preferred categories
        List<Document> articlesByPreference = categorizedArticles.find(
                new Document("category", new Document("$in", preferences))
        ).into(new ArrayList<>());

        System.out.println("Found " + articlesByPreference.size() + " articles for preferred categories.");

        // Group articles by category
        Map<String, List<Document>> articlesByCategory = articlesByPreference.stream()
                .collect(Collectors.groupingBy(article -> article.getString("category")));

        // Distribute articles equally among categories
        List<Document> balancedRecommendations = new ArrayList<>();
        int limitPerCategory = Math.max(1, 20 / preferences.size()); // Equal distribution

        for (String category : preferences) {
            if (articlesByCategory.containsKey(category)) {
                balancedRecommendations.addAll(
                        articlesByCategory.get(category).stream()
                                .limit(limitPerCategory)
                                .collect(Collectors.toList())
                );
            }
        }

        // Fallback: Ensure exactly 20 recommendations if less than 20 articles are found
        if (balancedRecommendations.size() < 20) {
            List<Document> fallbackArticles = articlesByPreference.stream()
                    .filter(article -> !balancedRecommendations.contains(article))
                    .limit(20 - balancedRecommendations.size())
                    .collect(Collectors.toList());
            balancedRecommendations.addAll(fallbackArticles);
        }

        System.out.println("Balanced recommendations prepared with " + balancedRecommendations.size() + " articles.");
        displayArticles(balancedRecommendations);
    });
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
        executorService.submit(() -> {
        MongoDatabase database = getDatabase();
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
        }});
    }

    @FXML
    public void onClickRead(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            saveActionToDB("read", selectedArticle.getArticleId());
            showAlert("Success", "Read the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
            Platform.runLater(() -> {
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
                stage.setScene(new Scene(root, 600, 453));
                root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
                stage.setTitle("Read Article");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            });
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickLike(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            MongoDatabase database = getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();
            if (userDoc != null) {
                List<Integer> likedArticles = userDoc.getList("liked", Integer.class);

                if (likedArticles != null && likedArticles.contains(selectedArticle.getArticleId())) {
                    // Unlike the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.pull("liked", selectedArticle.getArticleId())
                    );
                    likeButton.setText("Like");
                    showAlert("Success", "Unliked the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                } else {
                    // Like the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.addToSet("liked", selectedArticle.getArticleId())
                    );
                    likeButton.setText("Unlike");
                    showAlert("Success", "Liked the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                }
            }
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickSkip(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            // Show confirmation dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmation");
            confirmDialog.setHeaderText(null);
            confirmDialog.setContentText("Are you sure you want to skip this article?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Save the skip action to the database
                saveActionToDB("skipped", selectedArticle.getArticleId());

                // Remove the skipped article from the TableView
                articlesTable.getItems().remove(selectedArticle);
                articlesTable.refresh();

                // Show success message
                showAlert("Success", "Skipped the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            MongoDatabase database = getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();
            if (userDoc != null) {
                List<Integer> savedArticles = userDoc.getList("saved", Integer.class);

                if (savedArticles != null && savedArticles.contains(selectedArticle.getArticleId())) {
                    // Unsave the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.pull("saved", selectedArticle.getArticleId())
                    );
                    saveButton.setText("Save");
                    showAlert("Success", "Unsaved the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                } else {
                    // Save the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.addToSet("saved", selectedArticle.getArticleId())
                    );
                    saveButton.setText("Unsave");
                    showAlert("Success", "Saved the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                }
            }
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
        Platform.runLater(() -> {
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
}