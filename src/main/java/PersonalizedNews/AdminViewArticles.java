package PersonalizedNews;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class AdminViewArticles {

    @FXML
    public TableView<Article> AdminViewArticlesTable;
    @FXML
    public TableColumn<Article, Integer> viewArticleID;
    @FXML
    public TableColumn<Article, String> viewTitle;
    @FXML
    public TableColumn<Article, String> viewAuthor;
    @FXML
    public TableColumn<Article, String> viewDescription;
    @FXML
    public TableColumn<Article, String> viewPublishedDate;
    @FXML
    public TableColumn<Article, String> viewContent;
    @FXML
    public TableColumn<Article, String> viewCategory;

    private final ObservableList<Article> articles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set cell value factories to bind table columns to Article properties
        viewArticleID.setCellValueFactory(new PropertyValueFactory<>("articleId"));
        viewTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        viewAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        viewDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        viewPublishedDate.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        viewContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        viewCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Load articles into the table
        loadArticlesFromDB();
    }

    private void loadArticlesFromDB() {
        String mongoUri = "mongodb://127.0.0.1:27017";

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase("News");
            MongoCollection<Document> categorizedCollection = database.getCollection("CategorizedArticles");

            // Fetch all categorized articles
            FindIterable<Document> categorizedArticles = categorizedCollection.find();

            for (Document doc : categorizedArticles) {
                Article article = new Article(
                        doc.getInteger("articleId"),
                        doc.getString("title"),
                        doc.getString("author"),
                        doc.getString("description"),
                        doc.getString("publishedDate"),
                        doc.getString("category"),
                        doc.getString("content")
                );
                articles.add(article);
            }

            AdminViewArticlesTable.setItems(articles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickBack(ActionEvent event) {
        try {
            // Navigate back to the Manage Articles page
            Parent root = FXMLLoader.load(getClass().getResource("ManageArticles.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 574, 400));
            root.getStylesheets().add(getClass().getResource("Button.css").toExternalForm());
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
