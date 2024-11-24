package PersonalizedNews;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class WelcomePage implements Initializable {
    public ImageView WelcomeIcon;

    public void initialize(URL location, ResourceBundle resources) {
        loadImage(WelcomeIcon, "Welcome.png");
    }

    private void loadImage(ImageView imageView, String imagePath) {
    // Use the path relative to the resources folder
    Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
    if (image != null) {
        imageView.setImage(image);
    } else {
        System.err.println("Image resource not found: " + imagePath);
    }}

    public void onClickUser(ActionEvent event) {
        try {
            // Load User Login Page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserLogin.fxml"));
            Parent root = loader.load();

            // Get the Stage from the current scene and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 440, 280));
            root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
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
            stage.setScene(new Scene(root, 434, 298));
            root.getStylesheets().add(getClass().getResource("Personalized_News.css").toExternalForm());
            stage.setTitle("Administrator Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}