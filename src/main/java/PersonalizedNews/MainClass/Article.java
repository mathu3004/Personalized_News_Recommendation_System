package PersonalizedNews.MainClass;

import javafx.scene.control.RadioButton;

import java.util.HashSet;
import java.util.Set;

public class Article {
    private RadioButton select;
    private int articleId;
    private String title;
    private String author;
    private String description;
    private String publishedDate;
    private String category;
    private String content;
    private Set<Admin> admins;
    private Set<User> users;

    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
        this.admins = new HashSet<>();
        this.users = new HashSet<>();
    }

    public Article(int articleId, String category, String author, String publishedDate, String title, String description, RadioButton select) {
        this.articleId = articleId;
        this.category = category;
        this.author = author;
        this.publishedDate = publishedDate;
        this.title = title;
        this.description = description;
        this.select = select;
        this.admins = new HashSet<>();
        this.users = new HashSet<>();
    }

    public Article(int articleId, String title, String author, String description, String publishedDate, String content) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.admins = new HashSet<>();
        this.users = new HashSet<>();
    }

    // Constructors
    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category, Set<String> admins, Set<String> users) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
        this.admins = new HashSet<>();
        this.users = new HashSet<>();
    }

    public int getArticleId() { return articleId; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getContent() {
        return content;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public RadioButton getSelect() {
        return select;
    }

    // Methods to manage relationships
    public void addAdmin(Admin admin) {
        admins.add(admin);
    }

    public void removeAdmin(Admin admin) {
        admins.remove(admin);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<Admin> getAdmins() {
        return admins;
    }

    public String toString() {
        return "Article{" +
                "articleId=" + articleId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", select=" + select +
                '}';
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}