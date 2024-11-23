package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminViewArticles {
    @FXML
    public TableView AdminViewArticlesTable;
    @FXML
    public TableColumn viewArticleID;
    @FXML
    public TableColumn viewTitle;
    @FXML
    public TableColumn viewAuthor;
    @FXML
    public TableColumn viewDescription;
    @FXML
    public TableColumn viewPublishedDate;
    @FXML
    public TableColumn viewContent;
    @FXML
    public TableColumn viewCategory;

    public void onClickBack(ActionEvent event) {
        try {
            // Navigate to the signup page
            Parent root = FXMLLoader.load(getClass().getResource("ManageArticles.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Manage Articles Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
