package PersonalizedNews.Application;

import PersonalizedNews.Categorization.FetchArticlesCategory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsApplication extends Application {
    // Create a fixed thread pool with a configurable number of threads
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private static Stage primaryStage; // Keep a reference to the primary stage

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // Store the primary stage reference
        initializeUI(stage);
        initializeBackgroundTasks();
    }

    private void initializeUI(Stage stage) throws Exception {
        // Load the FXML and set up the scene
        Parent root = FXMLLoader.load(getClass().getResource("/PersonalizedNews/WelcomePage.fxml"));
        Scene scene = new Scene(root, 600, 450);
        root.getStylesheets().add(getClass().getResource("/PersonalizedNews/Button.css").toExternalForm());

        // Load the application icon
        Image icon = new Image("/NEWSICON.png");
        stage.getIcons().add(icon);
        stage.setTitle("Welcome to Mark's News");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeBackgroundTasks() {
        // Submit FetchArticles initialization task to the executor
        executorService.submit(() -> {
            try {
                System.out.println("Initializing FetchArticles...");
                FetchArticlesCategory.initialize();
                Platform.runLater(() -> System.out.println("FetchArticles initialized successfully!"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Method to restart the application dynamically
    public static void restart() {
        Platform.runLater(() -> {
            try {
                primaryStage.close(); // Close the current stage
                primaryStage = new Stage(); // Create a new stage
                new NewsApplication().start(primaryStage); // Restart the application
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Gracefully shut down the executor service
        executorService.shutdown();
        System.out.println("ExecutorService shut down.");
    }

    public static void main(String[] args) {
        launch();
    }
}
