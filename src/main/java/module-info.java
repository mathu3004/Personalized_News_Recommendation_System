module com.example.personalized_news_recommendation_system {
    requires javafx.controls;
    requires javafx.fxml;

    opens PersonalizedNews to javafx.fxml, javafx.graphics;
    exports PersonalizedNews;
}