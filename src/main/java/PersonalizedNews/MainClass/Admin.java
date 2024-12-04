package PersonalizedNews.MainClass;

import org.bson.Document;

import java.util.HashSet;
import java.util.Set;

public class Admin extends Human {
    private String adminPosition;
    private Set<Article> articlesManaged;

    // Constructor
    public Admin(String username, String email, String password) {
        super(username, email, password);
        this.articlesManaged = new HashSet<>();
    }

    public Admin(String username, String email, String password, String adminPosition) {
        super(username, email, password);
        this.adminPosition = adminPosition;
        this.articlesManaged = new HashSet<>();
    }

    // Methods for managing articles
    public void addArticle(Article article) {
        articlesManaged.add(article);
        article.addAdmin(this);
    }

    public void deleteArticle(Article article) {
        articlesManaged.remove(article);
        article.removeAdmin(this);
    }

    public void editArticle(Article article, String title, String author, String description, String publishedDate, String content) {
        if (articlesManaged.contains(article)) {
            article.setTitle(title);
            article.setContent(content);
            article.setDescription(description);
            article.setPublishedDate(publishedDate);
            article.setAuthor(author);
        }
    }

    public void viewArticles(Set<Article> allArticles) {
        System.out.println("Articles managed by " + getUsername() + ":");
        for (Article article : allArticles) {
            System.out.println(article);
        }
    }

    public Set<Article> getArticlesManaged() {
        return articlesManaged;
    }

    public String getAdminPosition() {
        return adminPosition;
    }

    public void setAdminPosition(String adminPosition) {
        this.adminPosition = adminPosition;
    }

    // Validate the admin details
    @Override
    public boolean isValid() {
        return (getPassword() != null && !getPassword().trim().isEmpty() &&
                (getEmail() != null && !getEmail().trim().isEmpty() || getUsername() != null && !getUsername().trim().isEmpty()));
    }

    public String getPosition() {
        return "Admin";
    }

    // Convert to MongoDB query document
    public Document toQueryDocument() {
        Document query = new Document();
        if (getEmail() != null && !getEmail().isEmpty()) {
            query.append("email", getEmail());
        }
        if (getUsername() != null && !getUsername().isEmpty()) {
            query.append("username", getUsername());
        }
        query.append("password", getPassword());
        return query;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", password='" + getPassword() + '\'' +
                '}';
    }
}