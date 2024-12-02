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
    private Set<Human> human;
    private int ratings;

    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
    }

    public Article(int articleId, String category, String author, String publishedDate, String title, String description, RadioButton select) {
        this.articleId = articleId;
        this.category = category;
        this.author = author;
        this.publishedDate = publishedDate;
        this.title = title;
        this.description = description;
        this.select = select;
    }

    public Article(int articleId, String title, String author, String description, String publishedDate, String content) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
    }

    // Constructors
    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category, int ratings, Set<Human> human) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
        this.ratings = 0;
        this.human = new HashSet<>();
    }

    public Article(int articleId, String title, String author, String description, String publishedDate, String content, String category, int ratings) {
        this.articleId = articleId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publishedDate = publishedDate;
        this.content = content;
        this.category = category;
        this.ratings = ratings;
    }

    public int getArticleId() { return articleId; }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public RadioButton getSelect() {
        return select;
    }

    public int getRatings() {
        return ratings;
    }

    public Set<Human> getHuman() {
        return human;
    }

    public void addHuman(Human human) {
        this.human.add(human);
    }

    public void removeHuman(Human human) {
        this.human.remove(human);
    }

    public void updateRatings(int ratings) {
        this.ratings += ratings;
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
                ", ratings=" + ratings +
                ", human=" + human +
                '}';
    }
}