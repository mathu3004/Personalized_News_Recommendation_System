package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserHome {

    public void openCategoryView(String category,ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewCategorizedArticles.fxml"));
            Parent root = loader.load();

            ViewCategorizedArticles controller = loader.getController();
            controller.loadArticlesFromDatabase(category);

            // Get the current stage from the event source
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 600, 430)); // Set the new scene on the current stage
            root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
            stage.setTitle(category + " Articles");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickViewAI(ActionEvent event) {
        openCategoryView("AI", event);
    }

    public void onClickViewTech(ActionEvent event) {
        openCategoryView("Technology", event);
    }

    public void onClickViewBusiness(ActionEvent event) {
        openCategoryView("Business", event);
    }

    public void onClickViewTravel(ActionEvent event) {
        openCategoryView("Travel", event);
    }

    public void onClickViewSports(ActionEvent event) {
        openCategoryView("Sports", event);
    }

    public void onClickViewEntertainment(ActionEvent event) {
        openCategoryView("Entertainment", event);
    }

    public void onClickViewHealth(ActionEvent event) {
        openCategoryView("Health", event);
    }

    public void onClickViewPolitical(ActionEvent event) {
        openCategoryView("Politics", event);
    }
}
