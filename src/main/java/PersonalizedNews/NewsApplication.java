package PersonalizedNews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NewsApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("WelcomePage.fxml"));
        Scene scene = new Scene(root, 600,450);

        // Load the application icon
        Image icon = new Image("/NEWSICON.png");
        stage.getIcons().add(icon);
        stage.setTitle("Welcome to Mark's News");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
