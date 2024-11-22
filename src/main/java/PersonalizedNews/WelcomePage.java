package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomePage {

    public void onClickUser(ActionEvent event) {
        try {
            // Load User Login Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserLogin.fxml"));
            Parent root = loader.load();

            // Get the Stage from the current scene and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 700));
            stage.setTitle("User Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickAdministrator(ActionEvent event) {
        try {
            // Load Administrator Login Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdministratorLogin.fxml"));
            Parent root = loader.load();

            // Get the Stage from the current scene and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 980, 700));
            stage.setTitle("Administrator Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
