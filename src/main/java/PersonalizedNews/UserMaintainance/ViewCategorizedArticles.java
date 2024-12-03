package PersonalizedNews.UserMaintainance;

import PersonalizedNews.MainClass.Article;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
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
import java.util.List;

public class ViewCategorizedArticles {
    @FXML
    public TableView<Article> viewCategorizedTable;
    @FXML
    public TableColumn<Article, String> viewTitle;
    @FXML
    public TableColumn<Article, String> viewAuthor;
    @FXML
    public TableColumn<Article, String> viewDescription;
    @FXML
    public TableColumn<Article, String> viewDate;
    @FXML
    public TableColumn<Article, String> viewCategory;
    @FXML
    public TableColumn<Article, RadioButton> viewSelect;
    @FXML
    public Button skipButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button likeButton;

    private String username;

    private ObservableList<Article> articles = FXCollections.observableArrayList();

    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; // Update with your connection string
    private static final String DATABASE_NAME = "News"; // Update with your database name
    private static final String COLLECTION_NAME = "CategorizedArticles"; // Update with your collection name

    @FXML
    public void initialize() {
        // Bind TableView columns to Article properties
        viewTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        viewAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        viewDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        viewDate.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        viewCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        viewSelect.setCellValueFactory(new PropertyValueFactory<>("select"));
        initializeUsername();
        loadArticlesFromDatabase(null);
        // Listener to select radio button when a row is clicked
        viewCategorizedTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
        // Initially load all articles
        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            // Ensure RatedArticles document exists for the user
            Document userDoc = ratedArticles.find(Filters.eq("username", username)).first();
            if (userDoc == null) {
                userDoc = new Document("username", username)
                        .append("liked", new ArrayList<>())
                        .append("skipped", new ArrayList<>())
                        .append("saved", new ArrayList<>())
                        .append("read", new ArrayList<>());
                ratedArticles.insertOne(userDoc);
            }
        }
    }

    @FXML
    public void loadArticlesFromDatabase(String category) {
        articles.clear();

        ToggleGroup toggleGroup = new ToggleGroup(); // Create a new ToggleGroup

        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
            MongoCollection<Document> ratedCollection = database.getCollection("RatedArticles");

            FindIterable<Document> documents;
            if (category == null) {
                documents = collection.find();
            } else if (category.equals("Saved") || category.equals("Liked") || category.equals("Read")) {
                // Fetch user-specific saved or liked articles
                Document userDoc = ratedCollection.find(Filters.eq("username", username)).first();
                if (userDoc != null) {
                    List<Integer> articleIds;
                    if (category.equals("Saved")) {
                        articleIds = userDoc.getList("saved", Integer.class);
                    } else if (category.equals("Liked")){ // "Liked"
                        articleIds = userDoc.getList("liked", Integer.class);
                    } else {
                        articleIds = userDoc.getList("read", Integer.class);
                    }
                    documents = collection.find(Filters.in("articleId", articleIds));
                } else {
                    documents = collection.find(Filters.eq("articleId", -1)); // Return no results if userDoc is null
                }
            } else {
                documents = collection.find(Filters.eq("category", category));
            }

            for (Document doc : documents) {
                RadioButton radioButton = new RadioButton();
                radioButton.setToggleGroup(toggleGroup); // Add RadioButton to the ToggleGroup
                articles.add(new Article(
                        doc.getInteger("articleId"),
                        doc.getString("category"),
                        doc.getString("author"),
                        doc.getString("publishedAt"),
                        doc.getString("title"),
                        doc.getString("description"),
                        radioButton
                ));
            }
        }
        viewCategorizedTable.setItems(articles);
    }

    private void resetButtonStates(Article selectedArticle) {
        if (selectedArticle == null) return;

        MongoDatabase database = ViewCustomArticles.getDatabase();
        MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

        // Fetch user document
        Document userDoc = ratedArticles.find(new Document("username", username)).first();

        if (userDoc != null) {
            List<Integer> likedArticles = userDoc.getList("liked", Integer.class);
            List<Integer> savedArticles = userDoc.getList("saved", Integer.class);
            List<Integer> skippedArticles = userDoc.getList("skipped", Integer.class);

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

            if (skippedArticles != null && skippedArticles.contains(selectedArticle.getArticleId())) {
                skipButton.setText("Unskip");
            } else {
                skipButton.setText("Skip");
            }
        } else {
            // Default state if no user document is found
            likeButton.setText("Like");
            saveButton.setText("Save");
            skipButton.setText("Skip");
        }
    }

    private void saveActionToDB(String action, int articleId) {
        MongoDatabase database = ViewCustomArticles.getDatabase();
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
            // Add the article to the "read" list in RatedArticles
            saveActionToDB("read", selectedArticle.getArticleId());
            showAlert("Success", "Read the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
            try {
                // Load the ReadArticles view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PersonalizedNews/ReadArticles.fxml"));
                Parent root = loader.load();

                // Pass the article data to the ReadArticles controller
                ReadArticles controller = loader.getController();
                controller.setUsername(username);
                controller.loadArticleDetails(selectedArticle.getArticleId());

                // Set the scene and show the ReadArticles view
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 600, 453));
                root.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());
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
            MongoDatabase database = ViewCustomArticles.getDatabase();
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
            MongoDatabase database = ViewCustomArticles.getDatabase();
            MongoCollection<Document> ratedArticles = database.getCollection("RatedArticles");

            Document userDoc = ratedArticles.find(new Document("username", username)).first();
            if (userDoc != null) {
                List<Integer> skippedArticles = userDoc.getList("skipped", Integer.class);

                if (skippedArticles != null && skippedArticles.contains(selectedArticle.getArticleId())) {
                    // Unskip the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.pull("skipped", selectedArticle.getArticleId())
                    );
                    saveButton.setText("Skip");
                    showAlert("Success", "Unskipped the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                } else {
                    // Skip the article
                    ratedArticles.updateOne(
                            new Document("username", username),
                            Updates.addToSet("skipped", selectedArticle.getArticleId())
                    );
                    saveButton.setText("Unskip");
                    showAlert("Success", "Skipped the article with ID " + selectedArticle.getArticleId(), Alert.AlertType.INFORMATION);
                }
            }
        } else {
            showAlert("Error", "No article selected!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onClickSave(ActionEvent event) {
        Article selectedArticle = getSelectedArticle();
        if (selectedArticle != null) {
            MongoDatabase database = ViewCustomArticles.getDatabase();
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
        return viewCategorizedTable.getItems().stream()
                .filter(article -> article.getSelect().isSelected())
                .findFirst()
                .orElse(null);
    }

    public void onClickBackDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/PersonalizedNews/UserPortal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 855, 525));
            root.getStylesheets().add(getClass().getResource("/PersonalizedNews/GlowButton.css").toExternalForm());
            stage.setTitle("User Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}