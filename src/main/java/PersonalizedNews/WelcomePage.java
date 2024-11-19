package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomePage {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void onClickUser(ActionEvent event) {
        try {
            // Load User Login Page
            root = FXMLLoader.load(getClass().getResource("UserLogin.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickAdministrator(ActionEvent event) {
        try {
            // Load Administrator Login Page
            root = FXMLLoader.load(getClass().getResource("AdministratorLogin.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Administrator Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
