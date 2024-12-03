package PersonalizedNews.MainClass;

import java.time.LocalDate;
import java.util.*;

public class MainClass {
    public static void main(String[] args) {
        // Create Admins
        Admin admin1 = new Admin("admin1", "admin1@example.com", "adminPass123", "Editor");
        Admin admin2 = new Admin("admin2", "admin2@example.com", "adminPass456", "Manager");

        // Create Users
        User user1 = new User("John", "Doe", "user1@example.com", "user1", "userPass123",
                LocalDate.of(1995, 5, 15), "Male", Arrays.asList("Technology", "Health"));
        User user2 = new User("Jane", "Smith", "user2@example.com", "user2", "userPass456",
                LocalDate.of(1998, 8, 20), "Female", Arrays.asList("News", "Entertainment"));

        Set<Article> allArticles = new HashSet<>();
        // Create Articles
        Article article1 = new Article(1, "Breaking News", "Author1", "Content about breaking news",
                "2024-12-03", "Breaking news content", "News");
        Article article2 = new Article(2, "Tech Innovations", "Author2", "Content about tech innovations",
                "2024-12-01", "Tech content", "Technology");
        Article article3 = new Article(3, "Health Tips", "Author3", "Content about health tips",
                "2024-11-30", "Health content", "Health");

        // --- Admin Operations ---
        // Admin adds articles
        admin1.addArticle(article1);
        admin1.addArticle(article2);
        admin2.addArticle(article3);

        // Admin edits an article
        admin1.editArticle(article1, "Updated Breaking News", "Updated author", "Updated News", "2023-02-01", "Updated content");

        // Admin deletes an article
        admin2.deleteArticle(article3);

        // Display articles managed by admin1
        System.out.println("Articles managed by " + admin1.getUsername() + ":");
        for (Article article : admin1.getArticlesManaged()) {
            System.out.println(article);
        }

        // --- User Operations ---
        // Users read, like, and save articles
        user1.readArticle(article1);
        user1.saveArticle(article2);
        user1.skipArticle(article2);

        user2.readArticle(article2);
        user2.likeArticle(article2);
        user2.saveArticle(article1);

        // Display liked articles by user1
        System.out.println("\nLiked articles by " + user1.getUsername() + ":");
        for (Article article : user1.getLikedArticles()) {
            System.out.println(article);
        }

        // Display saved articles by user2
        System.out.println("\nSaved articles by " + user2.getUsername() + ":");
        for (Article article : user2.getSavedArticles()) {
            System.out.println(article);
        }

        // User views articles by category
        System.out.println("\nViewing articles in category 'Technology':");
        user1.viewArticlesByCategory("Technology");

        // --- Article Operations ---
        // Add and remove users from articles
        article1.addUser(user1);
        article1.addUser(user2);

        // Display user1's preferred categories
        System.out.println("\nUser1's preferred categories after rating:");
        System.out.println(user1.getPreferences());

        // --- Article Operations ---
        // Add admins and users to articles
        article1.addAdmin(admin1);
        article1.addUser(user1);
        article1.addUser(user2);

        System.out.println("\nUsers interacting with Article 1:");
        for (Human user : article1.getUsers()) {
            System.out.println(user.getUsername());
        }

        article1.removeUser(user2);
        System.out.println("\nUsers interacting with Article 1 after removal:");
        for (Human user : article1.getUsers()) {
            System.out.println(user.getUsername());
        }

        // Add and remove admins from articles
        article1.addAdmin(admin1);
        article1.addAdmin(admin2);

        System.out.println("\nAdmins managing Article 1:");
        for (Admin admin : article1.getAdmins()) {
            System.out.println(admin.getUsername());
        }

        article1.removeAdmin(admin2);
        System.out.println("\nAdmins managing Article 1 after removal:");
        for (Admin admin : article1.getAdmins()) {
            System.out.println(admin.getUsername());
        }

        // Admin views all articles
        admin1.viewArticles(allArticles);

        // User views articles by category
        user1.viewArticlesByCategory(allArticles, "Technology");

        // User views customized articles
        user1.viewCustomizedArticles(allArticles);

        //User view reacted articles
        user2.viewRatedArticles();
    }
}
