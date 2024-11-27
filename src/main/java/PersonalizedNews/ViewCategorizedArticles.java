package PersonalizedNews;

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
    }

    public void initializeUsername() {
        username = SessionManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            System.out.println("Error: Username is not set in session!");
            return;
        }
        System.out.println("Logged in as: " + username);
        // Initially load all articles

    }

    @FXML
    public void loadArticlesFromDatabase(String category) {
        articles.clear();

        ToggleGroup toggleGroup = new ToggleGroup(); // Create a new ToggleGroup

        try (MongoClient mongoClient = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            FindIterable<Document> documents;
            if (category == null) {
                documents = collection.find();
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
            // Add the article to the "read" list in RatedArticles
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
        return viewCategorizedTable.getItems().stream()
                .filter(article -> article.getSelect().isSelected())
                .findFirst()
                .orElse(null);
    }

    public void onClickBackDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("UserPortal.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 855, 525));
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