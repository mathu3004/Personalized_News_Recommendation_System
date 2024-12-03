module com.example.personalized_news_recommendation_system {
    // Required JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // Required MongoDB modules
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    // Required third-party libraries
    requires org.apache.opennlp.tools;
    requires org.apache.commons.text;

    // Required Java desktop module
    requires java.desktop;

    // Open the package for reflective access (JavaFX base and FXML modules)
    opens PersonalizedNews to javafx.graphics, javafx.fxml, java.base;

    // Export the package for other modules
    exports PersonalizedNews.MainClass;
    opens PersonalizedNews.MainClass to java.base, javafx.fxml, javafx.graphics;
    exports PersonalizedNews.AdminMaintainance;
    opens PersonalizedNews.AdminMaintainance to java.base, javafx.fxml, javafx.graphics;
    exports PersonalizedNews.UserMaintainance;
    opens PersonalizedNews.UserMaintainance to java.base, javafx.fxml, javafx.graphics;
    exports PersonalizedNews.Categorization;
    opens PersonalizedNews.Categorization to java.base, javafx.fxml, javafx.graphics;
    exports PersonalizedNews.Application;
    opens PersonalizedNews.Application to java.base, javafx.fxml, javafx.graphics;
}