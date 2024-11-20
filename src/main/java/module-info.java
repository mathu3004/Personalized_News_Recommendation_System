module com.example.personalized_news_recommendation_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.mongodb.driver.sync.client;
    requires java.desktop;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.apache.opennlp.tools; // Add this line

    opens PersonalizedNews to javafx.graphics, javafx.fxml;

    exports com.example.personalized_news_recommendation_system;
}