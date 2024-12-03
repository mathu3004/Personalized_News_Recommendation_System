package PersonalizedNews.UserMaintainance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserHome {

    public void openCategoryView(String category,ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PersonalizedNews/ViewCategorizedArticles.fxml"));
            Parent root = loader.load();

            ViewCategorizedArticles controller = loader.getController();
            controller.loadArticlesFromDatabase(category);

            // Get the current stage from the event source
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 430));
            root.getStylesheets().add(getClass().getResource("/PersonalizedNews/Personalized_News.css").toExternalForm());
            stage.setTitle(category + " Articles");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickViewAI(ActionEvent event) {
        openCategoryView("AI", event);
    }

    @FXML
    public void onClickViewTech(ActionEvent event) {
        openCategoryView("Technology", event);
    }

    @FXML
    public void onClickViewBusiness(ActionEvent event) {
        openCategoryView("Business", event);
    }

    @FXML
    public void onClickViewTravel(ActionEvent event) {
        openCategoryView("Travel", event);
    }

    @FXML
    public void onClickViewSports(ActionEvent event) {
        openCategoryView("Sports", event);
    }

    @FXML
    public void onClickViewEntertainment(ActionEvent event) {
        openCategoryView("Entertainment", event);
    }

    @FXML
    public void onClickViewHealth(ActionEvent event) {
        openCategoryView("Health", event);
    }

    @FXML
    public void onClickViewPolitical(ActionEvent event) {
        openCategoryView("Politics", event);
    }

    @FXML
    public void onClickViewSaved(ActionEvent event) {openCategoryView("Saved", event);}

    @FXML
    public void onClickViewLiked(ActionEvent event) {openCategoryView("Liked", event);}

    @FXML
    public void onClickViewRead(ActionEvent event) {openCategoryView("Read", event);}
}
